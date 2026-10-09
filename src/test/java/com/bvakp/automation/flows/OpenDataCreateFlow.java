package com.bvakp.automation.flows;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.utils.TestDataUtil;

public class OpenDataCreateFlow {

    private final OpenDataPage openDataPage;
    private final OpenDataSourceFlow sourceFlow;

    public OpenDataCreateFlow(OpenDataPage openDataPage, OpenDataSourceFlow sourceFlow) {

        this.openDataPage = openDataPage;
        this.sourceFlow = sourceFlow;
    }

    public String createDataSetForApproval() {
        String sourceName = sourceFlow.createSource();
        openDataPage.clickAcikVeriPortali();

        openDataPage.selectDataSet(sourceName);
        openDataPage.clickIleri();
        String dataSetName = TestDataUtil.dinamikAdOlusturma("otomasyon_veri_seti");
        openDataPage.enterDataSetName(dataSetName);

        openDataPage.selectSorumluBirim(
                "İşletme Dairesi Başkanlığı"
        );

        openDataPage.selectKategori(
                "Altyapı & Şebeke"
        );

        openDataPage.selectLisans(
                "TCDD Taşımacılık Açık Veri Lisansı"
        );

        openDataPage.enterDataSetAciklama(
                "Test veri seti açıklaması"
        );

        openDataPage.selectGuncellemeTipi(
                "Periyodik"
        );

        openDataPage.enterTakvimBaslangici(
                "25.09.2026"
        );

        openDataPage.enterSiklik("1");

        openDataPage.selectAralik("Hafta");
        //openDataPage.selectBolgeAdiAnonimlestirmeKurali();

        openDataPage.clickOnizle();
        openDataPage.clickOnayaGonder();
        openDataPage.confirmOnayaGonder();
        return dataSetName;
    }
}