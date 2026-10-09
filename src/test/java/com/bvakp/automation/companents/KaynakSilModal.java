package com.bvakp.automation.companents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class KaynakSilModal {

    private final Page page;
    private final Locator modalBasligi;

    public KaynakSilModal(Page page) {

        this.page = page;

        this.modalBasligi =
                page.getByRole(
                        AriaRole.HEADING,
                        new Page.GetByRoleOptions()
                                .setName("Kaynağı Sil")
                                .setExact(true)
                );
    }

    /**
     * Kaynağı Sil modalının görüntülendiğini kontrol eder.
     */
    public boolean kaynakSilModalGoruntulendiMi() {

        modalBasligi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        return modalBasligi.isVisible();
    }

    /**
     * Kaynak silme işlemini onaylar.
     */
    public void silmeOnaylama() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Sil")
                        .setExact(true)
        ).click();
    }

    /**
     * Silme sonrasında modalın kapandığını kontrol eder.
     */
    public boolean kaynakSilModalKapandiMi() {

        try {

            modalBasligi.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    com.microsoft.playwright.options.WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(10000)
            );

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}