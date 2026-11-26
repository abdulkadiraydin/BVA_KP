package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Kamuya açık portal (CKAN) sayfalarının ortak temeli: portal adresi, başlıklı bölümler ve sayfalar
 * arasında aynı olan veri talebi listesi (Hesabım › Taleplerim, Veri Talepleri).
 */
public abstract class PublicPortalBasePage extends BasePage {

  /** ALTCHA widget'ının sayfada görünmesi için üst sınır. */
  private static final double ALTCHA_GORUNME_TIMEOUT_MS = 15_000;

  /** auto="onfocus" ile doğrulamanın kendiliğinden başlaması için tanınan süre. */
  private static final int ALTCHA_BASLAMA_TIMEOUT_MS = 5_000;

  /** ALTCHA doğrulaması tarayıcıda hesaplama yapar; yoğun makinede 10 sn'yi aşabilir. */
  private static final double ALTCHA_TIMEOUT_MS = 120_000;

  protected PublicPortalBasePage(Page page) {
    super(page);
  }

  /** public.data.url; sondaki "/" işaretleri atılır (adres config'te "/" ile bitebilir). */
  protected static String tabanAdres() {
    String adres = ConfigManager.get("public.data.url");
    while (adres.endsWith("/")) {
      adres = adres.substring(0, adres.length() - 1);
    }
    return adres;
  }

  /** Portalda verilen yolu açar (ör. "/", "/dataset/abc"). */
  protected void adresAcma(String yol) {
    page.navigate(tabanAdres() + yol);
  }

  /**
   * Sayfanın script'leri yüklenene kadar bekler. Menü, onay penceresi ve talep formu JS ile açılır;
   * JS hazır olmadan tıklanırsa menü açılmaz ya da form onay sormadan gönderilir.
   */
  protected void sayfaHazirOlanaKadarBekleme() {
    page.waitForLoadState(LoadState.LOAD);
  }

  /**
   * Başlığı verilen sayfa bölümü (ör. "Ek Bilgiler", "Veri Sözlüğü"); başlık birebir eşleşir.
   * Portal sayfaları başlıklı bölümlerden oluşur; değerler önce bölümle daraltılarak aranır.
   */
  protected Locator bolum(String baslik) {
    return page.locator("section")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(baslik).setExact(true))));
  }

  // =====================================================================
  // Veri talebi listesi (Taleplerim ve Veri Talepleri'nde aynı yapı)
  // =====================================================================

  /** Başlığı (seviye 2) birebir eşleşen talep satırı. */
  protected Locator talepSatiri(String baslik) {
    return page.getByRole(AriaRole.LISTITEM)
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions().setName(baslik).setExact(true).setLevel(2))));
  }

  /** Talep listede görünüyorsa true döner. */
  public boolean talepListedeMi(String baslik) {
    return gorunurMu(talepSatiri(baslik));
  }

  /** Talep listede yoksa true döner. */
  public boolean talepListedeYokMu(String baslik) {
    return gorunmuyorMu(talepSatiri(baslik));
  }

  /** Talep satırındaki durum etiketi (ör. "Onay bekliyor", "Açık"). */
  public String talepDurumuOkuma(String baslik) {
    return talepSatiri(baslik).locator(".bvakp-history-kind").innerText().trim();
  }

  /** Talep satırındaki alt bilgi: sahip · (kategori ·) zaman · yorum sayısı (· Gizli). */
  public String talepBilgisiOkuma(String baslik) {
    return talepSatiri(baslik)
        .locator(".bvakp-my-request-meta")
        .innerText()
        .replaceAll("\\s+", " ")
        .trim();
  }

  /**
   * Robot doğrulamasını tamamlar. Doğrulama her zaman istenmez (ör. giriş sayfasında yalnız bazı
   * durumlarda çıkar); widget sayfada yoksa adım atlanır.
   *
   * <p>Widget auto="onfocus" çalışır: forma yazı girilince doğrulama kendiliğinden başlar ve
   * sürerken kutu tıklanamaz. Bu yüzden kutuya yalnız doğrulama kısa süre içinde başlamazsa ya da
   * hata/süre dolumu durumunda kalırsa tıklanır.
   */
  protected void robotDogrulamasiniTamamlama(Locator kapsam) {
    // ALTCHA kutusu giriş alanlarından biraz sonra yüklenir; count() beklemediği için kutu henüz
    // yokken 0 döner ve doğrulama atlanırdı. Kutu birkaç saniye beklenir, gelmezse sayfada ALTCHA
    // yoktur.
    try {
      kapsam
          .locator("altcha-widget")
          .first()
          .waitFor(
              new Locator.WaitForOptions()
                  .setState(WaitForSelectorState.ATTACHED)
                  .setTimeout(10_000));
    } catch (TimeoutError e) {
      return;
    }

    kapsam
        .locator("altcha-widget .altcha")
        .waitFor(
            new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(ALTCHA_GORUNME_TIMEOUT_MS));

    Locator baslamis =
        kapsam.locator(
            "altcha-widget .altcha[data-state='verifying'],"
                + " altcha-widget .altcha[data-state='verified']");
    if (!gorunurMu(baslamis, ALTCHA_BASLAMA_TIMEOUT_MS)) {
      // Kutunun ortasındaki tik görseli tıklamayı engeller; kutuya bağlı etikete tıklanır.
      kapsam.locator("altcha-widget .altcha-checkbox-wrap label").click();
    }

    kapsam
        .locator("altcha-widget .altcha[data-state='verified']")
        .waitFor(new Locator.WaitForOptions().setTimeout(ALTCHA_TIMEOUT_MS));
  }
}
