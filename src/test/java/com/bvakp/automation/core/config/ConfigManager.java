package com.bvakp.automation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Properties;

/**
 * Test ayarlarını okur. Bir değer şu öncelikle aranır:
 *
 * <ol>
 *   <li>-D parametresi (ör. {@code -Dheadless=false})
 *   <li>Ortam değişkeni: BVAKP_ + anahtarın büyük harfli hali, nokta yerine alt çizgi (ör. password
 *       → {@code BVAKP_PASSWORD}, base.url → {@code BVAKP_BASE_URL})
 *   <li>config/config.local.properties: kişisel ayarlar, Git'e eklenmez, isteğe bağlı
 *   <li>config/env/&lt;ortam&gt;.properties: ortama özel adresler ve kullanıcı
 *   <li>config/config.properties: bütün ortamlarda ortak ayarlar
 *   <li>getOrDefault ile verilen varsayılan
 * </ol>
 *
 * <p>Ortam {@code -Denv=...} veya {@code BVAKP_ENV} ile seçilir; verilmezse "test". Parola gibi
 * gizli değerler hiçbir dosyaya yazılmaz; ortam değişkeninden verilir.
 */
public final class ConfigManager {

  private static final String VARSAYILAN_ORTAM = "test";
  private static final String ORTAM_DEGISKENI_ONEKI = "BVAKP_";

  private static final String CONFIG_KLASORU = "config";
  private static final Path KAYNAK_KLASORU = Paths.get("src", "test", "resources", CONFIG_KLASORU);

  private static final String ORTAM = ortamBelirleme();
  private static final Properties properties = new Properties();

  static {
    // Sonra yüklenen dosya öncekinin aynı anahtarlarını ezer.
    dosyaYukleme("config.properties", true);
    dosyaYukleme("env/" + ORTAM + ".properties", true);
    dosyaYukleme("config.local.properties", false);
  }

  private ConfigManager() {}

  /** Seçili ortamın adı (ör. "test"); log ve raporda hangi ortamda koşulduğunu göstermek için. */
  public static String ortam() {
    return ORTAM;
  }

  /**
   * Zorunlu ayarı döndürür; hiçbir kaynakta yoksa nereye yazılabileceğini söyleyen hatayla durur.
   *
   * @param key ayar anahtarı (ör. base.url)
   * @return boşlukları kırpılmış değer
   */
  public static String get(String key) {

    String value = bul(key);

    if (value == null) {
      throw new RuntimeException(
          "Config değeri bulunamadı: "
              + key
              + " (ortam: "
              + ORTAM
              + "). -D"
              + key
              + ", ortam değişkeni "
              + ortamDegiskeniAdi(key)
              + " veya config dosyalarından biriyle verilmeli.");
    }
    return value;
  }

  /** Zorunlu ayarı true/false olarak döndürür. */
  public static boolean getBoolean(String key) {
    return Boolean.parseBoolean(get(key));
  }

  /**
   * İsteğe bağlı ayarı döndürür; hiçbir kaynakta yoksa varsayılanı döndürür.
   *
   * @param key ayar anahtarı
   * @param defaultValue ayar yoksa kullanılacak değer
   * @return boşlukları kırpılmış değer veya varsayılan
   */
  public static String getOrDefault(String key, String defaultValue) {
    String value = bul(key);
    return value != null ? value : defaultValue;
  }

  /** -D, ortam değişkeni ve yüklenen dosyalar sırasıyla aranır; boş değerler yok sayılır. */
  private static String bul(String key) {

    String value = System.getProperty(key);

    if (bosMu(value)) {
      value = System.getenv(ortamDegiskeniAdi(key));
    }
    if (bosMu(value)) {
      value = properties.getProperty(key);
    }
    return bosMu(value) ? null : value.trim();
  }

  /** -Denv, BVAKP_ENV, yoksa "test". Dosya adında kullanılacağı için harf/rakam/-/_ ile sınırlı. */
  private static String ortamBelirleme() {

    String ortam = System.getProperty("env");
    if (bosMu(ortam)) {
      ortam = System.getenv(ortamDegiskeniAdi("env"));
    }
    ortam = bosMu(ortam) ? VARSAYILAN_ORTAM : ortam.trim().toLowerCase(Locale.ROOT);

    if (!ortam.matches("[a-z0-9_-]+")) {
      throw new IllegalArgumentException("Geçersiz ortam adı: " + ortam);
    }
    return ortam;
  }

  /**
   * Dosyayı önce classpath'ten, bulunamazsa src/test/resources altından yükler.
   *
   * @param goreliYol config klasörüne göre yol (ör. env/test.properties)
   * @param zorunlu true ise dosya yoksa hata verir
   */
  private static void dosyaYukleme(String goreliYol, boolean zorunlu) {

    String kaynak = CONFIG_KLASORU + "/" + goreliYol;
    Path dosya = KAYNAK_KLASORU.resolve(goreliYol);

    try (InputStream classpath = ConfigManager.class.getClassLoader().getResourceAsStream(kaynak)) {

      if (classpath != null) {
        properties.load(classpath);
        return;
      }
      if (Files.exists(dosya)) {
        try (InputStream stream = Files.newInputStream(dosya)) {
          properties.load(stream);
        }
        return;
      }
      if (zorunlu) {
        throw new RuntimeException(
            "Config dosyası bulunamadı: " + kaynak + " | " + dosya.toAbsolutePath());
      }

    } catch (IOException e) {
      throw new RuntimeException("Config dosyası okunurken hata oluştu: " + kaynak, e);
    }
  }

  /** base.url → BVAKP_BASE_URL */
  private static String ortamDegiskeniAdi(String key) {
    return ORTAM_DEGISKENI_ONEKI + key.replace('.', '_').toUpperCase(Locale.ROOT);
  }

  /** Değer null veya yalnızca boşluksa true döner. */
  private static boolean bosMu(String value) {
    return value == null || value.isBlank();
  }
}
