package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.LoadState;
import java.util.regex.Pattern;

/**
 * Açık Veri Portalı "Güncelleme Takvimi" sekmesi: dönem seçici, özet kartları, takvim, Güncelleme
 * Planı tablosu, İşlem Geçmişi ve Güncelleme Ayrıntısı çekmecesi.
 *
 * <p>Locator'lar ilgili bölüme sınırlıdır (#refresh-plan, #refresh-history, çekmece). Kontrol
 * metotları bekleyerek true/false döner; doğrulama testte yapılır.
 */
public class OpenDataUpdateCalendarPage extends BasePage {

  private static final int SATIR_TIMEOUT_MS = 10_000;
  private static final int GECMIS_TUR_KONTROL_MS = 3_000;
  private static final int GECMIS_TUR_ARALIGI_MS = 2_000;

  /** Güncelleme sonrası portala aktarım 3 dakikaya kadar sürebilir. */
  private static final int GECMIS_SONUC_TIMEOUT_MS = 180_000;

  /** Türkçe biçimli tam sayı: "42", "3.529". */
  private static final Pattern SAYI = Pattern.compile("^\\d{1,3}(\\.\\d{3})*$");

  public OpenDataUpdateCalendarPage(Page page) {
    super(page);
  }

  // =====================================================================
  // Sekme
  // =====================================================================

  /** Güncelleme Takvimi sekmesi. */
  private Locator guncellemeTakvimiSekmesi() {
    return page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Güncelleme Takvimi"));
  }

  /** Güncelleme Takvimi sekmesine geçer ve plan tablosunun görünmesini bekler. */
  public void takvimSekmesineGecme() {
    try {
      guncellemeTakvimiSekmesi().click(new Locator.ClickOptions().setTimeout(15_000));
    } catch (PlaywrightException e) {
      throw new IllegalStateException(
          "Güncelleme Takvimi sekmesi bulunamadı. URL: "
              + page.url()
              + " | Hata: "
              + e.getMessage());
    }
    planTablosu().waitFor();
  }

  /** Güncelleme Takvimi sekmesi seçiliyse true döner. */
  public boolean sekmeSeciliMi() {
    return gorunurMu(guncellemeTakvimiSekmesi().and(page.locator("[aria-selected='true']")));
  }

  // =====================================================================
  // Dönem seçici ve özet kartları
  // =====================================================================

  /** value: 7d, 30d, 90d, 1y */
  private Locator donemButonu(String value) {
    return page.locator("[aria-label='Dönem'] button[value='" + value + "']");
  }

  /**
   * Dönem seçicide verilen dönemi seçer (ör. 7d) ve yeni verilerin gelmesini bekler; kart değerleri
   * ancak istekler bitince güncel olur.
   */
  public void donemSecme(String value) {
    donemButonu(value).click();
    try {
      page.waitForLoadState(
          LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(10_000));
    } catch (PlaywrightException e) {
      // Sayfa arka planda sürekli istek atıyorsa ağ hiç durulmaz; kart kontrolleri yine bekler.
    }
  }

  /** Verilen dönem seçiliyse true döner. */
  public boolean donemSeciliMi(String value) {
    return gorunurMu(donemButonu(value).and(page.locator("[aria-pressed='true']")));
  }

  /** Verilen dönem seçili değilse true döner. */
  public boolean donemSeciliDegilMi(String value) {
    return gorunurMu(donemButonu(value).and(page.locator("[aria-pressed='false']")));
  }

  /** Başlığı verilen özet kartının değer alanı. */
  private Locator ozetKartiDegeri(String baslik) {
    return page.locator("div.MuiCard-root")
        .filter(new Locator.FilterOptions().setHasText(baslik))
        .locator("p")
        .first();
  }

  /** Kartın değeri Türkçe biçimli bir sayı olana kadar bekler (ör. "30", "1.234"). */
  public boolean ozetKartiSayisalMi(String baslik) {
    return gorunurMu(ozetKartiDegeri(baslik).filter(new Locator.FilterOptions().setHasText(SAYI)));
  }

  /** Kartın sayısını döndürür ("1.234" → 1234); önce ozetKartiSayisalMi ile beklenmelidir. */
  public int ozetKartiSayisiAlma(String baslik) {
    return Integer.parseInt(ozetKartiDegeri(baslik).innerText().trim().replace(".", ""));
  }

