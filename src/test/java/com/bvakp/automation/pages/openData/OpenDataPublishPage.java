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
    public void clickPublishTab() {
        publishTab.click();
        ReportManager.step("Veri Seti Yönetimi sekmesi açıldı.");
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
        ReportManager.step(
                "Görüntülenecek veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions().setTimeout(10_000)
        );

        ReportManager.step(
                "Veri seti bulundu. Görüntüle butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Görüntüle")
        ).click();

        ReportManager.step(
                "Veri seti görüntüleme ekranı açıldı: " + dataSetName
        );
    }
    public void clickKvkkOnayla(String dataSetName) {
        ReportManager.step(
                "Veri seti satırında KVKK Onayla butonuna tıklanıyor: " + dataSetName
        );

        Locator row = getDataSetRow(dataSetName);
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("KVKK Onayla")
        ).click();
    }

    public boolean isStatus(String dataSetName, String expectedStatus) {
        ReportManager.step(
                "Veri seti statü kontrolü başlatılıyor: " + dataSetName
        );

        try {
            ReportManager.step(
                    "Statü kontrolü öncesinde veri seti aranıyor: " + dataSetName
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

                ReportManager.step(
                        "Aranan veri seti bulundu ve adı eşleşti: " + actualName
                );

                Locator statusCell = row.locator(
                        "[role='gridcell'][data-field='status']"
                );

                String actualStatus = statusCell.innerText().trim();

                ReportManager.step(
                        "Mevcut statü: " + actualStatus
                                + " | Beklenen statü: " + expectedStatus
                );

                if (actualStatus.equals(expectedStatus)) {
                    ReportManager.step(
                            "Statü doğrulaması başarılı: " + expectedStatus
                    );
                    return true;
                }

                ReportManager.step(
                        "Statü doğrulaması başarısız. Beklenen: "
                                + expectedStatus + " | Gerçek: " + actualStatus
                );

                return false;
            }

            ReportManager.step(
                    "Aranan veri seti tabloda bulunamadı: " + dataSetName
            );
            return false;

        } catch (Exception e) {
            ReportManager.step(
                    "Statü kontrolü sırasında hata oluştu: "
                            + e.getMessage()
            );
            return false;
        }
    }

    public boolean isPortalStatus(String dataSetName, String expectedPortalStatus) {
        ReportManager.step(
                "Portal aktarım statüsü kontrolü başlatılıyor: " + dataSetName
        );

        try {
            ReportManager.step(
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

                ReportManager.step(
                        "Veri seti bulundu ve adı eşleşti: " + actualName
                );

                Locator portalCell = row.locator(
                        "[role='gridcell'][data-field='ckanSyncStatus']"
                );

                String actualPortalStatus = portalCell.innerText().trim();

                ReportManager.step(
                        "Mevcut portal statüsü: " + actualPortalStatus
                                + " | Beklenen portal statüsü: "
                                + expectedPortalStatus
                );

                if (actualPortalStatus.equals(expectedPortalStatus)) {
                    ReportManager.step(
                            "Portal statüsü doğrulaması başarılı: "
                                    + expectedPortalStatus
                    );
                    return true;
                }

                ReportManager.step(
                        "Portal statüsü doğrulaması başarısız. Beklenen: "
                                + expectedPortalStatus
                                + " | Gerçek: " + actualPortalStatus
                );

                return false;
            }

            ReportManager.step(
                    "Aranan veri seti tabloda bulunamadı: " + dataSetName
            );
            return false;

        } catch (Exception e) {
            ReportManager.step(
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

        ReportManager.step(
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
                    ReportManager.step(
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

        ReportManager.step(
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

        ReportManager.step(
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
                    ReportManager.step(
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

        ReportManager.step(
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
        ReportManager.step(
                "Yayınlanacak veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        ReportManager.step(
                "Veri seti bulundu. Yayınla butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Yayınla")
        ).click();

        ReportManager.step(
                "Yayınla butonuna tıklandı: " + dataSetName
        );
    }
    public void enterKvkkAciklama(String aciklama) {
        ReportManager.step("KVKK onay açıklaması giriliyor.");
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmKvkkOnay() {
        ReportManager.step("KVKK onay penceresinde Onayla butonuna tıklanıyor.");
        kvkkOnaylaButton.click();
    }
    public void cancelKvkkOnay() {
        kvkkIptalButton.click();
        ReportManager.step("KVKK onay işlemi iptal edildi.");
    }
    public void enterYayinAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
        ReportManager.step("Yayın açıklaması girildi.");
    }
    public void confirmYayinla() {
        yayinlaButton.click();
        ReportManager.step("Veri seti yayınlama işlemi onaylandı.");
    }
    public void clickArsivle(String dataSetName) {
        ReportManager.step(
                "Arşivlenecek veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        ReportManager.step(
                "Veri seti bulundu. Arşivle butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Arşivle")
        ).click();

        ReportManager.step(
                "Arşivle butonuna tıklandı: " + dataSetName
        );
    }
    public void enterArsivAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
        ReportManager.step("Arşivleme açıklaması girildi.");
    }
    public void confirmArsivle() {
        arsivleButton.click();
        ReportManager.step("Veri seti arşivleme işlemi onaylandı.");
    }
    public void cancelArsivle() {
        arsivIptalButton.click();
        ReportManager.step("Veri seti arşivleme işlemi iptal edildi.");
    }
    public void clickSil(String dataSetName) {
        ReportManager.step("Silinecek veri seti aranıyor: " + dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions().setTimeout(10_000)
        );

        ReportManager.step("Veri seti bulundu. Sil butonuna tıklanıyor.");

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Sil")
        ).click();

        ReportManager.step("Sil butonuna tıklandı: " + dataSetName);
    }

    public void confirmSil() {
        ReportManager.step("Veri seti silme işlemi onaylanıyor.");

        silButton.click();

        ReportManager.step("Veri seti silme işlemi onaylandı.");
    }

    public void cancelSil() {
        ReportManager.step("Veri seti silme işlemi iptal ediliyor.");

        silIptalButton.click();

        ReportManager.step("Veri seti silme işlemi iptal edildi.");
    }

    public boolean isDataSetDisplayed(String dataSetName) {
        ReportManager.step(
                "Veri setinin listede bulunup bulunmadığı kontrol ediliyor: "
                        + dataSetName
        );

        Locator row = getDataSetRow(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);
        try {
            row.waitFor(
                    new Locator.WaitForOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(3_000)
            );

            ReportManager.step(
                    "Veri seti hâlâ listede bulunuyor: " + dataSetName
            );

            return true;

        } catch (Exception e) {
            ReportManager.step(
                    "Veri seti listede bulunamadı: " + dataSetName
            );

            return false;
        }
    }
    public void clickKvkkReddet(String dataSetName) {
        ReportManager.step(
                "Veri seti satırında KVKK Reddet butonuna tıklanıyor: " + dataSetName
        );

        Locator row = getDataSetRow(dataSetName);

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("KVKK Reddet")
        ).click();
    }
    public void enterKvkkRedAciklama(String aciklama) {
        ReportManager.step("KVKK ret açıklaması giriliyor.");
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmKvkkRed() {
        ReportManager.step("KVKK reddetme işlemi onaylanıyor.");
        kvkkRedOnaylaButton.click();
    }
    public void cancelKvkkRed() {
        kvkkRedIptalButton.click();
        ReportManager.step("KVKK reddetme işlemi iptal edildi.");
    }

    public void clickDuzenle(String dataSetName) {

        ReportManager.step(
                "Düzenlenecek veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        ReportManager.step(
                "Veri seti bulundu. Düzenle butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Düzenle")
        ).click();

        ReportManager.step(
                "Veri seti düzenleme ekranı açıldı: " + dataSetName
        );
    }

    public void selectDropdown(String dropdownName, String option) {
        ReportManager.step("Dropdown açılıyor: " + dropdownName);

        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(dropdownName)
        ).click();

        ReportManager.step("Seçenek seçiliyor: " + option);

        Locator optionLocator = page.locator("li[role='option']")
                .filter(new Locator.FilterOptions().setHasText(option));

        optionLocator.click();

        ReportManager.step(
                "Dropdown seçimi tamamlandı: "
                        + dropdownName + " = " + option
        );
    }
    public void enterDuzenlemeAciklama(String aciklama) {
        ReportManager.step("Düzenleme açıklaması giriliyor.");

        page.locator("textarea[name='description']").fill(aciklama);

        ReportManager.step("Düzenleme açıklaması girildi.");
    }

    public void clickDuzenlemeKaydet() {
        ReportManager.step("Düzenleme kaydet butonuna tıklanıyor.");

        duzenlemeKaydetButton.click();

        ReportManager.step("Düzenleme kaydet butonuna tıklandı.");
    }

    public void assertDuzenlemeAlanlari(
            String sorumluBirim,
            String kategori,
            String aciklama) {

        ReportManager.step(
                "Düzenlenen Sorumlu Birim alanı doğrulanıyor: " + sorumluBirim
        );

        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Sorumlu Birim")
                ).innerText(),
                sorumluBirim,
                "Sorumlu Birim alanı beklenen değerle eşleşmiyor."
        );

        ReportManager.step("Sorumlu Birim alanı doğrulandı.");

        ReportManager.step(
                "Düzenlenen Kategori alanı doğrulanıyor: " + kategori
        );

        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Kategori")
                ).innerText(),
                kategori,
                "Kategori alanı beklenen değerle eşleşmiyor."
        );

        ReportManager.step("Kategori alanı doğrulandı.");

        ReportManager.step("Düzenlenen açıklama alanı doğrulanıyor.");

        Assert.assertEquals(
                page.locator("textarea[name='description']").inputValue(),
                aciklama,
                "Açıklama alanı beklenen değerle eşleşmiyor."
        );

        ReportManager.step("Açıklama alanı doğrulandı.");
    }

    public void clickDegisiklikOnayaGonder() {

        ReportManager.step(
                "Değişiklikleri Onaya Gönder butonuna tıklanıyor."
        );

        degisiklikOnayaGonderButton.click();

        ReportManager.step(
                "Değişiklikleri Onaya Gönder butonuna tıklandı."
        );
    }

    public void confirmDegisiklikOnayaGonder() {

        ReportManager.step(
                "Değişikliklerin onaya gönderilmesi onaylanıyor."
        );

        degisiklikOnayaGonderConfirmButton.click();

        ReportManager.step(
                "Değişikliklerin onaya gönderilmesi onaylandı."
        );
    }

    public void clickGeriDon() {

        ReportManager.step("Geri Dön butonuna tıklanıyor.");

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Geri Dön")
        ).click();

        ReportManager.step("Geri Dön butonuna tıklandı.");
    }
    public void searchDataSet(String dataSetName) {

        ReportManager.step(
                "Veri seti arama alanına yazılıyor: " + dataSetName
        );

        Locator searchInput = page.getByPlaceholder("Veri seti ara");
        searchInput.fill(dataSetName);

        String searchValue = searchInput.inputValue();

        ReportManager.step(
                "Arama alanındaki değer: " + searchValue
        );

        System.out.println(
                "Arama alanındaki değer: " + searchValue
        );
    }
    public void clickGuncelle(String dataSetName) {

        ReportManager.step(
                "Güncellenecek veri seti aranıyor: " + dataSetName
        );

        Locator row = getDataSetRow(dataSetName);

        ReportManager.step(
                "Veri seti satırındaki Güncelle butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Güncelle")
        ).click();

        ReportManager.step(
                "Güncelle butonuna tıklandı: " + dataSetName
        );
    }
    public void confirmGuncelle() {

        ReportManager.step(
                "Güncelleme işlemi onaylanıyor."
        );

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Güncelle")
                        .setExact(true)
        ).click();

        ReportManager.step(
                "Güncelleme işlemi onaylandı."
        );
    }
    public String getCurrentVersion(String dataSetName) {

        ReportManager.step(
                "Versiyonu okunacak veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        ReportManager.step(
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

        ReportManager.step(
                "Veri seti versiyonu okundu: " + version
        );

        return version;
    }

    public void clickYayinReddet(String dataSetName) {

        ReportManager.step(
                "Yayın reddedilecek veri seti aranıyor: " + dataSetName
        );

        searchDataSet(dataSetName);

        Locator row = getDataSetRow(dataSetName);

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        ReportManager.step(
                "Veri seti bulundu. Yayını Reddet butonuna tıklanıyor."
        );

        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Yayını Reddet")
        ).click();

        ReportManager.step(
                "Yayını Reddet butonuna tıklandı."
        );
    }


    public void enterYayinRedAciklama(String aciklama) {

        ReportManager.step("Yayın ret açıklaması giriliyor.");

        popupAciklamaInput.fill(aciklama);

        ReportManager.step("Yayın ret açıklaması girildi.");
    }


    public void confirmYayinRed() {

        ReportManager.step("Yayın reddi onay butonuna tıklanıyor.");

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Reddet")
                        .setExact(true)
        ).click();

        ReportManager.step("Yayın reddi onaylandı.");
    }


    public boolean isDataSetNameMatching(String dataSetName) {
        ReportManager.step(
                "Veri seti arama sonucu kontrol ediliyor: " + dataSetName
        );

        Locator searchInput = page.getByPlaceholder("Veri seti ara");

        String searchValue = searchInput.inputValue().trim();

        if (!dataSetName.equals(searchValue)) {
            ReportManager.step(
                    "Arama alanı eşleşmiyor. Beklenen: "
                            + dataSetName + " | Gerçek: " + searchValue
            );
            return false;
        }

        ReportManager.step(
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

            ReportManager.step(
                    "Tablodaki veri seti adı kontrol ediliyor: " + actualName
            );

            if (dataSetName.equals(actualName)) {
                ReportManager.step(
                        "Veri seti adı eşleşti: " + dataSetName
                );
                return true;
            }
        }

        ReportManager.step(
                "Aranan veri seti adı tabloda bulunamadı: " + dataSetName
        );

        return false;
    }

}