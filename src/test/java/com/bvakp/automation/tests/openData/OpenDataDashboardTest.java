package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.pages.openData.OpenDataDashboardPage;
import com.bvakp.automation.pages.openData.OpenDataDashboardPage.TrendDonemi;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Açık Veri Portalı "Portal Dashboard" sekmesi testleri.
 *
 * <p>Doğrulanan alanlar, test sonunda alınan ekran görüntüsünde görünmeleri için
 * kırmızı çerçeveyle işaretlenir.
 */
public class OpenDataDashboardTest extends AvpBaseTest {

    /** Oturumu hazırlar ve Portal Dashboard sekmesini açar. */
    private OpenDataDashboardPage dashboardAc() {

        avpOturumuHazirlama();

        ReportManager.step("Açık Veri Portalı → Portal Dashboard açılıyor.");
        new OpenDataPage(page()).acikVeriPortalinaGitme();

        OpenDataDashboardPage dashboardPage = new OpenDataDashboardPage(page());
        dashboardPage.portalDashboardAcma();

        return dashboardPage;
    }

    /** Toplam Veri Seti, Toplam İndirme, API Çağrısı ve Benzersiz Kullanıcı kartlarını doğrular. */
    @Test
    public void portalIstatistikleriniGoruntuleme() {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        ReportManager.step("İstatistik kartları doğrulanıyor.");
        Assert.assertTrue(dashboardPage.toplamVeriSetiKartiGorunurMu(), "Toplam Veri Seti kartı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.toplamIndirmeKartiGorunurMu(), "Toplam İndirme kartı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.apiCagrisiKartiGorunurMu(), "API Çağrısı kartı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.benzersizKullaniciKartiGorunurMu(), "Benzersiz Kullanıcı kartı görüntülenemedi.");

        alanlariCercevele(
                dashboardPage.toplamVeriSetiKarti(),
                dashboardPage.toplamIndirmeKarti(),
                dashboardPage.apiCagrisiKarti(),
                dashboardPage.benzersizKullaniciKarti());

        ReportManager.info("PORTAL İSTATİSTİK KARTLARI DOĞRULANDI");
    }

    /** Format Dağılımı başlığını, grafiğini ve CSV/TSV/XLSX bilgilerini doğrular. */
    @Test
    public void formatDagiliminiGoruntuleme() {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        ReportManager.step("Format Dağılımı alanı doğrulanıyor.");
        Assert.assertTrue(dashboardPage.formatDagilimiBasligiGorunurMu(), "Format Dağılımı başlığı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.formatDagilimiGrafikGorunurMu(), "Format Dağılımı grafik alanı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.formatDagilimiLegendleriGorunurMu(),
                "Format Dağılımındaki CSV, TSV ve XLSX bilgileri görüntülenemedi.");

        alanlariCercevele(dashboardPage.formatDagilimiBasligi());

        ReportManager.info("FORMAT DAĞILIMI ALANI DOĞRULANDI");
    }

    @DataProvider(name = "trendDonemleri")
    public Object[][] trendDonemleri() {
        return new Object[][]{
                {TrendDonemi.GUNLUK},
                {TrendDonemi.HAFTALIK},
                {TrendDonemi.AYLIK}
        };
    }

    /** İndirme ve API Erişim Trendi'nde seçilen dönemin aktif olduğunu doğrular. */
    @Test(dataProvider = "trendDonemleri")
    public void indirmeVeApiErisimTrendiDonemSecimi(TrendDonemi donem) {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        Assert.assertTrue(dashboardPage.indirmeApiTrendiBasligiGorunurMu(),
                "İndirme ve API Erişim Trendi alanı görüntülenemedi.");

        ReportManager.step(donem.butonAdi() + " trend görünümü seçiliyor.");
        dashboardPage.trendDonemiSecme(donem);

        Assert.assertTrue(dashboardPage.trendDonemiSeciliMi(donem),
                donem.butonAdi() + " seçildikten sonra buton aktif görünmüyor.");

        alanlariCercevele(dashboardPage.indirmeApiTrendiBasligi());

        ReportManager.info("TREND DÖNEM SEÇİMİ DOĞRULANDI | " + donem.butonAdi());
    }

    /** En Çok İndirilen Veri Setleri başlığını ve açıklamasını doğrular. */
    @Test
    public void enCokIndirilenVeriSetleriniGoruntuleme() {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        ReportManager.step("En Çok İndirilen Veri Setleri alanı doğrulanıyor.");
        Assert.assertTrue(dashboardPage.enCokIndirilenVeriSetleriBasligiGorunurMu(),
                "En Çok İndirilen Veri Setleri başlığı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.enCokIndirilenVeriSetleriAciklamasiGorunurMu(),
                "En Çok İndirilen Veri Setleri açıklaması görüntülenemedi.");

        alanlariCercevele(dashboardPage.enCokIndirilenVeriSetleriBasligi());

        ReportManager.info("EN ÇOK İNDİRİLEN VERİ SETLERİ ALANI DOĞRULANDI");
    }

    /** En Çok API ile Sorgulanan Veri Setleri başlığını ve açıklamasını doğrular. */
    @Test
    public void enCokApiIleSorgulananVeriSetleriniGoruntuleme() {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        ReportManager.step("En Çok API ile Sorgulanan Veri Setleri alanı doğrulanıyor.");
        Assert.assertTrue(dashboardPage.enCokApiIleSorgulananVeriSetleriBasligiGorunurMu(),
                "En Çok API ile Sorgulanan Veri Setleri başlığı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.enCokApiIleSorgulananVeriSetleriAciklamasiGorunurMu(),
                "En Çok API ile Sorgulanan Veri Setleri açıklaması görüntülenemedi.");

        alanlariCercevele(dashboardPage.enCokApiIleSorgulananVeriSetleriBasligi());

        ReportManager.info("EN ÇOK API İLE SORGULANAN VERİ SETLERİ ALANI DOĞRULANDI");
    }

    /** Kategori Dağılımı başlığını ve açıklamasını doğrular. */
    @Test
    public void kategoriDagiliminiGoruntuleme() {

        OpenDataDashboardPage dashboardPage = dashboardAc();

        ReportManager.step("Kategori Dağılımı alanı doğrulanıyor.");
        Assert.assertTrue(dashboardPage.kategoriDagilimiBasligiGorunurMu(),
                "Kategori Dağılımı başlığı görüntülenemedi.");
        Assert.assertTrue(dashboardPage.kategoriDagilimiAciklamasiGorunurMu(),
                "Kategori Dağılımı açıklaması görüntülenemedi.");

        alanlariCercevele(dashboardPage.kategoriDagilimiBasligi());

        ReportManager.info("KATEGORİ DAĞILIMI ALANI DOĞRULANDI");
    }

    /**
     * Verilen alanların bulunduğu kartı kırmızı çerçeveyle işaretler.
     *
     * <p>Çerçeveler kaldırılmaz: test sonunda TestListener'ın aldığı ekran görüntüsünde
     * doğrulanan alanların görünmesi amaçlanır. Çerçeveleme başarısız olursa test etkilenmez.
     */
    private void alanlariCercevele(Locator... alanlar) {

        for (Locator alan : alanlar) {
            try {
                alan.first().scrollIntoViewIfNeeded();
                alan.first().evaluate("""
                        element => {
                            let hedef = element.closest('.MuiCard-root, .MuiPaper-root');
                            if (!hedef) {
                                let parent = element.parentElement;
                                while (parent) {
                                    const rect = parent.getBoundingClientRect();
                                    if (rect.width >= 180 && rect.height >= 80) {
                                        hedef = parent;
                                        break;
                                    }
                                    parent = parent.parentElement;
                                }
                            }
                            if (hedef) {
                                hedef.style.outline = '5px solid red';
                                hedef.style.outlineOffset = '-5px';
                            }
                        }
                        """);
            } catch (RuntimeException e) {
                ReportManager.warn("Alan çerçevelenemedi: " + e.getMessage());
            }
        }
    }
}