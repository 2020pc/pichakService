package com.caspian.pichak.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
@PropertySource({"classpath:application.properties"})
@Service("loginAttemptService")
public class LoginAttemptService {
    private final LoadingCache<String, Integer> attemptsCache;
    @Value("${security.security-max_attempt}")
    private int MAX_ATTEMPT;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.DAYS).build(new CacheLoader<String, Integer>() {
            public Integer load(final String key) {
                return 0;
            }
        });
    }

    public void loginSucceeded(final String key) {
        this.attemptsCache.invalidate(key);
    }

    public void loginFailed(final String key) {
        int attempts = 0;

        try {
            attempts = (Integer)this.attemptsCache.get(key);
        } catch (ExecutionException var4) {
            attempts = 0;
        }

        ++attempts;
        this.attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(final String key) {
        return false;
    }
}
