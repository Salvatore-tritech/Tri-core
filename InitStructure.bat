@echo off
:: -----------------------------------------------------------------------------
:: tricore_setup.bat  –  robust, idempotent Docker‑based PostgreSQL installer
:: -----------------------------------------------------------------------------
:: * Installa Docker Desktop for Windows (se non ancora presente)
:: * Crea un volume "pgdata" (persistent) se non esiste
:: * Avvia (o ri‑avvia) il container "tricore-postgres" con immagine postgres:17.5
:: * Verifica funzionamento del database dopo l'installazione
:: * Logga tutto su file e su stdout
:: * In caso di errore rimuove le risorse create nella sessione (autoclean)
:: -----------------------------------------------------------------------------
:: Usage: tricore_setup.bat [--force-recreate] [--db-name NAME] [--db-user USER]
::                           [--db-pass PASSWORD] [--pg-version VERSION] [--pg-port PORT]
::
:: Requisiti: Windows 10/11 con supporto WSL2 o Hyper-V
:: -----------------------------------------------------------------------------

setlocal EnableDelayedExpansion

:: -----------------------------------------------------------------------------
:: Variables / defaults
:: -----------------------------------------------------------------------------
set "SCRIPT_NAME=%~n0"
set "SCRIPT_DIR=%~dp0"
set "TIMESTAMP=%date:~6,4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "TIMESTAMP=!TIMESTAMP: =0!"
set "LOG_DIR=%SCRIPT_DIR%logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
set "LOG_FILE=%LOG_DIR%\%SCRIPT_NAME%_%TIMESTAMP%.log"

set "PG_VERSION=17.5"
set "PG_CONTAINER=tricore-postgres"
set "PG_VOLUME=pgdata"
set "PG_PORT=5432"
set "DB_USER=tricore"
set "DB_PASS=tritech"
set "DB_NAME=tricore"
set "FORCE_RECREATE=false"

:: Resources created in this run (for cleanup)
set "CREATED_CONTAINER="
set "CREATED_VOLUME="

:: Flag per verificare errori
set "HAS_ERROR=false"

:: ----------------------------------------------------------------------
:: Environment variables (load from .env if exists)
:: ----------------------------------------------------------------------
if exist "%SCRIPT_DIR%.env" (
    echo [INFO] Caricamento variabili da .env
    for /f "usebackq tokens=1,* delims==" %%a in ("%SCRIPT_DIR%.env") do (
        set "KEY=%%a"
        set "VAL=%%b"
        set "!KEY!=!VAL!"
    )
)

