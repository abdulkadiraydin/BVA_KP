package com.bvakp.automation.reporting;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.mail.TestFailureAlertMailService;
import com.bvakp.automation.utils.ScreenshotUtil;
import com.bvakp.automation.utils.TestFailureTracker;
import com.bvakp.automation.utils.TestSummaryWriter;

import org.testng.IConfigurationListener;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Path;

public class TestListener
        implements ITestListener,
        IInvokedMethodListener,
        IConfigurationListener,
        ISuiteListener {

    private static final String SCREENSHOT_ATTRIBUTE =
            "test.screenshot.path";

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

        // afterInvocation sırasında alınan aynı ekran görüntüsü kullanılır.
        Path screenshotPath =
                (Path) result.getAttribute(SCREENSHOT_ATTRIBUTE);

        if (screenshotPath != null) {
            ReportManager.info(
                    "Başarılı test screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );
        } else {
            ReportManager.warn(
                    "Başarılı test için screenshot bulunamadı | "
                            + testName
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

        String testName = getTestName(result);
        String className = getClassName(result);
        long duration = getDuration(result);

        // Test hatasını raporlamak için kullanılır.
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

        // Screenshot daha önce afterInvocation içinde alınmıştır.
        // TestListener burada ikinci bir screenshot oluşturmaz.
        Path screenshotPath =
                (Path) result.getAttribute(SCREENSHOT_ATTRIBUTE);

        if (screenshotPath != null) {
            ReportManager.info(
                    "Başarısız test screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );
        } else {
            ReportManager.warn(
                    "Başarısız test için screenshot bulunamadı | "
                            + testName
            );
        }

        // Screenshot sonucundan bağımsız hata sayısını günceller.
        int failureCount = TestFailureTracker.hataKaydet(
                className,
                testName
        );

        ReportManager.info(
                "Ardışık hata sayısı | "
                        + testName
                        + " = "
                        + failureCount
        );

        // Hata eşiği aşıldığında bilgilendirme maili gönderir.
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

                TestFailureTracker.mailGonderildiKaydet(
                        className,
                        testName
                );

            } catch (Exception e) {
                ReportManager.error(
                        "Test hata bilgilendirme maili gönderilemedi: "
                                + e.getMessage(),
                        e
                );
            }
        }

        // Başarısız test sonucunu HTML raporuna kaydeder.
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

        // Video/trace dosyaları @AfterMethod'da tamamlandıktan sonra
        // ait oldukları testin Allure result.json dosyasına eklenir.
        AllureArtifactCollector.publishPendingArtifacts();

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

        // main branch'indeki suite sonu raporlama çağrısını korur.
        // Birleştirilmiş AllureRunOrganizer işlemi JVM kapanışına erteler;
        // böylece Allure attachment dosyaları eksiksiz arşivlenir.
        AllureRunOrganizer.organizeResults();

        System.out.println(
                "Test özeti oluşturuldu -> "
                        + "Toplam: " + toplam
                        + ", Başarılı: " + basarili
                        + ", Başarısız: " + basarisiz
                        + ", Atlanan: " + atlanan
        );
    }


    /**
     * Test metodu biter bitmez, sonuç callback'lerinden önce
     * tek ekran görüntüsü almak ve Allure/HTML raporlarına
     * eklemek için kullanılır.
     */
    @Override
    public void afterInvocation(
            IInvokedMethod method,
            ITestResult result) {

        if (!method.isTestMethod()) {
            return;
        }

        // Testin Allure UUID'sini ekran görüntüsü ve teardown öncesinde saklar.
        AllureArtifactCollector.bindCurrentTest(getClassName(result));

        if (result.getAttribute(SCREENSHOT_ATTRIBUTE) != null) {
            return;
        }

        try {
            var page = PlaywrightManager.getPage();

            if (page == null || page.isClosed()) {
                ReportManager.warn(
                        "Screenshot alınamadı: Aktif Page bulunamadı | "
                                + getTestName(result)
                );
                return;
            }

            String testName = getTestName(result);
            boolean basarisiz = result.getStatus() == ITestResult.FAILURE
                    || result.getThrowable() != null;

            String suffix = basarisiz ? "_hatali" : "_basarili";
            Path screenshotPath = ScreenshotUtil.takeScreenshot(
                    page,
                    testName + suffix
            );

            if (screenshotPath == null) {
                ReportManager.warn(
                        "Screenshot oluşturulamadı | " + testName
                );
                return;
            }

            // Screenshot'un fiziksel arsivde dogru class'a kopyalanmasini saglar.
            AllureArtifactCollector.registerScreenshot(screenshotPath);

            // Test sonucu callback'leri ve hata maili aynı dosyayı kullanır.
            result.setAttribute(
                    SCREENSHOT_ATTRIBUTE,
                    screenshotPath
            );

            ReportManager.attachScreenshot(
                    basarisiz ? "Failure Screenshot" : "Success Screenshot",
                    screenshotPath
            );

        } catch (Exception e) {
            // Raporlama hatası testin asıl sonucunu değiştirmez.
            ReportManager.warn(
                    "Screenshot işlemi başarısız | "
                            + getTestName(result) + " | " + e.getMessage()
            );
        }
    }

}
