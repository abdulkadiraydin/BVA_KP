package com.bvakp.automation.pages.publicPortal;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Kamuya açık portalda kaynak (önizleme) sayfası (/dataset/{ad}/resource/{id}): Tablo, Veri Sözlüğü
 * ve API bölümleri. Veri seti detayındaki "Önizle" ile açılır.
 *
 * <p>Tablo önizlemesi iframe içinde olduğu için okunmaz; kayıt değerleri API ile doğrulanır.
 * Bölümler aynı sayfada olduğundan okumalar sekme açmadan da çalışır.
 */
public class PublicPortalResourcePage extends PublicPortalBasePage {

  private final Locator formatlar;
  private final Locator sekmeler;
  private final Locator toplamKayit;
  private final Locator sozlukSatirlari;
  private final Locator apiAdresi;
  private final Locator apiKaynakId;
  private final Locator apiAlanlari;

  public PublicPortalResourcePage(Page page) {
    super(page);
    this.formatlar = page.locator("header.bvakp-detail-head .bvakp-fmt");
    this.sekmeler =
        page.getByRole(
            AriaRole.NAVIGATION, new Page.GetByRoleOptions().setName("Kaynak bölümleri"));
    this.toplamKayit = page.getByText(Pattern.compile("^Toplam [\\d.]+ kayıt$"));

    // Başlık satırında (ALAN, TİP, AÇIKLAMA) hücre yoktur; yalnız veri satırları alınır.
    this.sozlukSatirlari =
        bolum("Veri Sözlüğü")
            .getByRole(AriaRole.ROW)
            .filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL)));

    // API bölümü; sekmedeki "API" linki de bu bölüme (#api) gider.
    Locator api = page.locator("#api");
    this.apiAdresi = api.locator(".bvakp-api-url");
    this.apiKaynakId = api.locator(".bvakp-api-rid");
    this.apiAlanlari = api.locator(".bvakp-api-field");
  }

  /** Kaynak sayfası açık ve başlık veri seti adıyla aynıysa true döner. */
  public boolean kaynakSayfasiGoruntulendiMi(String veriSetiAdi) {
    return gorunurMu(
        page.getByRole(AriaRole.MAIN)
            .getByRole(
                AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName(veriSetiAdi).setExact(true).setLevel(1)));
  }

  /** Sekmeye tıklar: "Tablo", "Veri Sözlüğü" veya "API". */
  public void sekmeAcma(String sekmeAdi) {
    sekmeler
        .getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName(sekmeAdi).setExact(true))
        .click();
    ReportManager.info("Kaynak sekmesi açıldı: " + sekmeAdi);
  }

  /** Başlıktaki format çipleri (Data API açıksa "API" da gelir). */
  public List<String> formatlariOkuma() {
    return formatlar.allTextContents().stream().map(String::trim).toList();
  }

  /** Tablo sekmesindeki "Toplam 3.529 kayıt" değeri. */
  public int toplamKayitSayisiOkuma() {
    return Integer.parseInt(toplamKayit.textContent().replaceAll("\\D", ""));
  }

  /** Veri Sözlüğü: alan adı → tip ("metin" gibi), tablodaki sırayla. */
  public Map<String, String> veriSozluguOkuma() {

    Map<String, String> alanlar = new LinkedHashMap<>();
    for (Locator satir : sozlukSatirlari.all()) {
      Locator hucreler = satir.getByRole(AriaRole.CELL);
      alanlar.put(hucreler.nth(0).textContent().trim(), hucreler.nth(1).textContent().trim());
    }
    return alanlar;
  }

  /** API bölümündeki uç nokta (…/api/3/action/datastore_search). */
  public String apiAdresiOkuma() {
    return apiAdresi.textContent().trim();
  }

  /** API bölümündeki kaynak kimliğini (resource id) döndürür. */
  public String apiKaynakIdOkuma() {
    return apiKaynakId.textContent().trim();
  }

  /** API bölümündeki "Alanlar": alan adı → API tipi ("text" gibi), sırayla. */
  public Map<String, String> apiAlanlariniOkuma() {

    Map<String, String> alanlar = new LinkedHashMap<>();
    for (Locator alan : apiAlanlari.all()) {
      alanlar.put(
          alan.locator("code").textContent().trim(),
          alan.locator(".bvakp-api-field-type").textContent().trim());
    }
    return alanlar;
  }
}