:: ----------------------------------------------------------------------
:: Parse command-line arguments
:: ----------------------------------------------------------------------
:parse_args
if "%~1"=="" goto :args_done
if /i "%~1"=="--force-recreate" (
    set "FORCE_RECREATE=true"
    shift
    goto :parse_args
)
if /i "%~1"=="--db-name" (
    set "DB_NAME=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--db-user" (
    set "DB_USER=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--db-pass" (
    set "DB_PASS=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--pg-version" (
    set "PG_VERSION=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--pg-port" (
    set "PG_PORT=%~2"
    shift
    shift
    goto :parse_args
)
if /i "%~1"=="--help" (
    call :show_help
    exit /b 0
)
echo [ERROR] Parametro sconosciuto: %~1
call :show_help
exit /b 1

:args_done

:: ----------------------------------------------------------------------
:: Logging functions
:: ----------------------------------------------------------------------
:log
set "level=%~1"
set "msg=%~2"
set "symbol="
set "timestamp=%date% %time%"

if "%level%"=="INFO" set "symbol=ℹ️ "
if "%level%"=="WARN" set "symbol=⚠️ "
if "%level%"=="ERROR" set "symbol=❌ "
if "%level%"=="OK" set "symbol=✅ "

>> "%LOG_FILE%" echo [%timestamp%] %symbol%%msg%
echo [%level%] %symbol%%msg%
exit /b 0

:: ----------------------------------------------------------------------
:: Visualizza informazioni d'aiuto
:: ----------------------------------------------------------------------
:show_help
echo.
echo Usage: %SCRIPT_NAME%.bat [OPTIONS]
echo.
echo Installer PostgreSQL + Docker per l'ambiente TriCore (Windows).
echo.
echo Opzioni:
echo   --force-recreate      Forza la ricreazione del container anche se esiste
echo   --db-name NAME        Nome del database (default: tricore)
echo   --db-user USER        Nome utente (default: tricore)
echo   --db-pass PASSWORD    Password (default: da .env o valore predefinito)
echo   --pg-version VERSION  Versione PostgreSQL (default: 17.5)
echo   --pg-port PORT        Porta esposta (default: 5432)
echo   --help                Mostra questo messaggio
echo.
echo Esempio:
echo   %SCRIPT_NAME%.bat --pg-version 16.4 --db-name customdb
echo.
exit /b 0

:: ----------------------------------------------------------------------
:: 1. Verifica e Installazione Docker Desktop
:: ----------------------------------------------------------------------
:check_docker
call :log "INFO" "Verifica installazione Docker..."

where docker >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    for /f "tokens=3" %%i in ('docker --version') do (
        set "DOCKER_VERSION=%%i"
        call :log "OK" "Docker gia installato - versione !DOCKER_VERSION!"
    )
) else (
    call :log "INFO" "Docker non trovato. Avvio installazione..."
    call :install_docker
    if "!HAS_ERROR!"=="true" exit /b 1
)

:: Verifica che Docker stia effettivamente funzionando
docker info >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    call :log "WARN" "Docker e installato ma non in esecuzione. Avvio Docker Desktop..."
    start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    call :log "INFO" "Attendo 30 secondi per l'avvio di Docker..."
    timeout /t 30 /nobreak >nul
    
    docker info >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        call :log "ERROR" "Docker non risponde. Verifica che Docker Desktop sia in esecuzione."
        set "HAS_ERROR=true"
        exit /b 1
    )
)

exit /b 0

:: ----------------------------------------------------------------------
:: Installazione Docker Desktop per Windows
:: ----------------------------------------------------------------------
:install_docker
call :log "INFO" "Download Docker Desktop per Windows..."

:: Crea cartella temporanea
set "TEMP_DIR=%TEMP%\docker_install_%TIMESTAMP%"
mkdir "%TEMP_DIR%" 2>nul

:: URL Docker Desktop
set "DOCKER_URL=https://desktop.docker.com/win/stable/Docker%%20Desktop%%20Installer.exe"
set "DOCKER_INSTALLER=%TEMP_DIR%\DockerDesktopInstaller.exe"

:: Download dell'installer
call :log "INFO" "Download Docker Desktop..."
powershell -Command "Invoke-WebRequest -Uri '%DOCKER_URL%' -OutFile '%DOCKER_INSTALLER%'"
if %ERRORLEVEL% NEQ 0 (
    call :log "ERROR" "Impossibile scaricare Docker Desktop."
    set "HAS_ERROR=true"
    exit /b 1
)

:: Esegui l'installer
call :log "INFO" "Installazione Docker Desktop (richiede privilegi amministrativi)..."
call :log "INFO" "Si aprira una finestra di installazione. Segui le istruzioni per completare."
call :log "INFO" "Al termine dell'installazione, premi un tasto per continuare."

start "" /wait "%DOCKER_INSTALLER%" install --quiet
if %ERRORLEVEL% NEQ 0 (
    call :log "ERROR" "L'installazione di Docker Desktop non e riuscita."
    set "HAS_ERROR=true"
    exit /b 1
)

call :log "INFO" "Docker installato. Riavvia lo script dopo il riavvio di Windows."
call :log "INFO" "Premi un tasto per terminare."
pause >nul
set "HAS_ERROR=true"
exit /b 1

:: ----------------------------------------------------------------------
:: 2. Gestione volume
:: ----------------------------------------------------------------------
:ensure_volume
call :log "INFO" "Verifica volume Docker..."

docker volume inspect %PG_VOLUME% >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    call :log "OK" "Volume %PG_VOLUME% gia presente - lo riutilizzo."
) else (
    call :log "INFO" "Creo volume %PG_VOLUME%..."
    docker volume create %PG_VOLUME% >nul
    if %ERRORLEVEL% NEQ 0 (
        call :log "ERROR" "Impossibile creare il volume Docker."
        set "HAS_ERROR=true"
        exit /b 1
    )
    set "CREATED_VOLUME=%PG_VOLUME%"
    call :log "OK" "Volume %PG_VOLUME% creato."
)

exit /b 0

:: ----------------------------------------------------------------------
:: 3. Gestione container PostgreSQL
:: ----------------------------------------------------------------------
:ensure_container
call :log "INFO" "Gestione container PostgreSQL..."

:: Verifica esistenza e stato container
set "CONTAINER_EXISTS=false"
set "CONTAINER_RUNNING=false"

docker container inspect %PG_CONTAINER% >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    set "CONTAINER_EXISTS=true"
    
    for /f "tokens=*" %%a in ('docker container inspect -f "{{.State.Running}}" %PG_CONTAINER% 2^>nul') do (
        set "RUNNING_STATE=%%a"
        if "!RUNNING_STATE!"=="true" set "CONTAINER_RUNNING=true"
    )
)

