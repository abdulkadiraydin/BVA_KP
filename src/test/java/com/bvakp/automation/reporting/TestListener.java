package com.bvakp.automation.reporting;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.utils.ScreenshotUtil;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

public class TestListener
        implements ITestListener, IConfigurationListener {

    @Override
    public void onTestStart(ITestResult result) {

        String testName = getTestName(result);
        String className = getClassName(result);

        HtmlReportManager.startTest(
                className,
                testName
        );

        ReportManager.info(
                "TEST BAŞLADI | " + testName
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        long duration = getDuration(result);

        String testName = getTestName(result);

        /*
         * Önce başarılı testin ekran görüntüsünü al.
         */
        Path screenshotPath =
                ScreenshotUtil.takeScreenshot(
                        PlaywrightManager.getPage(),
                        testName + "_basarili"
                );

        if (screenshotPath != null) {

            ReportManager.info(
                    "Başarılı test screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );

            /*
             * Hem Allure'a hem HTML raporuna ekle.
             */
            ReportManager.attachScreenshot(
                    "Success Screenshot",
                    screenshotPath
            );
        }

        /*
         * Screenshot kaydedildikten sonra testi başarılı kapat.
         */
        HtmlReportManager.markPassed(
                duration
        );

        ReportManager.info(
                "TEST BAŞARILI | "
                        + testName
                        + " | Süre: "
                        + duration
                        + " ms"
        );
    }

    @Override
    public void onTestFailure(ITestResult result) {

        AllureRunOrganizer.markFailed();

        String testName =
                getTestName(result);

        long duration =
                getDuration(result);

        ReportManager.error(
                "TEST BAŞARISIZ | "
                        + testName
        );

        if (result.getThrowable() != null) {

            ReportManager.error(
                    "Hata detayı: "
                            + result.getThrowable()
                            .getMessage(),
                    result.getThrowable()
            );
        }

        /*
         * Önce screenshot alınır.
         */
        Path screenshotPath =
                ScreenshotUtil.takeScreenshot(
                        PlaywrightManager.getPage(),
                        testName
                );

        if (screenshotPath != null) {

            ReportManager.info(
                    "Screenshot oluşturuldu | "
                            + screenshotPath
                            .toAbsolutePath()
            );

            /*
             * Hem Allure'a hem HTML raporuna bağlanır.
             */
            ReportManager.attachScreenshot(
                    "Failure Screenshot",
                    screenshotPath
            );
        }

        /*
         * Screenshot kaydedildikten sonra
         * testi kapatıyoruz.
         */
        HtmlReportManager.markFailed(
                duration,
                result.getThrowable()
        );
    }

    @Override
    public void onTestSkipped(ITestResult result) {

        long duration =
                getDuration(result);

        HtmlReportManager.markSkipped(
                duration
        );

        ReportManager.warn(
                "TEST ATLANDI | "
                        + getTestName(result)
        );
    }

    @Override
    public void onConfigurationFailure(
            ITestResult result) {

        AllureRunOrganizer.markFailed();

        ReportManager.error(
                "CONFIGURATION HATASI | "
                        + result.getName()
        );

        if (result.getThrowable() != null) {

            ReportManager.error(
                    "Configuration hata detayı: "
                            + result.getThrowable()
                            .getMessage(),
                    result.getThrowable()
            );
        }
    }

    private String getTestName(
            ITestResult result) {

        return result
                .getMethod()
                .getMethodName();
    }

    private String getClassName(
            ITestResult result) {

        return result
                .getTestClass()
                .getRealClass()
                .getSimpleName();
    }

    private long getDuration(
            ITestResult result) {

        return result.getEndMillis()
                - result.getStartMillis();
    }
}