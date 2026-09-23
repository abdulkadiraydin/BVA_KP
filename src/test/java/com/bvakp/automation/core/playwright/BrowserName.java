package com.bvakp.automation.core.playwright;

public enum BrowserName {

    CHROMIUM,
    FIREFOX,
    WEBKIT;

    public static BrowserName from(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Browser adı boş olamaz."
            );
        }

        try {
            return BrowserName.valueOf(
                    value.trim().toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Desteklenmeyen browser: " + value,
                    e
            );
        }
    }
}