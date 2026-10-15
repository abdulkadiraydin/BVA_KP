package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

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
     * Kaynak Ekle formunu doldurarak yeni kaynak oluşturur.
     *
     * @param kaynakAdi oluşturulacak kaynak adı
     * @param tabloAdi kaynak tablosu
     * @param aciklama kaynak açıklaması
     */
    public void createSource(String kaynakAdi, String tabloAdi, String aciklama) {
        openDataPage.kaynakEklemeEkraniniAcma();
        kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
        kaynakEkleModal.kaynakTablosuSecme(tabloAdi);
        kaynakEkleModal.kaynakAciklamasiGirme(aciklama);
        kaynakEkleModal.kaynakEkleme();
    }

    /**
     * Belirtilen kaynağın silme butonuna tıklar ve silmeyi onaylar.
     *
     * @param kaynakAdi silinecek kaynak adı
     */
    public void deleteSource(String kaynakAdi) {
        openDataPage.kaynakSilmeButonunaTiklama(kaynakAdi);
        openDataPage.kaynakSilmeOnayiVerme();
    }

    /**
     * Kaynağın adını ve açıklamasını düzenleyip kaydeder.
     *
     * @param kaynakAdi düzenlenecek kaynak adı
     * @param yeniKaynakAdi yeni kaynak adı
     * @param yeniAciklama yeni kaynak açıklaması
     */
    public void editSource(String kaynakAdi, String yeniKaynakAdi, String yeniAciklama) {
        openDataPage.kaynakDuzenlemeButonunaTiklama(kaynakAdi);
        openDataPage.kaynakAdiniGuncelleme(yeniKaynakAdi);
        openDataPage.kaynakAciklamasiniGuncelleme(yeniAciklama);
        openDataPage.kaynakDuzenlemeyiKaydetme();
    }

    /**
     * Belirtilen kaynağın görüntüleme ekranını açar.
     *
     * @param kaynakAdi görüntülenecek kaynak adı
     */
    public void openSource(String kaynakAdi) {
        openDataPage.kaynakGoruntulemeButonunaTiklama(kaynakAdi);
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
    }
}
