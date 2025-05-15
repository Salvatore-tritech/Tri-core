#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# tricore_setup.sh  –  robust, idempotent Docker‑based PostgreSQL installer
# -----------------------------------------------------------------------------
# * Installa Docker Engine + Compose su Ubuntu (se non ancora presenti)
# * Aggiunge l'utente corrente al gruppo docker (solo se serve)
# * Crea un volume "pgdata" (persistent) se non esiste
# * Avvia (o ri‑avvia) il container "tricore-postgres" con immagine postgres:17.5
# * Verifica funzionamento del database dopo l'installazione
# * Logga tutto su file e su stdout
# * In caso di errore rimuove le risorse create nella sessione (autoclean)
# -----------------------------------------------------------------------------
# Usage: ./tricore_setup.sh [--force-recreate]
#
# Options:
#   --force-recreate      Forza la ricreazione del container anche se esiste
#   --db-name NAME        Nome del database (default: tricore)
#   --db-user USER        Nome utente (default: tricore)
#   --db-pass PASSWORD    Password (default: da .env o valore predefinito)
#   --pg-version VERSION  Versione PostgreSQL (default: 17.5)
#   --pg-port PORT        Porta esposta (default: 5432)
#   --help                Mostra questo messaggio
#
# Requisiti: bash ≥ 5.x, apt, curl, gpg, lsb-release.
# Tested on Ubuntu 22.04 & 24.04 (May‑2025).
# -----------------------------------------------------------------------------
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
SCRIPT_NAME="$(basename "${BASH_SOURCE[0]}")"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
LOG_DIR="${LOG_DIR:-${SCRIPT_DIR}/logs}"
mkdir -p "$LOG_DIR"
LOG_FILE="${LOG_DIR}/${SCRIPT_NAME%.*}_${TIMESTAMP}.log"

# --- Output/Logging functions ------------------------------------------------
# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log() {
    local level="$1"
    local msg="$2"
    local color=""
    local symbol=""

    case "$level" in
        INFO)  color="$BLUE";   symbol="ℹ️ " ;;
        WARN)  color="$YELLOW"; symbol="⚠️ " ;;
        ERROR) color="$RED";    symbol="❌ " ;;
        OK)    color="$GREEN";  symbol="✅ " ;;
        *)     color="$NC";     symbol="   " ;;
    esac

    printf "${color}[%s] %s%s${NC}\n" "$(date '+%F %T')" "$symbol" "$msg" | tee -a "$LOG_FILE" >&2
}

info() { log "INFO" "$1"; }
warn() { log "WARN" "$1"; }
error() { log "ERROR" "$1"; }
success() { log "OK" "$1"; }

show_help() {
    cat << EOF
Usage: $SCRIPT_NAME [OPTIONS]

Installer PostgreSQL + Docker per l'ambiente TriCore.

Opzioni:
  --force-recreate      Forza la ricreazione del container anche se esiste
  --db-name NAME        Nome del database (default: tricore)
  --db-user USER        Nome utente (default: tricore)
  --db-pass PASSWORD    Password (default: da .env o valore predefinito)
  --pg-version VERSION  Versione PostgreSQL (default: 17.5)
  --pg-port PORT        Porta esposta (default: 5432)
  --help                Mostra questo messaggio

Esempio:
  $SCRIPT_NAME --pg-version 16.4 --db-name customdb
EOF
    exit 0
}

# --- variables / defaults ----------------------------------------------------
PG_VERSION="17.5"
PG_CONTAINER="tricore-postgres"
PG_VOLUME="pgdata"
PG_PORT="5432"
DB_USER="tricore"
DB_NAME="tricore"
FORCE_RECREATE=false

# Carica variabili da file .env se presente
ENV_FILE="${SCRIPT_DIR}/.env"
if [[ -f "$ENV_FILE" ]]; then
    info "Carico configurazione da $ENV_FILE"
    # shellcheck source=/dev/null
    source "$ENV_FILE"
fi

# Default password (sovrascritto da .env se presente)
DB_PASS=${DB_PASS:-"tritech"}

# Analisi parametri
while [[ $# -gt 0 ]]; do
    case "$1" in
        --force-recreate)
            FORCE_RECREATE=true
            shift
            ;;
        --db-name)
            DB_NAME="$2"
            shift 2
            ;;
        --db-user)
            DB_USER="$2"
            shift 2
            ;;
        --db-pass)
            DB_PASS="$2"
            shift 2
            ;;
        --pg-version)
            PG_VERSION="$2"
            shift 2
            ;;
        --pg-port)
            PG_PORT="$2"
            shift 2
            ;;
        --help)
            show_help
            ;;
        *)
            error "Parametro sconosciuto: $1"
            show_help
            ;;
    esac
