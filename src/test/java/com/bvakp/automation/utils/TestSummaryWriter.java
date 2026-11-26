package com.bvakp.automation.utils;

import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestResultSummaryUtil.TestSonucu;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * Test çalıştırması tamamlandığında test sonuçlarını kalıcı bir özet dosyasına yazar. Oluşturulan
 * dosya mail raporu hazırlanırken TestResultSummaryUtil tarafından okunur.
 */
public final class TestSummaryWriter {

  private static final Path SUMMARY_FILE = Paths.get("target", "test-summary.properties");

  private TestSummaryWriter() {}

  /** Koşunun test sayılarını özet dosyasına yazar (mail bu dosyayı okur). */
  public static void yaz(int toplam, int basarili, int basarisiz, int atlanan) {

    Properties properties = new Properties();

    // Anahtar adları TestResultSummaryUtil ile aynı olmalıdır.
    properties.setProperty("total", String.valueOf(toplam));
    properties.setProperty("passed", String.valueOf(basarili));
    properties.setProperty("failed", String.valueOf(basarisiz));
    properties.setProperty("skipped", String.valueOf(atlanan));

    try {
      Files.createDirectories(SUMMARY_FILE.getParent());

      try (OutputStream outputStream = Files.newOutputStream(SUMMARY_FILE)) {
        properties.store(outputStream, "BVA KP Automation Test Summary");
      }

      ReportManager.info("Test özet dosyası oluşturuldu: " + SUMMARY_FILE.toAbsolutePath());

    } catch (IOException e) {
      throw new RuntimeException("Test özet dosyası oluşturulurken hata oluştu.", e);
    }
  }

  /** Her testin sonucunu JSON olarak yazar; rapor maili test bazında tabloyu bundan üretir. */
  public static void sonuclariYaz(List<TestSonucu> sonuclar) {

    Path dosya = TestResultSummaryUtil.SONUCLAR_DOSYASI;

    try {
      Files.createDirectories(dosya.getParent());
      new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(dosya.toFile(), sonuclar);

      ReportManager.info("Test sonuçları dosyası oluşturuldu: " + dosya.toAbsolutePath());

    } catch (IOException e) {
      throw new RuntimeException("Test sonuçları dosyası oluşturulurken hata oluştu.", e);
    }
  }
}
