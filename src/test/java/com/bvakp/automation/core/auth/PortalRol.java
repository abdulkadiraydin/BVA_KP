package com.bvakp.automation.core.auth;

import com.bvakp.automation.core.config.ConfigManager;

/**
 * Açık Veri sürecindeki kullanıcı rolleri.
 *
 * <p>Her rolün kullanıcısı ortam dosyasındaki username.&lt;rol&gt; anahtarından, parolası
 * BVAKP_PASSWORD_&lt;ROL&gt; ortam değişkeninden okunur. Tanımlı değilse ortak username ve
 * BVAKP_PASSWORD kullanılır; böylece tek kullanıcıyla da çalışır.
 */
public enum PortalRol {
  /** Kaynak ve veri seti oluşturur, KVKK onayına gönderir, reddedilen veri setini düzenler. */
  HAZIRLAYAN("hazirlayan"),
  /** KVKK onayı verir veya reddeder. */
  KVKK("kvkk"),
  /** Yayınlar, yayını reddeder, arşivler, siler, manuel güncelleme başlatır. */
  YAYIN("yayin");

  private final String anahtar;

  PortalRol(String anahtar) {
    this.anahtar = anahtar;
  }

  /** Rolün kullanıcı adı; username.&lt;rol&gt; yoksa username. */
  public String kullaniciAdi() {
    return ConfigManager.getOrDefault("username." + anahtar, ConfigManager.get("username"));
  }

  /** Rolün parolası; BVAKP_PASSWORD_&lt;ROL&gt; yoksa BVAKP_PASSWORD. */
  public String parola() {
    return ConfigManager.getOrDefault("password." + anahtar, ConfigManager.get("password"));
  }
}