done

# Track resources created in *this* run for autoclean
CREATED_CONTAINER=""
CREATED_VOLUME=""

# -----------------------------------------------------------------------------
# Error & cleanup handlers
# -----------------------------------------------------------------------------
cleanup() {
    local exit_code=$?
    if [[ $exit_code -ne 0 ]]; then
        error "Errore rilevato (exit code $exit_code). Avvio procedura di clean‑up…"
        if [[ -n "$CREATED_CONTAINER" ]]; then
            warn "Rimozione container $CREATED_CONTAINER…";
            docker rm -f "$CREATED_CONTAINER" &>/dev/null || true;
        fi
        if [[ -n "$CREATED_VOLUME" ]]; then
            warn "Rimozione volume $CREATED_VOLUME…";
            docker volume rm "$CREATED_VOLUME" &>/dev/null || true;
        fi
        info "Log completo: $LOG_FILE"
    else
        success "Completato senza errori. Log: $LOG_FILE"

        # Stampa info di connessione
        echo
        echo "------------------------------------------------------------"
        echo "🔌  CONNESSIONE DATABASE POSTGRESQL:"
        echo "------------------------------------------------------------"
        echo "Host:     localhost"
        echo "Porta:    $PG_PORT"
        echo "Database: $DB_NAME"
        echo "Utente:   $DB_USER"
        echo "Password: ${DB_PASS:0:1}*****${DB_PASS: -1}"
        echo
        echo "URI di connessione:"
        echo "postgres://$DB_USER:******@localhost:$PG_PORT/$DB_NAME"
        echo "------------------------------------------------------------"
    fi
}
trap cleanup EXIT

on_err() {
    error "Errore alla linea $1"
    return 1  # trigger EXIT trap with non‑zero status
}
trap 'on_err $LINENO' ERR

# Gestore interruzioni
on_interrupt() {
    error "Interruzione richiesta dall'utente"
    exit 1
}
trap on_interrupt INT

# -----------------------------------------------------------------------------
# Utils
# -----------------------------------------------------------------------------
check_permissions() {
    # Se siamo root, tutto ok
    if [[ $EUID -eq 0 ]]; then
        return 0
    fi

    # Se non siamo root, verifichiamo l'appartenenza al gruppo docker
    if groups | grep -qw docker; then
        return 0
    else
        error "È necessario eseguire lo script come root o come utente nel gruppo docker"
        error "Prova: sudo $SCRIPT_NAME o aggiungi il tuo utente al gruppo docker"
        exit 1
    fi
}

# Controlla se un comando è disponibile
command_exists() {
    command -v "$1" &> /dev/null
}

# -----------------------------------------------------------------------------
# 1. Installazione Docker (se assente)
# -----------------------------------------------------------------------------
install_docker() {
    if command_exists docker; then
        local version
        version=$(docker --version | cut -d" " -f3 | tr -d ",")
        success "Docker già installato – versione $version"
        return
    fi

    info "Installazione Docker Engine + Compose plugin…"

    # Controllo se APT è bloccato
    if command_exists lsof && lsof /var/lib/dpkg/lock-frontend &>/dev/null; then
        error "APT è in uso da un altro processo. Riprova più tardi."
        exit 1
    fi

    apt-get update -qq || { error "Impossibile aggiornare APT"; exit 1; }
    apt-get install -y -qq ca-certificates curl gnupg lsb-release || {
        error "Impossibile installare dipendenze"
        exit 1
    }

    # Aggiunge chiave GPG ufficiale (se non già presente)
    install -m 0755 -d /etc/apt/keyrings
    if [[ ! -f /etc/apt/keyrings/docker.gpg ]]; then
        info "Configurazione chiave GPG Docker..."
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
            gpg --dearmor -o /etc/apt/keyrings/docker.gpg || {
            error "Impossibile importare la chiave GPG Docker"
            exit 1
        }
        chmod a+r /etc/apt/keyrings/docker.gpg
    fi

    # Repository stable
    if [[ ! -f /etc/apt/sources.list.d/docker.list ]]; then
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | \
            tee /etc/apt/sources.list.d/docker.list >/dev/null
    fi

    apt-get update -qq || {
        error "Impossibile aggiornare APT dopo l'aggiunta del repository Docker"
        exit 1
    }

    apt-get install -y -qq docker-ce docker-ce-cli containerd.io \
                      docker-buildx-plugin docker-compose-plugin || {
        error "Impossibile installare Docker"
        exit 1
    }

    # Verifica che il servizio sia attivo
    if ! systemctl is-active --quiet docker; then
        info "Avvio servizio Docker..."
        systemctl enable --now docker || {
            error "Impossibile avviare il servizio Docker"
            exit 1
        }
    fi

    success "Docker installato correttamente."
}

