package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Açık Veri Portalı "Güncelleme Takvimi" sekmesi: dönem seçici, özet kartları, takvim,
 * Güncelleme Planı tablosu, İşlem Geçmişi ve Güncelleme Ayrıntısı çekmecesi.
 *
 * <p>Locator'lar ilgili bölüme sınırlıdır (#refresh-plan, #refresh-history, çekmece).
 * assert... metotları Playwright assertThat ile bekleyerek doğrular; ...VarMi metotları
 * boolean döner ve doğrulama testte yapılır.
 */
public class OpenDataUpdateCalendarPage extends BasePage {

    private static final int SATIR_TIMEOUT_MS = 10_000;
    private static final int GECMIS_KAYIT_TIMEOUT_MS = 60_000;
    private static final int GECMIS_TUR_KONTROL_MS = 3_000;
    private static final int GECMIS_TUR_ARALIGI_MS = 2_000;
    /** Güncelleme sonrası portala aktarım 3 dakikaya kadar sürebilir. */
    private static final int GECMIS_SONUC_TIMEOUT_MS = 180_000;

    public OpenDataUpdateCalendarPage(Page page) {
        super(page);
    }

    // =====================================================================
    // Sekme
    // =====================================================================

    private Locator guncellemeTakvimiSekmesi() {
        return page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Güncelleme Takvimi"));
    }

    /** Güncelleme Takvimi sekmesine geçer ve plan tablosunun görünmesini bekler. */
    public void guncellemeTakvimiSekmesineGecme() {
        try {
            guncellemeTakvimiSekmesi().click(new Locator.ClickOptions().setTimeout(15_000));
        } catch (PlaywrightException e) {
            throw new AssertionError("Güncelleme Takvimi sekmesi bulunamadı. URL: " + page.url()
                    + " | Hata: " + e.getMessage());
        }
        planTablosu().waitFor();
    }

    public void assertSekmeSecili() {
        assertThat(guncellemeTakvimiSekmesi()).hasAttribute("aria-selected", "true");
    }

    // =====================================================================
    // Dönem seçici ve özet kartları
    // =====================================================================

    /** value: 7d, 30d, 90d, 1y */
    private Locator donemButonu(String value) {
        return page.locator("[aria-label='Dönem'] button[value='" + value + "']");
    }

    public void donemSecme(String value) {
        donemButonu(value).click();
    }

    public void assertDonemSecili(String value) {
        assertThat(donemButonu(value)).hasAttribute("aria-pressed", "true");
    }

    public void assertDonemSeciliDegil(String value) {
        assertThat(donemButonu(value)).hasAttribute("aria-pressed", "false");
    }

    private Locator ozetKartiDegeri(String baslik) {
        return page.locator("div.MuiCard-root")
                .filter(new Locator.FilterOptions().setHasText(baslik))
                .locator("p").first();
    }

