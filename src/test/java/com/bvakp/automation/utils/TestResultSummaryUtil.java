package com.bvakp.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Test çalıştırması sonucunda oluşturulan özet dosyasını okuyarak
 * toplam, başarılı, başarısız ve atlanan test sayılarını elde eder.
 *
 * Elde edilen bilgiler mail raporunda test sonuçlarının gösterilmesi
 * ve genel test durumunun BASARILI / HATALI olarak belirlenmesi için kullanılır.
 */
public final class TestResultSummaryUtil {

    private static final Path SUMMARY_FILE = Paths.get("target", "test-summary.properties");

    private TestResultSummaryUtil() {
    }

    public static TestSummary sonTestSonucunuAl() {

        if (!Files.exists(SUMMARY_FILE)) {
            throw new IllegalStateException(
                    "Test özet dosyası bulunamadı: " + SUMMARY_FILE.toAbsolutePath());
        }

        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(SUMMARY_FILE)) {

            properties.load(inputStream);

            int toplam = Integer.parseInt(properties.getProperty("total", "0"));
            int basarili = Integer.parseInt(properties.getProperty("passed", "0"));
            int basarisiz = Integer.parseInt(properties.getProperty("failed", "0"));
            int atlanan = Integer.parseInt(properties.getProperty("skipped", "0"));

            return new TestSummary(toplam, basarili, basarisiz, atlanan);

        } catch (IOException e) {
            throw new RuntimeException("Test özet dosyası okunurken hata oluştu.", e);
        }
    }

    public record TestSummary(int toplam, int basarili, int basarisiz, int atlanan) {

        /**
         * Koşunun genel durumunu verir.
         * Atlanan test, hazırlık hatasının işareti olabileceği için hatalı sayılır;
         * hiç test çalışmamış koşu da başarılı kabul edilmez.
         */
        public String durum() {
            if (basarisiz > 0 || atlanan > 0 || toplam == 0) {
                return "HATALI";
            }
            return "BASARILI";
        }
    }
}