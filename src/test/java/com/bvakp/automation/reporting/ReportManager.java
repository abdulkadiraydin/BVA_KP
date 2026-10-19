package com.bvakp.automation.reporting;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReportManager {

    private static final Logger logger =
            LoggerFactory.getLogger(ReportManager.class);

    private ReportManager() {
    }

    public static void step(String message) {

        logger.info("STEP | {}", message);

        Allure.step(message);

        HtmlReportManager.addStep(message);
    }

    public static void info(String message) {

        logger.info("INFO | {}", message);
    }

    public static void warn(String message) {

        logger.warn("WARN | {}", message);
    }

    public static void error(String message) {

        logger.error("ERROR | {}", message);
    }

    public static void error(
            String message,
            Throwable throwable) {

        logger.error(
                "ERROR | {}",
                message,
                throwable
        );
    }
    public static void pass(String message) {
        logger.info("PASS | {}", message);
        Allure.step("PASS | " + message);
        HtmlReportManager.addStep("PASS | " + message);
    }

    public static void fail(String message) {
        logger.error("FAIL | {}", message);
        Allure.step("FAIL | " + message);
        HtmlReportManager.addStep("FAIL | " + message);
    }


    /**
     * Ekran görüntüsünü HTML raporuna ekler.
     * Aktif Allure testi varsa aynı görüntüyü
     * Allure raporuna da ekler.
     */
    public static void attachScreenshot(
            String name,
            Path screenshotPath) {

        if (screenshotPath == null
                || !Files.isRegularFile(screenshotPath)) {
            logger.warn("Screenshot dosyası bulunamadı: {}", screenshotPath);
            return;
        }

        HtmlReportManager.setScreenshot(screenshotPath);

        if (Allure.getLifecycle()
                .getCurrentTestCase()
                .isEmpty()) {

            logger.warn(
                    "Aktif Allure testi bulunamadı. " +
                            "Screenshot yalnızca HTML raporuna eklendi: {}",
                    screenshotPath
            );

            return;
        }

        try (InputStream inputStream =
                     Files.newInputStream(screenshotPath)) {

            Allure.addAttachment(
                    name,
                    "image/png",
                    inputStream,
                    ".png"
            );

            logger.info(
                    "ATTACHMENT | Screenshot Allure'a eklendi: {}",
                    screenshotPath
            );

        } catch (IOException e) {

            logger.error(
                    "Screenshot Allure raporuna eklenemedi.",
                    e
            );
        }
    }

}