    /** Kartın değeri sayısal olmalı (veri yüklenene kadar bekler). */
    public void assertOzetKartiSayisal(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).hasText(Pattern.compile("^\\d+$"));
    }

    /** Kartın değeri en az 1 olmalı (veri yüklenene kadar bekler). */
    public void assertOzetKartiEnAz1(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).hasText(Pattern.compile("^[1-9]\\d*$"));
    }

    public void assertOzetKartiDolu(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).not().hasText("");
    }

    // =====================================================================
    // Güncelleme Planı tablosu
    // =====================================================================

    private Locator planBolumu() {
        return page.locator("#refresh-plan");
    }

    private Locator planTablosu() {
        return planBolumu().locator("[role='grid']");
    }

    private Locator planSatiri(String veriSetiAdi) {
        return planBolumu().locator("[role='row']").filter(new Locator.FilterOptions().setHas(
                page.locator("[data-field='name'] p[title='" + veriSetiAdi + "']")));
    }

    public void planAramaYapma(String metin) {
        planBolumu().getByPlaceholder("Veri seti, kategori veya birim ara").fill(metin);
    }

    public boolean planListesindeVarMi(String veriSetiAdi) {
        return gorunurMu(planSatiri(veriSetiAdi), SATIR_TIMEOUT_MS);
    }

    public String planSikligiAlma(String veriSetiAdi) {
        return planSatiri(veriSetiAdi).locator("[data-field='frequency']").innerText().trim();
    }

    /** Hücrede tarih ve sürüm ("v1") birlikte yer alır; sürüm span'ı okunur. */
    public String planSonGuncellemeSurumuAlma(String veriSetiAdi) {
        return planSatiri(veriSetiAdi).locator("[data-field='lastUpdatedAt'] span").innerText().trim();
    }

    // =====================================================================
    // Takvim
    // =====================================================================

    /** Ay dışı günler (dayOutsideMonth) elenir. */
    private Locator takvimGunu(int gun) {
        return page.locator("button.MuiPickersDay-root:not(.MuiPickersDay-dayOutsideMonth)")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + gun + "$")));
    }

    /** aria-label örneği: "14 Ekim 2026: 0 planlı, 2 başarılı, 0 portala aktarılamadı, 0 başarısız" */
    public void assertBugunBasariliGuncellemeVar(int gun) {
        assertThat(takvimGunu(gun)).hasAttribute("aria-label", Pattern.compile("\\b[1-9]\\d* başarılı"));
    }

    public boolean gunDetayindaKayitVarMi(String veriSetiAdi) {
        return gorunurMu(page.locator("li p[title='" + veriSetiAdi + "']"), SATIR_TIMEOUT_MS);
    }

    // =====================================================================
    // İşlem Geçmişi
    // =====================================================================

    private Locator gecmisBolumu() {
        return page.locator("#refresh-history");
    }

    private Locator gecmisSatiri(String veriSetiAdi) {
        return gecmisBolumu().locator("[role='row']").filter(new Locator.FilterOptions().setHas(
                page.locator("[data-field='datasetName'] p[title='" + veriSetiAdi + "']")));
    }

    /** value: ALL, SUCCEEDED, FAILED */
    public void gecmisSonucFiltresiSecme(String value) {
        gecmisBolumu().locator("[aria-label='Sonuç'] button[value='" + value + "']").click();
    }

    /** "Veri seti" autocomplete'inden veri setini seçip geçmiş tablosunu filtreler. */
    public void gecmisVeriSetiFiltreleme(String veriSetiAdi) {
        Locator giris = gecmisBolumu().getByLabel("Veri seti");
        giris.click();
        giris.fill(veriSetiAdi);
        page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(veriSetiAdi)).first().click();
    }

    public boolean gecmisteVarMi(String veriSetiAdi) {
        return gorunurMu(gecmisSatiri(veriSetiAdi), SATIR_TIMEOUT_MS);
    }

    /** Filtre sonrası kaydın geçmiş tablosunda görünmediğini bekleyerek doğrular. */
    public void assertGecmisteKayitYok(String veriSetiAdi) {
        assertThat(gecmisSatiri(veriSetiAdi)).hasCount(0);
    }

    public String gecmisSonucuAlma(String veriSetiAdi) {
        return gecmisHucresi(veriSetiAdi, "status");
    }

    public String gecmisSurumuAlma(String veriSetiAdi) {
        return gecmisHucresi(veriSetiAdi, "versionNo");
    }

    public String gecmisTetikleyenAlma(String veriSetiAdi) {
        return gecmisHucresi(veriSetiAdi, "trigger");
    }

    public String gecmisKayitSayisiAlma(String veriSetiAdi) {
        return gecmisHucresi(veriSetiAdi, "recordCount");
    }

    private String gecmisHucresi(String veriSetiAdi, String sutun) {
        return gecmisSatiri(veriSetiAdi).locator("[data-field='" + sutun + "']").innerText().trim();
    }

    /**
     * Geçmiş kaydı asenkron oluştuğu için sayfayı belirli aralıklarla yenileyip kaydı bekler.
     * Bir tur başarısız olursa (ör. yenileme sonrası sekme gelmediyse) sonraki tur denenir;
     * süre dolunca URL ve son hatayla AssertionError fırlatılır.
     */
    public void gecmisteKayitBekleme(String veriSetiAdi) {

        long bitis = System.currentTimeMillis() + GECMIS_KAYIT_TIMEOUT_MS;
        String sonHata = "yok";

        while (System.currentTimeMillis() < bitis) {
            try {
                page.reload();
                guncellemeTakvimiSekmesineGecme();
                gecmisVeriSetiFiltreleme(veriSetiAdi);

                if (gorunurMu(gecmisSatiri(veriSetiAdi), GECMIS_TUR_KONTROL_MS)) {
                    ReportManager.info("Geçmişte kayıt görüldü: " + veriSetiAdi);
                    return;
                }
            } catch (PlaywrightException | AssertionError e) {
                sonHata = e.getMessage();
            }

            // Yenilemeler arasında bekleme: sunucuyu gereksiz yere yormamak için.
            page.waitForTimeout(GECMIS_TUR_ARALIGI_MS);
        }

        throw new AssertionError("Geçmişte kayıt " + GECMIS_KAYIT_TIMEOUT_MS / 1000 + " sn içinde bulunamadı: "
                + veriSetiAdi + " | URL: " + page.url() + " | Son hata: " + sonHata);
    }

    /**
     * Geçmişteki kaydın sonucu beklenen değere ulaşana kadar sayfayı yenileyerek bekler.
     *
     * <p>Güncelleme sonrası kayıt önce ara durumlardan geçer ("Portala aktarılıyor" gibi);
     * yalnızca kaydın var olmasını beklemek, işlem bitmeden okumaya yol açar.
     */
    public void gecmisteSonucBekleme(String veriSetiAdi, String beklenenSonuc) {

        long bitis = System.currentTimeMillis() + GECMIS_SONUC_TIMEOUT_MS;
        String sonDurum = "kayıt bulunamadı";

        while (System.currentTimeMillis() < bitis) {
            try {
                page.reload();
                guncellemeTakvimiSekmesineGecme();
                gecmisVeriSetiFiltreleme(veriSetiAdi);

                if (gorunurMu(gecmisSatiri(veriSetiAdi), GECMIS_TUR_KONTROL_MS)) {
                    sonDurum = gecmisSonucuAlma(veriSetiAdi);
                    if (beklenenSonuc.equals(sonDurum)) {
                        ReportManager.info("Geçmiş sonucu doğrulandı: " + veriSetiAdi + " | " + sonDurum);
                        return;
                    }
                }
            } catch (PlaywrightException | AssertionError e) {
                sonDurum = "okunamadı: " + e.getMessage();
            }

            // Yenilemeler arasında bekleme: sunucuyu gereksiz yere yormamak için.
            page.waitForTimeout(GECMIS_TUR_ARALIGI_MS);
        }

        throw new AssertionError("Geçmiş sonucu " + GECMIS_SONUC_TIMEOUT_MS / 1000 + " sn içinde '"
                + beklenenSonuc + "' olmadı: " + veriSetiAdi + " | Son görülen: " + sonDurum);
    }

    /** "v2" → 2. Okunamazsa ("—" gibi) net hata verir. */
    public int surumNumarasi(String hamDeger) {
        String temiz = hamDeger == null ? "" : hamDeger.replaceAll("[^0-9]", "");
        if (temiz.isEmpty()) {
            throw new IllegalStateException("Sürüm okunamadı. Değer: [" + hamDeger + "]");
        }
        return Integer.parseInt(temiz);
    }

    // =====================================================================
    // Güncelleme Ayrıntısı çekmecesi
    // =====================================================================

    private Locator detayCekmecesi() {
        return page.locator("div.MuiDrawer-paper[role='dialog']");
    }

    /** Takvimde seçili günün listesinden veri setinin kaydına tıklar ve çekmeceyi bekler. */
    public void gunListesindenKayitAcma(String veriSetiAdi) {
        page.locator("li").filter(new Locator.FilterOptions().setHas(
                page.locator("p[title='" + veriSetiAdi + "']"))).first().click();
        detayCekmecesi().waitFor();
    }

    public String detayVeriSetiAdiAlma() {
        return detayCekmecesi().locator("p[title]").first().getAttribute("title");
    }

    public String detaySonucAlma() {
        return detayCekmecesi().locator(".MuiChip-label").first().innerText().trim();
    }

    /** Etiket metnine göre değer okur. Örn: "Tetikleyen", "Oluşan sürüm", "İşlenen kayıt", "Deneme" */
    public String detayAlanDegeriAlma(String etiket) {
        return detayCekmecesi()
                .getByText(etiket, new Locator.GetByTextOptions().setExact(true))
                .first()
                .locator("xpath=following-sibling::div[1]")
                .innerText().trim();
    }

    public String detayAkisMetniAlma() {
        return detayCekmecesi().locator("section[aria-label='Akış']").innerText();
    }

    public void detaydaVeriSetineGitTiklama() {
        detayCekmecesi().getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Veri setine git")).click();
    }

    public void detayiKapatma() {
        detayCekmecesi().getByLabel("Kapat").click();
    }

    public void assertDetayCekmecesiKapali() {
        assertThat(detayCekmecesi()).hasCount(0);
    }

    // =====================================================================
    // Veri seti detay sayfası ("Veri setine git" sonrası)
    // =====================================================================

    public void assertDetayBaslik(String veriSetiAdi) {
        page.waitForURL(url -> !url.endsWith("/opendata"), new Page.WaitForURLOptions().setTimeout(10_000));
        assertThat(page.locator("h6").first()).hasText(veriSetiAdi);
    }

    public void detaySekmesineGecme(String ad) {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(ad)).click();
    }

    public String versiyonDurumuAlma(String versiyon) {
        return page.locator("tbody tr")
                .filter(new Locator.FilterOptions().setHas(
                        page.locator("td").getByText(versiyon, new Locator.GetByTextOptions().setExact(true))))
                .locator("td").nth(1).innerText().trim();
    }
}