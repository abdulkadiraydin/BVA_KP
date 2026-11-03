package com.bvakp.automation.components;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Açık Veri Portalı "Kaynağı Düzenle" modalı.
 *
 * <p>Locator'lar modal içine sınırlıdır; Kaynak Ekle modalıyla aynı alan etiketlerini ("Ad *",
 * "Açıklama") kullandığı için bu ayrım önemlidir.
 */
public class KaynakDuzenleModal extends BasePage {

  private static final int MODAL_TIMEOUT_MS = 10_000;

  private final Locator modalBasligi;
  private final Locator adAlani;
  private final Locator aciklamaAlani;
  private final Locator kaydetButonu;

  public KaynakDuzenleModal(Page page) {

    super(page);

    Locator modal = page.getByRole(AriaRole.DIALOG);

    this.modalBasligi =
        modal.getByRole(
            AriaRole.HEADING,
            new Locator.GetByRoleOptions().setName("Kaynağı Düzenle").setExact(true));
    this.adAlani = modal.getByLabel("Ad *");
    this.aciklamaAlani = modal.getByLabel("Açıklama");
    this.kaydetButonu =
        modal.getByRole(
            AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Kaydet").setExact(true));
  }

  /** Kaynak Düzenle modalı açıksa true döner. */
  public boolean duzenleModaliAcikMi() {
    return gorunurMu(modalBasligi, MODAL_TIMEOUT_MS);
  }

  /** Kaynak adı alanını yeni değerle doldurur. */
  public void adDegistirme(String yeniKaynakAdi) {
    adAlani.fill(yeniKaynakAdi);
  }

  /** Açıklama alanını yeni değerle doldurur. */
  public void aciklamaDegistirme(String yeniAciklama) {
    aciklamaAlani.fill(yeniAciklama);
  }

  /** Düzenleme ekranındaki kaynak adı; mevcut ve güncellenmiş değeri doğrulamak için. */
  public String kaynakAdiAlma() {
    return adAlani.inputValue().trim();
  }

  /** Düzenleme ekranındaki açıklama; mevcut ve güncellenmiş değeri doğrulamak için. */
  public String kaynakAciklamasiAlma() {
    return aciklamaAlani.inputValue().trim();
  }

  /** Kaydet butonuna tıklar. */
  public void kaydetme() {
    kaydetButonu.click();
  }
}