# -----------------------------------------------------------------------------
# 2. Abilitazione gruppo docker (solo se serve)
# -----------------------------------------------------------------------------
configure_group() {
    # Ottieni l'utente reale (anche in caso di sudo)
    local real_user="${SUDO_USER:-$(whoami)}"

    if id -nG "$real_user" | grep -qw docker; then
        success "Utente $real_user è già nel gruppo docker."
    else
        info "Aggiungo $real_user al gruppo docker (richiede logout/login)…"
        usermod -aG docker "$real_user" || {
            error "Impossibile aggiungere l'utente al gruppo docker"
            exit 1
        }
        warn "IMPORTANTE: Termina e ri‑apri la sessione o esegui: newgrp docker"
    fi
}

# -----------------------------------------------------------------------------
# 3. Volume Docker (idempotente)
# -----------------------------------------------------------------------------
ensure_volume() {
    if docker volume inspect "$PG_VOLUME" &>/dev/null; then
        success "Volume $PG_VOLUME già presente – lo riutilizzo."
    else
        info "Creo volume $PG_VOLUME…"
        docker volume create "$PG_VOLUME" || {
            error "Impossibile creare il volume Docker"
            exit 1
        }
        CREATED_VOLUME="$PG_VOLUME"
    fi
}

# -----------------------------------------------------------------------------
# 4. Container PostgreSQL
# -----------------------------------------------------------------------------
ensure_container() {
    local container_exists=false
    local container_running=false

    # Verifica esistenza e stato container
    if docker container inspect "$PG_CONTAINER" &>/dev/null; then
        container_exists=true
        if docker container inspect --format="{{.State.Running}}" "$PG_CONTAINER" 2>/dev/null | grep -q "true"; then
            container_running=true
        fi
    fi

    # Se il container esiste e c'è richiesta di ricreazione, lo rimuoviamo
    if $container_exists && $FORCE_RECREATE; then
        info "Rimozione container esistente per ricreazione forzata..."
        docker rm -f "$PG_CONTAINER" &>/dev/null || {
            error "Impossibile rimuovere il container esistente"
            exit 1
        }
        container_exists=false
        container_running=false
    fi

    if $container_exists; then
        if $container_running; then
            success "Container $PG_CONTAINER è già in esecuzione."

            # Verifica che il container sia la versione richiesta
            local current_image
            current_image=$(docker container inspect --format="{{.Config.Image}}" "$PG_CONTAINER")
            local current_version
            current_version=$(echo "$current_image" | grep -oE 'postgres:[0-9.]+' | cut -d':' -f2 || echo "unknown")

            if [[ "$current_version" != "$PG_VERSION" ]]; then
                warn "ATTENZIONE: Il container usa PostgreSQL $current_version invece di $PG_VERSION"
                warn "Usa --force-recreate per aggiornare la versione"
            fi

            return
        else
            info "Ri‑avvio container esistente $PG_CONTAINER…"
            docker start "$PG_CONTAINER" || {
                error "Impossibile avviare il container esistente"
                exit 1
            }
            success "Container riavviato."
            return
        fi
    fi

    info "Avvio nuovo container PostgreSQL $PG_CONTAINER ($PG_VERSION)…"

    # Controllo porta
    if command_exists lsof && lsof -i :"$PG_PORT" &>/dev/null; then
        error "La porta $PG_PORT è già in uso. Specifica una porta diversa con --pg-port."
        exit 1
    fi

    # Avvio container PostgreSQL
    # Nota: supporto healthcheck su versioni recenti di Docker
    local docker_version_supports_healthcheck=true
    if docker version --format '{{.Server.Version}}' 2>/dev/null | grep -qE '^[0-1]\.' || \
       docker version --format '{{.Server.Version}}' 2>/dev/null | grep -qE '^1\.([0-9]|1[0-1])\.' ; then
        docker_version_supports_healthcheck=false
    fi

    if $docker_version_supports_healthcheck; then
        docker run -d --name "$PG_CONTAINER" \
            -e POSTGRES_USER="$DB_USER" \
            -e POSTGRES_PASSWORD="$DB_PASS" \
            -e POSTGRES_DB="$DB_NAME" \
            -p "$PG_PORT:5432" \
            -v "$PG_VOLUME:/var/lib/postgresql/data" \
            --restart unless-stopped \
            --health-cmd "pg_isready -U $DB_USER -d $DB_NAME" \
            --health-interval=10s \
            --health-timeout=5s \
            --health-retries=5 \
            "postgres:$PG_VERSION" || {
            error "Impossibile avviare il container PostgreSQL"
            exit 1
        }
    else
        # Versioni più vecchie di Docker senza supporto healthcheck
        docker run -d --name "$PG_CONTAINER" \
            -e POSTGRES_USER="$DB_USER" \
            -e POSTGRES_PASSWORD="$DB_PASS" \
            -e POSTGRES_DB="$DB_NAME" \
            -p "$PG_PORT:5432" \
            -v "$PG_VOLUME:/var/lib/postgresql/data" \
            --restart unless-stopped \
            "postgres:$PG_VERSION" || {
            error "Impossibile avviare il container PostgreSQL"
            exit 1
        }
    fi

    CREATED_CONTAINER="$PG_CONTAINER"
    success "Container PostgreSQL avviato."
}

