package com.bvakp.automation.base;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.reporting.AllureRunOrganizer;
import com.bvakp.automation.reporting.TestListener;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest {

    static {
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        AllureRunOrganizer.initialize();
    }


    /**
     * Her test öncesinde ilgili test thread'i için
     * bağımsız Playwright oturumu oluşturur.
     */
    @BeforeMethod
    public void setUp() {

        PlaywrightManager.initialize(
                authStateKullan()
        );
    }


    /**
     * Aktif test thread'ine ait Playwright Page
     * nesnesini döndürmek için kullanılır.
     *
     * @return aktif thread'e ait Page
     */
    protected Page page() {

        return PlaywrightManager.getPage();
    }


    /**
     * Testin kayıtlı authentication state
     * kullanıp kullanmayacağını belirler.
     *
     * @return varsayılan olarak false
     */
    protected boolean authStateKullan() {

        return false;
    }


    /**
     * Test tamamlandıktan sonra yalnızca aktif
     * test thread'ine ait Playwright oturumunu kapatır.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(
            ITestResult result) {

        String testName =
                result.getMethod()
                        .getMethodName();

        String status =
                switch (result.getStatus()) {

                    case ITestResult.SUCCESS ->
                            "BASARILI";

                    case ITestResult.FAILURE ->
                            "HATALI";

                    case ITestResult.SKIP ->
                            "ATLANDI";

                    default ->
                            "BILINMIYOR";
                };

        PlaywrightManager.close(
                testName,
                status
        );
    }
}