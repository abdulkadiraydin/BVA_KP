package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;

public class OpenDataPublishFlow {

    private final OpenDataPublishPage publishPage;

    public OpenDataPublishFlow(OpenDataPublishPage publishPage) {
        this.publishPage = publishPage;
    }

    public void approveKvkk(String dataSetName) {

        ReportManager.info("KVKK Onayla butonuna tıklanıyor.");
        publishPage.clickKvkkOnayla(dataSetName);

        ReportManager.info("KVKK onay açıklaması giriliyor.");
        publishPage.enterKvkkAciklama(
                "Otomasyon testi kapsamında KVKK onayı verilmiştir."
        );

        ReportManager.info("KVKK onay işlemi onaylanıyor.");
        publishPage.confirmKvkkOnay();
    }

    public void rejectKvkk(String dataSetName) {

        ReportManager.info("KVKK Reddet butonuna tıklanıyor.");
        publishPage.clickKvkkReddet(dataSetName);

        ReportManager.info("KVKK ret açıklaması giriliyor.");
        publishPage.enterKvkkRedAciklama(
                "Otomasyon testi kapsamında KVKK reddi verilmiştir."
        );

        ReportManager.info("KVKK reddetme işlemi onaylanıyor.");
        publishPage.confirmKvkkRed();
    }

    public void publishDataSet(String dataSetName) {

        ReportManager.info("Yayınla butonuna tıklanıyor.");
        publishPage.clickYayinla(dataSetName);

        ReportManager.info("Yayın açıklaması giriliyor.");
        publishPage.enterYayinAciklama(
                "Otomasyon testi kapsamında yayınlama işlemi gerçekleştirilmiştir."
        );

        ReportManager.info("Yayınlama işlemi onaylanıyor.");
        publishPage.confirmYayinla();
    }

    public void rejectPublication(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Yayın Reddet butonuna tıklanıyor.");
        publishPage.clickYayinReddet(dataSetName);

        ReportManager.info("Yayın ret açıklaması giriliyor.");
        publishPage.enterYayinRedAciklama(
                "Otomasyon testi kapsamında yayın reddedilmiştir."
        );

        ReportManager.info("Yayın reddi onaylanıyor.");
        publishPage.confirmYayinRed();
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.info(
                "Veri setinin Düzenleme Bekliyor statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );
    }

    public void editKvkkPendingDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Veri seti düzenleme ekranı açılıyor.");
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

        ReportManager.info("Güncellenen alanlar doğrulanıyor.");
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Güncellenmiş test veri seti açıklaması"
        );

        ReportManager.info("Değişiklikler onaya gönderiliyor.");
        publishPage.clickDegisiklikOnayaGonder();

        ReportManager.info(
                "Değişikliklerin onaya gönderilmesi onaylanıyor."
        );
        publishPage.confirmDegisiklikOnayaGonder();

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

    public void editPublicationPendingDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin Düzenleme Bekliyor statüsünde olması bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

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

    public void deleteBeforePublication(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Silinecek veri seti aranıyor: " + dataSetName);
        publishPage.clickSil(dataSetName);

        ReportManager.info("Veri seti silme işlemi onaylanıyor.");
        publishPage.confirmSil();

        ReportManager.info(
                "Silme onayı verildi. Veri setinin listeden kaldırılması bekleniyor: "
                        + dataSetName
        );
    }

    public void deleteAfterPublication(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Silinecek veri seti aranıyor: " + dataSetName);
        publishPage.clickSil(dataSetName);

        ReportManager.info("Veri seti silme işlemi onaylanıyor.");
        publishPage.confirmSil();

        ReportManager.info(
                "Silme onayı verildi. Veri setinin listeden kaldırılması bekleniyor: "
                        + dataSetName
        );
    }

    public void archiveDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Arşivlenecek veri seti aranıyor: " + dataSetName
        );
        publishPage.clickArsivle(dataSetName);

        ReportManager.info("Arşivleme işlemi onaylanıyor.");
        publishPage.confirmArsivle();
    }

    public void viewDataSet(String dataSetName) {

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Görüntülenecek veri seti açılıyor: " + dataSetName
        );
        publishPage.clickGoruntule(dataSetName);

        ReportManager.info("Veri seti görüntüleme ekranından geri dönülüyor.");
        publishPage.clickGeriDon();
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

        ReportManager.info("Mevcut veri seti versiyonu okunuyor.");
        String oldVersion = publishPage.getCurrentVersion(dataSetName);

        ReportManager.info("Mevcut versiyon: " + oldVersion);

        ReportManager.info("Veri seti Güncelle butonuna tıklanıyor.");
        publishPage.clickGuncelle(dataSetName);

        ReportManager.info("Güncelleme işlemi onaylanıyor.");
        publishPage.confirmGuncelle();

        ReportManager.info(
                "Veri setinin yeni versiyonu okunuyor: " + dataSetName
        );
        String newVersion = publishPage.getCurrentVersion(dataSetName);

        ReportManager.info(
                "Güncelleme işlemi tamamlandı. Eski versiyon: "
                        + oldVersion + " | Yeni versiyon: " + newVersion
        );
    }
}