package com.zufar.icedlatte.auth.google.endpoint;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.zufar.icedlatte.security.api.SecurityPrincipalProvider;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = AuthEndpoint.GOOGLE_AUTH)
public class AuthEndpoint {

  @Autowired Environment environment;

  @Value("${google.auth.server.url}")
  public String AUTHORIZATION_SERVER_URL;

  @Value("${google.client-id}")
  public String CLIENT_ID;

  @Value("${google.client-secret}")
  public String CLIENT_SECRET;

  @Value("${google.scope}")
  public String SCOPE;

  public static final String GOOGLE_AUTH = "/api/v1/3part-auth/google";

  private final SecurityPrincipalProvider securityPrincipalProvider;

  @GetMapping()
  public ResponseEntity<?> googleAuth() {

    log.info("Received the request to add a google auth");
    String redirectUri =
        "http://localhost"
            + ":"
            + environment.getProperty("server.port")
            + GOOGLE_AUTH
            + "/callback";
    String url =
        AUTHORIZATION_SERVER_URL
            + "?"
            + "scope="
            + SCOPE
            + "&"
            + "access_type=offline&"
            + "include_granted_scopes=true&"
            + "response_type=code&"
            + "state=state_parameter_passthrough_value&"
            + "redirect_uri="
            + redirectUri
            + "&"
            + "client_id="
            + CLIENT_ID;
    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("Location", url).build();
  }

  @GetMapping("/callback")
  public ResponseEntity<?> googleAuthCallback(@RequestParam("code") String code)
      throws IOException, GeneralSecurityException {
    log.warn("Received callback the request a google auth");

    // get token
    AuthorizationCodeFlow flow = initializeFlow();
    String redirectUri =
        "http://localhost"
            + ":"
            + environment.getProperty("server.port")
            + GOOGLE_AUTH
            + "/callback";
    TokenResponse token = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

    // verify idToken
    GoogleIdToken idToken = verifier.verify((String) token.get("id_token"));
    if (idToken != null) {
      GoogleIdToken.Payload payload = idToken.getPayload();
      log.info("User email: " + payload.getEmail());

      // TODO login into local

      log.info("Success logged with email={}", payload.getEmail());
      return ResponseEntity.ok(payload.getEmail());
    } else {
      log.error("Invalid ID token.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }

  private GoogleAuthorizationCodeFlow initializeFlow()
      throws IOException, GeneralSecurityException {
    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    return new GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, List.of(SCOPE))
        .setAccessType("offline")
        .setApprovalPrompt("force")
        .build();
  }
}
