package com.bvakp.automation.pages.publicportal;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kamuya açık portalda kaynak (önizleme) sayfası: Tablo, Veri Sözlüğü ve API bölümleri.
 *
 * <p>Tablo önizlemesi iframe içinde olduğu için okunmaz; kayıt değerleri API ile doğrulanır.
 * Bölümler aynı sayfada olduğundan okumalar sekme açmadan da çalışır.
 */
public class PublicPortalResourcePage extends BasePage {

    private final Locator baslik;
    private final Locator formatlar;
    private final Locator sekmeler;
    private final Locator toplamKayit;
    private final Locator sozlukSatirlari;
    private final Locator apiAdresi;
    private final Locator apiKaynakId;
    private final Locator apiAlanlari;

    public PublicPortalResourcePage(Page page) {
        super(page);
        this.baslik = page.locator("h1.bvakp-resource-title");
        this.formatlar = page.locator(".bvakp-detail-head .bvakp-chips .bvakp-fmt");
        this.sekmeler = page.locator("nav.bvakp-tabs");
        this.toplamKayit = page.locator(".bvakp-table-hint strong");
        this.sozlukSatirlari = page.locator(".bvakp-dict-table tbody tr");
        this.apiAdresi = page.locator("#api .bvakp-api-url");
        this.apiKaynakId = page.locator("#api .bvakp-api-rid");
        this.apiAlanlari = page.locator("#api .bvakp-api-field");
    }

    public boolean kaynakSayfasiGoruntulendiMi(String veriSetiAdi) {
        return gorunurMu(baslik) && veriSetiAdi.equals(baslik.textContent().trim());
    }

    /** Sekmeye tıklar: "Tablo", "Veri Sözlüğü" veya "API". */
    public void sekmeAcma(String sekmeAdi) {
        sekmeler.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(sekmeAdi).setExact(true)).click();
        ReportManager.info("Kaynak sekmesi açıldı: " + sekmeAdi);
    }

    /** Başlıktaki format çipleri (Data API açıksa "API" da gelir). */
    public List<String> formatlariOkuma() {
        return formatlar.allTextContents().stream().map(String::trim).toList();
    }

    /** Tablo sekmesindeki "Toplam 49 kayıt" değeri. */
    public int toplamKayitSayisiOkuma() {
        return Integer.parseInt(toplamKayit.textContent().replaceAll("\\D", ""));
    }

    /** Veri Sözlüğü: alan adı → tip ("metin" gibi), tablodaki sırayla. */
    public Map<String, String> veriSozluguOkuma() {

        Map<String, String> alanlar = new LinkedHashMap<>();
        for (Locator satir : sozlukSatirlari.all()) {
            alanlar.put(satir.locator(".bvakp-dict-name").textContent().trim(),
                    satir.locator(".bvakp-type").textContent().trim());
        }
        return alanlar;
    }

    /** API bölümündeki uç nokta (…/api/3/action/datastore_search). */
    public String apiAdresiOkuma() {
        return apiAdresi.textContent().trim();
    }

    public String apiKaynakIdOkuma() {
        return apiKaynakId.textContent().trim();
    }

    /** API bölümündeki "Alanlar": alan adı → API tipi ("text" gibi), sırayla. */
    public Map<String, String> apiAlanlariniOkuma() {

        Map<String, String> alanlar = new LinkedHashMap<>();
        for (Locator alan : apiAlanlari.all()) {
            alanlar.put(alan.locator("code").textContent().trim(),
                    alan.locator(".bvakp-api-field-type").textContent().trim());
        }
        return alanlar;
    }
}