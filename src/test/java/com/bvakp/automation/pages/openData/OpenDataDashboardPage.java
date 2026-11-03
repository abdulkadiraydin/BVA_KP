package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
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

  /**
   * Verilen dönem butonu seçili görünüyorsa true. Seçim durumu, butonun aria-pressed özelliğinden
   * veya Mui-selected sınıfından okunur.
   */
  public boolean trendDonemiSeciliMi(TrendDonemi donem) {

    Locator buton = trendButonu(donem);

    if (!gorunurMu(buton, DASHBOARD_TIMEOUT_MS)) {
      return false;
    }

    String ariaPressed = buton.getAttribute("aria-pressed");
    String sinif = buton.getAttribute("class");

    return "true".equals(ariaPressed) || (sinif != null && sinif.contains("Mui-selected"));
  }

  /** Verilen trend döneminin butonunu döndürür. */
  private Locator trendButonu(TrendDonemi donem) {
    return page.getByRole(
        AriaRole.BUTTON, new Page.GetByRoleOptions().setName(donem.butonAdi()).setExact(true));
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
