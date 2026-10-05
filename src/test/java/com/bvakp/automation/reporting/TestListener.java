package com.bvakp.automation.reporting;

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

public class TestListener
        implements ITestListener,
        IConfigurationListener,
        ISuiteListener {

    @Override
    public void onTestStart(ITestResult result) {

        /*
         * Listener artık testng.xml üzerinden suite seviyesinde
         * çalıştığı için bütün test class'ları rapora dahil edilir.
         */
        AllureRunOrganizer.initialize();

        String testName =
                getTestName(result);

        String className =
                getClassName(result);

        HtmlReportManager.startTest(
                className,
                testName
        );

        ReportManager.info(
                "TEST BAŞLADI | "
                        + className
                        + "."
                        + testName
        );
    }

    @Override
    public void onTestSuccess(ITestResult result) {

        long duration =
                getDuration(result);

        String testName =
                getTestName(result);

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

        HtmlReportManager.markPassed(
                duration
        );

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
        TestFailureTracker
                .basariliTestKaydet(
                        getClassName(result),
                        testName
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

            String className =
                    getClassName(result);

            int failureCount =
                    TestFailureTracker.hataKaydet(
                            className,
                            testName
                    );

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

                try {

                    TestFailureAlertMailService
                            .hataBilgilendirmeMailiGonder(
                                    className,
                                    testName,
                                    failureCount,
                                    result.getThrowable(),
                                    screenshotPath
                            );

                    /*
                     * Mail gerçekten başarılı gönderildikten sonra
                     * günlük gönderim tarihi kaydedilir.
                     */
                    TestFailureTracker
                            .mailGonderildiKaydet(
                                    className,
                                    testName
                            );

                } catch (Exception e) {

                    /*
                     * Mail hatası test sonucunu değiştirmesin.
                     */
                    ReportManager.error(
                            "Test hata bilgilendirme maili gönderilemedi: "
                                    + e.getMessage(),
                            e
                    );
                }
            }
        }

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
                            + result.getThrowable().getMessage(),
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

    /**
     * TestNG suite çalışması tamamen tamamlandığında
     * başarılı, başarısız ve atlanan test sayılarını hesaplar.
     *
     * Hesaplanan sonuçlar mail raporunda kullanılmak üzere
     * target/test-summary.properties dosyasına yazılır.
     */
    @Override
    public void onFinish(ISuite suite) {

        int basarili = 0;
        int basarisiz = 0;
        int atlanan = 0;

        /*
         * Suite içerisinde birden fazla <test> bloğu bulunabileceği için
         * bütün TestNG test context sonuçları birlikte hesaplanır.
         */
        for (ISuiteResult suiteResult
                : suite.getResults().values()) {

            basarili +=
                    suiteResult
                            .getTestContext()
                            .getPassedTests()
                            .size();

            basarisiz +=
                    suiteResult
                            .getTestContext()
                            .getFailedTests()
                            .size();

            atlanan +=
                    suiteResult
                            .getTestContext()
                            .getSkippedTests()
                            .size();
        }

        int toplam =
                basarili
                        + basarisiz
                        + atlanan;

        TestSummaryWriter.yaz(
                toplam,
                basarili,
                basarisiz,
                atlanan
        );

        System.out.println(
                "Test özeti oluşturuldu -> "
                        + "Toplam: " + toplam
                        + ", Başarılı: " + basarili
                        + ", Başarısız: " + basarisiz
                        + ", Atlanan: " + atlanan
        );
    }
}