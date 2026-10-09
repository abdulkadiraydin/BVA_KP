package com.bvakp.automation.pages.openData;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class PublicOpenDataPage extends BasePage {

    private final Locator searchInput;
    public PublicOpenDataPage(Page page) {
        super(page);
        this.searchInput = page.locator("input[name='q']");
    }
    public void open() {
        page.navigate(ConfigManager.get("public.data.url"));
    }
    public void selectCategory(String category) {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(category)
        ).click();
    }
    public void searchDataSet(String dataSetName) {
        searchInput.fill(dataSetName);
        searchInput.press("Enter");
    }
    public boolean isDataSetDisplayed(String dataSetName) {
        return page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(dataSetName)
        ).isVisible();
    }
    public void clickDataSet(String dataSetName) {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName(dataSetName)
        ).click();
    }

}