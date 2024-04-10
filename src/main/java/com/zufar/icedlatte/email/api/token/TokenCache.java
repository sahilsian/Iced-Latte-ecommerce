package com.zufar.icedlatte.email.api.token;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zufar.icedlatte.email.exception.IncorrectTokenException;
import com.zufar.icedlatte.security.dto.UserRegistrationRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TokenCache {

    private final Cache<String, UserRegistrationRequest> tokenCache;

    public TokenCache(@Value("${temporary-cache.time.token}") Integer expireTime) {
        this.tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireTime, TimeUnit.MINUTES)
                .build();
    }

    public void addToken(String tokenKey, UserRegistrationRequest request) {
        tokenCache.put(tokenKey, request);
    }

    public UserRegistrationRequest getToken(String tokenKey) {
        UserRegistrationRequest userRegistrationRequest = tokenCache.getIfPresent(tokenKey);
        if (userRegistrationRequest == null) {
            throw new IncorrectTokenException();
        }
        return userRegistrationRequest;
    }

    public void removeToken(String tokenKey) {
        tokenCache.invalidate(tokenKey);
    }

    // for test only
    public String getToken(UserRegistrationRequest request) {
        return getKeyByValue(tokenCache, request);
    }

    private String getKeyByValue(Cache<String, UserRegistrationRequest> tokenCache, UserRegistrationRequest request) {
        for (Map.Entry<String, UserRegistrationRequest> entry : tokenCache.asMap().entrySet()) {
            if (request.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}
