package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataSourceFlow;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenDataCreateTest extends BaseTest {
    private static final Logger log =
            LoggerFactory.getLogger(OpenDataCreateTest.class);
    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;
    private OpenDataSourceFlow sourceFlow;
    private OpenDataCreateFlow createFlow;
    @BeforeMethod
    public void setUpCreate() {

        // Login
        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
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
        createFlow = new OpenDataCreateFlow(openDataPage, sourceFlow);
    }

    @Test
    public void kaynakOlusturma() {

        String sourceName =
                sourceFlow.createSource();

        Assert.assertTrue(
                openDataPage.isSourceDisplayed(sourceName),
                "Oluşturulan kaynak listede görüntülenemedi: " + sourceName
        );
    }
    @Test
    public void kaynakSilme() {

        String sourceName = sourceFlow.createSource();
        sourceFlow.deleteSource(sourceName);

        Assert.assertTrue(
                openDataPage.isSourceDeleted(sourceName),
                "Kaynak silinemedi: " + sourceName
        );
    }
    @Test
    public void kaynakGoruntuleme() {

        String sourceName =
                sourceFlow.createSource();

        sourceFlow.openSource(sourceName);

        // Görüntüleme ekranındaki doğrulamalar daha sonra eklenecek.
    }
    @Test
    public void kaynakDuzenleme() {

        String sourceName =
                sourceFlow.createSource();

        sourceFlow.editSource(sourceName);

        // Düzenleme alanlarının doğrulamaları daha sonra eklenecek.
    }
    @Test
    public void veriSetiOnayaGonderme() {

        String dataSetName = createFlow.createDataSetForApproval();
        publishPage.clickPublishTab();
        String expectedStatus = "KVKK Onayında";
        publishPage.waitForStatus(
                dataSetName,
                expectedStatus
        );

        String actualStatus = publishPage.getStatus(dataSetName);

        log.info(
                "Veri Seti: {} | Beklenen Status: {} | Mevcut Status: {}",
                dataSetName,
                expectedStatus,
                actualStatus
        );
    }

}