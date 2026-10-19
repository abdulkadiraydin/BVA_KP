package com.bvakp.automation.reporting;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.mail.TestFailureAlertMailService;
import com.bvakp.automation.utils.ScreenshotUtil;
import com.bvakp.automation.utils.TestFailureTracker;
import com.bvakp.automation.utils.TestSummaryWriter;

import org.testng.IConfigurationListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

/**
 * Test yaşam döngüsünü dinler: rapor, screenshot, hata takibi ve özet.
 */
public class TestListener
        implements ITestListener,
        IConfigurationListener,
        ISuiteListener {

    // ------------------------------------------------------------------
    // Test başladı
    // ------------------------------------------------------------------

    @Override
    public void onTestStart(ITestResult result) {

        String testName = getTestName(result);
        String className = getClassName(result);

        HtmlReportManager.startTest(className, testName);

        ReportManager.info(
                "TEST BAŞLADI | " + className + "." + testName
        );
    }

    // ------------------------------------------------------------------
    // Test başarılı
    // ------------------------------------------------------------------

    @Override
    public void onTestSuccess(ITestResult result) {

        long duration = getDuration(result);
        String testName = getTestName(result);

        /*
         * Başarılı testte screenshot varsayılan olarak kapalıdır.
         * Açmak için config.properties: screenshot.on.success=true
         */
        if (basariliTestteScreenshotAlinsinMi()) {

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

                ReportManager.attachScreenshot(
                        "Success Screenshot",
                        screenshotPath
                );
            }
        }

        HtmlReportManager.markPassed(duration);

        ReportManager.info(
                "TEST BAŞARILI | "
                        + testName
                        + " | Süre: "
                        + duration
                        + " ms"
        );

        TestFailureTracker.basariliTestKaydet(
                getClassName(result),
                testName
        );
    }

    // ------------------------------------------------------------------
    // Test başarısız
    // ------------------------------------------------------------------

    @Override
    public void onTestFailure(ITestResult result) {

        AllureRunOrganizer.markFailed();

        String testName = getTestName(result);
        String className = getClassName(result);
        long duration = getDuration(result);

        ReportManager.error("TEST BAŞARISIZ | " + testName);

        if (result.getThrowable() != null) {

            ReportManager.error(
                    "Hata detayı: " + result.getThrowable().getMessage(),
                    result.getThrowable()
            );
        }

        /*
         * Screenshot yalnızca ek bilgidir. Alınamazsa (null) hata takibi
         * ve mail yine de çalışır.
         */
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

            /* Hem Allure'a hem HTML raporuna bağlanır. */
            ReportManager.attachScreenshot(
                    "Failure Screenshot",
                    screenshotPath
            );

        } else {

            ReportManager.warn("Screenshot alınamadı | " + testName);
        }

        /*
         * Hata sayacı ve mail, screenshot'a bağlı değildir.
         */
        int failureCount =
                TestFailureTracker.hataKaydet(className, testName);

        ReportManager.info(
                "Ardışık hata sayısı | "
                        + testName
                        + " = "
                        + failureCount
        );

        if (TestFailureTracker.mailGonderilmeliMi(
                className,
                testName,
                failureCount
        )) {

            hataMailiGonder(
                    className,
                    testName,
                    failureCount,
                    result.getThrowable(),
                    screenshotPath
            );
        }

        HtmlReportManager.markFailed(
                duration,
                result.getThrowable()
        );
    }

    // ------------------------------------------------------------------
    // Test atlandı
    // ------------------------------------------------------------------

    @Override
    public void onTestSkipped(ITestResult result) {

        HtmlReportManager.markSkipped(getDuration(result));

        ReportManager.warn(
                "TEST ATLANDI | " + getTestName(result)
        );
    }

    // ------------------------------------------------------------------
    // Configuration (@BeforeMethod, @AfterMethod vb.) hatası
    // ------------------------------------------------------------------

    @Override
    public void onConfigurationFailure(ITestResult result) {

        AllureRunOrganizer.markFailed();

        ReportManager.error(
                "CONFIGURATION HATASI | " + result.getName()
        );

        if (result.getThrowable() != null) {

            ReportManager.error(
                    "Configuration hata detayı: "
                            + result.getThrowable().getMessage(),
                    result.getThrowable()
            );
        }
    }

    // ------------------------------------------------------------------
    // Suite bitti: sonuç özeti
    // ------------------------------------------------------------------

    /**
     * Başarılı, başarısız ve atlanan test sayılarını hesaplar ve
     * mail raporunda kullanılmak üzere target/test-summary.properties
     * dosyasına yazar.
     */
    @Override
    public void onFinish(ISuite suite) {

        int basarili = 0;
        int basarisiz = 0;
        int atlanan = 0;

        /*
         * Suite içinde birden fazla <test> bloğu olabileceği için
         * bütün test context sonuçları birlikte hesaplanır.
         */
        for (ISuiteResult suiteResult : suite.getResults().values()) {

            basarili += suiteResult.getTestContext()
                    .getPassedTests().size();

            basarisiz += suiteResult.getTestContext()
                    .getFailedTests().size();

            atlanan += suiteResult.getTestContext()
                    .getSkippedTests().size();
        }

        int toplam = basarili + basarisiz + atlanan;

        /* Raporlar koşu biter bitmez burada üretilir. */
        AllureRunOrganizer.organizeResults();

        TestSummaryWriter.yaz(toplam, basarili, basarisiz, atlanan);

        System.out.println(
                "Test özeti oluşturuldu -> "
                        + "Toplam: " + toplam
                        + ", Başarılı: " + basarili
                        + ", Başarısız: " + basarisiz
                        + ", Atlanan: " + atlanan
        );
    }

    // ------------------------------------------------------------------
    // Yardımcı metotlar
    // ------------------------------------------------------------------

    private boolean basariliTestteScreenshotAlinsinMi() {

        return Boolean.parseBoolean(
                ConfigManager.getOrDefault("screenshot.on.success", "false")
        );
    }

    /**
     * Mail hatası test sonucunu değiştirmesin diye
     * exception burada yakalanır.
     */
    private void hataMailiGonder(
            String className,
            String testName,
            int failureCount,
            Throwable hata,
            Path screenshotPath) {

        try {

            TestFailureAlertMailService.hataBilgilendirmeMailiGonder(
                    className,
                    testName,
                    failureCount,
                    hata,
                    screenshotPath
            );

            /*
             * Mail gerçekten gönderildikten sonra
             * günlük gönderim tarihi kaydedilir.
             */
            TestFailureTracker.mailGonderildiKaydet(className, testName);

        } catch (Exception e) {

            ReportManager.error(
                    "Test hata bilgilendirme maili gönderilemedi: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private String getTestName(ITestResult result) {

        return result.getMethod().getMethodName();
    }

    private String getClassName(ITestResult result) {

        return result.getTestClass().getRealClass().getSimpleName();
    }

    private long getDuration(ITestResult result) {

        return result.getEndMillis() - result.getStartMillis();
    }
}