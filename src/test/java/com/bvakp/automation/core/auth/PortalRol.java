package com.bvakp.automation.core.auth;

import com.bvakp.automation.core.config.ConfigManager;

/**
 * Kamuya açık portalın (CKAN) kullanıcı rolleri.
 *
 * <p>Kullanıcı adı ortam dosyasındaki portal.username.&lt;rol&gt; anahtarından, parola
 * BVAKP_PORTAL_PASSWORD_&lt;ROL&gt; ortam değişkeninden okunur. Bunlar AVP'nin Keycloak
 * kullanıcılarından ayrı, CKAN'ın kendi kullanıcılarıdır.
 */
public enum PortalRol {
    /** Veri talebi oluşturan ve API anahtarı kullanan portal kullanıcısı. */
    TALEP("talep"),
    /** Veri taleplerini yöneten sistem yöneticisi. */
    ONAY("onay");

    private final String anahtar;

    PortalRol(String anahtar) {
        this.anahtar = anahtar;
    }

    /** Rolün kullanıcı adı (portal.username.&lt;rol&gt;). */
    public String kullaniciAdi() {
        return ConfigManager.get("portal.username." + anahtar);
    }

    /** Rolün parolası (BVAKP_PORTAL_PASSWORD_&lt;ROL&gt;). */
    public String parola() {
        return ConfigManager.get("portal.password." + anahtar);
    }
}