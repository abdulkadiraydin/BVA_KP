package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.pages.openData.OpenDataDashboardPage;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;
import java.util.Map;

public class OpenDataDashboardTest extends AvpBaseTest {

    private record TestContext(
            Page page,
            OpenDataDashboardPage dashboardPage) {
    }


    /**
     * Her test için aktif thread'e ait Page ve
     * Dashboard Page Object nesnelerini hazırlamak için kullanılır.
     */
    private TestContext testHazirligi() {

        avpOturumuHazirlama();

        Page aktifPage =
                page();

        OpenDataPage openDataPage =
                new OpenDataPage(
                        aktifPage
                );

        OpenDataDashboardPage dashboardPage =
                new OpenDataDashboardPage(
                        aktifPage
                );


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage.acikVeriPortalinaGitme();


        ReportManager.step(
                "Portal Dashboard sekmesi açılıyor."
        );

        dashboardPage.portalDashboardAcma();


        return new TestContext(
                aktifPage,
                dashboardPage
        );
    }

    /**
     * Belirtilen dashboard alanlarını kırmızı çerçeve ile
     * işaretlemek için kullanılır.
     *
     * Test sonunda TestListener screenshot aldığı için
     * çerçeveler bu metot içerisinde kaldırılmaz.
     *
     * @param alanlar çerçevelenecek alanlar
     */
    private void alanlariCercevele(
            Locator... alanlar) {

        for (Locator alan : alanlar) {

            alan.scrollIntoViewIfNeeded();

            alan.evaluate(
                    """
                    element => {
    
                        let hedef =
                            element.closest('.MuiCard-root, .MuiPaper-root');
    
                        if (!hedef) {
    
                            let parent =
                                element.parentElement;
    
                            while (parent) {
    
                                const rect =
                                    parent.getBoundingClientRect();
    
                                if (rect.width >= 180
                                        && rect.height >= 80) {
    
                                    hedef = parent;
                                    break;
                                }
    
                                parent =
                                    parent.parentElement;
                            }
                        }
    
                        if (hedef) {
    
                            hedef.style.outline =
                                '5px solid red';
    
                            hedef.style.outlineOffset =
                                '-5px';
                        }
                    }
                    """
            );
        }
    }


    /**
     * Portal Dashboard ekranındaki istatistik kartlarının
     * ayrı ayrı görüntülendiğini doğrulamak için kullanılır.
     */
    @Test(priority = 1)
    public void portalIstatistikleriniGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();


        ReportManager.step(
                "Toplam Veri Seti kartı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.toplamVeriSetiKartiGorunurMu(),
                "Toplam Veri Seti kartı görüntülenemedi."
        );

        ReportManager.info(
                "Toplam Veri Seti kartı başarıyla görüntülendi."
        );


        ReportManager.step(
                "Toplam İndirme kartı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.toplamIndirmeKartiGorunurMu(),
                "Toplam İndirme kartı görüntülenemedi."
        );

        ReportManager.info(
                "Toplam İndirme kartı başarıyla görüntülendi."
        );


        ReportManager.step(
                "API Çağrısı kartı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.apiCagrisiKartiGorunurMu(),
                "API Çağrısı kartı görüntülenemedi."
        );

        ReportManager.info(
                "API Çağrısı kartı başarıyla görüntülendi."
        );


        ReportManager.step(
                "Benzersiz Kullanıcı kartı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.benzersizKullaniciKartiGorunurMu(),
                "Benzersiz Kullanıcı kartı görüntülenemedi."
        );

        ReportManager.info(
                "Benzersiz Kullanıcı kartı başarıyla görüntülendi."
        );

        ReportManager.step(
                "Doğrulanan istatistik kartları kırmızı çerçeve ile işaretleniyor."
        );

        alanlariCercevele(
                dashboardPage.toplamVeriSetiKarti(),
                dashboardPage.toplamIndirmeKarti(),
                dashboardPage.apiCagrisiKarti(),
                dashboardPage.benzersizKullaniciKarti()
        );

