package com.bvakp.automation.pages.openData;

import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
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
    public void clickPublishTab()
    {
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
        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions().setTimeout(10_000)
        );
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
        try {
            searchDataSet(dataSetName);

            Locator rows = page.locator("[role='row']");

            for (int i = 0; i < rows.count(); i++) {
                Locator row = rows.nth(i);

                Locator nameCell = row.locator(
                        "[role='gridcell'][data-field='name'] p[title]"
                );

                if (nameCell.count() == 0) {
                    continue;
                }

                String actualName = nameCell.first().getAttribute("title");

                if (!dataSetName.equals(actualName)) {
                    continue;
                }

                ReportManager.info("Aranan veri seti bulundu ve adı eşleşti: " + actualName);

                Locator statusCell = row.locator(
                        "[role='gridcell'][data-field='status']"
                );

                String actualStatus = statusCell.innerText().trim();

                ReportManager.info(
                        "Mevcut statü: " + actualStatus
                                + " | Beklenen statü: " + expectedStatus
                );

                if (actualStatus.equals(expectedStatus)) {
                    ReportManager.info(
                            "Statü doğrulaması başarılı: " + expectedStatus
                    );
                    return true;
                }

                ReportManager.error(
                        "Statü doğrulaması başarısız. Beklenen: "
                                + expectedStatus + " | Gerçek: " + actualStatus
                );

                return false;
            }

            ReportManager.error(
                    "Aranan veri seti tabloda bulunamadı: " + dataSetName
            );
            return false;

        } catch (Exception e) {
            ReportManager.error(
                    "Statü kontrolü sırasında hata oluştu: " + e.getMessage()
            );
            return false;
        }
    }

    public boolean isPortalStatus(String dataSetName, String expectedPortalStatus) {
        ReportManager.info(
                "Portal aktarım statüsü kontrolü başlatılıyor: " + dataSetName
        );

        try {
            ReportManager.info(
                    "Portal statüsü kontrolü öncesinde veri seti aranıyor: "
                            + dataSetName
            );

            searchDataSet(dataSetName);

            Locator rows = page.locator("[role='row']");

            for (int i = 0; i < rows.count(); i++) {
                Locator row = rows.nth(i);

                Locator nameCell = row.locator(
                        "[role='gridcell'][data-field='name'] p[title]"
                );

                if (nameCell.count() == 0) {
                    continue;
                }

                String actualName = nameCell.first().getAttribute("title");

                if (!dataSetName.equals(actualName)) {
                    continue;
                }

                ReportManager.info(
                        "Veri seti bulundu ve adı eşleşti: " + actualName
                );

                Locator portalCell = row.locator(
                        "[role='gridcell'][data-field='ckanSyncStatus']"
                );

                String actualPortalStatus = portalCell.innerText().trim();

                ReportManager.info(
                        "Mevcut portal statüsü: " + actualPortalStatus
                                + " | Beklenen portal statüsü: "
                                + expectedPortalStatus
                );

                if (actualPortalStatus.equals(expectedPortalStatus)) {
                    ReportManager.info(
                            "Portal statüsü doğrulaması başarılı: "
                                    + expectedPortalStatus
                    );
                    return true;
                }

                ReportManager.error(
                        "Portal statüsü doğrulaması başarısız. Beklenen: "
                                + expectedPortalStatus
                                + " | Gerçek: " + actualPortalStatus
                );

                return false;
            }

            ReportManager.error(
                    "Aranan veri seti tabloda bulunamadı: " + dataSetName
            );
            return false;

        } catch (Exception e) {
            ReportManager.error(
                    "Portal statüsü kontrolünde hata oluştu: "
                            + e.getMessage()
            );
            return false;
        }
    }

    public void waitForStatus(String dataSetName, String expectedStatus) {
        long startTime = System.currentTimeMillis();
        int timeout = 60_000;
        String actualStatus = "Veri seti bulunamadı";

        ReportManager.info(
                "Veri seti aranarak statü bekleniyor: " + dataSetName
                        + " | Beklenen statü: " + expectedStatus
        );

        while (System.currentTimeMillis() - startTime < timeout) {
            page.reload();
            clickPublishTab();

            // Yenileme sonrasında veri setini ara
            searchDataSet(dataSetName);
            page.waitForTimeout(500);

            Locator rows = page.locator("[role='row']");
            boolean dataSetFound = false;

            for (int i = 0; i < rows.count(); i++) {
                Locator row = rows.nth(i);

                Locator nameCell = row.locator(
                        "[role='gridcell'][data-field='name'] p[title]"
                );

                if (nameCell.count() == 0) {
                    continue;
                }

                String actualName = nameCell.first().getAttribute("title");

                if (!dataSetName.equals(actualName)) {
                    continue;
                }

                dataSetFound = true;

                Locator statusCell = row.locator(
                        "[role='gridcell'][data-field='status']"
                );

                if (statusCell.count() == 0) {
                    actualStatus = "Statü alanı bulunamadı";
                    break;
                }

                actualStatus = statusCell.innerText().trim();

                if (expectedStatus.equals(actualStatus)) {
                    ReportManager.info(
                            "Veri seti bulundu ve beklenen statü doğrulandı: "
                                    + dataSetName + " | Statü: " + actualStatus
                    );
                    return;
                }

                break;
            }

            if (!dataSetFound) {
                actualStatus = "Veri seti listede bulunamadı";
            }

            page.waitForTimeout(3000);
        }

        ReportManager.error(
                "Statü bekleme işlemi başarısız. Veri seti: " + dataSetName
                        + " | Beklenen statü: " + expectedStatus
                        + " | Son durum: " + actualStatus
        );

        throw new AssertionError(
                "Veri seti beklenen statüye ulaşmadı. Veri seti: "
                        + dataSetName
                        + " | Beklenen: " + expectedStatus
                        + " | Son görülen statü: " + actualStatus
        );
    }

    public void waitForPortalStatus(String dataSetName, String expectedPortalStatus) {
        long startTime = System.currentTimeMillis();
        String actualPortalStatus = "Veri seti bulunamadı";

        ReportManager.info(
                "Portal aktarım statüsü bekleniyor. Veri seti: "
                        + dataSetName
                        + " | Beklenen statü: " + expectedPortalStatus
        );

        while (System.currentTimeMillis() - startTime < PORTAL_TRANSFER_TIMEOUT) {
            page.reload();
            clickPublishTab();

            // Her yenilemeden sonra veri setini ara
            searchDataSet(dataSetName);
            page.waitForTimeout(500);

            Locator rows = page.locator("[role='row']");
            boolean dataSetFound = false;

            for (int i = 0; i < rows.count(); i++) {
                Locator row = rows.nth(i);

                Locator nameCell = row.locator(
                        "[role='gridcell'][data-field='name'] p[title]"
                );

                if (nameCell.count() == 0) {
                    continue;
                }

                String actualName = nameCell.first().getAttribute("title");

                if (!dataSetName.equals(actualName)) {
                    continue;
                }

                dataSetFound = true;

                Locator portalStatusCell = row.locator(
                        "[role='gridcell'][data-field='ckanSyncStatus']"
                );

                if (portalStatusCell.count() == 0) {
                    actualPortalStatus = "Portal statüsü alanı bulunamadı";
                    break;
                }

                actualPortalStatus = portalStatusCell.innerText().trim();

                if (expectedPortalStatus.equals(actualPortalStatus)) {
                    ReportManager.info(
                            "Portal aktarım statüsü doğrulandı. Veri seti: "
                                    + dataSetName
                                    + " | Statü: " + actualPortalStatus
                    );
                    return;
                }

                break;
            }

            if (!dataSetFound) {
                actualPortalStatus = "Veri seti listede bulunamadı";
            }

            page.waitForTimeout(PORTAL_REFRESH_INTERVAL);
        }

        ReportManager.error(
                "Portal aktarım statüsü beklenen değere ulaşmadı. Veri seti: "
                        + dataSetName
                        + " | Beklenen: " + expectedPortalStatus
                        + " | Son durum: " + actualPortalStatus
        );

        throw new AssertionError(
                "Veri seti '" + dataSetName
                        + "' için portal durumu '" + expectedPortalStatus
                        + "' " + PORTAL_TRANSFER_TIMEOUT / 1000
                        + " saniye içinde oluşmadı. Son durum: "
                        + actualPortalStatus
        );
    }

    public void clickYayinla(String dataSetName) {
        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Yayınla")
        ).click();

        ReportManager.info(
                "Yayınla butonuna tıklandı: " + dataSetName
        );
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

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Arşivle")
        ).click();
        ReportManager.info(
                "Arşivle butonuna tıklandı: " + dataSetName
        );
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

        row.waitFor(
                new Locator.WaitForOptions().setTimeout(10_000)
        );
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Sil")
        ).click();
        ReportManager.info("Sil butonuna tıklandı: " + dataSetName);
    }

    public void confirmSil() {
        silButton.click();
    }

    public void cancelSil() {
        silIptalButton.click();
    }
    public boolean isDataSetDisplayed(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);
        try {
            row.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(3_000)
            );

            ReportManager.info(
                    "Veri seti hâlâ listede bulunuyor: " + dataSetName
            );

            return true;

        } catch (Exception e) {
            ReportManager.fail(
                    "Veri seti listede bulunamadı: " + dataSetName
            );

            return false;
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

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Düzenle")
        ).click();

        ReportManager.info(
                "Veri seti düzenleme ekranı açıldı: " + dataSetName
        );
    }

    public void selectDropdown(String dropdownName, String option) {
        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(dropdownName)
        ).click();
        Locator optionLocator = page.locator("li[role='option']")
                .filter(new Locator.FilterOptions().setHasText(option));

        optionLocator.click();

        ReportManager.info(
                "Dropdown seçimi tamamlandı: "
                        + dropdownName + " = " + option
        );
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
                sorumluBirim,
                "Sorumlu Birim alanı beklenen değerle eşleşmiyor."
        );

        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Kategori")
                ).innerText(),
                kategori,
                "Kategori alanı beklenen değerle eşleşmiyor."
        );

        Assert.assertEquals(
                page.locator("textarea[name='description']").inputValue(),
                aciklama,
                "Açıklama alanı beklenen değerle eşleşmiyor."
        );

        ReportManager.info("Açıklama alanı doğrulandı.");
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
        Locator searchInput = page.getByPlaceholder("Veri seti ara");
        searchInput.fill(dataSetName);

        String searchValue = searchInput.inputValue();

        ReportManager.info(
                "Arama alanındaki değer: " + searchValue
        );
    }
    public void clickGuncelle(String dataSetName) {
        Locator row = getDataSetRow(dataSetName);
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Güncelle")
        ).click();
        ReportManager.info(
                "Güncelle butonuna tıklandı: " + dataSetName
        );
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
        searchDataSet(dataSetName);

        ReportManager.info(
                "Arama sonuçlarının güncellenmesi için 3 saniye bekleniyor."
        );
        page.waitForTimeout(3000);

        Locator row = getDataSetRow(dataSetName);

        Locator versionCell = row.locator(
                "[role='gridcell'][data-field='currentVersionNo']"
        );

        versionCell.waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.ATTACHED)
                        .setTimeout(10_000)
        );

        String version = versionCell.getAttribute("title");

        ReportManager.info("Veri seti versiyonu okundu: " + version);

        return version;
    }

    public void clickYayinReddet(String dataSetName) {
        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Yayını Reddet")
        ).click();
        ReportManager.info(
                "Yayını Reddet butonuna tıklandı."
        );
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

        ReportManager.info("Yayın reddi onaylandı.");
    }
    public boolean isDataSetNameMatching(String dataSetName) {

        Locator searchInput = page.getByPlaceholder("Veri seti ara");

        String searchValue = searchInput.inputValue().trim();

        if (!dataSetName.equals(searchValue)) {
            ReportManager.error(
                    "Arama alanı eşleşmiyor. Beklenen: "
                            + dataSetName + " | Gerçek: " + searchValue
            );
            return false;
        }

        ReportManager.info(
                "Arama alanındaki değer doğrulandı: " + searchValue
        );

        Locator rows = page.locator("[role='row']");

        for (int i = 0; i < rows.count(); i++) {
            Locator nameCell = rows.nth(i)
                    .locator("[role='gridcell'][data-field='name'] p[title]");

            if (nameCell.count() == 0) {
                continue;
            }

            String actualName = nameCell.first().getAttribute("title");

            if (dataSetName.equals(actualName)) {
                ReportManager.info(
                        "Veri seti adı eşleşti: " + dataSetName
                );
                return true;
            }
        }

        ReportManager.error(
                "Aranan veri seti adı tabloda bulunamadı: " + dataSetName
        );

        return false;
    }

}