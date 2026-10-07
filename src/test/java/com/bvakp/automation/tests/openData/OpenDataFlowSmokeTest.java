package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.flows.OpenDataCreateFlow.CreateDataSetForApproval;
import com.bvakp.automation.flows.OpenDataSourceFlow.CreateSource;
import com.bvakp.automation.flows.OpenDataSourceFlow.DeleteSource;
import com.bvakp.automation.flows.OpenDataSourceFlow.EditSource;
import com.bvakp.automation.flows.OpenDataSourceFlow.OpenSource;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenDataFlowSmokeTest extends AvpBaseTest {


    /**
     * OpenDataSourceFlow altındaki kaynak
     * işlemlerinin çalıştığını kontrol eder.
     */
    @Test
    public void sourceFlowKontrolu() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        CreateSource createSource =
                new CreateSource(page);

        EditSource editSource =
                new EditSource(page);

        OpenSource openSource =
                new OpenSource(page);

        DeleteSource deleteSource =
                new DeleteSource(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_flow"
                );

        String yeniKaynakAdi =
                kaynakAdi
                        + "_duzenlendi";

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";

        String aciklama =
                "Flow kaynak testi - "
                        + kaynakAdi;

        String yeniAciklama =
                "Flow kaynak testi güncellendi - "
                        + yeniKaynakAdi;


        /*
         * Portal hazırlanır.
         */
        avpOturumuHazirlama();

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * CREATE SOURCE
         */
        createSource.createSource(
                kaynakAdi,
                tabloAdi,
                aciklama
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "CreateSource flow kaynağı oluşturamadı."
        );


        /*
         * EDIT SOURCE
         */
        editSource.editSource(
                kaynakAdi,
                yeniKaynakAdi,
                yeniAciklama
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                yeniKaynakAdi
                        ),
                "EditSource flow kaynağı güncelleyemedi."
        );


        /*
         * OPEN SOURCE
         */
        openSource.openSource(
                yeniKaynakAdi
        );


        Assert.assertTrue(
                openDataPage
                        .genelBilgiSayfasiGoruntulendiMi(),
                "OpenSource flow kaynak görüntüleme ekranını açamadı."
        );


        /*
         * Tekrar kaynak listesine dönülür.
         */
        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * DELETE SOURCE
         */
        deleteSource.deleteSource(
                yeniKaynakAdi
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakListedenSilindiMi(
                                yeniKaynakAdi
                        ),
                "DeleteSource flow kaynağı silemedi."
        );
    }


    /**
     * OpenDataCreateFlow altında bulunan
     * veri seti oluşturma ve onaya gönderme
     * flow'unu kontrol eder.
     */
    @Test
    public void createDataSetForApprovalFlowKontrolu() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);

        CreateSource createSource =
                new CreateSource(page);

        CreateDataSetForApproval createDataSetForApproval =
                new CreateDataSetForApproval(page);


        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_flow_onay_kaynak"
                );

        String kaynakAciklamasi =
                "Flow onay kaynak testi - "
                        + kaynakAdi;

        String tabloAdi =
                "arac_bakim_is_yeri_sayisi";


        String veriSetiAdi =
                TestDataUtil.dinamikAdOlusturma(
                        "otomasyon_flow_veri_seti"
                );

        String veriSetiAciklamasi =
                "Flow veri seti onay testi - "
                        + veriSetiAdi;


        avpOturumuHazirlama();

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Flow'un kullanacağı kaynak hazırlanır.
         */
        createSource.createSource(
                kaynakAdi,
                tabloAdi,
                kaynakAciklamasi
        );


        Assert.assertTrue(
                openDataPage
                        .kaynakListedeMi(
                                kaynakAdi
                        ),
                "Flow testi için kaynak oluşturulamadı."
        );


        /*
         * Veri seti oluşturulur ve onaya gönderilir.
         */
        createDataSetForApproval
                .createDataSetForApproval(
                        kaynakAdi,
                        veriSetiAdi,
                        "İşletme Dairesi Başkanlığı",
                        "Altyapı & Şebeke",
                        "TCDD Taşımacılık Açık Veri Lisansı",
                        veriSetiAciklamasi
                );


        /*
         * Flow tamamlandıktan sonra yalnızca
         * sonuç doğrulanır.
         */
        Assert.assertTrue(
                openDataPage
                        .veriSetiKvkkOnayinaGonderildiMi(),
                "CreateDataSetForApproval flow veri setini KVKK onayına gönderemedi."
        );
    }
}