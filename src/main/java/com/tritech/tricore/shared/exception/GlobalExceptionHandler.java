package com.tritech.tricore.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RestControllerAdvice
public class GlobalExceptionHandler {

	// Mappa centrale per eccezioni specifiche e creazione dinamica di ProblemDetail
	private static final Map<Class<? extends Exception>, Function<Exception, ProblemDetail>> EXCEPTION_HANDLERS = new HashMap<>();

	// Inizializzazione statica della mappa
	static {
		EXCEPTION_HANDLERS.put(UserNotAuthenticatedException.class,
				exception -> createProblemDetail(HttpStatus.UNAUTHORIZED.value(), "User not authenticated.",
						exception.getMessage()));

		// Possono essere aggiunte altre eccezioni specifiche
		EXCEPTION_HANDLERS.put(IllegalArgumentException.class,
				exception -> createProblemDetail(HttpStatus.BAD_REQUEST.value(), "Invalid request.",
						exception.getMessage()));
	}

	/**
	 * Handler globale che gestisce tutte le eccezioni.
	 * @param exception l'eccezione catturata
	 * @return un oggetto ProblemDetail personalizzato
	 */
	@ExceptionHandler(Exception.class)
	public ProblemDetail handleException(Exception exception) {
		// Cerca un handler personalizzato per il tipo di eccezione
		return EXCEPTION_HANDLERS.getOrDefault(exception.getClass(), this::handleUnknownException).apply(exception);
	}

	/**
	 * Gestione delle eccezioni non previste (fallback).
	 * @param exception l'eccezione non gestita
	 * @return un ProblemDetail generico
	 */
	private ProblemDetail handleUnknownException(Exception exception) {
		return createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unknown internal error.",
				exception.getMessage() != null ? exception.getMessage() : "No details available.");
	}

	/**
	 * Metodo di utilità per creare un ProblemDetail ben formato.
	 * @param status lo stato HTTP
	 * @param title il titolo dell'errore
	 * @param description una descrizione dettagliata dell'errore
	 * @return un oggetto ProblemDetail strutturato
	 */
	private static ProblemDetail createProblemDetail(int status, String title, String description) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), title);
		problemDetail.setProperty("description", description);
		return problemDetail;
	}

}
