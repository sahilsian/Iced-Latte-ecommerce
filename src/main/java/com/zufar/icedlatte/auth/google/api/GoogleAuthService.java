package com.zufar.icedlatte.auth.google.api;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import java.io.File;
import java.io.StringReader;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    /** Directory to store user credentials. */
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".store/icedlatte");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single
     * globally shared instance across your application.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    @Value("${google.scope}")
    public String SCOPE;

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /** Global instance of the JSON factory. */
    static final JsonFactory JSON_FACTORY = new GsonFactory();

    @Value("${google.token.server.url}")
    public String TOKEN_SERVER_URL;

    @Value("${google.auth.server.url}")
    public String AUTHORIZATION_SERVER_URL;

    @Value("${google.key}")
    public String API_KEY;

    @Value("${google.secret}")
    public String API_SECRET;

    /** Port in the "Callback URL". */
    @Value("${google.callback.port}")
    public static final int PORT = 80;

    /** Domain name in the "Callback URL". */
    @Value("${google.callback.url}")
    public static final String DOMAIN = "https://iced-latte.uk/auth/login";

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize() throws Exception {
        // set up authorization code flow
        AuthorizationCodeFlow flow =
                new AuthorizationCodeFlow.Builder(
                        BearerToken.authorizationHeaderAccessMethod(),
                        HTTP_TRANSPORT,
                        JSON_FACTORY,
                        new GenericUrl(TOKEN_SERVER_URL),
                        new ClientParametersAuthentication(
                                API_KEY, API_SECRET),
                        API_KEY,
                        AUTHORIZATION_SERVER_URL)
                        .setScopes(Collections.singleton(SCOPE))
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .build();
        // authorize
        LocalServerReceiver receiver =
                new LocalServerReceiver.Builder()
                       //.setCallbackPath(DOMAIN)
                       // .setHost(DOMAIN)
                        //.setPort(PORT)
                        .build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String auth() {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            authorize();
            String authorization = AUTHORIZATION_SERVER_URL + "oauthchooseaccount?client_id="
                    + API_KEY + "&scope=" + SCOPE;
            return authorization;
        } catch (Exception e) {
            log.error("Error: " , e);
        }
        return null;
    }

    public String auth2() {
        try {
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            authorize2();
            String authorization = AUTHORIZATION_SERVER_URL + "oauthchooseaccount?client_id="
                    + API_KEY + "&scope=" + SCOPE;
            return authorization;
        } catch (Exception e) {
            log.error("Error: " , e);
        }
        return null;
    }

    /** Authorizes the installed application to access user's protected data. */
    private Credential authorize2() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // load client secrets

        String CLIENT_SECRETS_JSON = "{\n" +
                "  \"installed\": {\n" +
                "    \"client_id\": \"" + API_KEY + "\",\n" +
                "    \"client_secret\": \"" + API_SECRET + "\",\n" +
                "    \"redirect_uris\": [\"https://iced-latte.uk/api/auth/google/callback\"],\n" +
                "    \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                "    \"token_uri\": \"https://oauth2.googleapis.com/token\"\n" +
                "  }\n" +
                "}";

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
                new StringReader(CLIENT_SECRETS_JSON));
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(SCOPE))
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}
