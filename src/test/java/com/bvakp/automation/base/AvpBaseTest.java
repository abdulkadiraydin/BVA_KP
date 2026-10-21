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

/**
 * AVP (Açık Veri Portalı) testlerinin ortak temeli: oturum hazırlığı ve koşu sonu temizliği.
 */
public abstract class AvpBaseTest extends BaseTest {

    private static final int PORTAL_ACILIS_TIMEOUT_MS = 30_000;
    private static final int MAKSIMUM_NAVIGASYON_DENEMESI = 2;

    /**
     * Kayıtlı oturum dosyasının kullanılıp kullanılmayacağını belirler.
     *
     * <p>false (varsayılan) ise her test kendi girişini yapar. Paralel koşuda oturum
     * dosyasını paylaşmak, sunucunun token'ları birbirine karşı geçersiz kılmasına yol açıyor.
     */
    @Override
    protected boolean authStateKullan() {
        return Boolean.parseBoolean(ConfigManager.getOrDefault("auth.state.reuse", "false"));
    }

    /**
     * Portalı açar; kayıtlı oturum geçerliyse doğrudan devam eder,
     * değilse kullanıcı bilgileriyle giriş yapıp oturumu kaydeder.
     */
    protected void avpOturumuHazirlama() {

        ReportManager.step("AVP portalı açılıyor.");
        avpPortaliniAcma();

        LoginPage loginPage = new LoginPage(page());

        switch (loginPage.oturumDurumunuBekle()) {

            case GOSTERGE_PANOSU ->
                    ReportManager.info("Geçerli oturum kullanıldı.");

            case LOGIN_EKRANI -> {
                ReportManager.step("Kullanıcı bilgileri ile giriş yapılıyor.");
                loginPage.girisYapma(ConfigManager.get("username"), ConfigManager.get("password"));

                if (!loginPage.basariliGirisYapildiMi()) {
                    throw new IllegalStateException(
                            "AVP kullanıcı girişi başarısız. Gösterge Panosu görüntülenemedi.");
                }

                AvpAuthStateManager.authStateKaydetme(PlaywrightManager.getContext());
                ReportManager.info("AVP girişi başarılı, oturum kaydedildi.");
            }

            case BELIRSIZ -> throw new IllegalStateException(
                    "AVP oturum durumu belirlenemedi: ne giriş ekranı ne Gösterge Panosu görüntülendi. "
                            + "Mevcut URL: " + page().url());
        }
    }

    /** Portalı açar; geçici ağ hatalarında bir kez daha dener. */
    private void avpPortaliniAcma() {

        String portalUrl = ConfigManager.get("base.url");

        for (int deneme = 1; deneme <= MAKSIMUM_NAVIGASYON_DENEMESI; deneme++) {
            try {
                page().navigate(portalUrl, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                        .setTimeout(PORTAL_ACILIS_TIMEOUT_MS));
                return;

            } catch (PlaywrightException e) {
                if (!geciciAgHatasiMi(e) || deneme == MAKSIMUM_NAVIGASYON_DENEMESI) {
                    throw e;
                }
                ReportManager.warn("Geçici ağ hatası, portal tekrar açılıyor (" + deneme + "/"
                        + MAKSIMUM_NAVIGASYON_DENEMESI + "): " + e.getMessage());
            }
        }
    }

    private static boolean geciciAgHatasiMi(PlaywrightException e) {
        String mesaj = e.getMessage();
        return mesaj != null && (mesaj.contains("ERR_NETWORK_CHANGED") || mesaj.contains("ERR_ABORTED"));
    }

    /** Suite sonunda, cleanup.enabled açıksa testlerin oluşturduğu kaynakları temizler. */
    @AfterSuite(alwaysRun = true)
    public void kosuSonuOtomasyonKaynaklariniTemizleme() {

        if (!Boolean.parseBoolean(ConfigManager.getOrDefault("cleanup.enabled", "false"))) {
            ReportManager.info("Koşu sonu temizliği kapalı (cleanup.enabled=false). Atlandı.");
            return;
        }

        ReportManager.info("KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME BAŞLATILDI");

        try {
            OtomasyonKaynakTemizleyici.kosuSonuTemizliginiCalistir();
            ReportManager.info("KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLEME TAMAMLANDI");
        } catch (Exception e) {
            ReportManager.error("KOŞU SONU OTOMASYON KAYNAKLARI TEMİZLENEMEDİ: " + e.getMessage(), e);
        }
    }
}