package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class OpenDataPage extends BasePage {

    // =========================
    // Açık Veri / Kaynak
    // =========================

    private final Locator acikVeriPortali;
    private final Locator kaynakEkleButton;
    private final Locator adInput;
    private final Locator tabloInput;
    private final Locator ilkTablo;
    private final Locator aciklamaInput;
    private final Locator kaynakEkleSubmitButton;
    private final Locator ileriButton;

    // =========================
    // Veri Seti Oluşturma
    // =========================

    private final Locator dataSetNameInput;
    private final Locator sorumluBirimSelect;
    private final Locator kategoriSelect;
    private final Locator lisansSelect;
    private final Locator dataSetAciklamaInput;
    private final Locator guncellemeTipiSelect;
    private final Locator takvimBaslangiciInput;
    private final Locator siklikInput;
    private final Locator aralikSelect;
    private final Locator bolgeAdiAnonimlestirmeSelect;
    private final Locator onizleButton;
    private final Locator onayaGonderButton;
    private final Locator onayaGonderConfirmButton;

    public OpenDataPage(Page page) {

        super(page);

        // =========================
        // Açık Veri / Kaynak
        // =========================

        this.acikVeriPortali = page.getByText(
                "Açık Veri Portalı",
                new Page.GetByTextOptions().setExact(true)
        );

        this.kaynakEkleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        );

        this.adInput = page.locator("input[name='name']");

        this.tabloInput = page.locator(
                "input[role='combobox']"
        );

        this.ilkTablo = page.locator(
                "[role='option']"
        ).first();

        this.aciklamaInput = page.locator(
                "textarea[name='description']"
        );

        this.kaynakEkleSubmitButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        ).last();

        this.ileriButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İleri")
        );

        // =========================
        // Veri Seti Oluşturma
        // =========================

        this.dataSetNameInput = page.locator(
                "input[name='name']"
        );

        this.sorumluBirimSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Sorumlu Birim")
        );

        this.kategoriSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Kategori")
        );

        this.lisansSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Lisans")
        );

        this.dataSetAciklamaInput = page.locator(
                "textarea[name='description']"
        );

        this.guncellemeTipiSelect = page.locator(
                "div[role='combobox']"
        ).filter(
                new Locator.FilterOptions().setHasText("Manuel")
        );

        this.takvimBaslangiciInput = page.locator(
                "input[placeholder='DD.MM.YYYY']"
        );

        this.siklikInput = page.locator(
                "input[type='number'][min='1']"
        );

        this.aralikSelect = page.locator(
                "div[role='combobox'][aria-labelledby^='_r_bd_-label']"
        );

        this.onizleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Önizle")
        );
        this.bolgeAdiAnonimlestirmeSelect =
                page.locator("[role='row'][data-id='bolge_no']")
                        .locator("[data-field='__rule']")
                        .getByRole(AriaRole.COMBOBOX);

        this.onayaGonderButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        );

        this.onayaGonderConfirmButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        ).last();
    }

    // =========================
    // Açık Veri
    // =========================

    public void clickAcikVeriPortali() {
        acikVeriPortali.click();
    }

    // =========================
    // Kaynak Oluşturma
    // =========================

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

    public boolean isSourceDisplayed(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        return row.isVisible();
    }

    public void deleteSource(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        // Satırdaki Sil
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Sil")
        ).click();

        // Popup'ın açılmasını bekle
        page.waitForTimeout(500);

        // Popup'taki Sil
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Sil")
                        .setExact(true)
        ).last().click();

        // Backend işleminin tamamlanmasını bekle
        page.waitForTimeout(2000);

        page.reload();
    }

    public boolean isSourceDeleted(String sourceName) {

        Locator source = page.locator(
                "[role='gridcell'][data-field='name']"
        ).filter(
                new Locator.FilterOptions()
                        .setHasText(sourceName)
        );

        return source.count() == 0;
    }

    // =========================
    // Mevcut Veri Setini Seçme
    // =========================

    public void selectDataSet(String dataSetName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(dataSetName));

        row.getByRole(
                AriaRole.CHECKBOX,
                new Locator.GetByRoleOptions()
                        .setName("Seçim")
        ).check();
    }

    public void clickIleri() {
        ileriButton.click();
    }

    // =========================
    // Veri Seti Oluşturma
    // =========================

    public void enterDataSetName(String name) {
        dataSetNameInput.fill(name);
    }

    public void selectSorumluBirim(String birim) {

        sorumluBirimSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(birim))
                .click();
    }

    public void selectKategori(String kategori) {

        kategoriSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(kategori))
                .click();
    }

    public void selectLisans(String lisans) {

        lisansSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(lisans))
                .click();
    }

    public void enterDataSetAciklama(String aciklama) {
        dataSetAciklamaInput.fill(aciklama);
    }

    public void selectGuncellemeTipi(String tip) {

        guncellemeTipiSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(tip))
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
                .filter(new Locator.FilterOptions()
                        .setHasText("Aralık"));

        String forId = aralikLabel.getAttribute("for");

        page.locator("#" + forId).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(aralikValue))
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

    public void veriSetiOlusturmaSekmesineGitme() {

        page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions()
                        .setName("Veri Seti Oluşturma")
        ).click();
    }
    public void selectBolgeAdiAnonimlestirmeKurali() {

        bolgeAdiAnonimlestirmeSelect.click();

        page.locator("li[role='option'][data-value='MASK']")
                .click();
    }

    /**
     * Kaynak Ekle ekranını açar.
     */
    public void kaynakEkleEkraniniAcma() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
        ).click();
    }

    /**
     * Oluşturulan kaynağın kaynak listesinde görüntülendiğini kontrol eder.
     *
     * @param kaynakAdi doğrulanacak kaynak adı
     * @return kaynak listede görünüyorsa true
     */
    public boolean kaynakListesindeGoruntulendiMi(
            String kaynakAdi) {

        Locator kaynak =
                page.getByRole(
                        AriaRole.GRIDCELL,
                        new Page.GetByRoleOptions()
                                .setName(kaynakAdi)
                                .setExact(true)
                );

        kaynak.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return kaynak.isVisible();
    }

    public void openSource(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Görüntüle")
        ).click();
    }
    public void editSource(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Düzenle")
        ).click();
    }
/**
 * Verilen kaynak adına ait Görüntüle butonuna tıklar.
 *
 * @param kaynakAdi görüntülenecek kaynak adı
 */
}