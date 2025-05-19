package com.tritech.tricore.adapter.input.web;

import com.tritech.tricore.shared.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "API per l'autenticazione e " + "informazioni utente")
public class AuthController {

	@Operation(summary = "Ottieni informazioni utente",
			description = "Restituisce le informazioni sull'utente " + "correntemente autenticato")
	@ApiResponse(responseCode = "200", description = "Informazioni utente recuperate con successo")
	@ApiResponse(responseCode = "401", description = "Non autenticato")
	public ResponseEntity<UserInfoDTO> getUserInfo(@AuthenticationPrincipal OidcUser oidcUser) {
		if (oidcUser == null) {
			return ResponseEntity.status(401).build();
		}

		UserInfoDTO userInfo = new UserInfoDTO(oidcUser.getFullName(), oidcUser.getEmail(), oidcUser.getPicture());

		return ResponseEntity.ok(userInfo);
	}

	@GetMapping("/user/claims")
	@Operation(summary = "Ottieni tutti i claims", description = "Restituisce tutti i claims dell'utente autenticato")
	@ApiResponse(responseCode = "200", description = "Claims recuperati con " + "successo")
	@ApiResponse(responseCode = "401", description = "Non autenticato")
	public ResponseEntity<Map<String, Object>> getUserClaims(@AuthenticationPrincipal OidcUser oidcUser) {
		if (oidcUser == null) {
			return ResponseEntity.status(401).build();
		}

		return ResponseEntity.ok(oidcUser.getClaims());
	}

}
