package com.bvakp.automation.flows.OpenDataSourceFlow;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

public class OpenSource {

    private final OpenDataPage openDataPage;


    /**
     * Kaynak görüntüleme flow'unu hazırlar.
     */
    public OpenSource(Page page) {

        this.openDataPage =
                new OpenDataPage(page);
    }


    /**
     * Verilen kaynağın görüntüleme ekranını açar.
     */
    public void openSource(
            String kaynakAdi) {

        openDataPage
                .kaynakGoruntulemeButonunaTiklama(
                        kaynakAdi
                );
    }
}