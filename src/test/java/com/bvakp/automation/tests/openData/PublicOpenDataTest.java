package com.bvakp.automation.tests.openData;

import com.bvakp.automation.api.DataStoreApi;
import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.PublicDataSetPage;
import com.bvakp.automation.pages.openData.PublicOpenDataPage;
import com.bvakp.automation.utils.TestDataUtil;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PublicOpenDataTest extends AvpBaseTest {

    private final ThreadLocal<TestContext> testContextThread =
            new ThreadLocal<>();


    /**
     * Tek bir test metodunun kullanacağı Page, Page Object,
     * Flow ve API nesnelerini birlikte tutar.
     */
    private record TestContext(
            Page page,
            OpenDataPage openDataPage,
            OpenDataPublishPage publishPage,
            OpenDataPublishFlow publishFlow,
            OpenDataCreateFlow openDataCreateFlow,
            PublicOpenDataPage publicOpenDataPage,
            DataStoreApi dataStoreApi) {
    }


    /**
     * Her test öncesinde aktif thread için AVP oturumunu hazırlar
     * ve yalnızca o teste ait Page Object, Flow ve API nesnelerini oluşturur.
     */
    @BeforeMethod
    public void setUpPublicOpenData() {

        avpOturumuHazirlama();

        Page aktifPage =
                page();

        OpenDataPage openDataPage =
                new OpenDataPage(
                        aktifPage
                );

        openDataPage.clickAcikVeriPortali();


        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(
                        aktifPage
                );

        OpenDataPublishFlow publishFlow =
                new OpenDataPublishFlow(
                        publishPage
                );

        OpenDataCreateFlow openDataCreateFlow =
                new OpenDataCreateFlow(
                        aktifPage
                );

        PublicOpenDataPage publicOpenDataPage =
                new PublicOpenDataPage(
                        aktifPage
                );

        DataStoreApi dataStoreApi =
                new DataStoreApi(
                        PlaywrightManager.getApiRequestContext()
                );


        testContextThread.set(
                new TestContext(
                        aktifPage,
                        openDataPage,
                        publishPage,
                        publishFlow,
                        openDataCreateFlow,
                        publicOpenDataPage,
                        dataStoreApi
                )
        );
    }


    /**
     * Aktif test thread'ine ait context bilgisini döndürür.
     *
     * @return aktif test context'i
     */
    private TestContext context() {

        TestContext context =
                testContextThread.get();

        if (context == null) {

            throw new IllegalStateException(
                    "PublicOpenDataTest context oluşturulmamış."
            );
        }

        return context;
    }


    /**
     * Test tamamlandıktan sonra thread üzerinde tutulan
     * context referansını temizler.
     */
    @AfterMethod(alwaysRun = true)
    public void publicOpenDataContextTemizleme() {

        testContextThread.remove();
    }


    /**
     * Yayınlanan veri setinin Public Açık Veri Portalında
     * görüntülendiğini ve DataStore API üzerinden erişilebildiğini doğrular.
     */
    @Test
    public void yayinlananVeriSetiPublicPortalKontrolu() {

        TestContext context =
                context();

        OpenDataCreateFlow openDataCreateFlow =
                context.openDataCreateFlow();

        OpenDataPublishFlow publishFlow =
                context.publishFlow();

        PublicOpenDataPage publicOpenDataPage =
                context.publicOpenDataPage();

        DataStoreApi dataStoreApi =
                context.dataStoreApi();


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_kaynak"
                );

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_public_api"
                );


        openDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Public portal API testi kaynağı"
        );


        openDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Public portal API testi veri seti"
        );


        publishFlow.approveKvkk(
                dataSetName
        );

        publishFlow.publishDataSet(
                dataSetName
        );


        publicOpenDataPage.open();

        publicOpenDataPage.selectCategory(
                "Altyapı & Şebeke"
        );

        publicOpenDataPage.searchDataSet(
                dataSetName
        );

        publicOpenDataPage.clickDataSet(
                dataSetName
        );


        PublicDataSetPage publicDataSetPage =
                new PublicDataSetPage(
                        context.page()
                );


        publicDataSetPage.clickApi();


        String apiUrl =
                publicDataSetPage.getApiUrl();

        String resourceId =
                publicDataSetPage.getResourceId();


        APIResponse response =
                dataStoreApi.getRecords(
                        apiUrl,
                        resourceId,
                        5
                );


        Assert.assertEquals(
                response.status(),
                200,
                "DataStore API beklenen HTTP 200 durum kodunu döndürmedi."
        );


        Assert.assertTrue(
                dataStoreApi.isDataStoreApiSuccessful(
                        response
                ),
                "DataStore API yanıtı başarılı değil."
        );


        Assert.assertTrue(
                dataStoreApi.isApiSuccess(
                        response
                ),
                "API success alanı beklenen değerde değil."
        );
    }
}