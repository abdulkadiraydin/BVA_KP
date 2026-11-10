package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Kamuya açık portal (CKAN) sayfalarının ortak temeli: portal adresi ve sayfalar arasında aynı olan
 * veri talebi listesi (Hesabım › Taleplerim ve Yönetim › Veri Talepleri).
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
  // Veri talebi listesi (Taleplerim ve Yönetim'de aynı yapı)
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
