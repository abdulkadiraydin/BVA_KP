package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataSourceFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.PublicDataSetPage;
import com.bvakp.automation.pages.openData.PublicOpenDataPage;
import com.bvakp.automation.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.bvakp.automation.api.DataStoreApi;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.microsoft.playwright.APIResponse;

public class PublicOpenDataTest extends BaseTest {

    private PublicOpenDataPage publicOpenDataPage;
    private DataStoreApi dataStoreApi;

    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;

    private OpenDataSourceFlow sourceFlow;
    private OpenDataCreateFlow createFlow;
    private OpenDataPublishFlow publishFlow;

    @BeforeMethod
    public void setUpPublicOpenData() {

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
        createFlow = new OpenDataCreateFlow(openDataPage, sourceFlow);
        publishFlow = new OpenDataPublishFlow(publishPage);

        publicOpenDataPage = new PublicOpenDataPage(page);
        dataStoreApi = new DataStoreApi(
                PlaywrightManager.getApiRequestContext()
        );
    }
    @Test
    public void yayinlananVeriSetiPublicPortalKontrolu() {

        String dataSetName = createFlow.createDataSetForApproval();

        publishFlow.approveKvkk(dataSetName);

        publishFlow.publishDataSet(dataSetName);

        publicOpenDataPage.open();

        publicOpenDataPage.selectCategory("Altyapı & Şebeke");

        publicOpenDataPage.searchDataSet(dataSetName);

        publicOpenDataPage.clickDataSet(dataSetName);

        PublicDataSetPage publicDataSetPage =
                new PublicDataSetPage(page);

        publicDataSetPage.clickApi();

        String apiUrl = publicDataSetPage.getApiUrl();
        String resourceId = publicDataSetPage.getResourceId();

        APIResponse response = dataStoreApi.getRecords(
                apiUrl,
                resourceId,
                5
        );

        Assert.assertEquals(response.status(), 200);
        Assert.assertTrue(dataStoreApi.isDataStoreApiSuccessful(response));
        Assert.assertTrue(dataStoreApi.isApiSuccess(response));
    }
}