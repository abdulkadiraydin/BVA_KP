package com.bvakp.automation.pages;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    /**
     * Config dosyasında tanımlanan base URL'i açmak için kullanılır.
     */
    public void openBaseUrl() {
        page.navigate(ConfigManager.get("base.url"));
    }
    public String getCurrentUrl() {
        return page.url();
    }

    public String getTitle() {
        return page.title();
    }
    /**
     * Ekranda çıkan bildirimin (toast) görünmesini bekler.
     * Bildirim, ilgili işlemin tamamlandığını gösterir.
     *
     * @param bildirimMetni bildirimde geçen metin (tamamı gerekmez)
     */
    public void bildirimBekleme(String bildirimMetni) {

        page.getByText(bildirimMetni)
                .first()
                .waitFor(
                        new Locator.WaitForOptions()
                                .setState(WaitForSelectorState.VISIBLE)
                                .setTimeout(15_000)
                );

        ReportManager.info("Bildirim görüldü: " + bildirimMetni);
    }
}