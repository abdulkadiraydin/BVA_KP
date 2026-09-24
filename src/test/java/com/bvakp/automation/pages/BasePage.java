package com.bvakp.automation.pages;

import com.bvakp.automation.core.config.ConfigManager;
import com.microsoft.playwright.Page;

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
}