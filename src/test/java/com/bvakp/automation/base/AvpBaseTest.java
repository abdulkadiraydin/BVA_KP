package com.bvakp.automation.base;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.DashboardPage;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;

public class AvpBaseTest extends BaseTest {

    /**
     * AVP testlerinde daha önce kaydedilmiş authentication
     * bilgisinin kullanılmasını sağlamak için kullanılır.
     *
     * @return AVP testlerinde auth state kullanılacağı için true
     */
    @Override
    protected boolean authStateKullanimi() {
        return true;
    }

    /**
     * AVP testleri başlamadan önce portalı açmak ve
     * kullanılabilir bir kullanıcı oturumu sağlamak için kullanılır.
     *
     * Kayıtlı authentication state geçerliyse tekrar kullanıcı adı
     * ve şifre girilmez. Login ekranı görüntülenirse environment
     * variable bilgileriyle giriş yapılır ve yeni oturum kaydedilir.
     *
     * @return giriş sonrası kullanılacak DashboardPage nesnesi
     */
    protected DashboardPage avpGirisYapma() {

        LoginPage loginPage = new LoginPage(page);
        DashboardPage dashboardPage = new DashboardPage(page);

        ReportManager.step("AVP portalı açılıyor.");
        loginPage.open();

        ReportManager.step("AVP oturum durumu kontrol ediliyor.");

        /*
         * Portal yönlendirmesi tamamlanana kadar Login ekranı veya
         * Dashboard ekranından birinin görünmesi beklenir.
         */
        page.waitForCondition(() ->
                loginPage.girisEkraniGoruntuleniyorMu()
                        || dashboardPage.gostergePanosuGorunuyorMu()
        );

        /*
         * Login ekranı açılmışsa kayıtlı oturum yoktur
         * veya mevcut oturumun süresi dolmuştur.
         */
        if (loginPage.girisEkraniGoruntuleniyorMu()) {

            ReportManager.step(
                    "Aktif AVP oturumu bulunamadı. Giriş işlemi gerçekleştiriliyor."
            );

            String kullaniciAdi = System.getenv("BVA_USERNAME");
            String sifre = System.getenv("BVA_PASSWORD");

            Assert.assertNotNull(
                    kullaniciAdi,
                    "BVA_USERNAME environment variable tanımlı değil."
            );

            Assert.assertFalse(
                    kullaniciAdi.isBlank(),
                    "BVA_USERNAME environment variable boş."
            );

            Assert.assertNotNull(
                    sifre,
                    "BVA_PASSWORD environment variable tanımlı değil."
            );

            Assert.assertFalse(
                    sifre.isBlank(),
                    "BVA_PASSWORD environment variable boş."
            );

            ReportManager.step("Kullanıcı adı giriliyor.");
            loginPage.kullaniciAdiGirme(kullaniciAdi);

            ReportManager.step("Şifre giriliyor.");
            loginPage.sifreGirme(sifre);

            ReportManager.step("Sign In butonuna tıklanıyor.");
            loginPage.girisButonunaTiklama();

        } else {

            ReportManager.step(
                    "Kayıtlı AVP oturumu bulundu. Login adımları atlanıyor."
            );
        }

        ReportManager.step(
                "Gösterge Panosu ekranının açıldığı doğrulanıyor."
        );

        Assert.assertTrue(
                dashboardPage.gostergePanosuGoruntulenmeKontrolu(),
                "Gösterge Panosu görüntülenemedi."
        );

        /*
         * Başarılı ve geçerli oturum sonraki testlerde
         * kullanılmak üzere tekrar kaydedilir.
         */
        AvpAuthStateManager.authStateKaydetme(
                PlaywrightManager.getContext()
        );

        return dashboardPage;
    }
}