package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.auth.PortalRol;
import com.bvakp.automation.pages.openData.OpenDataDashboardPage;
import com.bvakp.automation.pages.openData.OpenDataDashboardPage.TrendDonemi;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Açık Veri Portalı "Portal Dashboard" sekmesi testleri.
 *
 * <p>Doğrulanan alanlar, test sonunda alınan ekran görüntüsünde görünmeleri için kırmızı çerçeveyle
 * işaretlenir.
 */
public class OpenDataDashboardTest extends AvpBaseTest {

  private static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  /** Oturumu hazırlar ve Portal Dashboard sekmesini açar. */
  private OpenDataDashboardPage dashboardAc() {

    avpOturumuHazirlama(PortalRol.HAZIRLAYAN);

    ReportManager.step("Açık Veri Portalı → Portal Dashboard açılıyor.");
    new OpenDataPage(page()).acikVeriPortalinaGitme();

    OpenDataDashboardPage dashboardPage = new OpenDataDashboardPage(page());
    dashboardPage.portalDashboardAcma();

    return dashboardPage;
  }

  /**
   * Dört istatistik kartının görüntülendiğini, değerlerinin sayı olduğunu ve Yayındaki Veri Seti
   * değerinin en az 1 olduğunu doğrular.
   */
  @Test
  public void istatistikKartlariGoruntuleme() {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step("İstatistik kartları doğrulanıyor.");
    Assert.assertTrue(
        dashboardPage.toplamVeriSetiKartiGorunurMu(), "Toplam Veri Seti kartı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.toplamIndirmeKartiGorunurMu(), "Toplam İndirme kartı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.apiCagrisiKartiGorunurMu(), "API Çağrısı kartı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.tekilKullaniciKartiGorunurMu(), "Benzersiz Kullanıcı kartı görüntülenemedi.");
    int yayindakiVeriSeti = dashboardPage.yayindakiVeriSetiAlma();
    int toplamIndirme = dashboardPage.toplamIndirmeAlma();
    int apiCagrisi = dashboardPage.apiCagrisiAlma();
    int benzersizKullanici = dashboardPage.benzersizKullaniciAlma();
    ReportManager.info(
        "Kartlar | Yayındaki: "
            + yayindakiVeriSeti
            + " | İndirme: "
            + toplamIndirme
            + " | API: "
            + apiCagrisi
            + " | Kullanıcı: "
            + benzersizKullanici);

    // Ortamda otomasyonun yayınladığı veri setleri olduğu için en az 1 olmalı.
    Assert.assertTrue(
        yayindakiVeriSeti >= 1, "Yayındaki Veri Seti 0 görünüyor: " + yayindakiVeriSeti);
    alanlariCercevele(
        dashboardPage.toplamVeriSetiKarti(),
        dashboardPage.toplamIndirmeKarti(),
        dashboardPage.apiCagrisiKarti(),
        dashboardPage.benzersizKullaniciKarti());

    ReportManager.info("PORTAL İSTATİSTİK KARTLARI DOĞRULANDI");
  }

  /**
   * Format Dağılımı başlığını, grafiğini ve CSV/TSV/XLSX bilgilerini doğrular; formatların indirme
   * sayılarının toplamının Toplam İndirme kartıyla aynı olduğunu kontrol eder.
   */
  @Test
  public void formatDagiliminiGoruntuleme() {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step("Format Dağılımı alanı doğrulanıyor.");
    Assert.assertTrue(
        dashboardPage.formatDagilimiBasligiGorunurMu(), "Format Dağılımı başlığı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.formatDagilimiGrafikGorunurMu(),
        "Format Dağılımı grafik alanı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.formatLegendleriGorunurMu(),
        "Format Dağılımındaki CSV, TSV ve XLSX bilgileri görüntülenemedi.");
    int toplamIndirme = dashboardPage.toplamIndirmeAlma();
    int formatToplami = dashboardPage.formatToplamiAlma();
    ReportManager.info("Toplam İndirme: " + toplamIndirme + " | Format toplamı: " + formatToplami);
    Assert.assertEquals(
        formatToplami,
        toplamIndirme,
        "Format Dağılımındaki indirmelerin toplamı Toplam İndirme kartıyla aynı değil.");
    alanlariCercevele(dashboardPage.formatDagilimiBasligi());

    ReportManager.info("FORMAT DAĞILIMI ALANI DOĞRULANDI");
  }

  /** Dönem seçimi testinin çalışacağı dönemler. */
  @DataProvider(name = "donemler")
  public Object[][] donemler() {
    return new Object[][] {{TrendDonemi.GUNLUK}, {TrendDonemi.HAFTALIK}, {TrendDonemi.AYLIK}};
  }

  /**
   * Seçilen dönemin sayfaya uygulandığını doğrular: buton seçili olur ve başlığın altındaki dönem
   * satırı seçilen döneme göre değişir (ör. Haftalık: 29.10.2026 – 04.11.2026).
   */
  @Test(dataProvider = "donemler")
  public void donemSecimi(TrendDonemi donem) {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step(donem + " dönemi seçiliyor.");
    dashboardPage.trendDonemiSecme(donem);
    Assert.assertTrue(
        dashboardPage.trendDonemiSeciliMi(donem), donem + " seçildikten sonra buton aktif değil.");

    String bitis = TARIH.format(LocalDate.now());
    String baslangic = TARIH.format(LocalDate.now().minusDays(geriGunSayisi(donem)));

    ReportManager.step("Dönem satırı doğrulanıyor: " + baslangic + " – " + bitis);
    Assert.assertTrue(
        dashboardPage.donemSatiriGorunurMu(baslangic, bitis),
        donem + ": dönem satırı " + baslangic + " – " + bitis + " olmalıydı.");

    alanlariCercevele(dashboardPage.indirmeApiTrendiBasligi());

    ReportManager.info("DÖNEM SEÇİMİ DOĞRULANDI | " + donem);
  }

  /** Dönemin başlangıcının bugünden kaç gün önce olduğu (ekranda gözlenen davranış). */
  private static int geriGunSayisi(TrendDonemi donem) {
    return switch (donem) {
      case GUNLUK -> 1; // son 24 saat
      case HAFTALIK -> 6; // bugün dahil 7 gün
      case AYLIK -> 29; // bugün dahil 30 gün
    };
  }

  /** En Çok İndirilen Veri Setleri başlığını ve açıklamasını doğrular. */
  @Test
  public void enCokIndirilenlerGoruntuleme() {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step("En Çok İndirilen Veri Setleri alanı doğrulanıyor.");
    Assert.assertTrue(
        dashboardPage.enCokIndirilenBasligiGorunurMu(),
        "En Çok İndirilen Veri Setleri başlığı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.enCokIndirilenMetniGorunurMu(),
        "En Çok İndirilen Veri Setleri açıklaması görüntülenemedi.");
    List<Integer> indirmeler = dashboardPage.enCokIndirilenSayilariAlma();
    ReportManager.info("En çok indirilenler: " + indirmeler);
    Assert.assertFalse(indirmeler.isEmpty(), "En Çok İndirilen listesi boş.");
    Assert.assertTrue(
        buyuktenKucugeMu(indirmeler),
        "En Çok İndirilen listesi büyükten küçüğe sıralı değil: " + indirmeler);

    alanlariCercevele(dashboardPage.enCokIndirilenBasligi());

    ReportManager.info("EN ÇOK İNDİRİLEN VERİ SETLERİ ALANI DOĞRULANDI");
  }

  /** En Çok API ile Sorgulanan Veri Setleri başlığını ve açıklamasını doğrular. */
  @Test
  public void enCokSorgulananlarGoruntuleme() {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step("En Çok API ile Sorgulanan Veri Setleri alanı doğrulanıyor.");
    Assert.assertTrue(
        dashboardPage.enCokApiBasligiGorunurMu(),
        "En Çok API ile Sorgulanan Veri Setleri başlığı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.enCokApiMetniGorunurMu(),
        "En Çok API ile Sorgulanan Veri Setleri açıklaması görüntülenemedi.");
    List<Integer> cagrilar = dashboardPage.enCokApiSayilariAlma();
    ReportManager.info("En çok API ile sorgulananlar: " + cagrilar);
    Assert.assertFalse(cagrilar.isEmpty(), "En Çok API ile Sorgulanan listesi boş.");
    Assert.assertTrue(
        buyuktenKucugeMu(cagrilar),
        "En Çok API ile Sorgulanan listesi büyükten küçüğe sıralı değil: " + cagrilar);
    alanlariCercevele(dashboardPage.enCokApiBasligi());

    ReportManager.info("EN ÇOK API İLE SORGULANAN VERİ SETLERİ ALANI DOĞRULANDI");
  }

  /** Kategori Dağılımı başlığını ve açıklamasını doğrular. */
  @Test
  public void kategoriDagiliminiGoruntuleme() {

    OpenDataDashboardPage dashboardPage = dashboardAc();

    ReportManager.step("Kategori Dağılımı alanı doğrulanıyor.");
    Assert.assertTrue(
        dashboardPage.kategoriBasligiGorunurMu(), "Kategori Dağılımı başlığı görüntülenemedi.");
    Assert.assertTrue(
        dashboardPage.kategoriMetniGorunurMu(), "Kategori Dağılımı açıklaması görüntülenemedi.");

    alanlariCercevele(dashboardPage.kategoriDagilimiBasligi());

    ReportManager.info("KATEGORİ DAĞILIMI ALANI DOĞRULANDI");
  }

  /** Sayılar büyükten küçüğe sıralıysa true; eşit sayılar art arda gelebilir. */
  private static boolean buyuktenKucugeMu(List<Integer> sayilar) {
    for (int i = 1; i < sayilar.size(); i++) {
      if (sayilar.get(i) > sayilar.get(i - 1)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Verilen alanların bulunduğu kartı kırmızı çerçeveyle işaretler.
   *
   * <p>Çerçeveler kaldırılmaz: test sonunda TestListener'ın aldığı ekran görüntüsünde doğrulanan
   * alanların görünmesi amaçlanır. Çerçeveleme başarısız olursa test etkilenmez.
   */
  private void alanlariCercevele(Locator... alanlar) {

    for (Locator alan : alanlar) {
      try {
        alan.first().scrollIntoViewIfNeeded();
        alan.first()
            .evaluate(
                """
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
