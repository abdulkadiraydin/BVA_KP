package com.bvakp.automation.base;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.OtomasyonKaynakTemizleyici;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.annotations.AfterSuite;

public abstract class AvpBaseTest extends BaseTest {


    /**
     * Kayıtlı oturum dosyasının kullanılıp kullanılmayacağını belirler.
     * false ise her test kendi girişini yapar (paralel koşuda token çakışmasını önler).
     */
    @Override
    protected boolean authStateKullan() {
        return Boolean.parseBoolean(ConfigManager.getOrDefault("auth.state.reuse", "true"));
    }


    /**
     * AVP testleri için ilgili test thread'ine ait
     * bağımsız kullanıcı oturumunu hazırlar.
     */
    protected void avpOturumuHazirlama() {

        ReportManager.step(
                "AVP portalı açılıyor."
        );

        avpPortaliniAcma();


        LoginPage loginPage =
                new LoginPage(
                        page()
                );


        ReportManager.step(
                "AVP oturum durumu bekleniyor."
        );


        int maksimumBeklemeSaniye = 30;

        boolean loginEkraniAcildi = false;
        boolean dashboardAcildi = false;


        for (int saniye = 0;
             saniye < maksimumBeklemeSaniye;
             saniye++) {

            if (loginPage.gostergePanosuGorunurMu()) {

                dashboardAcildi = true;
                break;
            }


            if (loginPage.loginEkraniGorunurMu()) {

                loginEkraniAcildi = true;
                break;
            }


            page().waitForTimeout(
                    1000
            );
        }


        if (dashboardAcildi) {

            ReportManager.info(
                    "Geçerli authentication state kullanıldı."
            );

            return;
        }


        if (loginEkraniAcildi) {

            ReportManager.info(
                    "Aktif AVP oturumu bulunamadı. Giriş işlemi gerçekleştirilecek."
            );


            ReportManager.step(
                    "Geçerli kullanıcı bilgileri ile giriş yapılıyor."
            );

            loginPage.girisYapma(
                    ConfigManager.get("username"),
                    ConfigManager.get("password")
            );


            ReportManager.step(
                    "Gösterge Panosu ekranının açıldığı doğrulanıyor."
            );

            if (!loginPage.basariliGirisYapildiMi()) {

                throw new IllegalStateException(
                        "AVP kullanıcı girişi başarısız. Gösterge Panosu görüntülenemedi."
                );
            }


            ReportManager.step(
                    "AVP authentication state kaydediliyor."
            );

            AvpAuthStateManager.authStateKaydetme(
                    PlaywrightManager.getContext()
            );


            ReportManager.info(
                    "AVP authentication state güncellendi."
            );

            ReportManager.info(
                    "AVP kullanıcı girişi başarılı."
            );

            return;
        }


        throw new IllegalStateException(
                "AVP oturum durumu 30 saniye içerisinde belirlenemedi. "
                        + "Login ekranı veya Gösterge Panosu görüntülenemedi. "
                        + "Mevcut URL: "
                        + page().url()
        );
    }


    /**
     * AVP portalını ilgili test thread'ine ait Page
     * üzerinden açar ve geçici network hatasında tekrar dener.
     */
    private void avpPortaliniAcma() {

        String portalUrl =
                ConfigManager.get(
                        "base.url"
                );

        int maksimumDeneme = 2;


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


                page().navigate(
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
                                + page().url()
                );

                return;

            } catch (PlaywrightException e) {

                boolean tekrarDenenebilirHata =
                        e.getMessage() != null
                                && (
                                e.getMessage()
                                        .contains("ERR_NETWORK_CHANGED")
                                        ||
                                        e.getMessage()
                                                .contains("ERR_ABORTED")
                        );


                if (!tekrarDenenebilirHata
                        || deneme == maksimumDeneme) {

                    throw e;
                }


                ReportManager.info(
                        "Geçici navigation hatası oluştu. "
                                + "Portal navigation tekrar denenecek. "
                                + "Hata: "
                                + e.getMessage()
                );


                page().waitForTimeout(
                        1500
                );
            }
        }
    }


    /**
     * TestNG suite tamamlandıktan sonra koşu sırasında
     * oluşturulan otomasyon kaynaklarını temizler.
     */
    @AfterSuite(alwaysRun = true)
    public void kosuSonuOtomasyonKaynaklariniTemizleme() {

        boolean temizlikAcik = Boolean.parseBoolean(
                ConfigManager.getOrDefault("cleanup.enabled", "false")
        );

        if (!temizlikAcik) {
            ReportManager.info(
                    "Koşu sonu temizliği kapalı (cleanup.enabled=false). Atlandı."
            );
            return;
        }

        ReportManager.info("KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME BAŞLATILDI");

        try {
            OtomasyonKaynakTemizleyici.kosuSonuTemizliginiCalistir();
            ReportManager.info("KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME TAMAMLANDI");

        } catch (Exception e) {
            ReportManager.error(
                    "KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLENEMEDİ | Hata: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}