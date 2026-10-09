package com.bvakp.automation.flows;

import com.bvakp.automation.companents.KaynakDuzenleModal;
import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.companents.KaynakSilModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.utils.TestDataUtil;
import com.microsoft.playwright.Page;

public class OpenDataSourceFlow {

    private final Page page;
    private final OpenDataPage openDataPage;

    public OpenDataSourceFlow(Page page, OpenDataPage openDataPage) {
        this.page = page;
        this.openDataPage = openDataPage;
    }


    public String createSource() {

        String sourceName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String description =
                "Otomasyon kaynak açıklaması - " + sourceName;

        KaynakEkleModal kaynakEkleModal =
                new KaynakEkleModal(page);
        openDataPage.clickKaynakEkle();

        kaynakEkleModal.adGirme(sourceName);
        kaynakEkleModal.ilkTabloyuSecme();
        kaynakEkleModal.aciklamaGirme(description);
        kaynakEkleModal.kaynakEkleme();

        return sourceName;
    }
    public void deleteSource(String sourceName) {

        openDataPage.deleteSource(sourceName);

        KaynakSilModal kaynakSilModal =
                new KaynakSilModal(page);

        kaynakSilModal.silmeOnaylama();
    }
    public void openSource(String sourceName) {

        openDataPage.openSource(sourceName);
    }
    public void editSource(String sourceName) {

        openDataPage.editSource(sourceName);

        KaynakDuzenleModal kaynakDuzenleModal =
                new KaynakDuzenleModal(page);

        kaynakDuzenleModal.adDegistirme(
                "Güncellenmiş Kaynak"
        );

        kaynakDuzenleModal.aciklamaDegistirme(
                "Güncellenmiş kaynak açıklaması"
        );

        kaynakDuzenleModal.kaydetme();
    }
}