package com.bvakp.automation.companents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class KaynakEkleModal {

    private final Page page;

    private final Locator adAlani;
    private final Locator tabloAlani;
    private final Locator aciklamaAlani;
    private final Locator veriOnizleme;
    private final Locator kaynakEkleButonu;


    /**
     * Kaynak Ekle modalındaki elementleri tanımlar.
     *
     * @param page aktif Playwright sayfası
     */
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

        this.veriOnizleme =
                page.getByText(
                        "Veri Önizleme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        this.kaynakEkleButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Kaynak Ekle")
                                .setExact(true)
                );
    }


    /**
     * Kaynak Ekle formundaki temel alanların
     * görüntülendiğini kontrol eder.
     *
     * @return form alanları görünüyorsa true
     */
    public boolean kaynakEkleFormuGoruntulendiMi() {

        try {

            adAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            tabloAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            aciklamaAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return adAlani.isVisible()
                    && tabloAlani.isVisible()
                    && aciklamaAlani.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak adı alanına verilen değeri girer.
     *
     * @param kaynakAdi oluşturulacak kaynak adı
     */
    public void kaynakAdiGirme(
            String kaynakAdi) {

        adAlani.fill(
                kaynakAdi
        );
    }


    /**
     * Kaynak adı alanındaki mevcut değeri döndürür.
     *
     * @return kaynak adı alanındaki değer
     */
    public String kaynakAdiAlma() {

        return adAlani
                .inputValue()
                .trim();
    }


    /**
     * Tablo seçim alanını açar ve belirtilen
     * kaynak tablosunu seçer.
     *
     * @param tabloAdi seçilecek tablo adı
     */
    public void kaynakTablosuSecme(
            String tabloAdi) {

        tabloAlani.click();

        Locator tabloSecenegi =
                page.getByText(
                        tabloAdi,
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        tabloSecenegi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        tabloSecenegi.click();
    }


    /**
     * Tablo alanındaki seçili değeri döndürür.
     *
     * @return seçili tablo adı
     */
    public String seciliTabloyuAlma() {

        return tabloAlani
                .inputValue()
                .trim();
    }


    /**
     * Kaynak açıklaması alanına verilen değeri girer.
     *
     * @param aciklama kaynak açıklaması
     */
    public void kaynakAciklamasiGirme(
            String aciklama) {

        aciklamaAlani.fill(
                aciklama
        );
    }


    /**
     * Kaynak açıklaması alanındaki mevcut değeri döndürür.
     *
     * @return açıklama alanındaki değer
     */
    public String kaynakAciklamasiAlma() {

        return aciklamaAlani
                .inputValue()
                .trim();
    }


    /**
     * Tablo seçimi sonrasında Veri Önizleme
     * bölümünün görüntülendiğini kontrol eder.
     *
     * @return Veri Önizleme görünüyorsa true
     */
    public boolean veriOnizlemeGoruntulendiMi() {

        try {

            veriOnizleme.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return veriOnizleme.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak Ekle butonuna tıklayarak
     * kaynak oluşturma işlemini tamamlar.
     */
    public void kaynakEkleme() {

        kaynakEkleButonu.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        kaynakEkleButonu.click();
    }


    /**
     * Kaynak oluşturma sonrasında formun
     * kapandığını kontrol eder.
     *
     * @return form kapandıysa true
     */
    public boolean kaynakEkleFormuKapandiMi() {

        try {

            adAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(15000)
            );

            return true;

        } catch (PlaywrightException e) {

            return false;
        }
    }
    /**
     * Kaynak adı zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean kaynakAdiZorunluMesajiGoruntulendiMi() {

        return page.getByText(
                "Kaynak adı zorunludur.",
                new Page.GetByTextOptions()
                        .setExact(true)
        ).isVisible();
    }


    /**
     * Tablo zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean tabloZorunluMesajiGoruntulendiMi() {

        return page.getByText(
                "Bir tablo seçin.",
                new Page.GetByTextOptions()
                        .setExact(true)
        ).isVisible();
    }
    /**
     * Kaynak Ekle formunu açar.
     */
    public void kaynakEkleButonunaTiklama() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
                        .setExact(true)
        ).click();
    }

}