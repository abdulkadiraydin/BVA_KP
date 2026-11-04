package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Açık Veri Portalı "Portal Dashboard" sekmesi: istatistik kartları, format dağılımı, indirme/API
 * trendi, en çok indirilen/sorgulanan veri setleri ve kategori dağılımı.
 *
 * <p>Kartlar ve grafikler sekme açıldıktan sonra sunucudan yüklenir; bütün görünürlük kontrolleri
 * bu yüzden bekleyerek yapılır.
 */
public class OpenDataDashboardPage extends BasePage {

  private static final int DASHBOARD_TIMEOUT_MS = 15_000;

  /** Format legend satırı: "CSV: 119 (78%)" → 119. */
  private static final Pattern FORMAT_SATIRI = Pattern.compile("^[A-Z]+: ([\\d.]+) \\(\\d+%\\)$");

  /** Seçenekleri kodda yazım hatasına karşı tek yerde tutar. */
  public enum TrendDonemi {
    GUNLUK("Günlük"),
    HAFTALIK("Haftalık"),
    AYLIK("Aylık");

    private final String butonAdi;

    TrendDonemi(String butonAdi) {
      this.butonAdi = butonAdi;
    }

    /** Dönem butonunun ekrandaki adı. */
    public String butonAdi() {
      return butonAdi;
    }

    /** Raporda ve test adında HAFTALIK yerine Haftalık görünsün diye. */
    @Override
    public String toString() {
      return butonAdi;
    }
  }

  private final Locator portalDashboardSekmesi;
  private final Locator portalIstatistikleriBasligi;

  private final Locator toplamVeriSetiKarti;
  private final Locator toplamIndirmeKarti;
  private final Locator apiCagrisiKarti;
  private final Locator benzersizKullaniciKarti;

  private final Locator formatDagilimiBasligi;
  private final Locator formatDagilimiGrafikAlani;
  private final Locator csvLegend;
  private final Locator tsvLegend;
  private final Locator xlsxLegend;

  private final Locator indirmeApiTrendiBasligi;

  private final Locator enCokIndirilenBasligi;
  private final Locator enCokIndirilenMetni;
  private final Locator enCokApiBasligi;
  private final Locator enCokApiMetni;
  private final Locator kategoriDagilimiBasligi;
  private final Locator kategoriDagilimiAciklamasi;

  public OpenDataDashboardPage(Page page) {

    super(page);

    portalDashboardSekmesi =
        page.getByRole(
            AriaRole.TAB, new Page.GetByRoleOptions().setName("Portal Dashboard").setExact(true));
    portalIstatistikleriBasligi = tamMetin("Portal İstatistikleri ve Raporlama");

    toplamVeriSetiKarti = tamMetin("Yayındaki Veri Seti");
    toplamIndirmeKarti = tamMetin("Toplam İndirme");
    // "API Çağrısı" metni grafik legend'ında da geçtiği için kart etiketi (p) ayrıca hedeflenir.
    apiCagrisiKarti =
        page.locator("p")
            .filter(new Locator.FilterOptions().setHasText(Pattern.compile("^API Çağrısı$")));
    benzersizKullaniciKarti = tamMetin("Benzersiz Kullanıcı");

    formatDagilimiBasligi = tamMetin("Format Dağılımı");
    formatDagilimiGrafikAlani =
        page.locator("svg").filter(new Locator.FilterOptions().setHasText("İNDİRME"));
    csvLegend = page.getByText("CSV:");
    tsvLegend = page.getByText("TSV:");
    xlsxLegend = page.getByText("XLSX:");

    indirmeApiTrendiBasligi = tamMetin("İndirme ve API Erişim Trendi");

    enCokIndirilenBasligi = tamMetin("En Çok İndirilen Veri Setleri");
    enCokIndirilenMetni = tamMetin("Dönem içinde en çok indirilen veri setleri");
    enCokApiBasligi = tamMetin("En Çok API ile Sorgulanan Veri Setleri");
    enCokApiMetni = tamMetin("Dönem içinde API ile en çok sorgulanan veri setleri");
    kategoriDagilimiBasligi = tamMetin("Kategori Dağılımı");
    kategoriDagilimiAciklamasi = tamMetin("Yayınlanmış veri setlerinin kategorilere göre dağılımı");
  }

  /** Metni birebir eşleşen elementi döndürür. */
  private Locator tamMetin(String metin) {
    return page.getByText(metin, new Page.GetByTextOptions().setExact(true));
  }

  // =====================================================================
  // Sekme
  // =====================================================================

  /** Portal Dashboard sekmesine geçer ve başlığın yüklenmesini bekler. */
  public void portalDashboardAcma() {
    portalDashboardSekmesi.click();
    if (!gorunurMu(portalIstatistikleriBasligi, DASHBOARD_TIMEOUT_MS)) {
      throw new IllegalStateException("Portal Dashboard sekmesi açılamadı: başlık görüntülenmedi.");
    }
  }

  // =====================================================================
  // İstatistik kartları
  // =====================================================================

