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

    protected Page page;

    static {
        AllureRunOrganizer.initialize();
    }

    /**
     * Her test öncesinde Playwright oturumunu başlatır
     * ve aktif Page nesnesini hazırlar.
     */
    @BeforeMethod
    public void setUp() {

        PlaywrightManager.initialize(
                authStateKullan()
        );

        page =
                PlaywrightManager.getPage();
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
     * Test tamamlandıktan sonra Playwright oturumunu kapatır.
     * Test sonucu video ve raporlama işlemleri için kullanılır.
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