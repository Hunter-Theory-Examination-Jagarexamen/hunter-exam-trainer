package com.hunterexam.backend.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TempTokenStore {
    private static final Map<String, String> cache = new ConcurrentHashMap<>();

    public static void saveToken(String code, String token) {
        cache.put(code, token);
    }

    public static String getToken(String code) {
        return cache.remove(code);
    }
}