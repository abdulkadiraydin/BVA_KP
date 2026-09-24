package com.bvakp.automation.base;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.reporting.AllureRunOrganizer;
import com.bvakp.automation.reporting.TestListener;
import com.microsoft.playwright.Page;
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
     * Test başlamadan önce Playwright ortamını oluşturmak
     * ve aktif Page nesnesini test sınıfına aktarmak için kullanılır.
     *
     * Alt test sınıfları authStateKullanimi() metodunu override ederek
     * kayıtlı authentication bilgisinin kullanılıp kullanılmayacağını
     * belirleyebilir.
     */
    @BeforeMethod
    public void setUp() {

        PlaywrightManager.initialize(
                authStateKullanimi()
        );

        page = PlaywrightManager.getPage();
    }

    /**
     * Test sınıfının kayıtlı authentication state kullanıp
     * kullanmayacağını belirlemek için kullanılır.
     *
     * Varsayılan olarak false döner ve temiz browser oturumu açılır.
     * Gerekli alt sınıflar bu metodu override edebilir.
     *
     * @return authentication state kullanılacaksa true
     */
    protected boolean authStateKullanimi() {
        return false;
    }

    /**
     * Her test tamamlandıktan sonra Playwright tarafından
     * oluşturulan browser kaynaklarını kapatmak için kullanılır.
     */
    @AfterMethod
    public void tearDown() {
        PlaywrightManager.close();
    }
}