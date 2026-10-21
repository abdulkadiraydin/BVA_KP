package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.components.KaynakEkleModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;
import org.testng.Assert;

/**
 * Açık Veri Portalı kaynak CRUD ve veri setini KVKK onayına gönderme
 * işlemlerini tek bir Flow sınıfında toplar.
 */
public class OpenDataCreateFlow {

    private final OpenDataPage openDataPage;
    private final KaynakEkleModal kaynakEkleModal;

    /**
     * Ortak Page Object ve kaynak ekleme modalını aktif Playwright Page ile hazırlar.
     *
     * @param page aktif Playwright sayfası
     */
    public OpenDataCreateFlow(Page page) {
        this.openDataPage = new OpenDataPage(page);
        this.kaynakEkleModal = new KaynakEkleModal(page);
    }

    /**
     * Kaynak Ekle formunu doldurarak yeni kaynak oluşturur ve listede göründüğünü doğrular.
     * Testlerin ön koşulu olarak kullanılır; kaynak oluşturmanın ayrıntılı testi
     * OpenDataCreateTest.kaynakOlusturma'dadır.
     */
    public void createSource(String kaynakAdi, String tabloAdi, String aciklama) {

        ReportManager.step("Ön koşul: kaynak oluşturuluyor | " + kaynakAdi + " | Tablo: " + tabloAdi);

        openDataPage.kaynakEklemeEkraniniAcma();
        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(),
                "Ön koşul: Kaynak Ekle formu açılamadı.");

        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        kaynakEkleModal.kaynakTablosuSecme(tabloAdi);
        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);

        Assert.assertTrue(kaynakEkleModal.veriOnizlemeGoruntulendiMi(),
                "Ön koşul: Veri Önizleme görüntülenemedi.");

        kaynakEkleModal.kaynakEkleme();

        Assert.assertTrue(kaynakEkleModal.kaynakEkleFormuKapandiMi(),
                "Ön koşul: Kaynak oluşturulduktan sonra form kapanmadı.");
        Assert.assertTrue(openDataPage.kaynakListedeMi(kaynakAdi),
                "Ön koşul: Kaynak listede bulunamadı: " + kaynakAdi);

        ReportManager.info("Ön koşul kaynağı hazır | " + kaynakAdi);
    }

    public void createDataSetForApproval(
            String kaynakAdi,
            String veriSetiAdi,
            String sorumluBirim,
            String kategori,
            String kullanimLisansi,
            String aciklama) {

        openDataPage.kaynakSecme(kaynakAdi);
        openDataPage.veriSetiIleriGitme();
        openDataPage.yilAlaninaMaskeleKuraliUygulama();
        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        openDataPage.sorumluBirimSecme(sorumluBirim);
        openDataPage.kategoriSecme(kategori);
        openDataPage.kullanimLisansiSecme(kullanimLisansi);
        openDataPage.veriSetiAciklamasiGirme(aciklama);
        openDataPage.veriSetiOnizleme();
        openDataPage.veriSetiOnayaGonderme();
        openDataPage.veriSetiOnayaGondermeyiOnaylama();
        openDataPage.bildirimBekleme("Veri seti oluşturuldu ve KVKK onay kuyruğuna gönderildi");
    }
}
