package com.Project.Api_Gateway.Service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {


    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();
    

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenBlacklistService() {

        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }


    public void blacklistToken(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
    }


    public boolean isTokenBlacklisted(String token) {
        Long expirationTime = blacklist.get(token);
        if (expirationTime == null) {
            return false;
        }
        

        if (System.currentTimeMillis() > expirationTime) {
            blacklist.remove(token);
            return false;
        }
        
        return true;
    }


    public void removeToken(String token) {
        blacklist.remove(token);
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklist.entrySet().removeIf(entry -> currentTime > entry.getValue());
    }


}