# -----------------------------------------------------------------------------
# 5. Verifica funzionamento DB
# -----------------------------------------------------------------------------
verify_postgres() {
    info "Verifico stato PostgreSQL (attendi 10 secondi)…"

    # Attende che il container sia pronto
    local retries=10
    local delay=3
    local ready=false

    for ((i=1; i<=retries; i++)); do
        # Prima verifica se il container è in esecuzione
        if ! docker ps | grep -q "$PG_CONTAINER"; then
            info "Container non in esecuzione. Attendo... ($i/$retries)"
            sleep $delay
            continue
        fi

        # Controlla lo stato di salute del container
        local health_status
        health_status=$(docker container inspect --format='{{.State.Health.Status}}' "$PG_CONTAINER" 2>/dev/null || echo "")

        # Alcuni container potrebbero non avere health check configurato
        if [[ -z "$health_status" ]] || [[ "$health_status" == "<no value>" ]]; then
            # Usa pg_isready direttamente
            if docker exec "$PG_CONTAINER" pg_isready -U "$DB_USER" &>/dev/null; then
                ready=true
                break
            fi
        elif [[ "$health_status" == "healthy" ]]; then
            ready=true
            break
        fi

        info "Attendo che PostgreSQL sia pronto... ($i/$retries)"
        sleep $delay
    done

    if ! $ready; then
        warn "PostgreSQL non risulta pronto dopo $((retries * delay)) secondi."
        warn "Verifica lo stato con: docker logs $PG_CONTAINER"
        return 1
    fi

    # Test connessione con psql (nel container)
    info "Test connessione database..."
    if ! docker exec "$PG_CONTAINER" psql -U "$DB_USER" -d "$DB_NAME" -c "SELECT version();" &>/dev/null; then
        warn "Impossibile connettersi al database. Controlla i log per dettagli."
        return 1
    fi

    success "PostgreSQL è in esecuzione e accessibile."
    return 0
}

# -----------------------------------------------------------------------------
# MAIN
# -----------------------------------------------------------------------------
main() {
    info "Avvio configurazione ambiente TriCore PostgreSQL"
    info "Versione PostgreSQL: $PG_VERSION"
    info "Database: $DB_NAME (utente: $DB_USER)"
    info "Container: $PG_CONTAINER (volume: $PG_VOLUME)"
    info "Porta: $PG_PORT"
    info "Log dettagliato in: $LOG_FILE"
    echo

    # Verifica i permessi necessari
    check_permissions

    # Gestisce installazione Docker solo se siamo root
    if [[ $EUID -eq 0 ]]; then
        install_docker
        configure_group
    elif ! command_exists docker; then
        error "Docker non è installato e sono necessari i privilegi di root per installarlo."
        error "Esegui: sudo $SCRIPT_NAME $*"
        exit 1
    fi

    # Operazioni che possono essere eseguite sia da root che da utenti nel gruppo docker
    ensure_volume
    ensure_container
    verify_postgres
}

main "$@"
