package com.bvakp.automation.core.playwright;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class BrowserFactory {

    private BrowserFactory() {
    }

    public static Browser createBrowser(
            Playwright playwright,
            BrowserName browserName,
            boolean headless) {

        BrowserType.LaunchOptions launchOptions =
                new BrowserType.LaunchOptions()
                        .setHeadless(headless);

        return switch (browserName) {

            case CHROMIUM ->
                    playwright.chromium().launch(launchOptions);

            case FIREFOX ->
                    playwright.firefox().launch(launchOptions);

            case WEBKIT ->
                    playwright.webkit().launch(launchOptions);
        };
    }
}