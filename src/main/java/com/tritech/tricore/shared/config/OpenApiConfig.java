package com.tritech.tricore.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI().info(new Info().title("Tri-Core API")
			.version("1.0.0")
			.description("Documentazione API del sistema Tri-Core. Accedi tramite Google per autorizzare."));
	}

}
