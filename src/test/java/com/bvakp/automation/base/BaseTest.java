package com.bvakp.automation.base;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.reporting.AllureRunOrganizer;
import com.bvakp.automation.reporting.TestListener;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;

@Listeners(TestListener.class)
public abstract class BaseTest {

    static {
        java.util.Locale.setDefault(java.util.Locale.ENGLISH);
        AllureRunOrganizer.initialize();
    }

    /**
     * Her test öncesinde ilgili thread için bağımsız Playwright oturumu açar.
     * {@link ApiTesti} işaretli testlerde tarayıcı açılmaz, yalnızca API istemcisi başlar.
     *
     * @param testMetodu TestNG'nin verdiği, çalışacak test metodu
     */
    @BeforeMethod
    public void setUp(Method testMetodu) {

        if (testMetodu.isAnnotationPresent(ApiTesti.class)) {
            PlaywrightManager.apiBaslatma();
            return;
        }

        PlaywrightManager.initialize(authStateKullan());
    }

    /**
     * Aktif test thread'ine ait Page nesnesi.
     * {@link ApiTesti} işaretli testlerde null döner.
     */
    protected Page page() {
        return PlaywrightManager.getPage();
    }

    /** Testin kayıtlı authentication state kullanıp kullanmayacağı; varsayılan false. */
    protected boolean authStateKullan() {
        return false;
    }

    /** Test bitince yalnızca aktif thread'e ait Playwright oturumunu kapatır. */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {

        String testName = result.getMethod().getMethodName();

        String status = switch (result.getStatus()) {
            case ITestResult.SUCCESS -> "BASARILI";
            case ITestResult.FAILURE -> "HATALI";
            case ITestResult.SKIP -> "ATLANDI";
            default -> "BILINMIYOR";
        };

        PlaywrightManager.close(testName, status);
    }
}