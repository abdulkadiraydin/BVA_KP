package com.bvakp.automation.companents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class KaynakDuzenleModal {

    private final Page page;

    private final Locator adAlani;
    private final Locator tabloAlani;
    private final Locator aciklamaAlani;
    private final Locator modalBasligi;

    public KaynakDuzenleModal(Page page) {

        this.page = page;

        this.adAlani =
                page.getByLabel("Ad *");

        this.tabloAlani =
                page.getByLabel("Tablo *");

        this.aciklamaAlani =
                page.getByLabel("Açıklama");

        this.modalBasligi =
                page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions()
                                .setName("Kaynağı Düzenle")
                                .setExact(true)
                );
    }

    /**
     * Kaynağı Düzenle modalının açıldığını kontrol eder.
     */
    public boolean kaynakDuzenleModalGoruntulendiMi() {

        modalBasligi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return modalBasligi.isVisible();
    }

    /**
     * Kaynak adını değiştirir.
     */
    public void adDegistirme(String yeniKaynakAdi) {

        adAlani.fill(
                yeniKaynakAdi
        );
    }

    /**
     * Kaynak açıklamasını değiştirir.
     */
    public void aciklamaDegistirme(String yeniAciklama) {

        aciklamaAlani.fill(
                yeniAciklama
        );
    }

    /**
     * Kaynağın bağlı olduğu tabloyu değiştirir.
     */
    public void tabloDegistirme(String tabloAdi) {

        tabloAlani.click();

        page.getByRole(
                AriaRole.OPTION,
                new Page.GetByRoleOptions()
                        .setName(tabloAdi)
                        .setExact(true)
        ).click();
    }
    /**
     * Tablo değişikliğinden sonra Veri Önizleme alanını kontrol eder.
     */
    public boolean veriOnizlemeGoruntulendiMi() {

        Locator veriOnizleme =
                page.getByText(
                        "Veri Önizleme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        veriOnizleme.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return veriOnizleme.isVisible();
    }

    /**
     * Veri Önizleme alanındaki veri satırı sayısını döndürür.
     */
    public int veriOnizlemeSatirSayisiAlma() {

        Locator veriOnizleme =
                page.getByText(
                        "Veri Önizleme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        Locator satirlar =
                veriOnizleme.locator(
                        "xpath=following::table[1]//tbody/tr"
                );

        satirlar.first().waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return satirlar.count();
    }

    /**
     * Kaynak değişikliklerini kaydeder.
     */
    public void kaydetme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaydet")
                        .setExact(true)
        ).click();
    }

    /**
     * Kaynağın başarıyla güncellendiği mesajını kontrol eder.
     */
    public boolean guncellemeBasariliMesajiGoruntulendiMi(
            String kaynakAdi) {

        Locator mesaj =
                page.getByText(
                        "\"" + kaynakAdi + "\" kaynağı güncellendi.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        mesaj.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return mesaj.isVisible();
    }
}