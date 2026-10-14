package com.bvakp.automation.pages.openData;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.regex.Pattern;

public class OpenDataDashboardPage {

    private final Page page;

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
    private final Locator indirmeApiTrendiAlani;

    private final Locator gunlukButonu;
    private final Locator haftalikButonu;
    private final Locator aylikButonu;
    private final Locator enCokIndirilenVeriSetleriBasligi;
    private final Locator enCokIndirilenVeriSetleriAciklamasi;
    private final Locator enCokApiIleSorgulananVeriSetleriBasligi;
    private final Locator enCokApiIleSorgulananVeriSetleriAciklamasi;
    private final Locator kategoriDagilimiBasligi;
    private final Locator kategoriDagilimiAciklamasi;
    /**
     * Portal Dashboard ekranındaki bileşenleri
     * yönetmek ve doğrulamak için kullanılır.
     */
    public OpenDataDashboardPage(Page page) {

        this.page = page;

        portalDashboardSekmesi =
                page.getByRole(
                        AriaRole.TAB,
                        new Page.GetByRoleOptions()
                                .setName("Portal Dashboard")
                                .setExact(true)
                );

        portalIstatistikleriBasligi =
                page.getByText(
                        "Portal İstatistikleri ve Raporlama",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        toplamVeriSetiKarti =
                page.getByText(
                        "Toplam Veri Seti",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        toplamIndirmeKarti =
                page.getByText(
                        "Toplam İndirme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        apiCagrisiKarti =
                page.locator("p")
                        .filter(
                                new Locator.FilterOptions()
                                        .setHasText(
                                                Pattern.compile(
                                                        "^API Çağrısı$"
                                                )
                                        )
                        );

        benzersizKullaniciKarti =
                page.getByText(
                        "Benzersiz Kullanıcı",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        formatDagilimiBasligi =
                page.getByText(
                        "Format Dağılımı",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        formatDagilimiGrafikAlani =
                page.locator("svg")
                        .filter(
                                new Locator.FilterOptions()
                                        .setHasText("İNDİRME")
                        );

        csvLegend =
                page.getByText(
                        "CSV:",
                        new Page.GetByTextOptions()
                                .setExact(false)
                );

        tsvLegend =
                page.getByText(
                        "TSV:",
                        new Page.GetByTextOptions()
                                .setExact(false)
                );

        xlsxLegend =
                page.getByText(
                        "XLSX:",
                        new Page.GetByTextOptions()
                                .setExact(false)
                );
        indirmeApiTrendiBasligi =
                page.getByText(
                        "İndirme ve API Erişim Trendi",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        indirmeApiTrendiAlani =
                page.getByText(
                        "İndirme ve API Erişim TrendiAylık indirme ve API çağrısı sayıları",
                        new Page.GetByTextOptions()
                                .setExact(false)
                );

        gunlukButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Günlük")
                                .setExact(true)
                );

        haftalikButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Haftalık")
                                .setExact(true)
                );

        aylikButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Aylık")
                                .setExact(true)
                );
        enCokIndirilenVeriSetleriBasligi =
                page.getByText(
                        "En Çok İndirilen Veri Setleri",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        enCokIndirilenVeriSetleriAciklamasi =
                page.getByText(
                        "Dönem içinde en çok indirilen veri setleri",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );
        enCokApiIleSorgulananVeriSetleriBasligi =
                page.getByText(
                        "En Çok API ile Sorgulanan Veri Setleri",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        enCokApiIleSorgulananVeriSetleriAciklamasi =
                page.getByText(
                        "Dönem içinde API ile en çok sorgulanan veri setleri",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );
        kategoriDagilimiBasligi =
                page.getByText(
                        "Kategori Dağılımı",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        kategoriDagilimiAciklamasi =
                page.getByText(
                        "Yayınlanmış veri setlerinin kategorilere göre dağılımı",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );
    }


    /**
     * Portal Dashboard sekmesine geçiş yapar
     * ve dashboard içeriğinin açılmasını bekler.
     */
    public void portalDashboardAcma() {

        portalDashboardSekmesi.click();

        portalIstatistikleriBasligi.waitFor(
                new Locator.WaitForOptions()
                        .setState(
                                WaitForSelectorState.VISIBLE
                        )
                        .setTimeout(
                                15000
                        )
        );

    }


    /**
     * Toplam Veri Seti kartının görüntülendiğini kontrol eder.
     */
    public boolean toplamVeriSetiKartiGorunurMu() {

        return toplamVeriSetiKarti.isVisible();
    }


    /**
     * Toplam İndirme kartının görüntülendiğini kontrol eder.
     */
    public boolean toplamIndirmeKartiGorunurMu() {

        return toplamIndirmeKarti.isVisible();
    }


    /**
     * API Çağrısı kartının görüntülendiğini kontrol eder.
     */
    public boolean apiCagrisiKartiGorunurMu() {

        return apiCagrisiKarti.isVisible();
    }


    /**
     * Benzersiz Kullanıcı kartının görüntülendiğini kontrol eder.
     */
    public boolean benzersizKullaniciKartiGorunurMu() {

        return benzersizKullaniciKarti.isVisible();
    }


    /**
     * Format Dağılımı başlığının görüntülendiğini kontrol eder.
     */
    public boolean formatDagilimiBasligiGorunurMu() {

        return formatDagilimiBasligi.isVisible();
    }


    /**
     * Format Dağılımı grafiğinin görüntülendiğini kontrol eder.
     */
    public boolean formatDagilimiGrafikGorunurMu() {

        return formatDagilimiGrafikAlani.isVisible();
    }


    /**
     * CSV, TSV ve XLSX bilgilerinin
     * görüntülendiğini kontrol eder.
     */
    public boolean formatDagilimiLegendleriGorunurMu() {

        return csvLegend.isVisible()
                && tsvLegend.isVisible()
                && xlsxLegend.isVisible();
    }


    /*
     * Aşağıdaki locator getter'ları yalnızca
     * screenshot sırasında alanları çerçevelemek için kullanılır.
     */

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

    /**
     * Günlük trend görünümünü seçmek için kullanılır.
     */
    public void gunlukTrendSecme() {
        gunlukButonu.click();
    }


    /**
     * Haftalık trend görünümünü seçmek için kullanılır.
     */
    public void haftalikTrendSecme() {
        haftalikButonu.click();
    }


    /**
     * Aylık trend görünümünü seçmek için kullanılır.
     */
    public void aylikTrendSecme() {
        aylikButonu.click();
    }

    /**
     * Screenshot sırasında trend alanını
     * işaretlemek için kullanılır.
     */
    public Locator indirmeApiTrendiAlani() {

        return indirmeApiTrendiAlani;
    }
    /**
     * En Çok İndirilen Veri Setleri başlığının
     * görüntülendiğini kontrol eder.
     */
    public boolean enCokIndirilenVeriSetleriBasligiGorunurMu() {

        return enCokIndirilenVeriSetleriBasligi.isVisible();
    }


    /**
     * En Çok İndirilen Veri Setleri açıklamasının
     * görüntülendiğini kontrol eder.
     */
    public boolean enCokIndirilenVeriSetleriAciklamasiGorunurMu() {

        return enCokIndirilenVeriSetleriAciklamasi.isVisible();
    }


    /**
     * Screenshot sırasında ilgili alanı
     * çerçevelemek için locator döndürür.
     */
    public Locator enCokIndirilenVeriSetleriBasligi() {

        return enCokIndirilenVeriSetleriBasligi;
    }
    /**
     * En Çok API ile Sorgulanan Veri Setleri başlığının
     * görüntülendiğini kontrol eder.
     */
    public boolean enCokApiIleSorgulananVeriSetleriBasligiGorunurMu() {

        return enCokApiIleSorgulananVeriSetleriBasligi.isVisible();
    }


    /**
     * En Çok API ile Sorgulanan Veri Setleri açıklamasının
     * görüntülendiğini kontrol eder.
     */
    public boolean enCokApiIleSorgulananVeriSetleriAciklamasiGorunurMu() {

        return enCokApiIleSorgulananVeriSetleriAciklamasi.isVisible();
    }


    /**
     * Screenshot sırasında ilgili alanı
     * çerçevelemek için locator döndürür.
     */
    public Locator enCokApiIleSorgulananVeriSetleriBasligi() {

        return enCokApiIleSorgulananVeriSetleriBasligi;
    }
    /**
     * Kategori Dağılımı başlığının görüntülendiğini kontrol eder.
     */
    public boolean kategoriDagilimiBasligiGorunurMu() {

        return kategoriDagilimiBasligi.isVisible();
    }


    /**
     * Kategori Dağılımı açıklamasının görüntülendiğini kontrol eder.
     */
    public boolean kategoriDagilimiAciklamasiGorunurMu() {

        return kategoriDagilimiAciklamasi.isVisible();
    }


    /**
     * Screenshot sırasında Kategori Dağılımı alanını
     * çerçevelemek için locator döndürür.
     */
    public Locator kategoriDagilimiBasligi() {

        return kategoriDagilimiBasligi;
    }
}