  /** Toplam Veri Seti kartı görünüyorsa true döner. */
  public boolean toplamVeriSetiKartiGorunurMu() {
    return gorunurMu(toplamVeriSetiKarti, DASHBOARD_TIMEOUT_MS);
  }

  /** Toplam İndirme kartı görünüyorsa true döner. */
  public boolean toplamIndirmeKartiGorunurMu() {
    return gorunurMu(toplamIndirmeKarti, DASHBOARD_TIMEOUT_MS);
  }

  /** API Çağrısı kartı görünüyorsa true döner. */
  public boolean apiCagrisiKartiGorunurMu() {
    return gorunurMu(apiCagrisiKarti, DASHBOARD_TIMEOUT_MS);
  }

  /** Benzersiz Kullanıcı kartı görünüyorsa true döner. */
  public boolean tekilKullaniciKartiGorunurMu() {
    return gorunurMu(benzersizKullaniciKarti, DASHBOARD_TIMEOUT_MS);
  }

  // =====================================================================
  // Format dağılımı
  // =====================================================================

  /** Format Dağılımı başlığı görünüyorsa true döner. */
  public boolean formatDagilimiBasligiGorunurMu() {
    return gorunurMu(formatDagilimiBasligi, DASHBOARD_TIMEOUT_MS);
  }

  /** Format Dağılımı grafiği görünüyorsa true döner. */
  public boolean formatDagilimiGrafikGorunurMu() {
    return gorunurMu(formatDagilimiGrafikAlani, DASHBOARD_TIMEOUT_MS);
  }

  /**
   * CSV, TSV ve XLSX legend'larının üçü de görünüyorsa true. Not: legend yalnızca indirmesi olan
   * formatları gösteriyorsa sonuç test verisine bağlıdır.
   */
  public boolean formatLegendleriGorunurMu() {
    return gorunurMu(csvLegend, DASHBOARD_TIMEOUT_MS)
        && tsvLegend.isVisible()
        && xlsxLegend.isVisible();
  }

  /** Yayındaki Veri Seti kartındaki sayı. */
  public int yayindakiVeriSetiAlma() {
    return kartDegeri(toplamVeriSetiKarti);
  }

  /** Toplam İndirme kartındaki sayı. */
  public int toplamIndirmeAlma() {
    return kartDegeri(toplamIndirmeKarti);
  }

  /** API Çağrısı kartındaki sayı. */
  public int apiCagrisiAlma() {
    return kartDegeri(apiCagrisiKarti);
  }

  /** Benzersiz Kullanıcı kartındaki sayı. */
  public int benzersizKullaniciAlma() {
    return kartDegeri(benzersizKullaniciKarti);
  }

  /**
   * Etiketi verilen kartın sayısı ("174.579" → 174579). Sayı kartın ilk yazısıdır; "+46 bu dönem"
   * ayrı bir kutuda durur. Sayı değilse NumberFormatException ile test kırılır.
   */
  private int kartDegeri(Locator etiket) {
    String deger =
        page.locator(".MuiCard-root")
            .filter(new Locator.FilterOptions().setHas(etiket))
            .locator("p")
            .first()
            .textContent();
    return sayiyaCevir(deger);
  }

  /** Format Dağılımı'ndaki bütün formatların indirme sayılarının toplamı. */
  public int formatToplamiAlma() {
    gorunurMu(csvLegend, DASHBOARD_TIMEOUT_MS);
    int toplam = 0;
    for (String metin : page.locator(".recharts-legend-item-text").allTextContents()) {
      Matcher satir = FORMAT_SATIRI.matcher(metin.trim());
      if (satir.matches()) {
        toplam += sayiyaCevir(satir.group(1));
      }
    }
    return toplam;
  }

  /** Ekrandaki Türkçe biçimli sayıyı çevirir: "174.579" → 174579. */
  private static int sayiyaCevir(String metin) {
    return Integer.parseInt(metin.trim().replace(".", ""));
  }

  // =====================================================================
  // İndirme ve API erişim trendi
  // =====================================================================

  /** İndirme/API trendi başlığı görünüyorsa true döner. */
  public boolean trendBasligiGorunurMu() {
    return gorunurMu(indirmeApiTrendiBasligi, DASHBOARD_TIMEOUT_MS);
  }

  /** Trend grafiğinde verilen dönemi seçer. */
  public void trendDonemiSecme(TrendDonemi donem) {
    trendButonu(donem).click();
  }

  /** Dönem butonu seçili hâle gelene kadar bekler; süre içinde seçilmezse false döner. */
  public boolean trendDonemiSeciliMi(TrendDonemi donem) {
    return gorunurMu(
        trendButonu(donem).and(page.locator("[aria-pressed='true']")), DASHBOARD_TIMEOUT_MS);
  }

  /** Verilen trend döneminin butonunu döndürür. */
  private Locator trendButonu(TrendDonemi donem) {
    return page.getByRole(
        AriaRole.BUTTON, new Page.GetByRoleOptions().setName(donem.butonAdi()).setExact(true));
  }

