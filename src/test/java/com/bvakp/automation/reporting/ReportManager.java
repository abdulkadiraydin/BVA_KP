package com.bvakp.automation.reporting;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ReportManager {

    private static final Logger logger =
            TestLogger.getLogger(ReportManager.class);

    private ReportManager() {
    }

    public static void step(String message) {

        logger.info("STEP | {}", message);

        Allure.step(message);
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

    public static void attachScreenshot(
            String name,
            Path screenshotPath) {

        if (screenshotPath == null) {
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
                    "ATTACHMENT | Screenshot Allure raporuna eklendi: {}",
                    screenshotPath
            );

        } catch (IOException e) {

            logger.error(
                    "ERROR | Screenshot Allure raporuna eklenemedi.",
                    e
            );
        }
    }











}