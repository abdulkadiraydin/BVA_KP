package com.bvakp.automation.components;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;

/**
 * Açık Veri Portalı "Kaynak Ekle" modalı (Kaynak Tablo ve Hazır Sorgu sekmeleri).
 *
 * <p>Bütün locator'lar modal (dialog) içine sınırlıdır; arka plandaki sayfada aynı isimli buton
 * veya metinler (örneğin "Kaynak Ekle", tablo adları) olsa da karışmaz.
 */
public class KaynakEkleModal extends BasePage {

  private static final int FORM_TIMEOUT_MS = 10_000;
  private static final int SUNUCU_CEVABI_TIMEOUT_MS = 15_000;

  /** Negatif kontrollerde, beklenen mesaj görüldükten sonra kısa bir bekleme yeterlidir. */
  private static final int NEGATIF_KONTROL_TIMEOUT_MS = 2_000;

  private static final String TABLOLAR_OKUNAMADI_MESAJI = "Tablolar okunamadı";

  private final Locator modal;
  private final Locator adAlani;
  private final Locator tabloAlani;
  private final Locator aciklamaAlani;
  private final Locator veriOnizleme;
  private final Locator kaynakEkleButonu;
  private final Locator kaynakAdiZorunluMesaji;
  private final Locator tabloZorunluMesaji;

