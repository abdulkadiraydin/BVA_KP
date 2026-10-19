package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class OpenDataUpdateCalendarPage extends BasePage {

    private static final int GECMIS_KAYIT_TIMEOUT = 60_000;
    private static final int GECMIS_KONTROL_TIMEOUT = 3_000;

    public OpenDataUpdateCalendarPage(Page page) {
        super(page);
    }

    // ---------- Sekme ----------
    private Locator guncellemeTakvimiSekmesi() {
        return page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Güncelleme Takvimi"));
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
        ReportManager.info("Güncelleme Takvimi sekmesi seçili.");
    }

    // ---------- Dönem seçici + özet kartları ----------
    private Locator donemButonu(String value) {
        // value: 7d, 30d, 90d, 1y
        return page.locator("[aria-label='Dönem'] button[value='" + value + "']");
    }

    public void donemSecme(String value) {
        donemButonu(value).click();
    }

    public void assertDonemSecili(String value) {
        assertThat(donemButonu(value)).hasAttribute("aria-pressed", "true");
        ReportManager.info("Dönem seçili: " + value);
    }

    public void assertDonemSeciliDegil(String value) {
        assertThat(donemButonu(value)).hasAttribute("aria-pressed", "false");
    }

    private Locator ozetKarti(String baslik) {
        return page.locator("div.MuiCard-root").filter(
                new Locator.FilterOptions().setHasText(baslik));
    }

    private Locator ozetKartiDegeri(String baslik) {
        return ozetKarti(baslik).locator("p").first();
    }

    /** Kartın değeri sayısal olmalı (veri yüklenene kadar bekler). */
    public void assertOzetKartiSayisal(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).hasText(Pattern.compile("^\\d+$"));
        ReportManager.info("Özet kartı sayısal değer gösteriyor: " + baslik);
    }

    /** Kartın değeri en az 1 olmalı (veri yüklenene kadar bekler). */
    public void assertOzetKartiEnAz1(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).hasText(Pattern.compile("^[1-9]\\d*$"));
        ReportManager.info("Özet kartı en az 1 gösteriyor: " + baslik);
    }

    public void assertOzetKartiDolu(String baslik) {
        assertThat(ozetKartiDegeri(baslik)).not().hasText("");
        ReportManager.info("Özet kartı dolu: " + baslik);
    }

    // ---------- Güncelleme Planı tablosu ----------
    private Locator planBolumu() {
        return page.locator("#refresh-plan");
    }

    private Locator planTablosu() {
        return planBolumu().locator("[role='grid']");
    }

    private Locator planSatiri(String veriSetiAdi) {
        return planBolumu().locator("[role='row']").filter(
                new Locator.FilterOptions().setHas(
                        page.locator("[data-field='name'] p[title='" + veriSetiAdi + "']")));
    }

    public void planAramaYapma(String metin) {
        planBolumu().getByPlaceholder("Veri seti, kategori veya birim ara").fill(metin);
    }

    /** Plan tablosunda satırın görünmesini 10 sn'ye kadar bekler. */
    public boolean planListesindeVarMi(String veriSetiAdi) {
        try {
            planSatiri(veriSetiAdi).first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    public String planSikligiAlma(String veriSetiAdi) {
        return planSatiri(veriSetiAdi).locator("[data-field='frequency']").innerText().trim();
    }

    public String planSonGuncellemeSurumuAlma(String veriSetiAdi) {
        // Hücrede tarih + "v1" caption'ı var; caption span'ı alınır
        return planSatiri(veriSetiAdi).locator("[data-field='lastUpdatedAt'] span").innerText().trim();
    }

    // ---------- Takvim ----------
    private Locator takvimGunu(int gun) {
        // Ay dışı günler dayOutsideMonth sınıfıyla elenir
        return page.locator("button.MuiPickersDay-root:not(.MuiPickersDay-dayOutsideMonth)")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + gun + "$")));
    }

    /** aria-label örneği: "14 Ekim 2026: 0 planlı, 2 başarılı, 0 portala aktarılamadı, 0 başarısız" */
    public void assertBugunBasariliGuncellemeVar(int gun) {
        assertThat(takvimGunu(gun)).hasAttribute("aria-label",
                Pattern.compile("\\b[1-9]\\d* başarılı"));
        ReportManager.info("Takvimde " + gun + ". gün için başarılı güncelleme görünüyor.");
    }

    /** Seçili günün listesinde kaydın görünmesini 10 sn'ye kadar bekler. */
    public boolean gunDetayindaKayitVarMi(String veriSetiAdi) {
        try {
            page.locator("li p[title='" + veriSetiAdi + "']").first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(10_000));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    // ---------- İşlem Geçmişi ----------
    private Locator gecmisBolumu() {
        return page.locator("#refresh-history");
    }

    private Locator gecmisSatiri(String veriSetiAdi) {
        return gecmisBolumu().locator("[role='row']").filter(
                new Locator.FilterOptions().setHas(
                        page.locator("[data-field='datasetName'] p[title='" + veriSetiAdi + "']")));
    }

    public void gecmisSonucFiltresiSecme(String value) {
        // value: ALL, SUCCEEDED, FAILED
        gecmisBolumu().locator("[aria-label='Sonuç'] button[value='" + value + "']").click();
    }

    /** İşlem Geçmişi'ndeki "Veri seti" autocomplete'inden veri setini seçip tabloyu filtreler. */
    public void gecmisVeriSetiFiltreleme(String veriSetiAdi) {
        Locator giris = gecmisBolumu().getByLabel("Veri seti");
        giris.click();
        giris.fill(veriSetiAdi);
        page.getByRole(AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(veriSetiAdi)).first().click();
    }

    /** Geçmiş tablosunda satırın görünmesini 10 sn'ye kadar bekler. */
    public boolean gecmisteVarMi(String veriSetiAdi) {
        return gecmisteVarMi(veriSetiAdi, 10_000);
    }

    private boolean gecmisteVarMi(String veriSetiAdi, int timeoutMs) {
        try {
            gecmisSatiri(veriSetiAdi).first()
                    .waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    /** Kaydın geçmiş tablosunda GÖRÜNMEDİĞİNİ bekleyerek doğrular (filtre sonrası). */
    public void assertGecmisteKayitYok(String veriSetiAdi) {
        assertThat(gecmisSatiri(veriSetiAdi)).hasCount(0);
        ReportManager.info("Geçmiş tablosunda kayıt görünmüyor: " + veriSetiAdi);
    }

    public String gecmisSonucuAlma(String veriSetiAdi) {
        return gecmisSatiri(veriSetiAdi).locator("[data-field='status']").innerText().trim();
    }

    public String gecmisSurumuAlma(String veriSetiAdi) {
        return gecmisSatiri(veriSetiAdi).locator("[data-field='versionNo']").innerText().trim();
    }

    public String gecmisTetikleyenAlma(String veriSetiAdi) {
        return gecmisSatiri(veriSetiAdi).locator("[data-field='trigger']").innerText().trim();
    }

    public String gecmisKayitSayisiAlma(String veriSetiAdi) {
        return gecmisSatiri(veriSetiAdi).locator("[data-field='recordCount']")
                .innerText().trim();
    }

    /**
     * Sayfayı yenileyip geçmişte kaydın görünmesini bekler.
     * Her turda: reload, sekmeye geçiş, veri seti filtresi, kayıt kontrolü.
     * Bir tur başarısız olursa (ör. reload sonrası sekme gelmediyse) sonraki tur denenir,
     * süre dolunca URL ve son hata ile AssertionError fırlatılır.
     */
    public void gecmisteKayitBekleme(String veriSetiAdi) {
        long start = System.currentTimeMillis();
        String sonHata = "yok";

        while (System.currentTimeMillis() - start < GECMIS_KAYIT_TIMEOUT) {
            try {
                page.reload();
                guncellemeTakvimiSekmesineGecme();
                gecmisVeriSetiFiltreleme(veriSetiAdi);
                if (gecmisteVarMi(veriSetiAdi, GECMIS_KONTROL_TIMEOUT)) {
                    ReportManager.info("Geçmişte kayıt görüldü: " + veriSetiAdi);
                    return;
                }
            } catch (PlaywrightException | AssertionError e) {
                sonHata = e.getMessage();
            }
        }
        throw new AssertionError("Geçmişte kayıt bulunamadı: " + veriSetiAdi
                + " | URL: " + page.url() + " | Son hata: " + sonHata);
    }

    /** "v2" -> 2. Okunamazsa ("—" gibi) net hata verir. */
    public int surumNumarasi(String hamDeger) {
        String temiz = hamDeger == null ? "" : hamDeger.replaceAll("[^0-9]", "");
        if (temiz.isEmpty()) {
            throw new IllegalStateException("Sürüm okunamadı. Değer: [" + hamDeger + "]");
        }
        return Integer.parseInt(temiz);
    }

    // ---------- Güncelleme Ayrıntısı çekmecesi ----------
    private Locator detayCekmecesi() {
        return page.locator("div.MuiDrawer-paper[role='dialog']");
    }

    /** Takvimde seçili günün listesinden veri setinin kaydına tıklar. */
    public void gunListesindenKayitAcma(String veriSetiAdi) {
        page.locator("li").filter(
                        new Locator.FilterOptions().setHas(
                                page.locator("p[title='" + veriSetiAdi + "']")))
                .first().click();
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
        ReportManager.info("Güncelleme ayrıntısı çekmecesi kapalı.");
    }

    // ---------- Veri seti detay sayfası ("Veri setine git" sonrası) ----------
    public void assertDetayBaslik(String veriSetiAdi) {
        page.waitForURL(url -> !url.endsWith("/opendata"),
                new Page.WaitForURLOptions().setTimeout(10_000));
        assertThat(page.locator("h6").first()).hasText(veriSetiAdi);
        ReportManager.info("Veri seti detay sayfası açıldı: " + veriSetiAdi);
    }

    public void detaySekmesineGecme(String ad) {
        page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(ad)).click();
    }

    public String versiyonDurumuAlma(String versiyon) {
        return page.locator("tbody tr")
                .filter(new Locator.FilterOptions().setHas(
                        page.locator("td").getByText(versiyon,
                                new Locator.GetByTextOptions().setExact(true))))
                .locator("td").nth(1).innerText().trim();
    }
}