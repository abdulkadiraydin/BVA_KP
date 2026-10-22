package com.bvakp.automation.utils;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Tam sayfa ekran görüntüsü alır ve target/screenshots altına kaydeder.
 * Hata durumunda exception fırlatmaz, null döner; ekran görüntüsü alınamaması
 * test sonucunu değiştirmemelidir.
 */
public final class ScreenshotUtil {

    private static final Path SCREENSHOT_DIRECTORY = Paths.get("target", "screenshots");

    private static final DateTimeFormatter ZAMAN_BICIMI =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private ScreenshotUtil() {
    }

    /** Dosya adı: testAdi_basarili_2026-10-22_19-41-05_ab12cd34.png */
    public static Path takeScreenshot(Page page, String testName) {

        if (page == null) {
            return null;
        }

        try {
            Files.createDirectories(SCREENSHOT_DIRECTORY);

            // UUID, aynı adlı paralel testlerin aynı saniyede birbirini ezmesini önler.
            String fileName = sanitizeFileName(testName)
                    + "_" + LocalDateTime.now().format(ZAMAN_BICIMI)
                    + "_" + UUID.randomUUID().toString().substring(0, 8)
                    + ".png";

            Path screenshotPath = SCREENSHOT_DIRECTORY.resolve(fileName);

            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(screenshotPath)
                    .setFullPage(true));

            return screenshotPath;

        } catch (Exception e) {
            ReportManager.error("Screenshot alınırken hata oluştu.", e);
            return null;
        }
    }

    private static String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "screenshot";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}