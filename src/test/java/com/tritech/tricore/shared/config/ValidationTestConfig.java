package com.tritech.tricore.shared.config;

import jakarta.validation.Validation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ValidationTestConfig {

    @Bean
    public jakarta.validation.Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
