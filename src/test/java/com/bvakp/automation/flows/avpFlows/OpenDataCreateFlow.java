package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.components.KaynakEkleModal;
import com.bvakp.automation.model.KolonTanimi;
import com.bvakp.automation.model.VeriSetiTanimi;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

/**
 * Açık Veri Portalı'nda kaynak oluşturma ve veri setini KVKK onayına gönderme adımları.
 *
 * <p>Testlerin ön koşulu olarak kullanılır. Kaynak oluşturmanın ve veri setini onaya
 * göndermenin ayrıntılı testleri OpenDataCreateTest'tedir; o testler bu flow'u kullanmaz.
 */
public class OpenDataCreateFlow {

    private final OpenDataPage openDataPage;
    private final KaynakEkleModal kaynakEkleModal;

    public OpenDataCreateFlow(Page page) {
        this.openDataPage = new OpenDataPage(page);
        this.kaynakEkleModal = new KaynakEkleModal(page);
    }

    /**
     * Kaynak Ekle formunu doldurarak yeni kaynak oluşturur ve listede göründüğünü doğrular.
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

    /**
     * Verilen kaynaktan veri seti oluşturur ("yil" kolonuna maskeleme uygular),
     * KVKK onayına gönderir ve ekranlardan toplanan değerleri döndürür.
     *
     * <p>Dönüş değeri isteğe bağlıdır; sadece onaya göndermek isteyen çağıranlar yok sayabilir.
     *
     * @return veri setinin girilen ve ekranlardan okunan değerleri (yayın ayarları henüz yok)
     */
    public VeriSetiTanimi createDataSetForApproval(
            String kaynakAdi,
            String veriSetiAdi,
            String sorumluBirim,
            String kategori,
            String kullanimLisansi,
            String aciklama) {

        ReportManager.step("Ön koşul: veri seti oluşturulup KVKK onayına gönderiliyor | " + veriSetiAdi);

        openDataPage.kaynakSecme(kaynakAdi);
        openDataPage.veriSetiIleriGitme();

        openDataPage.yilAlaninaMaskeleKuraliUygulama();
        List<KolonTanimi> kolonlar = openDataPage.alanTanimlariniOkuma();

        openDataPage.veriSetiAdiGirme(veriSetiAdi);
        openDataPage.sorumluBirimSecme(sorumluBirim);
        openDataPage.kategoriSecme(kategori);
        openDataPage.kullanimLisansiSecme(kullanimLisansi);
        openDataPage.veriSetiAciklamasiGirme(aciklama);

        openDataPage.veriSetiOnizleme();
        List<Map<String, String>> onizlemeKayitlari = openDataPage.onizlemeKayitlariniOkuma();

        openDataPage.veriSetiOnayaGonderme();
        openDataPage.veriSetiOnayaGondermeyiOnaylama();
        openDataPage.bildirimBekleme("Veri seti oluşturuldu ve KVKK onay kuyruğuna gönderildi");

        return new VeriSetiTanimi(
                veriSetiAdi,
                aciklama,
                kaynakAdi,
                sorumluBirim,
                kategori,
                kullanimLisansi,
                kolonlar,
                onizlemeKayitlari,
                null);
    }
}