package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DataSetCreationPage extends BasePage {

    private final Locator dataSetNameInput;
    private final Locator sorumluBirimSelect;
    private final Locator kategoriSelect;
    private final Locator lisansSelect;
    private final Locator aciklamaInput;
    private final Locator guncellemeTipiSelect;
    private final Locator takvimBaslangiciInput;
    private final Locator siklikInput;
    private final Locator aralikSelect;
    private final Locator onizleButton;
    private final Locator onayaGonderButton;
    private final Locator onayaGonderConfirmButton;
    private final Locator veriSetiYonetimiTab;
    public DataSetCreationPage(Page page) {

        super(page);

        this.dataSetNameInput = page.locator("input[name='name']");
        this.sorumluBirimSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Sorumlu Birim")
        );
        this.kategoriSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Kategori")
        );
        this.lisansSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName("Lisans")
        );
        this.aciklamaInput = page.locator("textarea[name='description']");
        this.guncellemeTipiSelect = page.locator(
                "div[role='combobox']"
        ).filter(
                new Locator.FilterOptions().setHasText("Manuel")
        );
        this.takvimBaslangiciInput = page.locator(
                "input[placeholder='DD.MM.YYYY']"
        );
        this.siklikInput = page.locator("input[type='number'][min='1']");
        this.aralikSelect = page.locator(
                "div[role='combobox'][aria-labelledby^='_r_bd_-label']"
        );
        this.onizleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Önizle")
        );
        this.onayaGonderButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        );
        this.onayaGonderConfirmButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        ).last();
        this.veriSetiYonetimiTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Veri Seti Yönetimi")
        );
    }

    public void enterDataSetName(String name) {
        dataSetNameInput.fill(name);
    }
    public void selectSorumluBirim(String birim) {

        sorumluBirimSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions().setHasText(birim))
                .click();
    }
    public void selectKategori(String kategori) {

        kategoriSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions().setHasText(kategori))
                .click();
    }
    public void selectLisans(String lisans) {

        lisansSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions().setHasText(lisans))
                .click();
    }
    public void enterAciklama(String aciklama) {
        aciklamaInput.fill(aciklama);
    }
    public void selectGuncellemeTipi(String tip) {

        guncellemeTipiSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions().setHasText(tip))
                .click();
    }
    public void enterTakvimBaslangici(String tarih) {
        takvimBaslangiciInput.fill(tarih);
    }
    public void enterSiklik(String siklik) {
        siklikInput.fill(siklik);
    }
    public void selectAralik(String aralikValue) {

        Locator aralikLabel = page.locator("label")
                .filter(new Locator.FilterOptions().setHasText("Aralık"));

        String forId = aralikLabel.getAttribute("for");

        page.locator("#" + forId).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions().setHasText(aralikValue))
                .click();
    }
    public void clickOnizle() {
        onizleButton.click();
    }
    public void clickOnayaGonder() {
        onayaGonderButton.click();
    }
    public void confirmOnayaGonder() {
        onayaGonderConfirmButton.click();
    }
}