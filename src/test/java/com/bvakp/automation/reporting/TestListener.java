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
    /**
     * Test başladığında çağrılır. HTML raporda teste ait kaydı açar.
     */
    @Override
    public void onTestStart(ITestResult result) {

        // İlk çağrıda koşu başlangıç envanterini alır; sonraki çağrılarda işlem yapmaz.
        AllureRunOrganizer.initialize();

        String testName = getTestName(result);
        String className = getClassName(result);

        HtmlReportManager.startTest(className, testName);

        ReportManager.info("TEST BAŞLADI | " + className + "." + testName);
    }

    /**
     * Test başarıyla tamamlandığında çağrılır.
     *
     * <p>afterInvocation aşamasında alınan ekran görüntüsünü raporlar,
     * HTML rapordaki test kaydını BASARILI olarak kapatır ve testin
     * ardışık hata sayacını sıfırlar.
     *
     * <p>Bu metotta yeni ekran görüntüsü alınmaz.
     */
    @Override
    public void onTestSuccess(ITestResult result) {

        String testName = getTestName(result);
        String className = getClassName(result);
        long duration = getDuration(result);

        // Ekran görüntüsü afterInvocation içinde alınıp test sonucuna iliştirilmiştir.
        Path screenshotPath = (Path) result.getAttribute(SCREENSHOT_ATTRIBUTE);

        if (screenshotPath != null) {
            ReportManager.info(
                    "Başarılı test screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );
        } else {
            ReportManager.warn(
                    "Başarılı test için screenshot bulunamadı | " + testName
            );
        }

        HtmlReportManager.markPassed(duration);

        ReportManager.info(
                "TEST BAŞARILI | " + testName + " | Süre: " + duration + " ms"
        );

        // Başarılı koşu, ardışık hata zincirini keser; sayaç sıfırlanır.
        // Takip dosyasına yazılamaması test sonucunu etkilememelidir.
        try {
            TestFailureTracker.basariliTestKaydet(className, testName);
        } catch (RuntimeException e) {
            ReportManager.warn(
                    "Ardışık hata sayacı sıfırlanamadı | " + testName
                            + " | " + e.getMessage()
            );
        }
    }

    /**
     * Test başarısız olduğunda çağrılır. Ekran görüntüsü afterInvocation'da alınır.
     * Hata takibi ve mail sorunları test sonucunun raporlanmasını engellemez.
     */
    @Override
    public void onTestFailure(ITestResult result) {

        AllureRunOrganizer.markFailed();

        String testName = getTestName(result);
        String className = getClassName(result);
        long duration = getDuration(result);
        Throwable hata = result.getThrowable();

        ReportManager.error("TEST BAŞARISIZ | " + testName);

        if (hata != null) {
            ReportManager.error("Hata detayı: " + hata.getMessage(), hata);
        }

        Path screenshotPath = (Path) result.getAttribute(SCREENSHOT_ATTRIBUTE);

        if (screenshotPath != null) {
            ReportManager.info(
                    "Başarısız test screenshot oluşturuldu | "
                            + screenshotPath.toAbsolutePath()
            );
        } else {
            ReportManager.warn(
                    "Başarısız test için screenshot bulunamadı | " + testName
            );
        }

        // Sonuç, takip ve mail adımlarından önce kesinleştirilir.
        HtmlReportManager.markFailed(duration, hata);

        hataTakibiVeMailIsle(className, testName, hata, screenshotPath);
    }

    /** Ardışık hata sayacını günceller, eşik aşıldıysa mail gönderir. */
    private void hataTakibiVeMailIsle(
            String className,
            String testName,
            Throwable hata,
            Path screenshotPath) {

        try {
            int failureCount = TestFailureTracker.hataKaydet(className, testName);

            ReportManager.info(
                    "Ardışık hata sayısı | " + testName + " = " + failureCount
            );

            if (!TestFailureTracker.mailGonderilmeliMi(className, testName, failureCount)) {
                return;
            }

            TestFailureAlertMailService.hataBilgilendirmeMailiGonder(
                    className, testName, failureCount, hata, screenshotPath
            );

            // Yalnızca başarılı gönderimde kaydedilir; aksi halde sonraki hatada tekrar denenir.
            TestFailureTracker.mailGonderildiKaydet(className, testName);

        } catch (Exception e) {
            ReportManager.error(
                    "Hata takibi veya mail işlenemedi | " + testName + " | " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Test çalıştırılmadan atlandığında çağrılır (hazırlık hatası,
     * bağımlı test hatası veya SkipException).
     */
    @Override
    public void onTestSkipped(ITestResult result) {

        String testName = getTestName(result);
        String className = getClassName(result);
        long duration = getDuration(result);
        Throwable sebep = result.getThrowable();

        HtmlReportManager.markSkipped(className, testName, duration, sebep);

        if (sebep != null) {
            ReportManager.warn(
                    "TEST ATLANDI | " + testName + " | Sebep: " + sebep.getMessage()
            );
        } else {
            ReportManager.warn("TEST ATLANDI | " + testName);
        }
    }

    /**
     * Hazırlık veya temizlik metodu (@Before.../@After...) başarısız olduğunda çağrılır.
     * Bu durumda ilgili testler çalıştırılmaz ve atlandı olarak raporlanır.
     */
    @Override
    public void onConfigurationFailure(ITestResult result) {

        // Testler hatalı değil atlandı sayılacağı için koşu burada hatalı işaretlenir.
        AllureRunOrganizer.markFailed();

        String className = getClassName(result);
        String metotAdi = result.getName();
        Throwable hata = result.getThrowable();

        ReportManager.error("CONFIGURATION HATASI | " + className + "." + metotAdi);

        if (hata != null) {
            ReportManager.error("Configuration hata detayı: " + hata.getMessage(), hata);
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

    /**
     * Suite tamamlandığında bir kez çağrılır.
     * Video/trace dosyalarını Allure sonuçlarına bağlar ve mail raporu için
     * test özetini target/test-summary.properties dosyasına yazar.
     */
    @Override
    public void onFinish(ISuite suite) {

        // Son testin videosu tearDown'da tamamlandığı için bağlama işlemi burada yapılır.
        AllureArtifactCollector.publishPendingArtifacts();

        int basarili = 0;
        int basarisiz = 0;
        int atlanan = 0;

        // testng.xml'deki her <test> bloğu ayrı sonuç tutar; hepsi toplanır.
        for (ISuiteResult suiteResult : suite.getResults().values()) {
            basarili += suiteResult.getTestContext().getPassedTests().size();
            basarisiz += suiteResult.getTestContext().getFailedTests().size();
            atlanan += suiteResult.getTestContext().getSkippedTests().size();
        }

        int toplam = basarili + basarisiz + atlanan;

        try {
            TestSummaryWriter.yaz(toplam, basarili, basarisiz, atlanan);
        } catch (RuntimeException e) {
            ReportManager.error("Test özet dosyası yazılamadı: " + e.getMessage(), e);
        }

        // Arşivleme JVM kapanışında yapılır; bu çağrı main branch uyumu için korunur.
        AllureRunOrganizer.organizeResults();

        ReportManager.info(
                "Test özeti -> Toplam: " + toplam
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