  /** Kartın değeri boş değilse true döner (ör. "10 sn"). */
  public boolean ozetKartiDoluMu(String baslik) {
    return gorunurMu(
        ozetKartiDegeri(baslik)
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("\\S"))));
  }

  // =====================================================================
  // Güncelleme Planı tablosu
  // =====================================================================

  /** Güncelleme Planı bölümü. */
  private Locator planBolumu() {
    return page.locator("#refresh-plan");
  }

  /** Güncelleme Planı tablosu. */
  private Locator planTablosu() {
    return planBolumu().locator("[role='grid']");
  }

  /** Plan tablosunda veri setinin satırı. */
  private Locator planSatiri(String veriSetiAdi) {
    return planBolumu()
        .locator("[role='row']")
        .filter(
            new Locator.FilterOptions()
                .setHas(page.locator("[data-field='name'] p[title='" + veriSetiAdi + "']")));
  }

  /** Plan tablosunun arama kutusuna metin yazar. */
  public void planAramaYapma(String metin) {
    planBolumu().getByPlaceholder("Veri seti, kategori veya birim ara").fill(metin);
  }

  /** Veri seti plan tablosunda görünüyorsa true döner. */
  public boolean planListesindeVarMi(String veriSetiAdi) {
    return gorunurMu(planSatiri(veriSetiAdi), SATIR_TIMEOUT_MS);
  }

  /** Plan tablosunda veri setinin güncelleme sıklığını döndürür. */
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

  /**
   * Takvimde günün en az bir başarılı güncellemeyle işaretli olduğunu kontrol eder. aria-label
   * örneği: "14 Ekim 2026: 0 planlı, 2 başarılı, 0 portala aktarılamadı, 0 başarısız".
   */
  public boolean gunBasariliMi(int gun) {
    return gorunurMu(
        takvimGunu(gun)
            .and(page.locator("[aria-label*=' başarılı']:not([aria-label*=', 0 başarılı'])")));
  }

  /** Seçili günün listesinde veri seti görünüyorsa true döner. */
  public boolean gunDetayindaKayitVarMi(String veriSetiAdi) {
    return gorunurMu(page.locator("li p[title='" + veriSetiAdi + "']"), SATIR_TIMEOUT_MS);
  }

  // =====================================================================
  // İşlem Geçmişi
  // =====================================================================

  /** İşlem Geçmişi bölümü. */
  private Locator gecmisBolumu() {
    return page.locator("#refresh-history");
  }

  /** İşlem Geçmişinde veri setinin satırı. */
  private Locator gecmisSatiri(String veriSetiAdi) {
    return gecmisBolumu()
        .locator("[role='row']")
        .filter(
            new Locator.FilterOptions()
                .setHas(page.locator("[data-field='datasetName'] p[title='" + veriSetiAdi + "']")));
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
    page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName(veriSetiAdi))
        .first()
        .click();
  }

  /** Veri seti İşlem Geçmişinde görünüyorsa true döner. */
  public boolean gecmisteVarMi(String veriSetiAdi) {
    return gorunurMu(gecmisSatiri(veriSetiAdi), SATIR_TIMEOUT_MS);
  }

  /** Filtre sonrası veri setinin satırı geçmiş tablosunda yoksa true döner. */
  public boolean gecmisteYokMu(String veriSetiAdi) {
    return gorunmuyorMu(gecmisSatiri(veriSetiAdi), SATIR_TIMEOUT_MS);
  }

  /** İşlem Geçmişindeki sonucu döndürür (ör. Başarılı). */
  public String gecmisSonucuAlma(String veriSetiAdi) {
    return gecmisHucresi(veriSetiAdi, "status");
  }

  /** İşlem Geçmişindeki sürümü döndürür (ör. v2). */
  public String gecmisSurumuAlma(String veriSetiAdi) {
    return gecmisHucresi(veriSetiAdi, "versionNo");
  }

  /** İşlem Geçmişindeki tetikleyeni döndürür (ör. Manuel). */
  public String gecmisTetikleyenAlma(String veriSetiAdi) {
    return gecmisHucresi(veriSetiAdi, "trigger");
  }

  /** İşlem Geçmişindeki kayıt sayısını döndürür. */
  public String gecmisKayitSayisiAlma(String veriSetiAdi) {
    return gecmisHucresi(veriSetiAdi, "recordCount");
  }

  /** İşlem Geçmişinde veri setinin verilen sütundaki değeri. */
  private String gecmisHucresi(String veriSetiAdi, String sutun) {
    return gecmisSatiri(veriSetiAdi).locator("[data-field='" + sutun + "']").innerText().trim();
  }

  /**
   * Geçmişteki kaydın sonucu beklenen değere ulaşana kadar sayfayı yenileyerek bekler; süre dolarsa
   * son görüleni loga yazar ve false döner.
   *
   * <p>Güncelleme sonrası kayıt önce ara durumlardan geçer ("Portala aktarılıyor" gibi); yalnızca
   * kaydın var olmasını beklemek, işlem bitmeden okumaya yol açar.
   */
  public boolean gecmisteSonucaUlastiMi(String veriSetiAdi, String beklenenSonuc) {

    long bitis = System.currentTimeMillis() + GECMIS_SONUC_TIMEOUT_MS;
    String sonDurum = "kayıt bulunamadı";

    while (System.currentTimeMillis() < bitis) {
      try {
        page.reload();
        takvimSekmesineGecme();
        gecmisVeriSetiFiltreleme(veriSetiAdi);

        if (gorunurMu(gecmisSatiri(veriSetiAdi), GECMIS_TUR_KONTROL_MS)) {
          sonDurum = gecmisSonucuAlma(veriSetiAdi);
          if (beklenenSonuc.equals(sonDurum)) {
            ReportManager.info("Geçmiş sonucu doğrulandı: " + veriSetiAdi + " | " + sonDurum);
            return true;
          }
        }
      } catch (PlaywrightException | IllegalStateException e) {
        sonDurum = "okunamadı: " + e.getMessage();
      }

      // Yenilemeler arasında bekleme: sunucuyu gereksiz yere yormamak için.
      page.waitForTimeout(GECMIS_TUR_ARALIGI_MS);
    }

    ReportManager.warn(
        "Geçmiş sonucu "
            + GECMIS_SONUC_TIMEOUT_MS / 1000
            + " sn içinde '"
            + beklenenSonuc
            + "' olmadı: "
            + veriSetiAdi
            + " | Son görülen: "
            + sonDurum);
    return false;
  }

  // =====================================================================
  // Güncelleme Ayrıntısı çekmecesi
  // =====================================================================

  /** Güncelleme Ayrıntısı çekmecesi. */
  private Locator detayCekmecesi() {
    return page.locator("div.MuiDrawer-paper[role='dialog']");
  }

  /** Takvimde seçili günün listesinden veri setinin kaydına tıklar ve çekmeceyi bekler. */
  public void gunListesindenKayitAcma(String veriSetiAdi) {
    page.locator("li")
        .filter(new Locator.FilterOptions().setHas(page.locator("p[title='" + veriSetiAdi + "']")))
        .first()
        .click();
    detayCekmecesi().waitFor();
  }

  /** Çekmecedeki veri seti adını döndürür. */
  public String detayVeriSetiAdiAlma() {
    return detayCekmecesi().locator("p[title]").first().getAttribute("title");
  }

  /** Çekmecedeki sonucu döndürür. */
  public String detaySonucAlma() {
    return detayCekmecesi().locator(".MuiChip-label").first().innerText().trim();
  }

  /**
   * Etiket metnine göre değer okur. Örn: "Tetikleyen", "Oluşan sürüm", "İşlenen kayıt", "Deneme"
   */
  public String detayAlanDegeriAlma(String etiket) {
    return detayCekmecesi()
        .getByText(etiket, new Locator.GetByTextOptions().setExact(true))
        .first()
        .locator("xpath=following-sibling::div[1]")
        .innerText()
        .trim();
  }

  /** Çekmecedeki Akış bölümünün metnini döndürür. */
  public String detayAkisMetniAlma() {
    return detayCekmecesi().locator("section[aria-label='Akış']").innerText();
  }

  /** Çekmecedeki Veri setine git butonuna tıklar. */
  public void detaydaVeriSetineGitTiklama() {
    detayCekmecesi()
        .getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Veri setine git"))
        .click();
  }

  /** Çekmeceyi Kapat ile kapatır. */
  public void detayiKapatma() {
    detayCekmecesi().getByLabel("Kapat").click();
  }

  /** Çekmece kapandıysa true döner. */
  public boolean detayCekmecesiKapaliMi() {
    return gorunmuyorMu(detayCekmecesi());
  }

  // =====================================================================
  // Veri seti detay sayfası ("Veri setine git" sonrası)
  // =====================================================================

  /** Veri seti detay sayfası açıldıysa ve başlık veri seti adıysa true döner. */
  public boolean detayBasligiDogruMu(String veriSetiAdi) {
    return gorunurMu(
        page.getByRole(
            AriaRole.HEADING, new Page.GetByRoleOptions().setName(veriSetiAdi).setExact(true)));
  }

  /** Veri seti detayında adı verilen sekmeye geçer. */
  public void detaySekmesineGecme(String ad) {
    page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(ad)).click();
  }

  /** Versiyonlar tablosunda verilen versiyonun durumunu döndürür (ör. Aktif). */
  public String versiyonDurumuAlma(String versiyon) {
    return page.locator("tbody tr")
        .filter(
            new Locator.FilterOptions()
                .setHas(
                    page.locator("td")
                        .getByText(versiyon, new Locator.GetByTextOptions().setExact(true))))
        .locator("td")
        .nth(1)
        .innerText()
        .trim();
  }
}
