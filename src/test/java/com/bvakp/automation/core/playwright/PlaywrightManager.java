package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.config.ConfigManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class PlaywrightManager {

    private static final ThreadLocal<Playwright> playwrightThread =
            new ThreadLocal<>();

    private static final ThreadLocal<Browser> browserThread =
            new ThreadLocal<>();

    private static final ThreadLocal<BrowserContext> contextThread =
            new ThreadLocal<>();

    private static final ThreadLocal<Page> pageThread =
            new ThreadLocal<>();


    private PlaywrightManager() {
    }


    public static void initialize() {

        BrowserName browserName = BrowserName.from(
                ConfigManager.get("browser")
        );

        boolean headless =
                ConfigManager.getBoolean("headless");

        initialize(browserName, headless);
    }


    public static void initialize(
            BrowserName browserName,
            boolean headless) {

        Playwright playwright = Playwright.create();

        Browser browser = BrowserFactory.createBrowser(
                playwright,
                browserName,
                headless
        );

        BrowserContext context = browser.newContext();

        Page page = context.newPage();

        playwrightThread.set(playwright);
        browserThread.set(browser);
        contextThread.set(context);
        pageThread.set(page);
    }


    public static Page getPage() {
        return pageThread.get();
    }


    public static BrowserContext getContext() {
        return contextThread.get();
    }


    public static Browser getBrowser() {
        return browserThread.get();
    }


    public static Playwright getPlaywright() {
        return playwrightThread.get();
    }


    public static void close() {

        if (contextThread.get() != null) {
            contextThread.get().close();
            contextThread.remove();
        }

        if (browserThread.get() != null) {
            browserThread.get().close();
            browserThread.remove();
        }

        if (playwrightThread.get() != null) {
            playwrightThread.get().close();
            playwrightThread.remove();
        }

        pageThread.remove();
    }
}