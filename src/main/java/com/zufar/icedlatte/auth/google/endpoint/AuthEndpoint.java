package com.zufar.icedlatte.auth.google.endpoint;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.zufar.icedlatte.security.api.UserAuthenticationService;
import com.zufar.icedlatte.security.api.UserRegistrationService;
import com.zufar.icedlatte.security.dto.UserAuthenticationResponse;
import com.zufar.icedlatte.security.dto.UserRegistrationRequest;
import com.zufar.icedlatte.user.entity.UserEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.zufar.icedlatte.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = AuthEndpoint.GOOGLE_AUTH)
public class AuthEndpoint {

    @Value("${google.auth.server.url}")
    public String authorizationServerUrl;

    @Value("${google.client-id}")
    public String clientId;

    @Value("${google.client-secret}")
    public String clientSecret;

    @Value("${google.scope}")
    public String scope;

    @Value("${google.redirectUri}")
    String redirectUri;

    public static final String GOOGLE_AUTH = "/api/v1/3part-auth/google";

    private final Environment environment;
    private final UserRegistrationService userRegistrationService;
    private final UserDetailsService userDetailsService;
    private final UserAuthenticationService userAuthenticationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<String> googleAuth() {
        log.info("Received the request to initiate the Google authentication");

        String newRedirectUri = "http://localhost:" + environment.getProperty("server.port") + GOOGLE_AUTH + "/callback";
        String authorizationUrl = authorizationServerUrl + "?" +
                "scope=" + scope + "&" +
                "access_type=offline&" +
                "include_granted_scopes=true&" +
                "response_type=code&" +
                "state=state_parameter_passthrough_value&" +
                "redirect_uri=" + newRedirectUri + "&" +
                "client_id=" + clientId;

        return ResponseEntity
                .status(HttpStatus.TEMPORARY_REDIRECT)
                .header("Location", authorizationUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<UserAuthenticationResponse> googleAuthCallback(@RequestParam("code") String code) {
        try {
            log.warn("Received callback the request a google auth");
            TokenResponse token = createTokenResponse(code);
            GoogleIdToken idToken = createGoogleIdToken(token);

            if (idToken == null) {
                log.error("Invalid ID token.");
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .build();
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            log.info("User email: {}", email);

            Optional<UserEntity> userEntity = userRepository.findByEmail(email);
            if (userEntity.isEmpty()) {
                String password = UUID.randomUUID().toString();
                userRegistrationService.register(new UserRegistrationRequest(email, email, email, password));
            }
            log.info("Success logged with email={}", email);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UserAuthenticationResponse authenticationResponse =
                    userAuthenticationService.authenticate(userDetails, email);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + authenticationResponse.token());
            httpHeaders.set("Content-Type", "application/json");

            return new RestTemplate()
                    .exchange(redirectUri,
                            HttpMethod.POST,
                            new HttpEntity<>(authenticationResponse, httpHeaders),
                            UserAuthenticationResponse.class);
        } catch (Exception exception) {
            log.error("Error during Google authentication callback: {}", exception.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    private GoogleIdToken createGoogleIdToken(TokenResponse token) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
                .Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        String idToken = (String) token.get("id_token");

        return verifier.verify(idToken);
    }

    private TokenResponse createTokenResponse(String code) throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
        GoogleAuthorizationCodeFlow authorizationCodeFlow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, jsonFactory, clientId, clientSecret, List.of(scope))
                .setAccessType("offline").setApprovalPrompt("force")
                .build();
        return authorizationCodeFlow.newTokenRequest(code)
                .setRedirectUri("http://localhost" + ":" + environment.getProperty("server.port") + GOOGLE_AUTH + "/callback")
                .execute();
    }
}
