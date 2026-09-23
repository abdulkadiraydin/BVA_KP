package com.bvakp.automation.reporting;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.utils.ScreenshotUtil;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {

        ReportManager.info(
                "TEST BAŞLADI | " + getTestName(result)
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        long duration = getDuration(result);

        ReportManager.info(
                "TEST BAŞARILI | "
                        + getTestName(result)
                        + " | Süre: "
                        + duration
                        + " ms"
        );
    }

    @Override
    public void onTestFailure(ITestResult result) {
        AllureRunOrganizer.markFailed();
        String testName = getTestName(result);

        ReportManager.error(
                "TEST BAŞARISIZ | " + testName
        );

        if (result.getThrowable() != null) {

            ReportManager.error(
                    "Hata detayı: "
                            + result.getThrowable().getMessage(),
                    result.getThrowable()
            );
        }

        Path screenshotPath =
                ScreenshotUtil.takeScreenshot(
                        PlaywrightManager.getPage(),
                        testName
                );

        if (screenshotPath != null) {

            ReportManager.info(
                    "Screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );

            ReportManager.attachScreenshot(
                    "Failure Screenshot",
                    screenshotPath
            );
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        ReportManager.warn(
                "TEST ATLANDI | " + getTestName(result)
        );
    }

    private String getTestName(ITestResult result) {

        return result
                .getMethod()
                .getMethodName();
    }

    private long getDuration(ITestResult result) {

        return result.getEndMillis()
                - result.getStartMillis();
    }
}