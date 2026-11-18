package com.bvakp.automation.core.auth;

import com.bvakp.automation.core.config.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Kamuya açık portalın (CKAN) rol başına giriş oturumunu saklar ve tekrar kullanır.
 *
 * <p>Portal kısa sürede çok giriş yapılınca istek sınırına takılır (HTTP 429). Bu yüzden her rol
 * için koşu başına bir kez giriş yapılır; oturum çerezleri .automation-state altında saklanır ve
 * sonraki testlerde tarayıcıya yüklenir. Yalnız kamuya açık portalın çerezleri saklanır ve
 * değiştirilir; aynı tarayıcıdaki kapalı portal oturumu etkilenmez.
 *
 * <p>Dosyalar oturum bilgisi içerir; .automation-state Git dışında tutulmalıdır.
 */
public final class PublicPortalAuthStateManager {

  private static final Path KLASOR = Paths.get(".automation-state");
  private static final Gson GSON = new Gson();
  private static final Map<PublicPortalRol, Object> KILITLER = new EnumMap<>(PublicPortalRol.class);

  static {
    for (PublicPortalRol rol : PublicPortalRol.values()) {
      KILITLER.put(rol, new Object());
    }
  }

  private PublicPortalAuthStateManager() {}

  /** Aynı rol için aynı anda yalnız bir thread'in giriş yapması için kilit. */
  public static Object kilit(PublicPortalRol rol) {
    return KILITLER.get(rol);
  }

  /**
   * Rolün kayıtlı oturumunu tarayıcıya yükler. Önce portalın mevcut çerezleri silinir; kayıt yoksa
   * false döner (tarayıcı giriş yapılmamış durumda kalır).
   */
  public static boolean oturumuYukleme(BrowserContext context, PublicPortalRol rol) {
    portalCerezleriniSilme(context);

    Path dosya = dosya(rol);
    if (!Files.isRegularFile(dosya)) {
      return false;
    }
    try {
      List<Cookie> cerezler =
          GSON.fromJson(
              Files.readString(dosya, StandardCharsets.UTF_8),
              new TypeToken<List<Cookie>>() {}.getType());
      if (cerezler == null || cerezler.isEmpty()) {
        return false;
      }
      context.addCookies(cerezler);
      return true;
    } catch (IOException | RuntimeException e) {
      // Bozuk dosya: kayıt yokmuş gibi davranılır, yeniden giriş yapılıp üzerine yazılır.
      return false;
    }
  }

  /** Tarayıcıdaki portal çerezlerini rolün oturumu olarak kaydeder. */
  public static void oturumuKaydetme(BrowserContext context, PublicPortalRol rol) {
    try {
      Files.createDirectories(KLASOR);
      Files.writeString(
          dosya(rol), GSON.toJson(context.cookies(portalAdresi())), StandardCharsets.UTF_8);
    } catch (IOException e) {
      // Kayıt yazılamazsa test devam eder; sonraki test yeniden giriş yapar.
      throw new IllegalStateException("Portal oturumu kaydedilemedi: " + rol, e);
    }
  }

  /** Tarayıcıdaki kamuya açık portal çerezlerini siler (giriş yapılmamış duruma döner). */
  public static void portalCerezleriniSilme(BrowserContext context) {
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setDomain(portalSunucusu()));
  }

  private static Path dosya(PublicPortalRol rol) {
    return KLASOR.resolve("public-portal-" + rol.name().toLowerCase(Locale.ROOT) + ".json");
  }

  private static String portalAdresi() {
    return ConfigManager.get("public.data.url");
  }

  private static String portalSunucusu() {
    return URI.create(portalAdresi()).getHost();
  }
}