:: Gestione ricreazione forzata
if "%CONTAINER_EXISTS%"=="true" if "%FORCE_RECREATE%"=="true" (
    call :log "INFO" "Rimozione container esistente per ricreazione forzata..."
    docker rm -f %PG_CONTAINER% >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        call :log "ERROR" "Impossibile rimuovere il container esistente."
        set "HAS_ERROR=true"
        exit /b 1
    )
    set "CONTAINER_EXISTS=false"
    set "CONTAINER_RUNNING=false"
)

:: Gestione container esistente
if "%CONTAINER_EXISTS%"=="true" (
    if "%CONTAINER_RUNNING%"=="true" (
        call :log "OK" "Container %PG_CONTAINER% e gia in esecuzione."
        
        :: Verifica versione
        for /f "tokens=*" %%a in ('docker container inspect -f "{{.Config.Image}}" %PG_CONTAINER%') do set "CURRENT_IMAGE=%%a"
        for /f "tokens=2 delims=:" %%a in ("!CURRENT_IMAGE!") do set "CURRENT_VERSION=%%a"
        
        if not "!CURRENT_VERSION!"=="%PG_VERSION%" (
            call :log "WARN" "ATTENZIONE: Il container usa PostgreSQL !CURRENT_VERSION! invece di %PG_VERSION%"
            call :log "WARN" "Usa --force-recreate per aggiornare la versione"
        )
    ) else (
        call :log "INFO" "Riavvio container esistente %PG_CONTAINER%..."
        docker start %PG_CONTAINER% >nul
        if %ERRORLEVEL% NEQ 0 (
            call :log "ERROR" "Impossibile avviare il container esistente."
            set "HAS_ERROR=true"
            exit /b 1
        )
        call :log "OK" "Container riavviato."
    )
    exit /b 0
)

:: Crea nuovo container
call :log "INFO" "Avvio nuovo container PostgreSQL %PG_CONTAINER% (%PG_VERSION%)..."

:: Verifica se la porta è in uso
netstat -ano | findstr ":%PG_PORT% " >nul
if %ERRORLEVEL% EQU 0 (
    call :log "ERROR" "La porta %PG_PORT% e gia in uso. Specifica una porta diversa con --pg-port."
    set "HAS_ERROR=true"
    exit /b 1
)

:: Avvio container con supporto per healthcheck
for /f "tokens=*" %%v in ('docker version --format "{{.Server.Version}}" 2^>nul') do set "DOCKER_SERVER_VERSION=%%v"

:: Controlla se la versione supporta il healthcheck
set "SUPPORTS_HEALTHCHECK=true"
if not defined DOCKER_SERVER_VERSION set "SUPPORTS_HEALTHCHECK=false"
if "%SUPPORTS_HEALTHCHECK%"=="true" (
    echo %DOCKER_SERVER_VERSION% | findstr /R "^[0-1]\." >nul && set "SUPPORTS_HEALTHCHECK=false"
    echo %DOCKER_SERVER_VERSION% | findstr /R "^1\.\([0-9]\|1[0-1]\)\." >nul && set "SUPPORTS_HEALTHCHECK=false"
)

if "%SUPPORTS_HEALTHCHECK%"=="true" (
    docker run -d --name %PG_CONTAINER% ^
        -e POSTGRES_USER=%DB_USER% ^
        -e POSTGRES_PASSWORD=%DB_PASS% ^
        -e POSTGRES_DB=%DB_NAME% ^
        -p %PG_PORT%:5432 ^
        -v %PG_VOLUME%:/var/lib/postgresql/data ^
        --restart unless-stopped ^
        --health-cmd "pg_isready -U %DB_USER% -d %DB_NAME%" ^
        --health-interval=10s ^
        --health-timeout=5s ^
        --health-retries=5 ^
        postgres:%PG_VERSION% >nul
) else (
    :: Versioni più vecchie senza supporto healthcheck
    docker run -d --name %PG_CONTAINER% ^
        -e POSTGRES_USER=%DB_USER% ^
        -e POSTGRES_PASSWORD=%DB_PASS% ^
        -e POSTGRES_DB=%DB_NAME% ^
        -p %PG_PORT%:5432 ^
        -v %PG_VOLUME%:/var/lib/postgresql/data ^
        --restart unless-stopped ^
        postgres:%PG_VERSION% >nul
)

if %ERRORLEVEL% NEQ 0 (
    call :log "ERROR" "Impossibile avviare il container PostgreSQL."
    set "HAS_ERROR=true"
    exit /b 1
)

set "CREATED_CONTAINER=%PG_CONTAINER%"
call :log "OK" "Container PostgreSQL avviato."
exit /b 0

:: ----------------------------------------------------------------------
:: 4. Verifica PostgreSQL
:: ----------------------------------------------------------------------
:verify_postgres
call :log "INFO" "Verifica stato PostgreSQL (attendo 10 secondi)..."

:: Attendi che PostgreSQL sia pronto
set "ATTEMPTS=10"
set "DELAY=3"
set "READY=false"

