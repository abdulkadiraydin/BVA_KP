package com.bvakp.automation.companents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class KaynakEkleModal {
    private final Page page;
    private final Locator adAlani;
    private final Locator tabloAlani;
    private final Locator aciklamaAlani;
    private final Locator kaynakEkleBasligi;

    public KaynakEkleModal(Page page) {

        this.page = page;

        this.adAlani =
                page.getByLabel(
                        "Ad *"
                );

        this.tabloAlani =
                page.getByLabel(
                        "Tablo *"
                );

        this.aciklamaAlani =
                page.getByLabel(
                        "Açıklama"
                );
        this.kaynakEkleBasligi =
                page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions()
                                .setName("Kaynak Ekle")
                                .setExact(true)
                );
    }
    /**
     * Kaynak adını girer.
     */
    public void adGirme(
            String kaynakAdi) {

        adAlani.fill(
                kaynakAdi
        );
    }

    /**
     * Tablo listesini açar ve listedeki ilk tabloyu seçer.
     */
    public void ilkTabloyuSecme() {

        tabloAlani.click();

        Locator ilkTablo = page.locator("[role='option']").first();

        ilkTablo.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        ilkTablo.click();
    }

    /**
     * Kaynak açıklamasını girer.
     */
    public void aciklamaGirme(
            String aciklama) {

        aciklamaAlani.fill(
                aciklama
        );
    }

    /**
     * Veri Önizleme bölümünün görüntülendiğini kontrol eder.
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
     * Kaynak ekleme işlemini tamamlar.
     */
    public void kaynakEkleme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
                        .setExact(true)
        ).click();
    }
    /**
     * Kaynak Ekle ekranının görüntülendiğini kontrol eder.
     */
    public boolean kaynakEkleModalGoruntulendiMi() {

        kaynakEkleBasligi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return kaynakEkleBasligi.isVisible();
    }
    /**
     * Veri Önizleme tablosundaki veri satırı sayısını döndürür.
     *
     * @return önizleme tablosundaki satır sayısı
     */
    public int veriOnizlemeSatirSayisiAlma() {

        Locator veriOnizlemeBasligi =
                page.getByText(
                        "Veri Önizleme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        veriOnizlemeBasligi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        Locator satirlar =
                veriOnizlemeBasligi.locator(
                        "xpath=following::table[1]//tbody/tr"
                );

        satirlar.first().waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return satirlar.count();
    }
    /**
     * Kaynak Ekle modalının kapandığını kontrol eder.
     *
     * @return modal kapandıysa true
     */
    public boolean kaynakEkleModalKapandiMi() {

        try {

            kaynakEkleBasligi.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    com.microsoft.playwright.options.WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(15000)
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }
    /**
     * Kaynak adı zorunluluk uyarısının görüntülendiğini kontrol eder.
     */
    public boolean kaynakAdiZorunluUyarisiGoruntulendiMi() {

        Locator uyari =
                page.getByText(
                        "Kaynak adı zorunludur.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        uyari.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return uyari.isVisible();
    }

    /**
     * Tablo zorunluluk uyarısının görüntülendiğini kontrol eder.
     */
    public boolean tabloZorunluUyarisiGoruntulendiMi() {

        Locator uyari =
                page.getByText(
                        "Bir tablo seçin.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        uyari.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return uyari.isVisible();
    }
}