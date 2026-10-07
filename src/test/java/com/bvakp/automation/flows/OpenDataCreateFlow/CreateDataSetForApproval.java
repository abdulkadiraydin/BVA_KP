package com.bvakp.automation.flows.OpenDataCreateFlow;

import com.bvakp.automation.pages.openData.OpenDataPage;
import com.microsoft.playwright.Page;

public class CreateDataSetForApproval {

    private final OpenDataPage openDataPage;


    /**
     * Veri seti onaya gönderme flow'unu hazırlar.
     */
    public CreateDataSetForApproval(Page page) {

        this.openDataPage =
                new OpenDataPage(page);
    }


    /**
     * Kaynak üzerinden veri seti oluşturur
     * ve KVKK onay sürecine gönderir.
     */
    public void createDataSetForApproval(
            String kaynakAdi,
            String veriSetiAdi,
            String sorumluBirim,
            String kategori,
            String kullanimLisansi,
            String aciklama) {

        openDataPage
                .kaynakSecme(
                        kaynakAdi
                );

        openDataPage
                .veriSetiIleriGitme();

        openDataPage
                .yilAlaninaMaskeleKuraliUygulama();

        openDataPage
                .veriSetiAdiGirme(
                        veriSetiAdi
                );

        openDataPage
                .sorumluBirimSecme(
                        sorumluBirim
                );

        openDataPage
                .kategoriSecme(
                        kategori
                );

        openDataPage
                .kullanimLisansiSecme(
                        kullanimLisansi
                );

        openDataPage
                .veriSetiAciklamasiGirme(
                        aciklama
                );

        openDataPage
                .veriSetiOnizleme();

        openDataPage
                .veriSetiOnayaGonderme();

        openDataPage
                .veriSetiOnayaGondermeyiOnaylama();
    }
}