package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/**
 * Açık Veri Portalı "Portal Dashboard" sekmesi: istatistik kartları, format dağılımı,
 * indirme/API trendi, en çok indirilen/sorgulanan veri setleri ve kategori dağılımı.
 *
 * <p>Kartlar ve grafikler sekme açıldıktan sonra sunucudan yüklenir; bütün görünürlük
 * kontrolleri bu yüzden bekleyerek yapılır.
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

    private final Locator enCokIndirilenVeriSetleriBasligi;
    private final Locator enCokIndirilenVeriSetleriAciklamasi;
    private final Locator enCokApiIleSorgulananVeriSetleriBasligi;
    private final Locator enCokApiIleSorgulananVeriSetleriAciklamasi;
    private final Locator kategoriDagilimiBasligi;
    private final Locator kategoriDagilimiAciklamasi;

    public OpenDataDashboardPage(Page page) {

        super(page);

        portalDashboardSekmesi = page.getByRole(AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Portal Dashboard").setExact(true));
        portalIstatistikleriBasligi = tamMetin("Portal İstatistikleri ve Raporlama");

        toplamVeriSetiKarti = tamMetin("Yayındaki Veri Seti");
        toplamIndirmeKarti = tamMetin("Toplam İndirme");
        // "API Çağrısı" metni grafik legend'ında da geçtiği için kart etiketi (p) ayrıca hedeflenir.
        apiCagrisiKarti = page.locator("p").filter(new Locator.FilterOptions()
                .setHasText(Pattern.compile("^API Çağrısı$")));
        benzersizKullaniciKarti = tamMetin("Benzersiz Kullanıcı");

        formatDagilimiBasligi = tamMetin("Format Dağılımı");
        formatDagilimiGrafikAlani = page.locator("svg").filter(new Locator.FilterOptions().setHasText("İNDİRME"));
        csvLegend = page.getByText("CSV:");
        tsvLegend = page.getByText("TSV:");
        xlsxLegend = page.getByText("XLSX:");

        indirmeApiTrendiBasligi = tamMetin("İndirme ve API Erişim Trendi");

        enCokIndirilenVeriSetleriBasligi = tamMetin("En Çok İndirilen Veri Setleri");
        enCokIndirilenVeriSetleriAciklamasi = tamMetin("Dönem içinde en çok indirilen veri setleri");
        enCokApiIleSorgulananVeriSetleriBasligi = tamMetin("En Çok API ile Sorgulanan Veri Setleri");
        enCokApiIleSorgulananVeriSetleriAciklamasi = tamMetin("Dönem içinde API ile en çok sorgulanan veri setleri");
        kategoriDagilimiBasligi = tamMetin("Kategori Dağılımı");
        kategoriDagilimiAciklamasi = tamMetin("Yayınlanmış veri setlerinin kategorilere göre dağılımı");
    }

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

    public boolean toplamVeriSetiKartiGorunurMu() {
        return gorunurMu(toplamVeriSetiKarti, DASHBOARD_TIMEOUT_MS);
    }

    public boolean toplamIndirmeKartiGorunurMu() {
        return gorunurMu(toplamIndirmeKarti, DASHBOARD_TIMEOUT_MS);
    }

    public boolean apiCagrisiKartiGorunurMu() {
        return gorunurMu(apiCagrisiKarti, DASHBOARD_TIMEOUT_MS);
    }

    public boolean benzersizKullaniciKartiGorunurMu() {
        return gorunurMu(benzersizKullaniciKarti, DASHBOARD_TIMEOUT_MS);
    }

    // =====================================================================
    // Format dağılımı
    // =====================================================================

    public boolean formatDagilimiBasligiGorunurMu() {
        return gorunurMu(formatDagilimiBasligi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean formatDagilimiGrafikGorunurMu() {
        return gorunurMu(formatDagilimiGrafikAlani, DASHBOARD_TIMEOUT_MS);
    }

    /**
     * CSV, TSV ve XLSX legend'larının üçü de görünüyorsa true.
     * Not: legend yalnızca indirmesi olan formatları gösteriyorsa sonuç test verisine bağlıdır.
     */
    public boolean formatDagilimiLegendleriGorunurMu() {
        return gorunurMu(csvLegend, DASHBOARD_TIMEOUT_MS)
                && tsvLegend.isVisible()
                && xlsxLegend.isVisible();
    }

    // =====================================================================
    // İndirme ve API erişim trendi
    // =====================================================================

    public boolean indirmeApiTrendiBasligiGorunurMu() {
        return gorunurMu(indirmeApiTrendiBasligi, DASHBOARD_TIMEOUT_MS);
    }

    public void trendDonemiSecme(TrendDonemi donem) {
        trendButonu(donem).click();
    }

    /**
     * Verilen dönem butonu seçili görünüyorsa true.
     * Seçim durumu, butonun aria-pressed özelliğinden veya Mui-selected sınıfından okunur.
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

    private Locator trendButonu(TrendDonemi donem) {
        return page.getByRole(AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName(donem.butonAdi()).setExact(true));
    }

    // =====================================================================
    // En çok indirilen / sorgulanan, kategori dağılımı
    // =====================================================================

    public boolean enCokIndirilenVeriSetleriBasligiGorunurMu() {
        return gorunurMu(enCokIndirilenVeriSetleriBasligi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean enCokIndirilenVeriSetleriAciklamasiGorunurMu() {
        return gorunurMu(enCokIndirilenVeriSetleriAciklamasi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean enCokApiIleSorgulananVeriSetleriBasligiGorunurMu() {
        return gorunurMu(enCokApiIleSorgulananVeriSetleriBasligi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean enCokApiIleSorgulananVeriSetleriAciklamasiGorunurMu() {
        return gorunurMu(enCokApiIleSorgulananVeriSetleriAciklamasi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean kategoriDagilimiBasligiGorunurMu() {
        return gorunurMu(kategoriDagilimiBasligi, DASHBOARD_TIMEOUT_MS);
    }

    public boolean kategoriDagilimiAciklamasiGorunurMu() {
        return gorunurMu(kategoriDagilimiAciklamasi, DASHBOARD_TIMEOUT_MS);
    }

    // =====================================================================
    // Ekran görüntüsünde alanları çerçevelemek için locator'lar
    // =====================================================================

    public Locator toplamVeriSetiKarti() {
        return toplamVeriSetiKarti;
    }

    public Locator toplamIndirmeKarti() {
        return toplamIndirmeKarti;
    }

    public Locator apiCagrisiKarti() {
        return apiCagrisiKarti;
    }

    public Locator benzersizKullaniciKarti() {
        return benzersizKullaniciKarti;
    }

    public Locator formatDagilimiBasligi() {
        return formatDagilimiBasligi;
    }

    public Locator indirmeApiTrendiBasligi() {
        return indirmeApiTrendiBasligi;
    }

    public Locator enCokIndirilenVeriSetleriBasligi() {
        return enCokIndirilenVeriSetleriBasligi;
    }

    public Locator enCokApiIleSorgulananVeriSetleriBasligi() {
        return enCokApiIleSorgulananVeriSetleriBasligi;
    }

    public Locator kategoriDagilimiBasligi() {
        return kategoriDagilimiBasligi;
    }
}