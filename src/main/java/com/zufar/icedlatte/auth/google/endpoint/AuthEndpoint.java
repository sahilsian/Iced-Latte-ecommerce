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
import com.zufar.icedlatte.security.api.UserRegistrationService;
import com.zufar.icedlatte.security.dto.UserAuthenticationRequest;
import com.zufar.icedlatte.security.dto.UserRegistrationRequest;
import com.zufar.icedlatte.user.api.SingleUserProvider;
import com.zufar.icedlatte.user.entity.UserEntity;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
import com.google.gson.Gson;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping(value = AuthEndpoint.GOOGLE_AUTH)
public class AuthEndpoint {

  @Autowired Environment environment;

  @Value("${google.auth.server.url}")
  public String authorizationServerUrl;

  @Value("${google.client-id}")
  public String clientId;

  @Value("${google.client-secret}")
  public String clientSecret;

  @Value("${google.scope}")
  public String scope;

  @Value("${google.authenticateUri}")
  public String authenticateUri;

  @Value("${google.redirectUri}")
  String redirectUri;

  public static final String GOOGLE_AUTH = "/api/v1/3part-auth/google";

  private final UserRegistrationService userRegistrationService;
  private final SingleUserProvider singleUserProvider;

  @GetMapping()
  public ResponseEntity<?> googleAuth() {

    log.info("Received the request to add a google auth");
    String redirectAuthUri =
        "http://localhost"
            + ":"
            + environment.getProperty("server.port")
            + GOOGLE_AUTH
            + "/callback";
    String url =
        authorizationServerUrl
            + "?"
            + "scope="
            + scope
            + "&"
            + "access_type=offline&"
            + "include_granted_scopes=true&"
            + "response_type=code&"
            + "state=state_parameter_passthrough_value&"
            + "redirect_uri="
            + redirectAuthUri
            + "&"
            + "client_id="
            + clientId;
    return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).header("Location", url).build();
  }

  @GetMapping("/callback")
  public ResponseEntity<?> googleAuthCallback(@RequestParam("code") String code)
      throws IOException, GeneralSecurityException, InterruptedException {
    log.warn("Received callback the request a google auth");

    // get token
    AuthorizationCodeFlow flow = initializeFlow();
    String redirectAuthUri =
        "http://localhost"
            + ":"
            + environment.getProperty("server.port")
            + GOOGLE_AUTH
            + "/callback";
    TokenResponse token = flow.newTokenRequest(code).setRedirectUri(redirectAuthUri).execute();

    HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    GoogleIdTokenVerifier verifier =
        new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
            .setAudience(Collections.singletonList(clientId))
            .build();

    // verify idToken
    GoogleIdToken idToken = verifier.verify((String) token.get("id_token"));
    if (idToken != null) {
      GoogleIdToken.Payload payload = idToken.getPayload();
      String email = payload.getEmail();
      log.info("User email: {}", email);

      // find or create user
      String password = findOrCreateUser(email);
      log.info("Success logged with email={}", email);

      // login in main form
      sendPostRequest(authenticateUri, new UserAuthenticationRequest(email, password));

      // redirect on main page
      return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
          .header("Location", redirectUri)
          .build();
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
            httpTransport, jsonFactory, clientId, clientSecret, List.of(scope))
        .setAccessType("offline")
        .setApprovalPrompt("force")
        .build();
  }

  private String findOrCreateUser(String email) {
    Optional<UserEntity> userEntity = singleUserProvider.findUserByEmail(email);
    if (userEntity.isEmpty()) {
      String password = UUID.randomUUID().toString();
      userRegistrationService.register(new UserRegistrationRequest(email, email, email, password));
      return password;
    } else {
      return userEntity.get().getPassword();
    }
  }

  public static void sendPostRequest(
      String authenticateUri, UserAuthenticationRequest userAuthenticationRequest) throws InterruptedException {
    HttpClient client = HttpClient.newHttpClient();

    Gson gson = new Gson();
    String json = gson.toJson(userAuthenticationRequest);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(authenticateUri))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
    try {
      client.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      log.error("Error sending request", e);
      throw new InterruptedException();
    }
  }
}
