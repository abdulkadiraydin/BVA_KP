package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.List;

public class PublicDataSetPage extends BasePage {

    private final Locator resourceFormats;
    private final Locator apiMethod;
    private final Locator apiUrl;
    private final Locator resourceId;

    public PublicDataSetPage(Page page) {
        super(page);

        this.resourceFormats = page.locator(".bvakp-fmt");
        this.apiMethod = page.locator(".bvakp-api-method");
        this.apiUrl = page.locator(".bvakp-api-url");
        this.resourceId = page.locator(".bvakp-api-rid");
    }

    public List<String> getAvailableFormats() {
        return resourceFormats.allInnerTexts();
    }
    public Download downloadFormat(String format) {
        Locator resource = page.locator(".bvakp-resource")
                .filter(new Locator.FilterOptions().setHasText(format));

        return page.waitForDownload(() ->
                resource.getByRole(
                        AriaRole.LINK,
                        new Locator.GetByRoleOptions().setName("İndir")
                ).click()
        );
    }
    public void clickApi() {
        page.getByRole(
                AriaRole.LINK,
                new Page.GetByRoleOptions().setName("API’yi görüntüle")
        ).click();
    }
    public List<String> getApiFields() {
        return page.locator(".bvakp-api-field code").allInnerTexts();
    }
    public String getApiMethod() {
        return apiMethod.innerText().trim();
    }

    public String getApiUrl() {
        return apiUrl.innerText().trim();
    }

    public String getResourceId() {
        return resourceId.innerText().trim();
    }
}