        ReportManager.info(
                "PORTAL İSTATİSTİK KARTLARININ TAMAMI BAŞARIYLA DOĞRULANDI"
        );
    }
    /**
     * Portal Dashboard ekranındaki Format Dağılımı
     * alanının doğru görüntülendiğini doğrulamak için kullanılır.
     */
    @Test(priority = 2)
    public void formatDagiliminiGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();


        ReportManager.step(
                "Format Dağılımı başlığı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.formatDagilimiBasligiGorunurMu(),
                "Format Dağılımı başlığı görüntülenemedi."
        );

        ReportManager.info(
                "Format Dağılımı başlığı başarıyla görüntülendi."
        );


        ReportManager.step(
                "Format Dağılımı grafik alanı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.formatDagilimiGrafikGorunurMu(),
                "Format Dağılımı grafik alanı görüntülenemedi."
        );

        ReportManager.info(
                "Format Dağılımı grafik alanı başarıyla görüntülendi."
        );


        ReportManager.step(
                "Format Dağılımındaki CSV, TSV ve XLSX bilgileri kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.formatDagilimiLegendleriGorunurMu(),
                "Format Dağılımındaki format bilgileri görüntülenemedi."
        );

        ReportManager.info(
                "CSV, TSV ve XLSX format bilgileri başarıyla görüntülendi."
        );


        ReportManager.step(
                "Format Dağılımı alanı kırmızı çerçeve ile işaretleniyor."
        );


        alanlariCercevele(
                dashboardPage.formatDagilimiBasligi()
        );

        ReportManager.info(
                "FORMAT DAĞILIMI ALANI BAŞARIYLA DOĞRULANDI"
        );
    }
    @Test(priority = 3)
    public void indirmeVeApiErisimTrendiniGunlukGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();

        ReportManager.step(
                "Günlük trend görünümü seçiliyor."
        );

        dashboardPage.gunlukTrendSecme();

        ReportManager.info(
                "Günlük trend görünümü başarıyla seçildi."
        );

        alanlariCercevele(
                dashboardPage.indirmeApiTrendiAlani()
        );
    }
    @Test(priority = 4)
    public void indirmeVeApiErisimTrendiniHaftalikGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();

        ReportManager.step(
                "Haftalık trend görünümü seçiliyor."
        );

        dashboardPage.haftalikTrendSecme();

        ReportManager.info(
                "Haftalık trend görünümü başarıyla seçildi."
        );

        alanlariCercevele(
                dashboardPage.indirmeApiTrendiAlani()
        );
    }
    @Test(priority = 5)
    public void indirmeVeApiErisimTrendiniAylikGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();

        ReportManager.step(
                "Aylık trend görünümü seçiliyor."
        );

        dashboardPage.aylikTrendSecme();

        ReportManager.info(
                "Aylık trend görünümü başarıyla seçildi."
        );

        alanlariCercevele(
                dashboardPage.indirmeApiTrendiAlani()
        );
    }
    /**
     * Portal Dashboard ekranındaki En Çok İndirilen Veri Setleri
     * alanının görüntülendiğini doğrulamak için kullanılır.
     */
    @Test(priority = 6)
    public void enCokIndirilenVeriSetleriniGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();


        ReportManager.step(
                "En Çok İndirilen Veri Setleri başlığı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.enCokIndirilenVeriSetleriBasligiGorunurMu(),
                "En Çok İndirilen Veri Setleri başlığı görüntülenemedi."
        );

        ReportManager.info(
                "En Çok İndirilen Veri Setleri başlığı başarıyla görüntülendi."
        );


        ReportManager.step(
                "En Çok İndirilen Veri Setleri açıklaması kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.enCokIndirilenVeriSetleriAciklamasiGorunurMu(),
                "En Çok İndirilen Veri Setleri açıklaması görüntülenemedi."
        );

        ReportManager.info(
                "En Çok İndirilen Veri Setleri açıklaması başarıyla görüntülendi."
        );


        ReportManager.step(
                "En Çok İndirilen Veri Setleri alanı kırmızı çerçeve ile işaretleniyor."
        );

        alanlariCercevele(
                dashboardPage.enCokIndirilenVeriSetleriBasligi()
        );


        ReportManager.info(
                "EN ÇOK İNDİRİLEN VERİ SETLERİ ALANI BAŞARIYLA DOĞRULANDI"
        );
    }
    /**
     * Portal Dashboard ekranındaki En Çok API ile Sorgulanan Veri Setleri
     * alanının görüntülendiğini doğrulamak için kullanılır.
     */
    @Test(priority = 7)
    public void enCokApiIleSorgulananVeriSetleriniGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();


        ReportManager.step(
                "En Çok API ile Sorgulanan Veri Setleri başlığı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.enCokApiIleSorgulananVeriSetleriBasligiGorunurMu(),
                "En Çok API ile Sorgulanan Veri Setleri başlığı görüntülenemedi."
        );

        ReportManager.info(
                "En Çok API ile Sorgulanan Veri Setleri başlığı başarıyla görüntülendi."
        );


        ReportManager.step(
                "En Çok API ile Sorgulanan Veri Setleri açıklaması kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.enCokApiIleSorgulananVeriSetleriAciklamasiGorunurMu(),
                "En Çok API ile Sorgulanan Veri Setleri açıklaması görüntülenemedi."
        );

        ReportManager.info(
                "En Çok API ile Sorgulanan Veri Setleri açıklaması başarıyla görüntülendi."
        );


        alanlariCercevele(
                dashboardPage.enCokApiIleSorgulananVeriSetleriBasligi()
        );


        ReportManager.info(
                "EN ÇOK API İLE SORGULANAN VERİ SETLERİ ALANI BAŞARIYLA DOĞRULANDI"
        );
    }
    /**
     * Portal Dashboard ekranındaki Kategori Dağılımı
     * alanının görüntülendiğini doğrulamak için kullanılır.
     */
    @Test(priority = 8)
    public void kategoriDagiliminiGoruntuleme() {

        TestContext context =
                testHazirligi();

        OpenDataDashboardPage dashboardPage =
                context.dashboardPage();


        ReportManager.step(
                "Kategori Dağılımı başlığı kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.kategoriDagilimiBasligiGorunurMu(),
                "Kategori Dağılımı başlığı görüntülenemedi."
        );

        ReportManager.info(
                "Kategori Dağılımı başlığı başarıyla görüntülendi."
        );


        ReportManager.step(
                "Kategori Dağılımı açıklaması kontrol ediliyor."
        );

        Assert.assertTrue(
                dashboardPage.kategoriDagilimiAciklamasiGorunurMu(),
                "Kategori Dağılımı açıklaması görüntülenemedi."
        );

        ReportManager.info(
                "Kategori Dağılımı açıklaması başarıyla görüntülendi."
        );


        ReportManager.step(
                "Kategori Dağılımı alanı kırmızı çerçeve ile işaretleniyor."
        );

        alanlariCercevele(
                dashboardPage.kategoriDagilimiBasligi()
        );


        ReportManager.info(
                "KATEGORİ DAĞILIMI ALANI BAŞARIYLA DOĞRULANDI"
        );
    }
}