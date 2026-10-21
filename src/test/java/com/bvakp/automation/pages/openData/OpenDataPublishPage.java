package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import org.testng.Assert;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Açık Veri Portalı "Veri Seti Yönetimi" sekmesi: veri seti listesi, satır işlemleri
 * (KVKK onay/red, yayınla, arşivle, sil, güncelle, düzenle) ve onay popup'ları.
 *
 * <p>Statü, portal aktarımı ve versiyon gibi asenkron değişiklikler, liste belirli
 * aralıklarla yenilenerek beklenir (bkz. {@link #listedeBekle}).
 */
public class OpenDataPublishPage extends BasePage {

    private static final int SATIR_TIMEOUT_MS = 10_000;
    private static final int ARAMA_SONUCU_TIMEOUT_MS = 10_000;

    private static final int STATU_TIMEOUT_MS = 120_000;
    private static final int STATU_YENILEME_ARALIGI_MS = 3_000;
    private static final int PORTAL_AKTARIM_TIMEOUT_MS = 180_000;
    private static final int PORTAL_YENILEME_ARALIGI_MS = 5_000;
    private static final int VERSIYON_TIMEOUT_MS = 120_000;
    private static final int VERSIYON_YENILEME_ARALIGI_MS = 3_000;
    private static final int SILINME_TIMEOUT_MS = 60_000;
    private static final int SILINME_YENILEME_ARALIGI_MS = 3_000;

    private static final String KAYIT_BULUNAMADI = "Kayıt bulunamadı";

    private final Locator publishTab;
    private final Locator aramaAlani;
    private final Locator kayitBulunamadiMesaji;

    /** Satır işlemlerinden sonra açılan onay popup'ı. */
    private final Locator popup;
    private final Locator popupAciklamaInput;

    public OpenDataPublishPage(Page page) {

        super(page);

        this.publishTab = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Veri Seti Yönetimi"));
        this.aramaAlani = page.getByPlaceholder("Veri seti ara");
        this.kayitBulunamadiMesaji = page.getByText(KAYIT_BULUNAMADI);

        this.popup = page.getByRole(AriaRole.DIALOG);
        this.popupAciklamaInput = popup.locator("textarea[name='text']");
    }

    // =====================================================================
    // Liste ve arama
    // =====================================================================

    public void clickPublishTab() {
        publishTab.click();
    }

    /** Veri seti adının tam eşleştiği satır. */
    public Locator getDataSetRow(String dataSetName) {
        return page.locator("[role='row']").filter(new Locator.FilterOptions().setHas(
                page.locator("[role='gridcell'][data-field='name']")
                        .getByText(dataSetName, new Locator.GetByTextOptions().setExact(true))));
    }

    /**
     * Arama alanına veri seti adını yazar ve sonucun (eşleşen satır veya
     * "Kayıt bulunamadı") gelmesini bekler.
     */
    public void searchDataSet(String dataSetName) {

        aramaAlani.fill(dataSetName);

        try {
            getDataSetRow(dataSetName).or(kayitBulunamadiMesaji).first().waitFor(
                    new Locator.WaitForOptions().setTimeout(ARAMA_SONUCU_TIMEOUT_MS));
        } catch (PlaywrightException e) {
            // Sonuç gelmediyse çağıran metot kendi kontrolünde anlamlı hatayı verir.
        }
    }

    /** Arama alanı veri seti adını içeriyor ve tabloda tam eşleşen satır varsa true. */
    public boolean isDataSetNameMatching(String dataSetName) {

        String aramaDegeri = aramaAlani.inputValue().trim();

        if (!dataSetName.equals(aramaDegeri)) {
            ReportManager.warn("Arama alanı eşleşmiyor. Beklenen: " + dataSetName + " | Gerçek: " + aramaDegeri);
            return false;
        }

        return gorunurMu(getDataSetRow(dataSetName), SATIR_TIMEOUT_MS);
    }

    // =====================================================================
    // Satır işlemleri
    // =====================================================================

    public void clickGoruntule(String dataSetName) {
        satirButonunaTikla(dataSetName, "Görüntüle");
    }

    public void clickKvkkOnayla(String dataSetName) {
        satirButonunaTikla(dataSetName, "KVKK Onayla");
    }

    public void clickKvkkReddet(String dataSetName) {
        satirButonunaTikla(dataSetName, "KVKK Reddet");
    }

    public void clickYayinla(String dataSetName) {
        satirButonunaTikla(dataSetName, "Yayınla");
    }

    public void clickYayinReddet(String dataSetName) {
        satirButonunaTikla(dataSetName, "Yayını Reddet");
    }

    public void clickArsivle(String dataSetName) {
        satirButonunaTikla(dataSetName, "Arşivle");
    }

    public void clickSil(String dataSetName) {
        satirButonunaTikla(dataSetName, "Sil");
    }

    public void clickGuncelle(String dataSetName) {
        satirButonunaTikla(dataSetName, "Güncelle");
    }

    public void clickDuzenle(String dataSetName) {
        satirButonunaTikla(dataSetName, "Düzenle");
    }

    /** Veri setini arar, satırın görünmesini bekler ve satırdaki butona tıklar. */
    private void satirButonunaTikla(String dataSetName, String butonAdi) {

        searchDataSet(dataSetName);

        Locator satir = getDataSetRow(dataSetName);
        if (!gorunurMu(satir, SATIR_TIMEOUT_MS)) {
            throw new IllegalStateException("Veri seti listede bulunamadı: " + dataSetName
                    + " ('" + butonAdi + "' işlemi yapılamadı)");
        }

        satir.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(butonAdi)).click();
    }

    // =====================================================================
    // Onay popup'ları
    // =====================================================================

    public void enterKvkkAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }

    public void enterKvkkRedAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }

    public void enterYayinAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }

    public void enterYayinRedAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }

    public void enterArsivAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }

    public void confirmKvkkOnay() {
        popupButonunaTikla("Onayla");
    }

    public void confirmKvkkRed() {
        popupButonunaTikla("Reddet");
    }

    public void confirmYayinla() {
        popupButonunaTikla("Yayınla");
    }

    public void confirmYayinRed() {
        popupButonunaTikla("Reddet");
    }

    public void confirmArsivle() {
        popupButonunaTikla("Arşivle");
    }

    public void confirmSil() {
        popupButonunaTikla("Sil");
    }

    public void confirmGuncelle() {
        popupButonunaTikla("Güncelle");
    }

    public void confirmDegisiklikOnayaGonder() {
        popupButonunaTikla("Onaya Gönder");
    }

    public void cancelKvkkOnay() {
        popupButonunaTikla("İptal");
    }

    public void cancelKvkkRed() {
        popupButonunaTikla("İptal");
    }

    public void cancelArsivle() {
        popupButonunaTikla("İptal");
    }

    public void cancelSil() {
        popupButonunaTikla("İptal");
    }

    /** Popup içindeki butona tıklar; arka plandaki aynı adlı satır butonlarıyla karışmaz. */
    private void popupButonunaTikla(String butonAdi) {
        popup.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName(butonAdi).setExact(true)).click();
    }

    // =====================================================================
    // Düzenleme ekranı
    // =====================================================================

    public void selectDropdown(String dropdownName, String option) {
        page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName(dropdownName)).click();
        page.locator("li[role='option']").filter(new Locator.FilterOptions().setHasText(option)).click();
    }

    public void enterDuzenlemeAciklama(String aciklama) {
        page.locator("textarea[name='description']").fill(aciklama);
    }

    public void clickDuzenlemeKaydet() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Kaydet")).click();
    }

    /** Düzenleme ekranındaki "Onaya Gönder" butonu (onay popup'ını açar). */
    public void clickDegisiklikOnayaGonder() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Onaya Gönder")).first().click();
    }

    public void assertDuzenlemeAlanlari(String sorumluBirim, String kategori, String aciklama) {

        Assert.assertEquals(
                page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Sorumlu Birim")).innerText(),
                sorumluBirim, "Sorumlu Birim alanı beklenen değerle eşleşmiyor.");
        Assert.assertEquals(
                page.getByRole(AriaRole.COMBOBOX, new Page.GetByRoleOptions().setName("Kategori")).innerText(),
                kategori, "Kategori alanı beklenen değerle eşleşmiyor.");
        Assert.assertEquals(
                page.locator("textarea[name='description']").inputValue(),
                aciklama, "Açıklama alanı beklenen değerle eşleşmiyor.");
    }

    public void clickGeriDon() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Geri Dön")).click();
    }

    // =====================================================================
    // Ekran doğrulamaları
    // =====================================================================

    /** Veri seti detay ekranının ("Genel Bilgi" sekmesi ve veri seti adı) açılmasını bekler. */
    public void waitForDetailScreen(String dataSetName) {

        Locator genelBilgi = page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Genel Bilgi"));
        Locator veriSetiAdi = page.getByText(dataSetName, new Page.GetByTextOptions().setExact(true));

        if (!gorunurMu(genelBilgi) || !gorunurMu(veriSetiAdi)) {
            throw new AssertionError("Veri seti detay ekranı açılmadı: " + dataSetName);
        }
    }

    public void waitForVeriSetiYonetimi() {
        Locator baslik = page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Veri Seti Yönetimi"));
        if (!gorunurMu(baslik)) {
            throw new AssertionError("Veri Seti Yönetimi ekranına dönülemedi.");
        }
    }

    // =====================================================================
    // Asenkron değişiklikleri bekleme (liste yenilenerek)
    // =====================================================================

    /** Veri setinin statü sütununun beklenen değere ulaşmasını bekler. */
    public void waitForStatus(String dataSetName, String expectedStatus) {
        listedeBekle(dataSetName, "state", Locator::innerText,
                expectedStatus::equals, "Statü: " + expectedStatus,
                STATU_TIMEOUT_MS, STATU_YENILEME_ARALIGI_MS);
    }

    /** Veri setinin portal aktarım sütununun beklenen değere ulaşmasını bekler. */
    public void waitForPortalStatus(String dataSetName, String expectedPortalStatus) {
        listedeBekle(dataSetName, "ckanSyncStatus", Locator::innerText,
                expectedPortalStatus::equals, "Portal aktarımı: " + expectedPortalStatus,
                PORTAL_AKTARIM_TIMEOUT_MS, PORTAL_YENILEME_ARALIGI_MS);
    }

    /** Veri setinin versiyon numarasının beklenen değere ulaşmasını bekler. */
    public void waitForVersion(String dataSetName, int expectedVersion) {
        listedeBekle(dataSetName, "currentVersionNo", hucre -> hucre.getAttribute("title"),
                versiyon -> versiyon != null
                        && String.valueOf(expectedVersion).equals(versiyon.replace("v", "").trim()),
                "Versiyon: v" + expectedVersion,
                VERSIYON_TIMEOUT_MS, VERSIYON_YENILEME_ARALIGI_MS);
    }

    public String getCurrentVersion(String dataSetName) {

        searchDataSet(dataSetName);

        Locator versiyonHucresi = getDataSetRow(dataSetName).locator("[role='gridcell'][data-field='currentVersionNo']");
        String versiyon = versiyonHucresi.getAttribute("title");

        ReportManager.info("Veri seti versiyonu okundu: " + versiyon);
        return versiyon;
    }

    /** Silinen veri setinin listeden kalkmasını ("Kayıt bulunamadı") bekler. */
    public void waitForDataSetRemoval(String dataSetName) {

        ReportManager.info("Veri setinin listeden kalkması bekleniyor: " + dataSetName);

        long bitis = System.currentTimeMillis() + SILINME_TIMEOUT_MS;

        while (System.currentTimeMillis() < bitis) {
            listeyiYenileVeAra(dataSetName);

            if (gorunurMu(kayitBulunamadiMesaji, 3_000)) {
                ReportManager.info("Veri seti listeden kalktı: " + dataSetName);
                return;
            }

            page.waitForTimeout(SILINME_YENILEME_ARALIGI_MS);
        }

        throw new AssertionError("Silinen veri seti " + SILINME_TIMEOUT_MS / 1000
                + " sn içinde listeden kalkmadı: " + dataSetName);
    }

    /**
     * Listeyi belirli aralıklarla yenileyip veri setinin bir sütununu okur; değer koşulu
     * sağlayınca döner, süre dolarsa son görülen değerle hata verir.
     *
     * <p>Aralık bilinçlidir: bekleme olmadan yenilemek sunucuyu gereksiz yere yorar ve
     * paralel koşuda diğer testleri yavaşlatır.
     */
    private void listedeBekle(
            String dataSetName,
            String sutun,
            Function<Locator, String> degerOku,
            Predicate<String> kosul,
            String beklenenAciklama,
            int timeoutMs,
            int yenilemeAraligiMs) {

        ReportManager.info("Bekleniyor | " + dataSetName + " | " + beklenenAciklama);

        long bitis = System.currentTimeMillis() + timeoutMs;
        String sonDeger = "veri seti listede bulunamadı";

        while (System.currentTimeMillis() < bitis) {

            listeyiYenileVeAra(dataSetName);

            Locator hucre = getDataSetRow(dataSetName).locator("[role='gridcell'][data-field='" + sutun + "']");

            if (hucre.count() > 0) {
                String deger = degerOku.apply(hucre.first());
                sonDeger = deger == null ? "boş" : deger.trim();

                if (kosul.test(sonDeger)) {
                    ReportManager.info("Doğrulandı | " + dataSetName + " | " + beklenenAciklama);
                    return;
                }
            }

            page.waitForTimeout(yenilemeAraligiMs);
        }

        throw new AssertionError("Beklenen değere " + timeoutMs / 1000 + " sn içinde ulaşılamadı | "
                + dataSetName + " | Beklenen: " + beklenenAciklama + " | Son görülen: " + sonDeger);
    }

    private void listeyiYenileVeAra(String dataSetName) {
        page.reload();
        clickPublishTab();
        searchDataSet(dataSetName);
    }
}