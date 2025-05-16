package com.tritech.tricore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TriCoreApplication {

	/**
	 * Private constructor to hide the implicit public one.
	 */
	private TriCoreApplication() {
		// Hide constructor
	}

	/**
	 * Main method that starts the Spring Boot application.
	 * @param args command line arguments passed to the application
	 */
	public static void main(final String[] args) {
		SpringApplication.run(TriCoreApplication.class, args);
	}

}
