package com.bvakp.automation.core.playwright;
import java.util.Locale;

public enum BrowserName {

    CHROMIUM,
    FIREFOX,
    WEBKIT;

    public static BrowserName from(String value) {

        System.out.println("BROWSER DEGERI = [" + value + "]");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Browser adı boş olamaz."
            );
        }

        try {
            return BrowserName.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Desteklenmeyen browser: " + value,
                    e
            );
        }
    }
}