package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
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

    /**
     * PlaywrightManager sınıfının nesne olarak oluşturulmasını
     * engellemek için kullanılır.
     */
    private PlaywrightManager() {
    }

    /**
     * Config dosyasındaki browser ve headless bilgilerine göre
     * temiz bir Playwright oturumu başlatmak için kullanılır.
     *
     * Bu kullanımda kayıtlı AVP authentication bilgisi kullanılmaz.
     */
    public static void initialize() {

        BrowserName browserName = BrowserName.from(
                ConfigManager.get("browser")
        );

        boolean headless =
                ConfigManager.getBoolean("headless");

        initialize(
                browserName,
                headless,
                false
        );
    }

    /**
     * Browser başlatılırken daha önce kaydedilmiş AVP oturumunun
     * kullanılıp kullanılmayacağını belirlemek için kullanılır.
     *
     * @param authStateKullan true ise kayıtlı AVP oturumu kullanılır
     */
    public static void initialize(boolean authStateKullan) {

        BrowserName browserName = BrowserName.from(
                ConfigManager.get("browser")
        );

        boolean headless =
                ConfigManager.getBoolean("headless");

        initialize(
                browserName,
                headless,
                authStateKullan
        );
    }

    /**
     * Belirtilen browser ve headless ayarlarıyla
     * temiz bir Playwright oturumu başlatmak için kullanılır.
     *
     * @param browserName kullanılacak browser
     * @param headless browserın headless çalışıp çalışmayacağı
     */
    public static void initialize(
            BrowserName browserName,
            boolean headless) {

        initialize(
                browserName,
                headless,
                false
        );
    }

    /**
     * Playwright ortamını oluşturur ve istenirse daha önce
     * kaydedilmiş AVP authentication storage state bilgisini kullanır.
     *
     * Her thread için ayrı Playwright, Browser,
     * BrowserContext ve Page nesnesi saklanır.
     *
     * @param browserName kullanılacak browser
     * @param headless browserın headless çalışıp çalışmayacağı
     * @param authStateKullan kayıtlı AVP oturumu kullanılacaksa true
     */
    public static void initialize(
            BrowserName browserName,
            boolean headless,
            boolean authStateKullan) {

        Playwright playwright = Playwright.create();

        Browser browser = BrowserFactory.createBrowser(
                playwright,
                browserName,
                headless
        );

        BrowserContext context;

        if (authStateKullan
                && AvpAuthStateManager.authStateVarMi()) {

            context = browser.newContext(
                    new Browser.NewContextOptions()
                            .setStorageStatePath(
                                    AvpAuthStateManager.authStatePathAlma()
                            )
            );

        } else {

            context = browser.newContext();
        }

        Page page = context.newPage();

        playwrightThread.set(playwright);
        browserThread.set(browser);
        contextThread.set(context);
        pageThread.set(page);
    }

    /**
     * Aktif test thread'ine ait Page nesnesini
     * almak için kullanılır.
     *
     * @return aktif Playwright Page nesnesi
     */
    public static Page getPage() {
        return pageThread.get();
    }

    /**
     * Aktif test thread'ine ait BrowserContext nesnesini
     * almak için kullanılır.
     *
     * @return aktif BrowserContext
     */
    public static BrowserContext getContext() {
        return contextThread.get();
    }

    /**
     * Aktif test thread'ine ait Browser nesnesini
     * almak için kullanılır.
     *
     * @return aktif Browser
     */
    public static Browser getBrowser() {
        return browserThread.get();
    }

    /**
     * Aktif test thread'ine ait Playwright nesnesini
     * almak için kullanılır.
     *
     * @return aktif Playwright
     */
    public static Playwright getPlaywright() {
        return playwrightThread.get();
    }

    /**
     * Test tamamlandıktan sonra oluşturulan BrowserContext,
     * Browser ve Playwright nesnelerini güvenli şekilde kapatmak
     * ve ThreadLocal alanlarını temizlemek için kullanılır.
     */
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