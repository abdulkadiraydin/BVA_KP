package com.bvakp.automation.flows;

import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;

public class OpenDataPublishFlow {

    private final OpenDataPublishPage publishPage;

    public OpenDataPublishFlow(OpenDataPublishPage publishPage) {
        this.publishPage = publishPage;
    }
    public void approveKvkk(String dataSetName) {

        ReportManager.step(
                "KVKK onay işlemi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step(
                "KVKK Onayla butonuna tıklanıyor."
        );
        publishPage.clickKvkkOnayla(dataSetName);

        ReportManager.step(
                "KVKK onay açıklaması giriliyor."
        );
        publishPage.enterKvkkAciklama(
                "Otomasyon testi kapsamında KVKK onayı verilmiştir."
        );

        ReportManager.step(
                "KVKK onay işlemi onaylanıyor."
        );
        publishPage.confirmKvkkOnay();

        ReportManager.step(
                "KVKK onay akışındaki UI işlemleri tamamlandı."
        );
    }
    public void rejectKvkk(String dataSetName) {

        ReportManager.step(
                "KVKK reddetme işlemi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step(
                "KVKK Reddet butonuna tıklanıyor."
        );
        publishPage.clickKvkkReddet(dataSetName);

        ReportManager.step(
                "KVKK ret açıklaması giriliyor."
        );
        publishPage.enterKvkkRedAciklama(
                "Otomasyon testi kapsamında KVKK reddi verilmiştir."
        );

        ReportManager.step(
                "KVKK reddetme işlemi onaylanıyor."
        );
        publishPage.confirmKvkkRed();

        ReportManager.step(
                "KVKK reddetme akışındaki UI işlemleri tamamlandı."
        );
    }
    public void publishDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti yayınlama işlemi başlatıldı: " + dataSetName
        );

        ReportManager.step("Yayınla butonuna tıklanıyor.");
        publishPage.clickYayinla(dataSetName);

        ReportManager.step("Yayın açıklaması giriliyor.");
        publishPage.enterYayinAciklama(
                "Otomasyon testi kapsamında yayınlama işlemi gerçekleştirilmiştir."
        );

        ReportManager.step("Yayınlama işlemi onaylanıyor.");
        publishPage.confirmYayinla();

        ReportManager.step(
                "Veri seti yayınlama işlemi tamamlandı: " + dataSetName
        );
    }
    public void rejectPublication(String dataSetName) {

        ReportManager.step(
                "Yayın reddetme işlemi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Yayın Reddet butonuna tıklanıyor.");
        publishPage.clickYayinReddet(dataSetName);

        ReportManager.step("Yayın ret açıklaması giriliyor.");
        publishPage.enterYayinRedAciklama(
                "Otomasyon testi kapsamında yayın reddedilmiştir."
        );

        ReportManager.step("Yayın reddi onaylanıyor.");
        publishPage.confirmYayinRed();

        ReportManager.step(
                "Veri setinin Düzenleme Bekliyor statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

        ReportManager.step(
                "Yayın reddi tamamlandı. Veri seti Düzenleme Bekliyor statüsünde."
        );
    }
    public void editKvkkPendingDataSet(String dataSetName) {

        ReportManager.step(
                "KVKK onayındaki veri setini düzenleme işlemi başlatılıyor: "
                        + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Veri seti düzenleme ekranı açılıyor.");
        publishPage.clickDuzenle(dataSetName);

        ReportManager.step(
                "Sorumlu Birim alanı güncelleniyor: Yol Dairesi Başkanlığı"
        );
        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        ReportManager.step(
                "Kategori alanı güncelleniyor: Filo & Araçlar"
        );
        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        ReportManager.step("Veri seti açıklaması güncelleniyor.");
        publishPage.enterDuzenlemeAciklama(
                "Güncellenmiş test veri seti açıklaması"
        );

        ReportManager.step("Veri seti düzenleme işlemi kaydediliyor.");
        publishPage.clickDuzenlemeKaydet();

        ReportManager.step("Güncellenen alanlar doğrulanıyor.");
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Güncellenmiş test veri seti açıklaması"
        );

        ReportManager.step("Değişiklikler onaya gönderiliyor.");
        publishPage.clickDegisiklikOnayaGonder();

        ReportManager.step("Değişikliklerin onaya gönderilmesi onaylanıyor.");
        publishPage.confirmDegisiklikOnayaGonder();

        ReportManager.step("Önceki ekrana geri dönülüyor.");
        publishPage.clickGeriDon();

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step(
                "Veri seti düzenleme akışı tamamlandı. Statü: KVKK Onayında"
        );
    }
    public void editPublicationPendingDataSet(String dataSetName) {

        ReportManager.step(
                "Yayın reddi sonrası veri seti düzenleme işlemi başlatılıyor: "
                        + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin Düzenleme Bekliyor statüsünde olması bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

        ReportManager.step("Veri seti düzenleme ekranı açılıyor.");
        publishPage.clickDuzenle(dataSetName);

        ReportManager.step(
                "Sorumlu Birim güncelleniyor: Yol Dairesi Başkanlığı"
        );
        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        ReportManager.step("Kategori güncelleniyor: Filo & Araçlar");
        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        ReportManager.step("Veri seti açıklaması güncelleniyor.");
        publishPage.enterDuzenlemeAciklama(
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        ReportManager.step("Veri seti düzenleme işlemi kaydediliyor.");
        publishPage.clickDuzenlemeKaydet();

        ReportManager.step("Güncellenen alanlar doğrulanıyor.");
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        ReportManager.step("Değişiklikler onaya gönderiliyor.");
        publishPage.clickDegisiklikOnayaGonder();

        ReportManager.step("Değişikliklerin onaya gönderilmesi onaylanıyor.");
        publishPage.confirmDegisiklikOnayaGonder();

        ReportManager.step("Önceki ekrana geri dönülüyor.");
        publishPage.clickGeriDon();

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step(
                "Veri seti düzenleme akışı tamamlandı. Statü: KVKK Onayında"
        );
    }
    public void deleteBeforePublication(String dataSetName) {

        ReportManager.step(
                "Yayın öncesi silme işlemi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Silinecek veri seti aranıyor: " + dataSetName);
        publishPage.clickSil(dataSetName);

        ReportManager.step("Veri seti silme işlemi onaylanıyor.");
        publishPage.confirmSil();

        ReportManager.step(
                "Silme onayı verildi. Veri setinin listeden kaldırılması bekleniyor: "
                        + dataSetName
        );
    }
    public void deleteAfterPublication(String dataSetName) {

        ReportManager.step(
                "Yayın sonrası silme işlemi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Silinecek veri seti aranıyor: " + dataSetName);
        publishPage.clickSil(dataSetName);

        ReportManager.step("Veri seti silme işlemi onaylanıyor.");
        publishPage.confirmSil();

        ReportManager.step(
                "Silme onayı verildi. Veri setinin listeden kaldırılması bekleniyor: "
                        + dataSetName
        );
    }
    public void archiveDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti arşivleme işlemi başlatıldı: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Arşivlenecek veri seti aranıyor: " + dataSetName
        );
        publishPage.clickArsivle(dataSetName);

        ReportManager.step("Arşivleme işlemi onaylanıyor.");
        publishPage.confirmArsivle();

        ReportManager.step(
                "Veri seti arşivleme işlemi tamamlandı: " + dataSetName
        );
    }
    public void viewDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti görüntüleme işlemi başlatıldı: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Görüntülenecek veri seti açılıyor: " + dataSetName
        );
        publishPage.clickGoruntule(dataSetName);

        ReportManager.step("Veri seti görüntüleme ekranından geri dönülüyor.");
        publishPage.clickGeriDon();

        ReportManager.step(
                "Veri seti görüntüleme işlemi tamamlandı: " + dataSetName
        );
    }
    public void searchDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti arama ekranına geçiliyor."
        );
        publishPage.clickPublishTab();

        ReportManager.step(
                "Arama alanına veri seti adı giriliyor: " + dataSetName
        );
        publishPage.searchDataSet(dataSetName);

        ReportManager.step(
                "Veri seti arama alanına giriş işlemi tamamlandı."
        );
    }
    public void manuallyUpdateDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti manuel güncelleme işlemi başlatıldı: " + dataSetName
        );

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Mevcut veri seti versiyonu okunuyor.");
        String oldVersion = publishPage.getCurrentVersion(dataSetName);

        ReportManager.step(
                "Mevcut versiyon: " + oldVersion
        );

        ReportManager.step("Veri seti Güncelle butonuna tıklanıyor.");
        publishPage.clickGuncelle(dataSetName);

        ReportManager.step("Güncelleme işlemi onaylanıyor.");
        publishPage.confirmGuncelle();

        ReportManager.step(
                "Veri setinin yeni versiyonu okunuyor: " + dataSetName
        );
        String newVersion = publishPage.getCurrentVersion(dataSetName);

        ReportManager.step(
                "Güncelleme işlemi tamamlandı. Eski versiyon: "
                        + oldVersion + " | Yeni versiyon: " + newVersion
        );
    }

}