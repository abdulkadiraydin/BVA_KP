package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;


public class AcikVeriPage extends BasePage {

    private final Locator veriSetiOlusturmaMetni;
    private final Locator veriSetiOlusturmaSekmesi;
    private final Locator kaynakEkleButonu;

    /**
     * Açık Veri Portalı ekranındaki gerekli elementleri
     * tanımlamak için kullanılır.
     *
     * @param page aktif Playwright sayfası
     */
    public AcikVeriPage(Page page) {
        super(page);
        veriSetiOlusturmaSekmesi = page.getByText(
                "Veri Seti Oluşturma",
                new Page.GetByTextOptions().setExact(true)
        ).first();

        veriSetiOlusturmaMetni = page
                .getByText(
                        "Veri Seti Oluşturma",
                        new Page.GetByTextOptions().setExact(true)
                )
                .first();

        kaynakEkleButonu = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("​ Kaynak Ekle")
        );
    }


    /**
     * Açık Veri Portalı ekranında Veri Seti Oluşturma bölümünün
     * görüntülenip görüntülenmediğini kontrol etmek için kullanılır.
     *
     * @return Veri Seti Oluşturma alanı görünüyorsa true, görünmüyorsa false
     */
    public boolean veriSetiOlusturmaGoruntulenmeKontrolu() {

        veriSetiOlusturmaMetni.waitFor();

        return veriSetiOlusturmaMetni.isVisible();
    }

    /**
     * Açık Veri Portalı ekranındaki Kaynak Ekle butonuna
     * tıklayarak yeni kaynak ekleme ekranını açmak için kullanılır.
     */
    public void kaynakEklemeEkraniniAcma() {
        kaynakEkleButonu.click();
    }

    /**
     * Kaynak ekleme işleminden sonra oluşturulan kaynağın
     * Kaynak Tablo / Sorgu listesinde görüntülendiğini
     * kontrol etmek için kullanılır.
     *
     * @param kaynakAdi listede aranacak kaynak adı
     * @return kaynak adı listede görünüyorsa true
     */
    public boolean kaynakListesindeGoruntulenmeKontrolu(String kaynakAdi) {

        Locator kaynak = page.getByText(
                kaynakAdi,
                new Page.GetByTextOptions().setExact(true)
        ).first();

        kaynak.waitFor();

        return kaynak.isVisible();
    }
}