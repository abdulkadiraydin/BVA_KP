package com.bvakp.automation.base;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.auth.AvpOturum;
import com.bvakp.automation.core.auth.Rol;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.OtomasyonKaynakTemizleyici;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import org.testng.annotations.AfterSuite;

/** AVP (Açık Veri Portalı) testlerinin ortak temeli: oturum hazırlığı ve koşu sonu temizliği. */
public abstract class AvpBaseTest extends BaseTest {

  private static final int PORTAL_ACILIS_TIMEOUT_MS = 30_000;
  private static final int MAKSIMUM_NAVIGASYON_DENEMESI = 2;

  /**
   * Kayıtlı oturum dosyasının kullanılıp kullanılmayacağını belirler.
   *
   * <p>false (varsayılan) ise her test kendi girişini yapar. Paralel koşuda oturum dosyasını
   * paylaşmak, sunucunun token'ları birbirine karşı geçersiz kılmasına yol açıyor.
   */
  @Override
  protected boolean authStateKullan() {
    return Boolean.parseBoolean(ConfigManager.getOrDefault("auth.state.reuse", "false"));
  }

  /** Portalı açar ve verilen rolün kullanıcısıyla oturum hazırlar. */
  protected void avpOturumuHazirlama(Rol rol) {

    ReportManager.step("AVP portalı açılıyor.");
    avpPortaliniAcma();

    LoginPage loginPage = new LoginPage(page());

    switch (loginPage.oturumDurumunuBekle()) {
      case GOSTERGE_PANOSU -> {
        // Kayıtlı oturumun kime ait olduğu bilinmez; istenen rolle yeniden giriş yapılır.
        ReportManager.info("Kayıtlı oturum bulundu, istenen rolle giriş yapılıyor.");
        AvpOturum.aktifKullaniciSifirlama();
        AvpOturum.rolIleGirisYapma(page(), rol);
      }

      case LOGIN_EKRANI -> {
        ReportManager.step("Kullanıcı bilgileri ile giriş yapılıyor.");
        AvpOturum.girisYapma(page(), rol);
        AvpAuthStateManager.authStateKaydetme(PlaywrightManager.getContext());
      }

      case BELIRSIZ ->
          throw new IllegalStateException(
              "AVP oturum durumu belirlenemedi: ne giriş ekranı ne Gösterge Panosu görüntülendi. "
                  + "Mevcut URL: "
                  + page().url());
    }
  }

  /** Portalı açar; geçici ağ hatalarında bir kez daha dener. */
  private void avpPortaliniAcma() {

    String portalUrl = ConfigManager.get("base.url");

    for (int deneme = 1; deneme <= MAKSIMUM_NAVIGASYON_DENEMESI; deneme++) {
      try {
        page()
            .navigate(
                portalUrl,
                new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(PORTAL_ACILIS_TIMEOUT_MS));
        return;

      } catch (PlaywrightException e) {
        if (!geciciAgHatasiMi(e) || deneme == MAKSIMUM_NAVIGASYON_DENEMESI) {
          throw e;
        }
        ReportManager.warn(
            "Geçici ağ hatası, portal tekrar açılıyor ("
                + deneme
                + "/"
                + MAKSIMUM_NAVIGASYON_DENEMESI
                + "): "
                + e.getMessage());
      }
    }
  }

  /** Hata geçici bir ağ hatasıysa (ERR_NETWORK_CHANGED, ERR_ABORTED) true döner. */
  private static boolean geciciAgHatasiMi(PlaywrightException e) {
    String mesaj = e.getMessage();
    return mesaj != null
        && (mesaj.contains("ERR_NETWORK_CHANGED") || mesaj.contains("ERR_ABORTED"));
  }

  /** Suite sonunda, cleanup.enabled açıksa testlerin oluşturduğu kaynakları temizler. */
  @AfterSuite(alwaysRun = true)
  public void kosuSonuKaynakTemizleme() {

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
