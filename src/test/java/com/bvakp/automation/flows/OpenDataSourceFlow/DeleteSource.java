package com.bvakp.automation.flows.OpenDataSourceFlow;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

public class DeleteSource {

    private final OpenDataPage openDataPage;


    /**
     * Kaynak silme flow'unu hazırlar.
     */
    public DeleteSource(Page page) {

        this.openDataPage =
                new OpenDataPage(page);
    }


    /**
     * Verilen kaynağı siler.
     */
    public void deleteSource(
            String kaynakAdi) {

        openDataPage
                .kaynakSilmeButonunaTiklama(
                        kaynakAdi
                );

        openDataPage
                .kaynakSilmeOnayiVerme();
    }
}