  /**
   * Başlığın altındaki dönem satırı verilen tarihlerle görünene kadar bekler. Günlük seçimde
   * tarihlerin yanında saat de yazar (03.11.2026 14:05); saat kontrol edilmez.
   */
  public boolean donemSatiriGorunurMu(String baslangic, String bitis) {
    Pattern satir =
        Pattern.compile(
            "^Dönem: "
                + regexKacis(baslangic)
                + "( \\d{2}:\\d{2})? – "
                + regexKacis(bitis)
                + "( \\d{2}:\\d{2})?$");
    return gorunurMu(page.getByText(satir), DASHBOARD_TIMEOUT_MS);
  }

  /**
   * Metindeki regex karakterlerini kaçırır. Pattern.quote kullanılmaz: ürettiği \Q...\E yazımı
   * Playwright'ın tarayıcıda çalıştırdığı JavaScript regex'inde geçersizdir.
   */
  private static String regexKacis(String metin) {
    return metin.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0");
  }

  // =====================================================================
  // En çok indirilen / sorgulanan, kategori dağılımı
  // =====================================================================

  /** En Çok İndirilen başlığı görünüyorsa true döner. */
  public boolean enCokIndirilenBasligiGorunurMu() {
    return gorunurMu(enCokIndirilenBasligi, DASHBOARD_TIMEOUT_MS);
  }

  /** En Çok İndirilen açıklaması görünüyorsa true döner. */
  public boolean enCokIndirilenMetniGorunurMu() {
    return gorunurMu(enCokIndirilenMetni, DASHBOARD_TIMEOUT_MS);
  }

  /** En Çok API Çağrısı başlığı görünüyorsa true döner. */
  public boolean enCokApiBasligiGorunurMu() {
    return gorunurMu(enCokApiBasligi, DASHBOARD_TIMEOUT_MS);
  }

  /** En Çok API Çağrısı açıklaması görünüyorsa true döner. */
  public boolean enCokApiMetniGorunurMu() {
    return gorunurMu(enCokApiMetni, DASHBOARD_TIMEOUT_MS);
  }

  /** Kategori Dağılımı başlığı görünüyorsa true döner. */
  public boolean kategoriBasligiGorunurMu() {
    return gorunurMu(kategoriDagilimiBasligi, DASHBOARD_TIMEOUT_MS);
  }

  /** Kategori Dağılımı açıklaması görünüyorsa true döner. */
  public boolean kategoriMetniGorunurMu() {
    return gorunurMu(kategoriDagilimiAciklamasi, DASHBOARD_TIMEOUT_MS);
  }

  /** En Çok İndirilen listesindeki indirme sayıları, ekrandaki sırayla ("22 indirme" → 22). */
  public List<Integer> enCokIndirilenSayilariAlma() {
    return listeSayilari(enCokIndirilenBasligi);
  }

  /** En Çok API ile Sorgulanan listesindeki çağrı sayıları, ekrandaki sırayla. */
  public List<Integer> enCokApiSayilariAlma() {
    return listeSayilari(enCokApiBasligi);
  }

  /** Başlığın altındaki listede her satırın ikinci yazısındaki sayı; liste yoksa boş liste. */
  private List<Integer> listeSayilari(Locator baslik) {
    Locator satirlar = baslik.locator("xpath=ancestor::*[.//ol][1]//ol/li");
    if (!gorunurMu(satirlar.first(), DASHBOARD_TIMEOUT_MS)) {
      return List.of();
    }
    return satirlar.locator("p:nth-of-type(2)").allTextContents().stream()
        .map(metin -> sayiyaCevir(metin.trim().split(" ")[0]))
        .toList();
  }

  // =====================================================================
  // Ekran görüntüsünde alanları çerçevelemek için locator'lar
  // =====================================================================

  /** Toplam Veri Seti kartı (testte assertThat ile kullanmak için). */
  public Locator toplamVeriSetiKarti() {
    return toplamVeriSetiKarti;
  }

  /** Toplam İndirme kartı. */
  public Locator toplamIndirmeKarti() {
    return toplamIndirmeKarti;
  }

  /** API Çağrısı kartı. */
  public Locator apiCagrisiKarti() {
    return apiCagrisiKarti;
  }

  /** Benzersiz Kullanıcı kartı. */
  public Locator benzersizKullaniciKarti() {
    return benzersizKullaniciKarti;
  }

  /** Format Dağılımı başlığı. */
  public Locator formatDagilimiBasligi() {
    return formatDagilimiBasligi;
  }

  /** İndirme/API trendi başlığı. */
  public Locator indirmeApiTrendiBasligi() {
    return indirmeApiTrendiBasligi;
  }

  /** En Çok İndirilen başlığı. */
  public Locator enCokIndirilenBasligi() {
    return enCokIndirilenBasligi;
  }

  /** En Çok API Çağrısı başlığı. */
  public Locator enCokApiBasligi() {
    return enCokApiBasligi;
  }

  /** Kategori Dağılımı başlığı. */
  public Locator kategoriDagilimiBasligi() {
    return kategoriDagilimiBasligi;
  }
}
