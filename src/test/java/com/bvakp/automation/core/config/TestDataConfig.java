package com.bvakp.automation.core.config;

/**
 * Ortama göre değişen test verisi. Değerler env/<ortam>.properties dosyasından okunur (testdata.*
 * anahtarları); kodda sabit değer tutulmaz.
 *
 * <p>Değer tanımlı değilse ConfigManager hangi anahtarın eksik olduğunu yazan bir hata fırlatır.
 */
public final class TestDataConfig {

  private TestDataConfig() {}

  /** Kaynakların oluşturulduğu şema (ör. dwh). */
  public static String kaynakSemasi() {
    return ConfigManager.get("testdata.kaynak.sema");
  }

  /** Kaynak ve veri seti testlerinde kullanılan tablo (ör. d_hat). */
  public static String kaynakTablosu() {
    return ConfigManager.get("testdata.kaynak.tablo");
  }

  /** Maskeleme testlerinde Maskele kuralı uygulanan kolon (ör. hat). */
  public static String maskelenecekKolon() {
    return ConfigManager.get("testdata.maskelenecek.kolon");
  }

  /** Sorumlu Birim; tanımlı değilse boş döner ve listedeki ilk seçenek seçilir. */
  public static String sorumluBirim() {
    return ConfigManager.getOrDefault("testdata.sorumlu.birim", "");
  }

  /** Kategori; tanımlı değilse boş döner ve listedeki ilk seçenek seçilir. */
  public static String kategori() {
    return ConfigManager.getOrDefault("testdata.kategori", "");
  }

  /** Kullanım Lisansı; tanımlı değilse boş döner ve listedeki ilk seçenek seçilir. */
  public static String kullanimLisansi() {
    return ConfigManager.getOrDefault("testdata.kullanim.lisansi", "");
  }

  /** Düzenleme testinde seçilecek Sorumlu Birim; boşsa mevcut değerden farklı ilk seçenek. */
  public static String duzenlemeSorumluBirim() {
    return ConfigManager.getOrDefault("testdata.duzenleme.sorumlu.birim", "");
  }

  /** Düzenleme testinde seçilecek Kategori; boşsa mevcut değerden farklı ilk seçenek. */
  public static String duzenlemeKategori() {
    return ConfigManager.getOrDefault("testdata.duzenleme.kategori", "");
  }
}
