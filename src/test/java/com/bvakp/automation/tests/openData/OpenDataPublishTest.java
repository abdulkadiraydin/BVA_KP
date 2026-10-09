package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataSourceFlow;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenDataPublishTest extends BaseTest {
    private static final Logger log = LoggerFactory.getLogger(OpenDataPublishTest.class);
    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;
    private OpenDataPublishFlow publishFlow;
    private OpenDataCreateFlow createFlow;
    private OpenDataSourceFlow sourceFlow;

    @BeforeMethod
    public void setUpPublish() {
        LoginPage loginPage = new LoginPage(page);

        loginPage.open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );
        openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        publishPage = new OpenDataPublishPage(page);

        sourceFlow = new OpenDataSourceFlow(page, openDataPage);

        publishFlow = new OpenDataPublishFlow(publishPage);

        createFlow = new OpenDataCreateFlow(openDataPage, sourceFlow);

    }

    @Test
    public void veriSetiKvkkOnaylama() {

        String dataSetName = createFlow.createDataSetForApproval();
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

        String dataSetName = createFlow.createDataSetForApproval();

        log.info("KVKK reddetme testi başladı. Veri Seti: {}", dataSetName);

        publishPage.clickPublishTab();

        publishPage.waitForStatus(dataSetName, "KVKK Onayında");

        publishFlow.rejectKvkk(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Reddedildi"),
                "Veri seti KVKK reddedilemedi."
        );
    }
    @Test
    public void veriSetiYayinlama() {

        String dataSetName = createFlow.createDataSetForApproval();

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
                publishPage.isStatus(
                        dataSetName,
                        "Yayında"
                ),
                "Veri seti Yayında durumuna geçmedi."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(
                        dataSetName,
                        "Aktarıldı"
                ),
                "Veri seti portala aktarılmadı."
        );
    }
    @Test
    public void veriSetiYayiniReddetme() {

        String dataSetName = createFlow.createDataSetForApproval();

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

        String dataSetName = createFlow.createDataSetForApproval();
        publishFlow.rejectKvkk(dataSetName);
        publishFlow.editKvkkPendingDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(
                        dataSetName,
                        "KVKK Onayında"
                ),
                "Veri seti KVKK Onayında durumuna geçmedi."
        );
    }
    @Test
    public void veriSetiYayinOnayiDuzenleme() {

        String dataSetName = createFlow.createDataSetForApproval();

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
                publishPage.isStatus(
                        dataSetName,
                        "KVKK Onayında"
                ),
                "Düzenleme sonrası veri seti KVKK Onayında durumuna geçmedi."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(
                        dataSetName,
                        "Aktarılmadı"
                ),
                "Düzenleme sonrası portal durumu Aktarılmadı olmadı."
        );
    }
    @Test
    public void veriSetiYayinOncesiSilme() {
        String dataSetName = createFlow.createDataSetForApproval();
        publishFlow.deleteBeforePublication(dataSetName);
        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Veri seti yayın öncesinde silinemedi."
        );
    }
    @Test
    public void veriSetiYayinSonrasiSilme() {

        String dataSetName =
                createFlow.createDataSetForApproval();

        publishFlow.approveKvkk(dataSetName);

        publishFlow.publishDataSet(dataSetName);

        publishFlow.archiveDataSet(dataSetName);

        publishFlow.deleteAfterPublication(dataSetName);

        Assert.assertFalse(publishPage.isDataSetDisplayed(dataSetName), "Arşivlenen veri seti silinemedi.");
    }
    @Test
    public void veriSetiGoruntuleme() {

        String dataSetName = createFlow.createDataSetForApproval();

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

        String dataSetName = createFlow.createDataSetForApproval();

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

        String dataSetName = createFlow.createDataSetForApproval();

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

        String dataSetName = createFlow.createDataSetForApproval();

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
                publishPage.isStatus(
                        dataSetName,
                        "Arşivlendi"
                ),
                "Veri seti Arşivlendi durumuna geçmedi."
        );

        publishPage.waitForPortalStatus(dataSetName, "Aktarıldı");
    }
}