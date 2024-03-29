package com.zufar.icedlatte.auth.google.endpoint;

import com.google.api.client.auth.oauth2.Credential;
import com.zufar.icedlatte.auth.google.api.GoogleAuthService;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = AuthEndpoint.GOOGLE_AUTH)
public class AuthEndpoint implements com.zufar.icedlatte.openapi.cart.api.ShoppingCartApi {

    public static final String GOOGLE_AUTH = "/api/v1/google/auth/";

    private final GoogleAuthService googleAuthService;
    private final SecurityPrincipalProvider securityPrincipalProvider;

    @GetMapping
    public ResponseEntity<String> getCredential() {
        log.warn("Received the request to add a google auth");
        String credential = googleAuthService.auth();
        assert credential != null;
        log.info("Success logged with token={}", credential);
        return ResponseEntity.ok()
                .body(credential);
    }

    @GetMapping(value ="/2")
    public ResponseEntity<String> getCredential2() {
        log.warn("Received the request to add a google auth");
        String credential = googleAuthService.auth2();
        assert credential != null;
        log.info("Success logged with token={}", credential);
        return ResponseEntity.ok()
                .body(credential);
    }
}
