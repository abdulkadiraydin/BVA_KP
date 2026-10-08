package com.bvakp.automation.base;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.OtomasyonKaynakTemizleyici;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.annotations.AfterSuite;

public class AvpBaseTest extends BaseTest {

    /**
     * AVP testlerinde kayıtlı authentication
     * state kullanılmasını sağlar.
     *
     * @return auth state kullanılacaksa true
     */
    @Override
    protected boolean authStateKullan() {
        return true;
    }


    /**
     * AVP testleri için kullanıcı oturumunu hazırlar.
     *
     * Portal açıldıktan sonra login ekranı veya
     * Gösterge Panosu oluşana kadar beklenir.
     *
     * Login ekranı açılırsa giriş yapılır.
     * Gösterge Panosu açılırsa mevcut auth state kullanılır.
     */
    protected void avpOturumuHazirlama() {

        ReportManager.step(
                "AVP portalı açılıyor."
        );

        avpPortaliniAcma();


        LoginPage loginPage =
                new LoginPage(page);


        ReportManager.step(
                "AVP oturum durumu bekleniyor."
        );


        /*
         * OAuth / Keycloak yönlendirmesi asenkron çalıştığı için
         * login veya dashboard ekranlarından biri oluşana kadar
         * kontrollü şekilde beklenir.
         */
        int maksimumBeklemeSaniye =
                30;

        boolean loginEkraniAcildi =
                false;

        boolean dashboardAcildi =
                false;


        for (int saniye = 0;
             saniye < maksimumBeklemeSaniye;
             saniye++) {

            /*
             * Önce geçerli oturum kontrol edilir.
             */
            if (loginPage.gostergePanosuGorunurMu()) {

                dashboardAcildi =
                        true;

                break;
            }


            /*
             * Auth state geçersizse Keycloak login
             * ekranının açılması beklenir.
             */
            if (loginPage.loginEkraniGorunurMu()) {

                loginEkraniAcildi =
                        true;

                break;
            }


            page.waitForTimeout(
                    1000
            );
        }


        /*
         * Mevcut auth state ile dashboard açılmışsa
         * login işlemi yapılmadan devam edilir.
         */
        if (dashboardAcildi) {

            ReportManager.info(
                    "Geçerli authentication state kullanıldı."
            );

            return;
        }


        /*
         * Login ekranı oluştuysa kullanıcı bilgileri girilir.
         */
        if (loginEkraniAcildi) {

            ReportManager.info(
                    "Aktif AVP oturumu bulunamadı. "
                            + "Giriş işlemi gerçekleştirilecek."
            );


            ReportManager.step(
                    "Geçerli kullanıcı adı giriliyor."
            );

            loginPage
                    .kullaniciAdiGirme(
                            ConfigManager.get("username")
                    );


            ReportManager.step(
                    "Geçerli kullanıcı şifresi giriliyor."
            );

            loginPage
                    .sifreGirme(
                            ConfigManager.get("password")
                    );


            ReportManager.step(
                    "Sign In butonuna tıklanıyor."
            );

            loginPage
                    .girisButonunaTiklama();


            /*
             * Login işleminden sonra
             * Gösterge Panosu doğrulanır.
             */
            ReportManager.step(
                    "Gösterge Panosu ekranının açıldığı doğrulanıyor."
            );

            if (!loginPage.basariliGirisYapildiMi()) {

                throw new IllegalStateException(
                        "AVP kullanıcı girişi başarısız. "
                                + "Gösterge Panosu görüntülenemedi."
                );
            }


            /*
             * Başarılı login sonrasında authentication
             * state sonraki testlerde kullanılmak üzere kaydedilir.
             */
            ReportManager.step(
                    "AVP authentication state kaydediliyor."
            );

            PlaywrightManager
                    .getContext()
                    .storageState(
                            new BrowserContext.StorageStateOptions()
                                    .setPath(
                                            AvpAuthStateManager
                                                    .authStatePathAlma()
                                    )
                    );


            ReportManager.info(
                    "AVP authentication state güncellendi."
            );

            ReportManager.info(
                    "AVP kullanıcı girişi başarılı."
            );

            return;
        }


        /*
         * Belirlenen süre içerisinde ne login ne de dashboard
         * oluşmuşsa ortam beklenen duruma ulaşmamıştır.
         */
        throw new IllegalStateException(
                "AVP oturum durumu 30 saniye içerisinde belirlenemedi. "
                        + "Login ekranı veya Gösterge Panosu görüntülenemedi. "
                        + "Mevcut URL: "
                        + page.url()
        );
    }


    /**
     * AVP portalını açar.
     *
     * Chromium tarafından geçici ERR_NETWORK_CHANGED
     * hatası dönerse navigation işlemini bir kez tekrarlar.
     */
    private void avpPortaliniAcma() {

        String portalUrl =
                ConfigManager.get("base.url");

        int maksimumDeneme =
                2;


        for (int deneme = 1;
             deneme <= maksimumDeneme;
             deneme++) {

            try {

                ReportManager.info(
                        "AVP portal navigation denemesi: "
                                + deneme
                                + "/"
                                + maksimumDeneme
                );


                page.navigate(
                        portalUrl,
                        new Page.NavigateOptions()
                                .setWaitUntil(
                                        WaitUntilState.DOMCONTENTLOADED
                                )
                                .setTimeout(
                                        30000
                                )
                );


                ReportManager.info(
                        "AVP portal navigation tamamlandı | URL: "
                                + page.url()
                );

                return;


            } catch (PlaywrightException e) {

                boolean networkChanged =
                        e.getMessage() != null
                                && e.getMessage()
                                .contains(
                                        "ERR_NETWORK_CHANGED"
                                );


                /*
                 * ERR_NETWORK_CHANGED dışında bir hata oluşmuşsa
                 * doğrudan üst katmana iletilir.
                 */
                if (!networkChanged) {

                    throw e;
                }


                /*
                 * Son denemede de aynı hata oluşmuşsa
                 * test hata ile sonlandırılır.
                 */
                if (deneme == maksimumDeneme) {

                    throw e;
                }


                ReportManager.info(
                        "Geçici ERR_NETWORK_CHANGED hatası oluştu. "
                                + "Portal navigation tekrar denenecek."
                );


                page.waitForTimeout(
                        1500
                );
            }
        }
    }
    /**
     * Tüm TestNG suite çalışması tamamlandıktan sonra
     * otomasyon tarafından oluşturulan test verilerini temizler.
     */
    @AfterSuite(alwaysRun = true)
    public void kosuSonuOtomasyonKaynaklariniTemizleme() {

        ReportManager.info(
                "KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME BAŞLATILDI"
        );

        try {

            OtomasyonKaynakTemizleyici
                    .kosuSonuTemizliginiCalistir();


            ReportManager.info(
                    "KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME TAMAMLANDI"
            );

        } catch (Exception e) {

            ReportManager.error(
                    "KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLENEMEDİ"
                            + " | Hata: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}