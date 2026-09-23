package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class OpenDataPage extends BasePage {

    private final Locator acikVeriPortali;
    private final Locator kaynakEkleButton;
    private final Locator adInput;
    private final Locator tabloInput;
    private final Locator ilkTablo;
    private final Locator aciklamaInput;
    private final Locator kaynakEkleSubmitButton;
    private final Locator ileriButton;

    public OpenDataPage(Page page) {

        super(page);

        this.acikVeriPortali = page.getByText("Açık Veri Portalı",
                new Page.GetByTextOptions().setExact(true));

        this.kaynakEkleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        );

        this.adInput = page.locator("input[name='name']");

        this.tabloInput = page.locator("input[role='combobox']");
        this.ilkTablo = page.locator("[role='option']").first();
        this.aciklamaInput = page.locator("textarea[name='description']");
        this.kaynakEkleSubmitButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        ).last();
        this.ileriButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İleri")
        );
    }

    public void clickAcikVeriPortali() {
        acikVeriPortali.click();
    }
    public void clickKaynakEkle() {
        kaynakEkleButton.click();
    }
    public void enterAd(String ad) {
        adInput.fill(ad);
    }
    public void clickTablo() {
        tabloInput.click();
    }
    public void selectFirstTable() {
        ilkTablo.click();
    }
    public void enterAciklama(String aciklama) {
        aciklamaInput.fill(aciklama);
    }
    public void clickKaynakEkleSubmit() {
        kaynakEkleSubmitButton.click();
    }

    public void selectDataSet(String dataSetName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions().setHasText(dataSetName));

        row.getByRole(
                AriaRole.CHECKBOX,
                new Locator.GetByRoleOptions().setName("Seçim")
        ).check();
    }
    public void clickIleri() {
        ileriButton.click();
    }
}