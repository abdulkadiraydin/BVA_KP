package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;

public class OpenDataPublishPage extends BasePage {

    private static final int PORTAL_TRANSFER_TIMEOUT = 60_000;
    private static final int PORTAL_REFRESH_INTERVAL = 5_000;
    private final Locator publishTab;
    private final Locator popupAciklamaInput;
    private final Locator kvkkOnaylaButton;
    private final Locator kvkkIptalButton;
    private final Locator yayinlaButton;
    private final Locator yayinIptalButton;
    private final Locator arsivleButton;
    private final Locator arsivIptalButton;
    private final Locator silButton;
    private final Locator silIptalButton;
    private final Locator kvkkRedOnaylaButton;
    private final Locator kvkkRedIptalButton;
    private final Locator duzenlemeKaydetButton;
    private final Locator degisiklikOnayaGonderButton;
    private final Locator degisiklikOnayaGonderConfirmButton;
    public OpenDataPublishPage(Page page) {
        super(page);
        this.publishTab = page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Veri Seti Yönetimi")
        );
        this.popupAciklamaInput = page.locator("textarea[name='text']");

        this.kvkkOnaylaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onayla")
        );

        this.kvkkIptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.yayinlaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Yayınla")
        );

        this.yayinIptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.arsivleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Arşivle")
        );

        this.arsivIptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.silButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Sil")
        );

        this.silIptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.kvkkRedOnaylaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Reddet")
                        .setExact(true)
        );

        this.kvkkRedIptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.duzenlemeKaydetButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaydet")
        );
        this.degisiklikOnayaGonderButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        );
        this.degisiklikOnayaGonderConfirmButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Onaya Gönder")
                        .setExact(true)
        ).last();
    }
    public void clickPublishTab() {
        publishTab.click();
    }
    public Locator getDataSetRow(String dataSetName) {

        return page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHas(
                                page.locator("[role='gridcell'][data-field='name']")
                                        .getByText(
                                                dataSetName,
                                                new Locator.GetByTextOptions().setExact(true)
                                        )
                        )
                );
    }
    public void clickGoruntule(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Görüntüle")
        ).click();
    }
    public void clickKvkkOnayla(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("KVKK Onayla")
        ).click();
    }
    public boolean isStatus(String dataSetName, String expectedStatus) {
        Locator row = getDataSetRow(dataSetName);
        row.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
        Locator statusCell =
                row.locator("[role='gridcell'][data-field='status']");
        try {
            statusCell
                    .getByText(
                            expectedStatus,
                            new Locator.GetByTextOptions().setExact(true)
                    )
                    .waitFor(
                            new Locator.WaitForOptions().setTimeout(10_000)
                    );

            return true;

        } catch (Exception e) {
            return false;
        }
    }
    public String getStatus(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        Locator statusCell =
                row.locator("[role='gridcell'][data-field='status']");

        return statusCell.innerText().trim();
    }
    public boolean isPortalStatus(String dataSetName, String expectedPortalStatus) {

        Locator row = getDataSetRow(dataSetName);

        Locator portalCell = row.locator(
                "[role='gridcell'][data-field='ckanSyncStatus']"
        );

        return portalCell.getByText(
                expectedPortalStatus,
                new Locator.GetByTextOptions().setExact(true)
        ).isVisible();
    }
    public void waitForStatus(String dataSetName, String expectedStatus) {

        for (int i = 0; i < 6; i++) {

            page.reload();
            clickPublishTab();

            Locator row = getDataSetRow(dataSetName);

            try {
                row.waitFor(
                        new Locator.WaitForOptions()
                                .setTimeout(10_000)
                );
                return;

            } catch (Exception e) {

                if (i == 5) {
                    throw new AssertionError(
                            "Veri seti '" + dataSetName +
                                    "' Veri Seti Yönetimi ekranında bulunamadı."
                    );
                }
            }

            page.waitForTimeout(PORTAL_REFRESH_INTERVAL);
        }
    }
    public void waitForPortalStatus(String dataSetName, String expectedPortalStatus) {

        long startTime = System.currentTimeMillis();

        while (System.currentTimeMillis() - startTime < PORTAL_TRANSFER_TIMEOUT) {

            page.reload();

            Locator row = getDataSetRow(dataSetName);

            row.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10_000)
            );

            Locator portalStatus = row.locator(
                    "[role='gridcell'][data-field='ckanSyncStatus']"
            );

            if (portalStatus.getByText(
                    expectedPortalStatus,
                    new Locator.GetByTextOptions().setExact(true)
            ).isVisible()) {
                return;
            }

            page.waitForTimeout(PORTAL_REFRESH_INTERVAL);
        }

        throw new AssertionError(
                "Veri seti '" + dataSetName +
                        "' için portal durumu '" +
                        expectedPortalStatus +
                        "' " +
                        PORTAL_TRANSFER_TIMEOUT / 1000 +
                        " saniye içinde oluşmadı."
        );
    }
    public void clickYayinla(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Yayınla")
        ).click();
    }
    public void enterKvkkAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmKvkkOnay() {
        kvkkOnaylaButton.click();
    }
    public void cancelKvkkOnay() {
        kvkkIptalButton.click();
    }
    public void enterYayinAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmYayinla() {
        yayinlaButton.click();
    }
    public void clickArsivle(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Arşivle")
        ).click();
    }
    public void enterArsivAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmArsivle() {
        arsivleButton.click();
    }
    public void cancelArsivle() {
        arsivIptalButton.click();
    }
    public void clickSil(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Sil")
        ).click();
    }
    public void confirmSil() {
        silButton.click();
    }
    public void cancelSil() {
        silIptalButton.click();
    }
    public boolean isDataSetDisplayed(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);

        try {
            row.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.DETACHED)
                    .setTimeout(10_000));

            return false;

        } catch (Exception e) {
            return true;
        }
    }
    public void clickKvkkReddet(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("KVKK Reddet")
        ).click();
    }
    public void enterKvkkRedAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmKvkkRed() {
        kvkkRedOnaylaButton.click();
    }
    public void cancelKvkkRed() {
        kvkkRedIptalButton.click();
    }
    public void clickDuzenle(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Düzenle")
        ).click();
    }
    public void selectDropdown(String dropdownName, String option) {
        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(dropdownName)
        ).click();

        page.locator("li[role='option']")
                .filter(new Locator.FilterOptions().setHasText(option))
                .click();
    }
    public void enterDuzenlemeAciklama(String aciklama) {
        page.locator("textarea[name='description']").fill(aciklama);
    }
    public void clickDuzenlemeKaydet() {
        duzenlemeKaydetButton.click();
    }
    public void assertDuzenlemeAlanlari(
            String sorumluBirim,
            String kategori,
            String aciklama) {

        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Sorumlu Birim")
                ).innerText(),
                sorumluBirim
        );

        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Kategori")
                ).innerText(),
                kategori
        );
        Assert.assertEquals(
                page.locator("textarea[name='description']").inputValue(),
                aciklama
        );
    }
    public void clickDegisiklikOnayaGonder() {
        degisiklikOnayaGonderButton.click();
    }
    public void confirmDegisiklikOnayaGonder() {
        degisiklikOnayaGonderConfirmButton.click();
    }
    public void clickGeriDon() {
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Geri Dön")
        ).click();
    }
    public void searchDataSet(String dataSetName) {
        page.getByPlaceholder("Veri seti ara").fill(dataSetName);
    }
    public void clickGuncelle(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Güncelle")
        ).click();
    }
    public void confirmGuncelle() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Güncelle")
                        .setExact(true)
        ).click();
    }
    public String getCurrentVersion(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        return row.locator(
                "[role='gridcell'][data-field='currentVersionNo']"
        ).innerText().trim();
    }
    public void clickYayinReddet(String dataSetName) {

        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Yayını Reddet")
        ).click();
    }
    public void enterYayinRedAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmYayinRed() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Reddet")
                        .setExact(true)
        ).click();
    }
}