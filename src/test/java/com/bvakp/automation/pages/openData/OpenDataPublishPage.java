package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.testng.Assert;

public class OpenDataPublishPage extends BasePage {

    private static final int PORTAL_TRANSFER_TIMEOUT = 180_000;
    private static final int PORTAL_REFRESH_INTERVAL = 5_000;
    private static final int VERSION_TIMEOUT = 120_000;
    private static final int VERSION_REFRESH_INTERVAL = 3_000;
    private static final int REMOVAL_TIMEOUT = 60_000;
    private final Locator publishTab;
    private final Locator popupAciklamaInput;
    private final Locator kvkkOnaylaButton;
    private final Locator iptalButton;
    private final Locator yayinlaButton;
    private final Locator arsivleButton;
    private final Locator silButton;
    private final Locator kvkkRedOnaylaButton;
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

        this.iptalButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İptal")
        );
        this.yayinlaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Yayınla")
        );
        this.arsivleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Arşivle")
        );

        this.silButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Sil")
        );

        this.kvkkRedOnaylaButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Reddet")
                        .setExact(true)
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

        row.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
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
    public void waitForStatus(String dataSetName, String expectedStatus) {
        long startTime = System.currentTimeMillis();
        int timeout = 120_000;
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
                        "[role='gridcell'][data-field='state']"
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
                        + "' " + PORTAL_TRANSFER_TIMEOUT / 2000
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


    }
    public void enterKvkkAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmKvkkOnay() {
        kvkkOnaylaButton.click();
    }
    public void cancelKvkkOnay() {
        iptalButton.click();
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
    }
    public void enterArsivAciklama(String aciklama) {
        popupAciklamaInput.fill(aciklama);
    }
    public void confirmArsivle() {
        arsivleButton.click();
    }
    public void cancelArsivle() {
        iptalButton.click();
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
    }

    public void confirmSil() {
        silButton.click();
    }

    public void cancelSil() {
        iptalButton.click();
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
        iptalButton.click();
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
    }

    public void selectDropdown(String dropdownName, String option) {
        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions().setName(dropdownName)
        ).click();
        Locator optionLocator = page.locator("li[role='option']")
                .filter(new Locator.FilterOptions().setHasText(option));

        optionLocator.click();
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
        ReportManager.info("1");
        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Sorumlu Birim")
                ).innerText(),
                sorumluBirim,
                "Sorumlu Birim alanı beklenen değerle eşleşmiyor."
        );
        ReportManager.info("2");
        Assert.assertEquals(
                page.getByRole(
                        AriaRole.COMBOBOX,
                        new Page.GetByRoleOptions().setName("Kategori")
                ).innerText(),
                kategori,
                "Kategori alanı beklenen değerle eşleşmiyor."
        );
        ReportManager.info("3");
        Assert.assertEquals(
                page.locator("textarea[name='description']").inputValue(),
                aciklama,
                "Açıklama alanı beklenen değerle eşleşmiyor."
        );
        ReportManager.info("3");
        ReportManager.info("Düzenleme alanları doğrulandı.");
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
        page.waitForTimeout(500);
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
        searchDataSet(dataSetName);
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
    /**
     * Veri setinin versiyon numarasının beklenen değere ulaşmasını bekler.
     * Güncelleme asenkron çalıştığı için sayfa yenilenerek tekrar kontrol edilir.
     */
    public void waitForVersion(String dataSetName, int expectedVersion) {

        long startTime = System.currentTimeMillis();
        String actualVersion = "okunamadı";

        ReportManager.info(
                "Versiyon bekleniyor. Veri seti: " + dataSetName
                        + " | Beklenen: v" + expectedVersion
        );

        while (System.currentTimeMillis() - startTime < VERSION_TIMEOUT) {
            page.reload();
            clickPublishTab();
            searchDataSet(dataSetName);

            Locator versionCell = getDataSetRow(dataSetName).locator(
                    "[role='gridcell'][data-field='currentVersionNo']"
            );

            if (versionCell.count() > 0) {
                actualVersion = versionCell.first().getAttribute("title");

                if (actualVersion != null
                        && String.valueOf(expectedVersion).equals(
                        actualVersion.replace("v", "").trim())) {

                    ReportManager.info(
                            "Beklenen versiyona ulaşıldı: " + actualVersion
                    );
                    return;
                }
            }

            page.waitForTimeout(VERSION_REFRESH_INTERVAL);
        }

        ReportManager.error(
                "Versiyon beklenen değere ulaşmadı. Veri seti: " + dataSetName
                        + " | Beklenen: v" + expectedVersion
                        + " | Son görülen: " + actualVersion
        );

        throw new AssertionError(
                "Veri seti versiyonu beklenen değere ulaşmadı. Veri seti: "
                        + dataSetName
                        + " | Beklenen: v" + expectedVersion
                        + " | Son görülen: " + actualVersion
        );
    }
    /**
     * Silinen veri setinin listeden kalkmasını bekler.
     * Liste her turda yenilenir ve arama sonucunun boş olduğunu
     * gösteren "Kayıt bulunamadı" mesajı beklenir.
     */
    public void waitForDataSetRemoval(String dataSetName) {

        long startTime = System.currentTimeMillis();

        ReportManager.info(
                "Veri setinin listeden kalkması bekleniyor: " + dataSetName
        );

        while (System.currentTimeMillis() - startTime < REMOVAL_TIMEOUT) {
            page.reload();
            clickPublishTab();
            searchDataSet(dataSetName);

            try {
                page.getByText("Kayıt bulunamadı").waitFor(
                        new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(3_000)
                );

                ReportManager.info("Veri seti listeden kalktı: " + dataSetName);
                return;

            } catch (TimeoutError e) {
                // Liste henüz boş değil, bir sonraki turda tekrar denenecek.
            }
        }

        ReportManager.error(
                "Veri seti listeden kalkmadı: " + dataSetName
        );

        throw new AssertionError(
                "Silinen veri seti listeden kalkmadı. Veri seti: " + dataSetName
        );
    }
    /**
     * Veri seti detay ekranının açıldığını doğrular:
     * "Genel Bilgi" sekmesi ve veri seti adı görünür olmalıdır.
     */
    public void waitForDetailScreen(String dataSetName) {

        page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions().setName("Genel Bilgi")
        ).waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000)
        );

        page.getByText(
                dataSetName,
                new Page.GetByTextOptions().setExact(true)
        ).first().waitFor(
                new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000)
        );

        ReportManager.info("Veri seti detay ekranı açıldı: " + dataSetName);
    }
    public void waitForVeriSetiYonetimi() {
        page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Veri Seti Yönetimi"))
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(15_000));
        ReportManager.info("Veri Seti Yönetimi ekranına dönüldü.");
    }
}