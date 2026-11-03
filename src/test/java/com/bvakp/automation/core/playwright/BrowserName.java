package com.bvakp.automation.core.playwright;

import java.util.Locale;

/** Desteklenen tarayıcılar. */
public enum BrowserName {
  CHROMIUM,
  FIREFOX,
  WEBKIT;

  /** Ayardaki metni (ör. chromium) tarayıcı adına çevirir; geçersizse hata verir. */
  public static BrowserName from(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Browser adı boş olamaz.");
    }

    try {
      return BrowserName.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Desteklenmeyen browser: " + value, e);
    }
  }
}
