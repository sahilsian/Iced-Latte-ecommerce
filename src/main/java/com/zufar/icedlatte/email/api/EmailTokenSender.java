package com.zufar.icedlatte.email.api;

import com.zufar.icedlatte.email.api.token.TokenCache;
import com.zufar.icedlatte.email.api.token.TokenManager;
import com.zufar.icedlatte.email.sender.AuthTokenEmailConfirmation;
import com.zufar.icedlatte.security.dto.UserRegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailTokenSender {

    private final AuthTokenEmailConfirmation emailConfirmation;
    private final TokenManager tokenManager;
    private final TokenCache tokenCache;

    @Value("${temporary-cache.get-token-enabled}")
    private boolean getTokenEnabled = false;

    public void sendEmailVerificationCode(final UserRegistrationRequest request) {
        String token = tokenManager.generateToken(request);
        emailConfirmation.sendTemporaryCode(request.email(), token);
    }

    // for test purposes only
    public String getEmailVerificationCode(final UserRegistrationRequest request) {
        if (getTokenEnabled) {
            return tokenCache.getToken(request);
        } else {
            return null;
        }
    }
}
