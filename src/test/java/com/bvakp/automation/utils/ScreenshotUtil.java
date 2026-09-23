package com.bvakp.automation.utils;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class ScreenshotUtil {

    private static final String SCREENSHOT_DIR =
            "target/screenshots";

    private ScreenshotUtil() {
    }

    public static Path takeScreenshot(
            Page page,
            String testName) {

        if (page == null) {
            return null;
        }

        try {

            Path directory = Paths.get(SCREENSHOT_DIR);

            Files.createDirectories(directory);

            String timestamp =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyyMMdd_HHmmss_SSS"
                                    )
                            );

            String fileName =
                    testName + "_" + timestamp + ".png";

            Path screenshotPath =
                    directory.resolve(fileName);

            page.screenshot(
                    new Page.ScreenshotOptions()
                            .setPath(screenshotPath)
                            .setFullPage(true)
            );

            return screenshotPath;

        } catch (Exception e) {

            ReportManager.error(
                    "Screenshot alınırken hata oluştu.",
                    e
            );

            return null;
        }
    }
}