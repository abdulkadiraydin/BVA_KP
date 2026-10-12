package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.utils.TestDataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenDataPublishTest extends AvpBaseTest {
    private static final Logger log = LoggerFactory.getLogger(OpenDataPublishTest.class);
    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;

    private OpenDataCreateFlow openDataCreateFlow;
    private OpenDataPublishFlow publishFlow;

    @BeforeMethod
    public void setUpPublish() {
        avpOturumuHazirlama();

        openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        publishPage = new OpenDataPublishPage(page);

        openDataCreateFlow = new OpenDataCreateFlow(page);

        openDataCreateFlow = new OpenDataCreateFlow(page);

        publishFlow = new OpenDataPublishFlow(publishPage);


    }

    @Test
    public void veriSetiKvkkOnaylama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_onay");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK onay testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onay testi veri seti"
        );

        log.info("KVKK onaylama testi başladı. Veri Seti: {}", dataSetName);

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(
                        dataSetName,
                        "Yayın Onayında"
                ),
                "Veri seti KVKK onaylanamadı."
        );
    }
    @Test
    public void veriSetiKvkkReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_red");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK reddetme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK reddetme testi veri seti"
        );

        log.info("KVKK reddetme testi başladı. Veri Seti: {}", dataSetName);

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.rejectKvkk(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Reddedildi"),
                "Veri seti KVKK reddedilemedi."
        );
    }
    @Test
    public void veriSetiYayinlama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayinlama");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayınlama testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti yayınlama testi"
        );

        log.info(
                "Veri seti yayınlama testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.publishDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "Yayında"),
                "Veri seti Yayında durumuna geçmedi."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarıldı"),
                "Veri seti portala aktarılmadı."
        );
    }
    @Test
    public void veriSetiYayiniReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_red");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın reddetme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın reddetme testi veri seti"
        );

        log.info(
                "Veri seti yayın reddetme testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.rejectPublication(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(
                        dataSetName,
                        "Düzenleme Bekliyor"
                ),
                "Veri seti yayın reddi sonrasında Düzenleme Bekliyor durumuna geçmedi."
        );
    }
    @Test
    public void veriSetiKvkkOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_duzenleme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK düzenleme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onayı düzenleme testi veri seti"
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.rejectKvkk(dataSetName);
        publishFlow.editKvkkPendingDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Veri seti KVKK Onayında durumuna geçmedi."
        );
    }
    @Test
    public void veriSetiYayinOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_duzenleme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın onayı düzenleme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın onayı düzenleme testi veri seti"
        );

        log.info(
                "Yayın onayı sonrası düzenleme testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.rejectPublication(dataSetName);

        publishFlow.editPublicationPendingDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Düzenleme sonrası veri seti KVKK Onayında durumuna geçmedi."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarılmadı"),
                "Düzenleme sonrası portal durumu Aktarılmadı olmadı."
        );
    }
    @Test
    public void veriSetiYayinOncesiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_oncesi_silme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın öncesi silme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın öncesi silme testi veri seti"
        );

        publishFlow.deleteBeforePublication(dataSetName);

        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Veri seti yayın öncesinde silinemedi."
        );
    }
    @Test
    public void veriSetiYayinSonrasiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_sonrasi_silme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın sonrası silme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın sonrası silme testi veri seti"
        );

        publishFlow.approveKvkk(dataSetName);

        publishFlow.publishDataSet(dataSetName);

        publishFlow.archiveDataSet(dataSetName);

        publishFlow.deleteAfterPublication(dataSetName);

        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Arşivlenen veri seti silinemedi."
        );
    }
    @Test
    public void veriSetiGoruntuleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti görüntüleme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti görüntüleme testi"
        );

        log.info(
                "Veri seti görüntüleme testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishFlow.viewDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isDataSetDisplayed(dataSetName),
                "Veri seti görüntüleme ekranından sonra listeye dönülemedi."
        );
    }
    @Test
    public void veriSetiArama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arama");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arama testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arama testi"
        );

        log.info(
                "Veri seti arama testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishFlow.searchDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isDataSetDisplayed(dataSetName),
                "Aranan veri seti listede bulunamadı."
        );
    }
    @Test
    public void veriSetiManuelGuncelleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_manuel_guncelleme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Manuel güncelleme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Manuel güncelleme testi veri seti"
        );

        log.info(
                "Manuel güncelleme testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.publishDataSet(dataSetName);

        int oldVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );

        publishFlow.manuallyUpdateDataSet(dataSetName);

        int newVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );

        Assert.assertEquals(
                newVersion,
                oldVersion + 1,
                "Manuel güncelleme sonrası veri seti versiyonu 1 artmadı."
        );
    }
    @Test
    public void veriSetiArsiveAlma() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arsivleme");

        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arşivleme testi kaynağı"
        );

        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arşivleme testi"
        );

        log.info(
                "Veri seti arşivleme testi başladı. Veri Seti: {}",
                dataSetName
        );

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.publishDataSet(dataSetName);

        publishFlow.archiveDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "Arşivlendi"),
                "Veri seti Arşivlendi durumuna geçmedi."
        );

        publishPage.waitForPortalStatus(dataSetName, "Aktarıldı");
    }
}