  public KaynakEkleModal(Page page) {

    super(page);

    this.modal = page.getByRole(AriaRole.DIALOG);

    this.adAlani = modal.getByLabel("Ad *");
    this.tabloAlani = modal.getByLabel("Tablo *");
    this.aciklamaAlani = modal.getByLabel("Açıklama");
    this.veriOnizleme =
        modal.getByText("Veri Önizleme", new Locator.GetByTextOptions().setExact(true));
    this.kaynakEkleButonu =
        modal.getByRole(
            AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Kaynak Ekle").setExact(true));

    this.kaynakAdiZorunluMesaji =
        modal.getByText("Kaynak adı zorunludur.", new Locator.GetByTextOptions().setExact(true));
    this.tabloZorunluMesaji =
        modal.getByText("Bir tablo seçin.", new Locator.GetByTextOptions().setExact(true));
  }

  // =====================================================================
  // Form
  // =====================================================================

  /** Ad, tablo ve açıklama alanları görünüyorsa true döner. */
  public boolean kaynakEkleFormuGoruntulendiMi() {
    return gorunurMu(adAlani, FORM_TIMEOUT_MS)
        && tabloAlani.isVisible()
        && aciklamaAlani.isVisible();
  }

  /** Form kapandıysa (ad alanı kaybolduysa) true döner. */
  public boolean kaynakEkleFormuKapandiMi() {
    return gorunmuyorMu(adAlani, SUNUCU_CEVABI_TIMEOUT_MS);
  }

  /** Kaynak adı alanını doldurur. */
  public void kaynakAdiGirme(String kaynakAdi) {
    adAlani.fill(kaynakAdi);
  }

  /** Kaynak adı alanındaki değeri döndürür. */
  public String kaynakAdiAlma() {
    return adAlani.inputValue().trim();
  }

  /** Açıklama alanını doldurur. */
  public void kaynakAciklamasiGirme(String aciklama) {
    aciklamaAlani.fill(aciklama);
  }

  /** Açıklama alanındaki değeri döndürür. */
  public String kaynakAciklamasiAlma() {
    return aciklamaAlani.inputValue().trim();
  }

  /**
   * Tablo listesini açar ve verilen tabloyu seçer.
   *
   * <p>Liste sunucudan yüklenir. Sunucu "Tablolar okunamadı" derse (örneğin oturum geçersizse)
   * zaman aşımını beklemeden, sebebi açıkça yazan bir hatayla durur.
   */
  public void kaynakTablosuSecme(String tabloAdi) {

    tabloAlani.click();

    Locator tabloSecenegi =
        page.getByRole(
            AriaRole.OPTION, new Page.GetByRoleOptions().setName(tabloAdi).setExact(true));
    Locator tablolarOkunamadi = modal.getByText(TABLOLAR_OKUNAMADI_MESAJI);

    try {
      tabloSecenegi
          .or(tablolarOkunamadi)
          .first()
          .waitFor(new Locator.WaitForOptions().setTimeout(SUNUCU_CEVABI_TIMEOUT_MS));
    } catch (PlaywrightException e) {
      throw new IllegalStateException(
          "Tablo listesinde '"
              + tabloAdi
              + "' "
              + SUNUCU_CEVABI_TIMEOUT_MS / 1000
              + " sn içinde görünmedi.",
          e);
    }

    if (tablolarOkunamadi.isVisible()) {
      throw new IllegalStateException(
          "Tablo listesi yüklenemedi: '"
              + TABLOLAR_OKUNAMADI_MESAJI
              + "'. Oturum geçersiz olabilir (SECURITY.UNAUTHORIZED).");
    }

    tabloSecenegi.click();
  }

  /** Seçili kaynak tablosunun adını döndürür. */
  public String seciliTabloyuAlma() {
    return tabloAlani.inputValue().trim();
  }

  /** Tablo seçildikten sonra Veri Önizleme görünüyorsa true döner. */
  public boolean veriOnizlemeGoruntulendiMi() {
    return gorunurMu(veriOnizleme, SUNUCU_CEVABI_TIMEOUT_MS);
  }

  /** Modaldaki "Kaynak Ekle" (kaydet) butonuna tıklar. */
  public void kaynakEkleme() {
    kaynakEkleButonu.click();
  }

  // =====================================================================
  // Zorunlu alan mesajları
  // =====================================================================

  /** Kaynak adı zorunlu alan mesajı görünüyorsa true döner. */
  public boolean kaynakAdiZorunluMesajiVarMi() {
    return gorunurMu(kaynakAdiZorunluMesaji, FORM_TIMEOUT_MS);
  }

  /** Önce beklenen diğer mesajın göründüğü doğrulandıktan sonra kullanılmalıdır. */
  public boolean kaynakAdiZorunluMesajiYokMu() {
    return gorunmuyorMu(kaynakAdiZorunluMesaji, NEGATIF_KONTROL_TIMEOUT_MS);
  }

  /** Tablo zorunlu alan mesajı görünüyorsa true döner. */
  public boolean tabloZorunluMesajiVarMi() {
    return gorunurMu(tabloZorunluMesaji, FORM_TIMEOUT_MS);
  }

  /** Önce beklenen diğer mesajın göründüğü doğrulandıktan sonra kullanılmalıdır. */
  public boolean tabloZorunluMesajiYokMu() {
    return gorunmuyorMu(tabloZorunluMesaji, NEGATIF_KONTROL_TIMEOUT_MS);
  }

  // =====================================================================
  // Hazır Sorgu
  // =====================================================================

  /** Hazır Sorgu sekmesine geçer. */
  public void hazirSorguyaGecme() {
    modal
        .getByRole(
            AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Hazır Sorgu").setExact(true))
        .click();
  }

  /** Hazır sorgu alanına SQL yazar. */
  public void hazirSorguGirme(String sorgu) {
    modal
        .getByPlaceholder(
            """
                        SELECT a.*, b.ad
                        FROM tablo_a a
                        JOIN tablo_b b ON a.id = b.a_id""")
        .fill(sorgu);
  }

  /** Doğrula butonuna tıklar. */
  public void hazirSorguDogrulama() {
    modal
        .getByRole(
            AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Doğrula").setExact(true))
        .click();
  }

  /** Sorgu doğrulamasından sonra verilen kolonların hepsi görünürse true döner. */
  public boolean hazirSorguKolonlariVarMi(List<String> beklenenKolonlar) {
    for (String kolon : beklenenKolonlar) {
      Locator kolonLocator = modal.getByText(kolon, new Locator.GetByTextOptions().setExact(true));
      if (!gorunurMu(kolonLocator, SUNUCU_CEVABI_TIMEOUT_MS)) {
        return false;
      }
    }
    return true;
  }

  /** Sorgu doğrulamasında Kullanıcı Kaynaklı Hata görünüyorsa true döner. */
  public boolean kullaniciHatasiVarMi() {
    return gorunurMu(
        modal.getByText("Kullanıcı Kaynaklı Hata", new Locator.GetByTextOptions().setExact(true)),
        SUNUCU_CEVABI_TIMEOUT_MS);
  }

  /** "Yalnızca SELECT" uyarısı görünüyorsa true döner. */
  public boolean sadeceSelectHatasiVarMi() {
    return gorunurMu(modal.getByText("Sorguyu yalnızca SELECT ile yazın."), FORM_TIMEOUT_MS);
  }

  /** Sunucunun QUERY_NOT_READ_ONLY hata kodu görünüyorsa true döner. */
  public boolean readOnlyHataKoduVarMi() {
    return gorunurMu(modal.getByText("OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY"), FORM_TIMEOUT_MS);
  }
}
