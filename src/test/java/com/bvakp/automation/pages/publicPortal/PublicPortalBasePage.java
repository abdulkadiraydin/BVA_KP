package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Kamuya açık portal (CKAN) sayfalarının ortak temeli: portal adresi ve sayfalar arasında aynı
 * olan veri talebi listesi (Hesabım › Taleplerim ve Yönetim › Veri Talepleri).
 */
public abstract class PublicPortalBasePage extends BasePage {

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

  // =====================================================================
  // Veri talebi listesi (Taleplerim ve Yönetim'de aynı yapı)
  // =====================================================================

  /** Başlığı birebir eşleşen talep satırı. */
  protected Locator talepSatiri(String baslik) {
    return page.locator("ul.bvakp-my-requests-list > li")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator("h2")
                        .getByText(baslik, new Locator.GetByTextOptions().setExact(true))));
  }

  /** Talep listede görünüyorsa true döner. */
  public boolean talepListedeMi(String baslik) {
    return gorunurMu(talepSatiri(baslik));
  }

  /** Talep listede yoksa true döner. */
  public boolean talepListedeYokMu(String baslik) {
    return gorunmuyorMu(talepSatiri(baslik));
  }

  /** Talep satırındaki durum etiketi (ör. "Alındı"). */
  public String talepDurumuOkuma(String baslik) {
    return talepSatiri(baslik).locator(".bvakp-history-kind").innerText().trim();
  }

  /** Talep satırındaki alt bilgi: tarih · (kategori ·) destek sayısı · görünürlük. */
  public String talepBilgisiOkuma(String baslik) {
    return talepSatiri(baslik)
        .locator(".bvakp-my-request-meta")
        .innerText()
        .replaceAll("\\s+", " ")
        .trim();
  }
}
