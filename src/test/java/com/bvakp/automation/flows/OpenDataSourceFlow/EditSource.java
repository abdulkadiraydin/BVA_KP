package com.bvakp.automation.flows.OpenDataSourceFlow;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

public class EditSource {

    private final OpenDataPage openDataPage;


    /**
     * Kaynak düzenleme flow'unu hazırlar.
     */
    public EditSource(Page page) {

        this.openDataPage =
                new OpenDataPage(page);
    }


    /**
     * Kaynak adı ve açıklamasını günceller.
     */
    public void editSource(
            String kaynakAdi,
            String yeniKaynakAdi,
            String yeniAciklama) {

        openDataPage
                .kaynakDuzenlemeButonunaTiklama(
                        kaynakAdi
                );

        openDataPage
                .kaynakAdiniGuncelleme(
                        yeniKaynakAdi
                );

        openDataPage
                .kaynakAciklamasiniGuncelleme(
                        yeniAciklama
                );

        openDataPage
                .kaynakDuzenlemeyiKaydetme();
    }
}