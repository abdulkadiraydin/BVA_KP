package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenDataUpdateCalendarPage extends BasePage {

    public OpenDataUpdateCalendarPage(Page page) {
        super(page);
    }

    // ---------- Sekme ----------
    private Locator guncellemeTakvimiSekmesi() {
        return page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Güncelleme Takvimi"));
    }

    /**
     * Güncelleme Takvimi sekmesine geçer.
     * Sekme 10 sn içinde görünmezse (sayfa başka bir ekranda kalmış olabilir)
     * yan menüden Açık Veri Portalı'na dönmeyi dener.
     */
    public void guncellemeTakvimiSekmesineGecme() {

        Locator sekme = guncellemeTakvimiSekmesi();

        try {
            sekme.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        } catch (PlaywrightException ilkHata) {
            try {
                page.getByText("Açık Veri Portalı",
                                new Page.GetByTextOptions().setExact(true))
                        .first()
                        .click(new Locator.ClickOptions().setTimeout(5_000));
                sekme.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
            } catch (PlaywrightException ikinciHata) {
                throw new AssertionError(
                        "Güncelleme Takvimi sekmesi bulunamadı. URL: " + page.url()
                                + " | Hata: " + ikinciHata.getMessage());
            }
        }

        sekme.click(new Locator.ClickOptions().setTimeout(10_000));
        planTablosu().waitFor();
    }

    public boolean guncellemeTakvimiSekmesiSeciliMi() {
        return "true".equals(guncellemeTakvimiSekmesi().getAttribute("aria-selected"));
    }

    // ---------- Dönem seçici + özet kartları ----------
    private Locator donemButonu(String value) {
        // value: 7d, 30d, 90d, 1y
        return page.locator("[aria-label='Dönem'] button[value='" + value + "']");
    }

    public void donemSecme(String value) {
        donemButonu(value).click();
    }

    public boolean donemSeciliMi(String value) {
        return "true".equals(donemButonu(value).getAttribute("aria-pressed"));
    }

    private Locator ozetKarti(String baslik) {
        return page.locator("div.MuiCard-root").filter(
                new Locator.FilterOptions().setHasText(baslik));
    }

    public String ozetKartiDegeriAlma(String baslik) {
        // Kartın içindeki ilk metin değeri (sayı veya "3 sn")
        return ozetKarti(baslik).locator("p").first().innerText().trim();
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

    public String planSonCalismaAlma(String veriSetiAdi) {
        return planSatiri(veriSetiAdi).locator("[data-field='latestRun']").innerText().trim();
    }

    public String planSonGuncellemeSurumuAlma(String veriSetiAdi) {
        // Hücrede tarih + "v1" caption'ı var; caption span'ı alınır
        return planSatiri(veriSetiAdi).locator("[data-field='lastUpdatedAt'] span").innerText().trim();
    }

    public void simdiGuncelleTiklama(String veriSetiAdi) {
        planSatiri(veriSetiAdi).getByLabel("Şimdi güncelle").click();
    }

    public void planiDuzenleTiklama(String veriSetiAdi) {
        planSatiri(veriSetiAdi).getByLabel("Planı düzenle").click();
    }

    public void veriSetineGitTiklama(String veriSetiAdi) {
        planSatiri(veriSetiAdi).getByLabel("Veri setine git").click();
    }

    // ---------- Takvim ----------
    private Locator takvimGunu(int gun) {
        // Ay dışı günler dayOutsideMonth sınıfıyla elenir
        return page.locator("button.MuiPickersDay-root:not(.MuiPickersDay-dayOutsideMonth)")
                .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^" + gun + "$")));
    }

    public void takvimGunuSecme(int gun) {
        takvimGunu(gun).click();
    }

    /** Örn: "14 Ekim 2026: 0 planlı, 2 başarılı, 0 portala aktarılamadı, 0 başarısız" */
    public String takvimGunuOzetiAlma(int gun) {
        String label = takvimGunu(gun).getAttribute("aria-label");
        return label == null ? "" : label;
    }

    public int takvimGunuBasariliSayisi(int gun) {
        Matcher m = Pattern.compile("(\\d+) başarılı").matcher(takvimGunuOzetiAlma(gun));
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
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

    public void gecmisSonucFiltresiSecme(String value) {
        // value: ALL, SUCCEEDED, FAILED
        gecmisBolumu().locator("[aria-label='Sonuç'] button[value='" + value + "']").click();
    }

    private Locator gecmisSatiri(String veriSetiAdi) {
        return gecmisBolumu().locator("[role='row']").filter(
                new Locator.FilterOptions().setHas(
                        page.locator("[data-field='datasetName'] p[title='" + veriSetiAdi + "']")));
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
    public boolean gecmisteKayitYokMu(String veriSetiAdi) {
        try {
            PlaywrightAssertions.assertThat(gecmisSatiri(veriSetiAdi)).hasCount(0);
            return true;
        } catch (AssertionError e) {
            return false;
        }
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
     * Hata olursa (ör. reload sonrası sekme bulunamazsa) tekrar dener,
     * süre dolunca URL ve son hata ile birlikte AssertionError fırlatır.
     */
    public void gecmisteKayitBekleme(String veriSetiAdi, int timeoutMs) {
        long start = System.currentTimeMillis();
        String sonHata = "yok";

        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                page.reload();
                guncellemeTakvimiSekmesineGecme();
                if (gecmisteVarMi(veriSetiAdi, 3_000)) {
                    return;
                }
            } catch (PlaywrightException | AssertionError e) {
                sonHata = e.getMessage();
            }
            page.waitForTimeout(2000);
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

    public boolean detayCekmecesiKapaliMi() {
        try {
            PlaywrightAssertions.assertThat(detayCekmecesi()).hasCount(0);
            return true;
        } catch (AssertionError e) {
            return false;
        }
    }

    // ---------- Veri seti detay sayfası ("Veri setine git" sonrası) ----------
    public String detayBaslikAlma() {
        page.waitForURL(url -> !url.endsWith("/opendata"),
                new Page.WaitForURLOptions().setTimeout(10_000));
        Locator baslik = page.locator("h6").first();
        baslik.waitFor();
        return baslik.innerText().trim();
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