for /l %%i in (1, 1, %ATTEMPTS%) do (
    if "!READY!"=="false" (
        :: Verifica che il container sia in esecuzione
        docker ps | findstr %PG_CONTAINER% >nul
        if %ERRORLEVEL% NEQ 0 (
            call :log "INFO" "Container non in esecuzione. Attendo... (%%i/%ATTEMPTS%)"
            timeout /t %DELAY% /nobreak >nul
            goto :continue_loop
        )
        
        :: Verifica health status
        for /f "tokens=*" %%h in ('docker container inspect -f "{{.State.Health.Status}}" %PG_CONTAINER% 2^>nul') do set "HEALTH_STATUS=%%h"
        
        if not defined HEALTH_STATUS set "HEALTH_STATUS="
        if "!HEALTH_STATUS!"=="" (
            :: Nessun healthcheck, usa pg_isready
            docker exec "%PG_CONTAINER%" pg_isready -U "%DB_USER%" >nul 2>nul
            if %ERRORLEVEL% EQU 0 set "READY=true"
        ) else if "!HEALTH_STATUS!"=="healthy" (
            set "READY=true"
        )
        
        if "!READY!"=="false" (
            call :log "INFO" "Attendo che PostgreSQL sia pronto... (%%i/%ATTEMPTS%)"
            timeout /t %DELAY% /nobreak >nul
        )
        
        :continue_loop
    )
)

if "%READY%"=="false" (
    call :log "WARN" "PostgreSQL non risulta pronto dopo %ATTEMPTS% tentativi."
    call :log "WARN" "Verifica lo stato con: docker logs %PG_CONTAINER%"
    exit /b 1
)

:: Test connessione
call :log "INFO" "Test connessione database..."
docker exec %PG_CONTAINER% psql -U %DB_USER% -d %DB_NAME% -c "SELECT version();" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    call :log "WARN" "Impossibile connettersi al database. Controlla i log per dettagli."
    exit /b 1
)

call :log "OK" "PostgreSQL e in esecuzione e accessibile."
exit /b 0

:: ----------------------------------------------------------------------
:: Clean-up in caso di errore
:: ----------------------------------------------------------------------
:cleanup
if "%HAS_ERROR%"=="true" (
    call :log "ERROR" "Errore rilevato. Avvio procedura di clean-up..."
    
    if defined CREATED_CONTAINER (
        call :log "WARN" "Rimozione container %CREATED_CONTAINER%..."
        docker rm -f %CREATED_CONTAINER% >nul 2>nul
    )
    
    if defined CREATED_VOLUME (
        call :log "WARN" "Rimozione volume %CREATED_VOLUME%..."
        docker volume rm %CREATED_VOLUME% >nul 2>nul
    )
) else (
    call :log "OK" "Completato senza errori."
    
    :: Stampa info di connessione
    echo.
    echo ------------------------------------------------------------
    echo   CONNESSIONE DATABASE POSTGRESQL:
    echo ------------------------------------------------------------
    echo   Host:     localhost
    echo   Porta:    %PG_PORT%
    echo   Database: %DB_NAME%
    echo   Utente:   %DB_USER%
    echo   Password: %DB_PASS:~0,1%*****%DB_PASS:~-1%
    echo.
    echo   URI di connessione:
    echo   postgres://%DB_USER%:******@localhost:%PG_PORT%/%DB_NAME%
    echo ------------------------------------------------------------
)

exit /b 0

:: ----------------------------------------------------------------------
:: Main entry point
:: ----------------------------------------------------------------------
:main
call :log "INFO" "Avvio configurazione ambiente TriCore PostgreSQL (Windows)"
call :log "INFO" "Versione PostgreSQL: %PG_VERSION%"
call :log "INFO" "Database: %DB_NAME% (utente: %DB_USER%)"
call :log "INFO" "Container: %PG_CONTAINER% (volume: %PG_VOLUME%)"
call :log "INFO" "Porta: %PG_PORT%"
echo.

:: Verifica privilegi amministrativi (richiesti solo per installazione Docker)
net session >nul 2>nul
set "IS_ADMIN=%ERRORLEVEL%"

:: Controlla Docker
call :check_docker
if "%HAS_ERROR%"=="true" goto :cleanup

:: Verifica le risorse Docker
call :ensure_volume
if "%HAS_ERROR%"=="true" goto :cleanup

call :ensure_container
if "%HAS_ERROR%"=="true" goto :cleanup

call :verify_postgres
if "%ERRORLEVEL%" NEQ 0 set "HAS_ERROR=true"

call :cleanup
exit /b %ERRORLEVEL%

:: ----------------------------------------------------------------------
:: Esecuzione script
:: ----------------------------------------------------------------------
call :log "INFO" "Log dettagliato in %LOG_FILE%"
goto :main
endlocal