package com.bvakp.automation.flows;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;

public class OpenDataSetupFlow {

    private final OpenDataCreateFlow createFlow;
    private final OpenDataPublishPage publishPage;
    private final OpenDataPublishFlow publishFlow;

    public OpenDataSetupFlow(OpenDataCreateFlow createFlow,
                             OpenDataPublishPage publishPage,
                             OpenDataPublishFlow publishFlow) {
        this.createFlow = createFlow;
        this.publishPage = publishPage;
        this.publishFlow = publishFlow;
    }

    /** Kaynak + veri seti oluşturur, onaylar, yayınlar. Yayındaki veri seti adını döndürür. */
    public String yayindaVeriSetiOlusturma(String adOnEki) {

        String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
        String dataSetName = TestDataUtil.dinamikAdOlusturma(adOnEki);

        ReportManager.info("Ön koşul: yayında veri seti hazırlanıyor: " + dataSetName);

        createFlow.createSource(kaynakAdi, "arac_bakim_is_yeri_sayisi", "Takvim testi kaynağı");

        createFlow.createDataSetForApproval(
                kaynakAdi, dataSetName,
                "İşletme Dairesi Başkanlığı", "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı", "Takvim testi veri seti");

        PlaywrightManager.getPage().waitForTimeout(3000);
        publishPage.clickPublishTab();

        publishPage.waitForStatus(dataSetName, "KVKK Onayında");
        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(dataSetName, "Yayın Onayında");
        publishFlow.publishDataSet(dataSetName);

        publishPage.waitForStatus(dataSetName, "Yayında");
        publishPage.waitForPortalStatus(dataSetName, "Aktarıldı");

        return dataSetName;
    }
}