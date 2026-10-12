package com.bvakp.automation.flows;

import com.bvakp.automation.pages.openData.OpenDataPublishPage;

public class OpenDataPublishFlow {

    private final OpenDataPublishPage publishPage;

    public OpenDataPublishFlow(OpenDataPublishPage publishPage) {
        this.publishPage = publishPage;
    }
    public void approveKvkk(String dataSetName) {

        publishPage.clickKvkkOnayla(dataSetName);

        publishPage.enterKvkkAciklama(
                "Otomasyon testi kapsamında KVKK onayı verilmiştir."
        );

        publishPage.confirmKvkkOnay();
    }
    public void rejectKvkk(String dataSetName) {

        publishPage.clickKvkkReddet(dataSetName);

        publishPage.enterKvkkRedAciklama(
                "Otomasyon testi kapsamında KVKK reddi verilmiştir."
        );

        publishPage.confirmKvkkRed();
    }
    public void publishDataSet(String dataSetName) {

        publishPage.clickYayinla(dataSetName);

        publishPage.enterYayinAciklama(
                "Otomasyon testi kapsamında yayınlama işlemi gerçekleştirilmiştir."
        );

        publishPage.confirmYayinla();

        publishPage.waitForStatus(
                dataSetName,
                "Yayında"
        );

        publishPage.waitForPortalStatus(
                dataSetName,
                "Aktarıldı"
        );
    }
    public void rejectPublication(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.clickYayinReddet(dataSetName);

        publishPage.enterYayinRedAciklama(
                "Otomasyon testi kapsamında yayın reddedilmiştir."
        );

        publishPage.confirmYayinRed();

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );
    }
    public void editKvkkPendingDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.clickDuzenle(dataSetName);

        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        publishPage.enterDuzenlemeAciklama(
                "Güncellenmiş test veri seti açıklaması"
        );

        publishPage.clickDuzenlemeKaydet();

        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Güncellenmiş test veri seti açıklaması"
        );

        publishPage.clickDegisiklikOnayaGonder();

        publishPage.confirmDegisiklikOnayaGonder();

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
    }
    public void editPublicationPendingDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

        publishPage.clickDuzenle(dataSetName);

        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );

        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );

        publishPage.enterDuzenlemeAciklama(
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        publishPage.clickDuzenlemeKaydet();

        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Yayın reddi sonrası güncellenmiş test veri seti açıklaması"
        );

        publishPage.clickDegisiklikOnayaGonder();

        publishPage.confirmDegisiklikOnayaGonder();

        publishPage.clickGeriDon();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
    }
    public void deleteBeforePublication(String dataSetName) {

        publishPage.clickPublishTab();
        publishPage.clickSil(dataSetName);
        publishPage.confirmSil();
    }
    public void deleteAfterPublication(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.clickSil(dataSetName);
        publishPage.confirmSil();
    }
    public void archiveDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.clickArsivle(dataSetName);

        publishPage.confirmArsivle();
    }
    public void viewDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.clickGoruntule(dataSetName);

        publishPage.clickGeriDon();
    }
    public void searchDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        publishPage.searchDataSet(dataSetName);
    }
    public void manuallyUpdateDataSet(String dataSetName) {

        publishPage.clickPublishTab();

        String oldVersion = publishPage.getCurrentVersion(dataSetName);

        publishPage.clickGuncelle(dataSetName);

        publishPage.confirmGuncelle();

        String newVersion = publishPage.getCurrentVersion(dataSetName);
    }

}