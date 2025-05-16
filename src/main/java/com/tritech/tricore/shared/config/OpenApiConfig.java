package com.tritech.tricore.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Google OpenID Connect Demo API")
                        .description("API di esempio per dimostrare " +
                                "l'integrazione di Google OpenID Connect con " +
                                "Spring Boot")
                        .version("1.0.0"));
    }
}

