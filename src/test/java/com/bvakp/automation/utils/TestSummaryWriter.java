package com.bvakp.automation.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Test çalıştırması tamamlandığında test sonuçlarını kalıcı bir özet
 * dosyasına yazar.
 *
 * Oluşturulan dosya daha sonra mail raporu hazırlanırken
 * TestResultSummaryUtil tarafından okunur.
 */
public final class TestSummaryWriter {

    private static final Path SUMMARY_FILE =
            Paths.get(
                    "target",
                    "test-summary.properties"
            );

    private TestSummaryWriter() {
    }

    public static void yaz(
            int toplam,
            int basarili,
            int basarisiz,
            int atlanan) {

        Properties properties =
                new Properties();

        properties.setProperty(
                "total",
                String.valueOf(toplam)
        );

        properties.setProperty(
                "passed",
                String.valueOf(basarili)
        );

        properties.setProperty(
                "failed",
                String.valueOf(basarisiz)
        );

        properties.setProperty(
                "skipped",
                String.valueOf(atlanan)
        );

        try {

            Files.createDirectories(
                    SUMMARY_FILE.getParent()
            );

            try (OutputStream outputStream =
                         Files.newOutputStream(
                                 SUMMARY_FILE
                         )) {

                properties.store(
                        outputStream,
                        "BVA KP Automation Test Summary"
                );
            }

            System.out.println(
                    "Test özet dosyası oluşturuldu: "
                            + SUMMARY_FILE.toAbsolutePath()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Test özet dosyası oluşturulurken hata oluştu.",
                    e
            );
        }
    }
}