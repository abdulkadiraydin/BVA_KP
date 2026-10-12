package com.bvakp.automation.flows.OpenDataSourceFlow;

import com.bvakp.automation.companents.KaynakEkleModal;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

public class CreateSource {

    private final OpenDataPage openDataPage;
    private final KaynakEkleModal kaynakEkleModal;


    /**
     * Kaynak oluşturma flow'unu hazırlar.
     */
    public CreateSource(Page page) {

        this.openDataPage =
                new OpenDataPage(page);

        this.kaynakEkleModal =
                new KaynakEkleModal(page);
    }


    /**
     * Yeni kaynak oluşturur.
     */
    public void createSource(
            String kaynakAdi,
            String tabloAdi,
            String aciklama) {

        openDataPage
                .kaynakEklemeEkraniniAcma();

        kaynakEkleModal
                .kaynakAdiGirme(
                        kaynakAdi
                );

        kaynakEkleModal
                .kaynakTablosuSecme(
                        tabloAdi
                );

        kaynakEkleModal
                .kaynakAciklamasiGirme(
                        aciklama
                );

        kaynakEkleModal
                .kaynakEkleme();
    }
}