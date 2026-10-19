package com.bvakp.automation.flows.avpFlows;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;

public class OpenDataPublishFlow {

    private final OpenDataPublishPage publishPage;

    public OpenDataPublishFlow(OpenDataPublishPage publishPage) {
        this.publishPage = publishPage;
    }

    public void approveKvkk(String dataSetName) {

        ReportManager.info("KVKK Onayla butonuna tıklanıyor: " + dataSetName);
        publishPage.clickKvkkOnayla(dataSetName);

        ReportManager.info("KVKK onay açıklaması giriliyor.");
        publishPage.enterKvkkAciklama(
                "Otomasyon testi kapsamında KVKK onayı verilmiştir."
        );

        ReportManager.info("KVKK onay işlemi onaylanıyor.");
        publishPage.confirmKvkkOnay();
        publishPage.bildirimBekleme("Veri seti yayın onay kuyruğuna gönderildi");

    }

    public void rejectKvkk(String dataSetName) {

        ReportManager.info("KVKK Reddet butonuna tıklanıyor: " + dataSetName);
        publishPage.clickKvkkReddet(dataSetName);

        ReportManager.info("KVKK ret açıklaması giriliyor.");
        publishPage.enterKvkkRedAciklama(
                "Otomasyon testi kapsamında KVKK reddi verilmiştir."
        );

        ReportManager.info("KVKK reddetme işlemi onaylanıyor.");
        publishPage.confirmKvkkRed();
        publishPage.bildirimBekleme("Veri seti düzenlenmek üzere geri gönderildi");
    }

    public void publishDataSet(String dataSetName) {

        ReportManager.info("Yayınla butonuna tıklanıyor: " + dataSetName);
        publishPage.clickYayinla(dataSetName);

        ReportManager.info("Yayın açıklaması giriliyor.");
        publishPage.enterYayinAciklama(
                "Otomasyon testi kapsamında yayınlama işlemi gerçekleştirilmiştir."
        );

        ReportManager.info("Yayınlama işlemi onaylanıyor.");
        publishPage.confirmYayinla();
        publishPage.bildirimBekleme("Veri seti yayınlandı");
    }

    public void rejectPublication(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Yayını Reddet butonuna tıklanıyor: " + dataSetName);
        publishPage.clickYayinReddet(dataSetName);

        ReportManager.info("Yayın ret açıklaması giriliyor.");
        publishPage.enterYayinRedAciklama(
                "Otomasyon testi kapsamında yayın reddedilmiştir."
        );

        ReportManager.info("Yayın reddi onaylanıyor.");
        publishPage.confirmYayinRed();
        publishPage.bildirimBekleme("Veri seti düzenlenmek üzere geri gönderildi");
    }

    public void editKvkkPendingDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Düzenle butonuna tıklanıyor: " + dataSetName);
        publishPage.clickDuzenle(dataSetName);

        ReportManager.info(
                "Sorumlu Birim alanı güncelleniyor: Yol Dairesi Başkanlığı"
        );
        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        ReportManager.info(
                "Kategori alanı güncelleniyor: Filo & Araçlar"
        );
        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        ReportManager.info("Veri seti açıklaması güncelleniyor.");
        publishPage.enterDuzenlemeAciklama(
                "Güncellenmiş test veri seti açıklaması"
        );

        ReportManager.info("Veri seti düzenleme işlemi kaydediliyor.");
        publishPage.clickDuzenlemeKaydet();
        publishPage.bildirimBekleme("Veri seti güncellendi");

        ReportManager.info("Güncellenen alanlar doğrulanıyor.");
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Güncellenmiş test veri seti açıklaması"
        );

        ReportManager.info("Değişiklikler onaya gönderiliyor.");
        publishPage.clickDegisiklikOnayaGonder();

        ReportManager.info("Değişikliklerin onaya gönderilmesi onaylanıyor.");
        publishPage.confirmDegisiklikOnayaGonder();
        publishPage.bildirimBekleme("Veri seti onay kuyruğuna gönderildi.");

        ReportManager.info("Önceki ekrana geri dönülüyor.");
        publishPage.clickGeriDon();
        publishPage.waitForVeriSetiYonetimi();
    }

    public void editPublicationPendingDataSet(String dataSetName) {
        ReportManager.info("Veri seti düzenleme ekranı açılıyor.");
        publishPage.clickDuzenle(dataSetName);

        ReportManager.info(
                "Sorumlu Birim güncelleniyor: Yol Dairesi Başkanlığı"
        );
        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        ReportManager.info("Kategori güncelleniyor: Filo & Araçlar");
        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        ReportManager.info("Veri seti açıklaması güncelleniyor.");
        publishPage.enterDuzenlemeAciklama(
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        ReportManager.info("Veri seti düzenleme işlemi kaydediliyor.");
        publishPage.clickDuzenlemeKaydet();

        ReportManager.info("Güncellenen alanlar doğrulanıyor.");
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        ReportManager.info("Değişiklikler onaya gönderiliyor.");
        publishPage.clickDegisiklikOnayaGonder();

        ReportManager.info(
                "Değişikliklerin onaya gönderilmesi onaylanıyor."
        );
        publishPage.confirmDegisiklikOnayaGonder();
        publishPage.bildirimBekleme("Veri seti güncellendi");

        ReportManager.info("Önceki ekrana geri dönülüyor.");
        publishPage.clickGeriDon();

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
    }

    /**
     * Yayınlanmadan önce veri setini siler.
     * İleride yayın öncesine özel adımlar gerekirse buraya eklenir.
     */
    public void deleteBeforePublication(String dataSetName) {
        veriSetiniSil(dataSetName);
    }

    /**
     * Yayınlandıktan sonra veri setini siler.
     * İleride yayın sonrasına özel adımlar gerekirse buraya eklenir.
     */
    public void deleteAfterPublication(String dataSetName) {
        veriSetiniSil(dataSetName);
    }

    /** Her iki silme akışının ortak adımları. */
    private void veriSetiniSil(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Sil butonuna tıklanıyor: " + dataSetName);
        publishPage.clickSil(dataSetName);

        ReportManager.info("Veri seti silme işlemi onaylanıyor.");
        publishPage.confirmSil();

        ReportManager.info("Silme bildirimi bekleniyor: " + dataSetName);
        publishPage.bildirimBekleme("Veri seti silindi");
    }

    public void archiveDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Arşivle butonuna tıklanıyor: ");
        publishPage.clickArsivle(dataSetName);

        ReportManager.info("Arşivleme işlemi onaylanıyor.");
        publishPage.confirmArsivle();
        publishPage.bildirimBekleme("Veri seti arşivlendi");
    }

    public void viewDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Görüntüle butonuna tıklanıyor: " + dataSetName);
        publishPage.clickGoruntule(dataSetName);
    }

    public void searchDataSet(String dataSetName) {

        ReportManager.info("Veri seti arama ekranına geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Arama alanına veri seti adı giriliyor: " + dataSetName
        );
        publishPage.searchDataSet(dataSetName);
    }

    public void manuallyUpdateDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Güncellenecek veri seti aranıyor: " + dataSetName);
        publishPage.searchDataSet(dataSetName);

        ReportManager.info("Güncelle butonuna tıklanıyor: " + dataSetName);
        publishPage.clickGuncelle(dataSetName);

        ReportManager.info("Güncelleme işlemi onaylanıyor.");
        publishPage.confirmGuncelle();
        publishPage.bildirimBekleme("Güncelleme başlatıldı");
    }
}