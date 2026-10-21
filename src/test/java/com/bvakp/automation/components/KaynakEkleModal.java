package com.bvakp.automation.components;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

/**
 * Açık Veri Portalı "Kaynak Ekle" modalı (Kaynak Tablo ve Hazır Sorgu sekmeleri).
 *
 * <p>Bütün locator'lar modal (dialog) içine sınırlıdır; arka plandaki sayfada aynı
 * isimli buton veya metinler (örneğin "Kaynak Ekle", tablo adları) olsa da karışmaz.
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
        this.veriOnizleme = modal.getByText("Veri Önizleme", new Locator.GetByTextOptions().setExact(true));
        this.kaynakEkleButonu = modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Kaynak Ekle").setExact(true));

        this.kaynakAdiZorunluMesaji = modal.getByText("Kaynak adı zorunludur.",
                new Locator.GetByTextOptions().setExact(true));
        this.tabloZorunluMesaji = modal.getByText("Bir tablo seçin.",
                new Locator.GetByTextOptions().setExact(true));
    }

    // =====================================================================
    // Form
    // =====================================================================

    public boolean kaynakEkleFormuGoruntulendiMi() {
        return gorunurMu(adAlani, FORM_TIMEOUT_MS)
                && tabloAlani.isVisible()
                && aciklamaAlani.isVisible();
    }

    public boolean kaynakEkleFormuKapandiMi() {
        return gorunmuyorMu(adAlani, SUNUCU_CEVABI_TIMEOUT_MS);
    }

    public void kaynakAdiGirme(String kaynakAdi) {
        adAlani.fill(kaynakAdi);
    }

    public String kaynakAdiAlma() {
        return adAlani.inputValue().trim();
    }

    public void kaynakAciklamasiGirme(String aciklama) {
        aciklamaAlani.fill(aciklama);
    }

    public String kaynakAciklamasiAlma() {
        return aciklamaAlani.inputValue().trim();
    }

    /**
     * Tablo listesini açar ve verilen tabloyu seçer.
     *
     * <p>Liste sunucudan yüklenir. Sunucu "Tablolar okunamadı" derse (örneğin oturum
     * geçersizse) zaman aşımını beklemeden, sebebi açıkça yazan bir hatayla durur.
     */
    public void kaynakTablosuSecme(String tabloAdi) {

        tabloAlani.click();

        Locator tabloSecenegi = page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(tabloAdi).setExact(true));
        Locator tablolarOkunamadi = modal.getByText(TABLOLAR_OKUNAMADI_MESAJI);

        try {
            tabloSecenegi.or(tablolarOkunamadi).first().waitFor(
                    new Locator.WaitForOptions().setTimeout(SUNUCU_CEVABI_TIMEOUT_MS));
        } catch (PlaywrightException e) {
            throw new IllegalStateException("Tablo listesinde '" + tabloAdi + "' "
                    + SUNUCU_CEVABI_TIMEOUT_MS / 1000 + " sn içinde görünmedi.", e);
        }

        if (tablolarOkunamadi.isVisible()) {
            throw new IllegalStateException("Tablo listesi yüklenemedi: '" + TABLOLAR_OKUNAMADI_MESAJI
                    + "'. Oturum geçersiz olabilir (SECURITY.UNAUTHORIZED).");
        }

        tabloSecenegi.click();
    }

    public String seciliTabloyuAlma() {
        return tabloAlani.inputValue().trim();
    }

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

    public boolean kaynakAdiZorunluMesajiGoruntulendiMi() {
        return gorunurMu(kaynakAdiZorunluMesaji, FORM_TIMEOUT_MS);
    }

    /** Önce beklenen diğer mesajın göründüğü doğrulandıktan sonra kullanılmalıdır. */
    public boolean kaynakAdiZorunluMesajiGoruntulenmediMi() {
        return gorunmuyorMu(kaynakAdiZorunluMesaji, NEGATIF_KONTROL_TIMEOUT_MS);
    }

    public boolean tabloZorunluMesajiGoruntulendiMi() {
        return gorunurMu(tabloZorunluMesaji, FORM_TIMEOUT_MS);
    }

    /** Önce beklenen diğer mesajın göründüğü doğrulandıktan sonra kullanılmalıdır. */
    public boolean tabloZorunluMesajiGoruntulenmediMi() {
        return gorunmuyorMu(tabloZorunluMesaji, NEGATIF_KONTROL_TIMEOUT_MS);
    }

    // =====================================================================
    // Hazır Sorgu
    // =====================================================================

    public void hazirSorguyaGecme() {
        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Hazır Sorgu").setExact(true)).click();
    }

    public void hazirSorguGirme(String sorgu) {
        modal.getByPlaceholder("""
                SELECT a.*, b.ad
                FROM tablo_a a
                JOIN tablo_b b ON a.id = b.a_id""").fill(sorgu);
    }

    public void hazirSorguDogrulama() {
        modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Doğrula").setExact(true)).click();
    }

    /** Sorgu doğrulaması sonrası beklenen beş kolonun hepsi görünürse true. */
    public boolean hazirSorguKolonlariGoruntulendiMi() {

        String[] beklenenKolonlar = {
                "yil_yillik_hat_km",
                "bolge_kodu_yillik_hat_km",
                "bolge_adi_yillik_hat_km",
                "hat_kodu_yillik_hat_kapasitesi",
                "hat_adi_yillik_hat_kapasitesi"
        };

        for (String kolon : beklenenKolonlar) {
            Locator kolonLocator = modal.getByText(kolon, new Locator.GetByTextOptions().setExact(true));
            if (!gorunurMu(kolonLocator, SUNUCU_CEVABI_TIMEOUT_MS)) {
                return false;
            }
        }
        return true;
    }

    public boolean kullaniciKaynakliHataGoruntulendiMi() {
        return gorunurMu(modal.getByText("Kullanıcı Kaynaklı Hata",
                new Locator.GetByTextOptions().setExact(true)), SUNUCU_CEVABI_TIMEOUT_MS);
    }

    public boolean sadeceSelectSorgusuHatasiGoruntulendiMi() {
        return gorunurMu(modal.getByText("Sorguyu yalnızca SELECT ile yazın."), FORM_TIMEOUT_MS);
    }

    public boolean readOnlySorguHataKoduGoruntulendiMi() {
        return gorunurMu(modal.getByText("OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY"), FORM_TIMEOUT_MS);
    }
}