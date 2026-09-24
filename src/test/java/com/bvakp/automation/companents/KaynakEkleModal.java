package com.bvakp.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class KaynakEkleModal {

    private final Page page;
    private final Locator adAlani;
    private final Locator tabloAlani;
    private final Locator aciklamaAlani;
    private final Locator veriOnizlemeBasligi;
    private final Locator veriOnizlemeSatirlari;
    private final Locator kaynakEkleButonu;

    /**
     * Kaynak Ekle modalı içerisindeki elementleri tanımlamak için kullanılır.
     *
     * @param page aktif Playwright sayfası
     */
    public KaynakEkleModal(Page page) {

        this.page = page;

        adAlani = page.getByLabel("Ad *");
        tabloAlani = page.getByLabel("Tablo *");
        aciklamaAlani = page.getByLabel("Açıklama");
        veriOnizlemeBasligi = page.getByText(
                "Veri Önizleme",
                new Page.GetByTextOptions().setExact(true)
        );

        veriOnizlemeSatirlari = page.locator(".MuiDataGrid-row");

        kaynakEkleButonu = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        );
    }

    /**
     * Kaynak Ekle modalının açıldığını doğrulamak için
     * Ad alanının görünürlüğünü kontrol eder.
     *
     * @return Ad alanı görünüyorsa true, görünmüyorsa false
     */
    public boolean kaynakEkleModalGoruntulenmeKontrolu() {

        adAlani.waitFor();

        return adAlani.isVisible();
    }

    /**
     * Kaynak Ekle modalındaki Ad alanına
     * verilen kaynak adını girmek için kullanılır.
     *
     * @param ad oluşturulacak kaynağın adı
     */
    public void adGirme(String ad) {
        adAlani.fill(ad);
    }

    /**
     * Kaynak Ekle modalındaki Ad alanında bulunan
     * mevcut değeri almak için kullanılır.
     *
     * @return Ad alanında yazılı olan değer
     */
    public String adDegeriAlma() {
        return adAlani.inputValue();
    }

    /**
     * Kaynak Ekle modalındaki Tablo alanından
     * verilen tablo adına göre seçim yapmak için kullanılır.
     *
     * @param tabloAdi seçilecek tablo adı
     */
    /**
     * Kaynak Ekle modalındaki Tablo alanından
     * verilen tablo adına göre seçim yapmak için kullanılır.
     *
     * @param tabloAdi seçilecek tablo adı
     */
    public void tabloSecme(String tabloAdi) {

        tabloAlani.click();

        page.getByRole(
                AriaRole.OPTION,
                new Page.GetByRoleOptions().setName(tabloAdi)
        ).click();
    }

    /**
     * Kaynak Ekle modalındaki Tablo alanında
     * seçili olan mevcut değeri almak için kullanılır.
     *
     * @return Tablo alanında seçili olan değer
     */
    public String tabloDegeriAlma() {
        return tabloAlani.inputValue();
    }

    /**
     * Kaynak Ekle modalındaki Açıklama alanına
     * verilen açıklama bilgisini girmek için kullanılır.
     *
     * @param aciklama oluşturulacak kaynak açıklaması
     */
    public void aciklamaGirme(String aciklama) {
        aciklamaAlani.fill(aciklama);
    }
    /**
     * Kaynak Ekle modalındaki Açıklama alanında
     * bulunan mevcut değeri almak için kullanılır.
     *
     * @return Açıklama alanında yazılı olan değer
     */
    public String aciklamaDegeriAlma() {
        return aciklamaAlani.inputValue();
    }

    /**
     * Tablo seçildikten sonra Veri Önizleme alanının
     * görüntülenip görüntülenmediğini kontrol etmek için kullanılır.
     *
     * @return Veri Önizleme alanı görünüyorsa true
     */
    public boolean veriOnizlemeGoruntulenmeKontrolu() {

        veriOnizlemeBasligi.waitFor();

        return veriOnizlemeBasligi.isVisible();
    }

    /**
     * Veri Önizleme alanında görüntülenen
     * veri satırı sayısını almak için kullanılır.
     *
     * @return önizleme tablosundaki satır sayısı
     */
    public int veriOnizlemeSatirSayisiAlma() {

        return veriOnizlemeSatirlari.count();
    }

    public void kaynakEkleme() {
        kaynakEkleButonu.click();
    }
}