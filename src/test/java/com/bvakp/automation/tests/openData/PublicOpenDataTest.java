package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.flows.OpenDataCreateFlow.CreateDataSetForApproval;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataSourceFlow.CreateSource;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.PublicDataSetPage;
import com.bvakp.automation.pages.openData.PublicOpenDataPage;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.bvakp.automation.api.DataStoreApi;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.microsoft.playwright.APIResponse;

public class PublicOpenDataTest extends AvpBaseTest {

    private PublicOpenDataPage publicOpenDataPage;
    private DataStoreApi dataStoreApi;

    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;

    private CreateSource createSource;
    private CreateDataSetForApproval createDataSetForApproval;
    private OpenDataPublishFlow publishFlow;

    @BeforeMethod
    public void setUpPublicOpenData() {

        avpOturumuHazirlama();

        openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        publishPage = new OpenDataPublishPage(page);

        publishPage = new OpenDataPublishPage(page);

        createSource = new CreateSource(page);
        createDataSetForApproval = new CreateDataSetForApproval(page);
        publishFlow = new OpenDataPublishFlow(publishPage);

        publicOpenDataPage = new PublicOpenDataPage(page);
        dataStoreApi = new DataStoreApi(
                PlaywrightManager.getApiRequestContext()
        );
    }
    @Test
    public void yayinlananVeriSetiPublicPortalKontrolu() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_public_api");

        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Public portal API testi kaynağı"
        );

        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Public portal API testi veri seti"
        );

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