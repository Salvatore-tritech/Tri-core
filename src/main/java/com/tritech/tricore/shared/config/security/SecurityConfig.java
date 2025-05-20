package com.tritech.tricore.shared.config.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository)
			throws Exception {
		http.authorizeHttpRequests(authorize -> authorize
			.requestMatchers("/", "/error", "/api/public/**", "/login", "/api-docs/**", "/swagger-ui/**",
					"/swagger" + "-ui.html", "/v3/api-docs/**")
			.permitAll()
			.anyRequest()
			.authenticated())
			.oauth2Login(oauth2 -> oauth2.loginPage("/login")
				.defaultSuccessUrl("/login/success", true) //
				.failureUrl("/login/fail")
				.authorizationEndpoint(endpoint -> endpoint
					.authorizationRequestResolver(authorizationRequestResolver(clientRegistrationRepository))))
			.logout(logout -> logout.logoutUrl("/logout").logoutSuccessHandler((request, response, auth) -> {
				response.setStatus(HttpServletResponse.SC_OK);
			}).invalidateHttpSession(true).deleteCookies("JSESSIONID").permitAll())
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(AbstractHttpConfigurer::disable);
		return http.build();
	}

	private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
			ClientRegistrationRepository clientRegistrationRepository) {
		DefaultOAuth2AuthorizationRequestResolver resolver = new DefaultOAuth2AuthorizationRequestResolver(
				clientRegistrationRepository, "/oauth2/authorization");

		resolver.setAuthorizationRequestCustomizer(
				customizer -> customizer.additionalParameters(params -> params.put("prompt", "select_account")));

		return resolver;
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowCredentials(true);
		configuration.setAllowedOriginPatterns(List.of("http://localhost:3000"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
