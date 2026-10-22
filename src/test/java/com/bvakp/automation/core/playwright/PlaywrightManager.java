package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.reporting.AllureArtifactCollector;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.VideoManager;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Video;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Her test thread'i için Playwright, Browser, BrowserContext ve Page yönetimi.
 * Trace, video ve Allure kanıtlarını test kapanışında saklar.
 */
public final class PlaywrightManager {

    private static final ThreadLocal<Playwright> playwrightThread = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThread = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThread = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThread = new ThreadLocal<>();
    private static final ThreadLocal<APIRequestContext> apiRequestContextThread = new ThreadLocal<>();

    private static final DateTimeFormatter ZAMAN_BICIMI =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /** assertThat(...) beklemeleri; BasePage'deki varsayılan bekleme ile aynı. */
    private static final double ASSERT_BEKLEME_MS = 15_000;

    static {
        // Playwright varsayılanı 5 sn; yük altında yavaş açılan ekranlarda yetmiyor.
        PlaywrightAssertions.setDefaultAssertionTimeout(ASSERT_BEKLEME_MS);
    }

    private PlaywrightManager() {
    }

    /** Kayıtlı oturum kullanılmadan, konfigürasyondaki tarayıcıyı başlatır. */
    public static void initialize() {
        initialize(false);
    }

    /** İstenirse kayıtlı authentication state kullanılarak tarayıcıyı başlatır. */
    public static void initialize(boolean authStateKullan) {
        initialize(
                BrowserName.from(ConfigManager.get("browser")),
                ConfigManager.getBoolean("headless"),
                authStateKullan
        );
    }

    /** Belirtilen tarayıcı ve headless ayarıyla, auth state olmadan başlatır. */
    public static void initialize(BrowserName browserName, boolean headless) {
        initialize(browserName, headless, false);
    }

    /**
     * Teste özel tarayıcı ve API oturumunu oluşturur.
     * Nesneler oluşturuldukça ThreadLocal içine alınır; kapanışta temizlenebilir.
     */
    public static void initialize(
            BrowserName browserName,
            boolean headless,
            boolean authStateKullan) {

        Locale.setDefault(Locale.ENGLISH);

        int genislik = Integer.parseInt(ConfigManager.getOrDefault("viewport.width", "1280"));
        int yukseklik = Integer.parseInt(ConfigManager.getOrDefault("viewport.height", "720"));

        try {
            Playwright playwright = Playwright.create();
            playwrightThread.set(playwright);

            Browser browser = BrowserFactory.createBrowser(playwright, browserName, headless);
            browserThread.set(browser);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(genislik, yukseklik);

            // video.mode=off ise kayıt hiç başlatılmaz.
            if (VideoManager.kayitAcikMi()) {
                contextOptions
                        .setRecordVideoDir(VideoManager.getVideoDirectory())
                        .setRecordVideoSize(genislik, yukseklik);
            }

            if (authStateKullan && AvpAuthStateManager.authStateVarMi()) {
                contextOptions.setStorageStatePath(AvpAuthStateManager.authStatePathAlma());
            }

            BrowserContext context = browser.newContext(contextOptions);
            contextThread.set(context);

            // Trace, ilk sayfa oluşturulmadan önce başlatılmalıdır.
            TraceManager.start(context);

            Page page = context.newPage();
            pageThread.set(page);

            // 5xx yanıtlar (ör. nginx 503) zaman aşımıyla karışmasın diye ayrıca loglanır.
            page.onResponse(PlaywrightManager::sunucuHatasiniLoglama);

            APIRequestContext apiRequestContext = playwright.request().newContext();
            apiRequestContextThread.set(apiRequestContext);

        } catch (RuntimeException | Error e) {
            // Kurulum yarıda kaldıysa açılan Playwright kaynaklarını güvenli temizle.
            kaynaklariTemizle();
            throw e;
        }
    }

    /**
     * Tarayıcı açmadan yalnızca API istemcisini başlatır (@ApiTesti işaretli testler).
     * Kapanış için yine close(...) kullanılır; tarayıcı nesneleri olmadığı için atlanır.
     */
    public static void apiBaslatma() {
        try {
            Playwright playwright = Playwright.create();
            playwrightThread.set(playwright);
            apiRequestContextThread.set(playwright.request().newContext());

        } catch (RuntimeException | Error e) {
            kaynaklariTemizle();
            throw e;
        }
    }

    /** Sunucu 5xx döndürdüyse ortam hatası olarak loglar; testin akışını değiştirmez. */
    private static void sunucuHatasiniLoglama(Response yanit) {
        if (yanit.status() >= 500) {
            ReportManager.warn("ORTAM HATASI | " + yanit.status() + " | "
                    + yanit.request().resourceType() + " | " + yanit.url());
        }
    }

    /** Aktif thread'e ait Playwright sayfasını döndürür. */
    public static Page getPage() {
        return pageThread.get();
    }

    /** Aktif thread'e ait browser context nesnesini döndürür. */
    public static BrowserContext getContext() {
        return contextThread.get();
    }

    /** main branch'inin kullandığı Browser erişimini korur. */
    public static Browser getBrowser() {
        return browserThread.get();
    }

    /** Aktif thread'e ait Playwright nesnesini döndürür. */
    public static Playwright getPlaywright() {
        return playwrightThread.get();
    }

    /** Aktif thread'e ait API istek nesnesini döndürür. */
    public static APIRequestContext getApiRequestContext() {
        return apiRequestContextThread.get();
    }

    /** Test adı verilmediğinde varsayılan bilgilerle kapatır. */
    public static void close() {
        close("test", "BILINMIYOR");
    }

    /**
     * Önce trace'i kaydeder, ardından context'i kapatıp videoyu arşivler.
     * Her kapanış adımı bağımsızdır; tek bir hata diğer kaynakların kapanmasını engellemez.
     */
    public static void close(String testName, String status) {
        try {
            Video video = null;
            Page page = pageThread.get();
            if (page != null) {
                try {
                    video = page.video();
                } catch (Exception ignored) {
                    // Video etkin olmayabilir; diğer kanıtlar yine kaydedilir.
                }
            }

            BrowserContext context = contextThread.get();
            if (context != null) {
                try {
                    Path tracePath = TraceManager.stop(context, testName, status);
                    if (tracePath != null) {
                        AllureArtifactCollector.registerTrace(tracePath);
                    }
                } catch (Exception e) {
                    System.err.println("Trace kaydedilemedi: " + e.getMessage());
                }
            }

            APIRequestContext apiRequestContext = apiRequestContextThread.get();
            if (apiRequestContext != null) {
                guvenliKapat("APIRequestContext", apiRequestContext::dispose);
                apiRequestContextThread.remove();
            }

            // Context kapanışı videonun dosyaya tamamlanmasını sağlar.
            if (context != null) {
                guvenliKapat("BrowserContext", context::close);
                contextThread.remove();
            }

            if (video != null) {
                videoyuIsle(video, testName, status);
            }

        } finally {
            // Beklenmedik bir hata oluşsa bile kaynaklar ve ThreadLocal'lar temizlenir.
            kaynaklariTemizle();
            AllureArtifactCollector.clearCurrentTest();
        }
    }

    /**
     * video.mode kuralına göre videoyu saklar ve Allure'a bağlar ya da siler.
     * Context kapatıldıktan sonra çağrılmalıdır; video dosyası ancak o zaman tamamlanır.
     */
    private static void videoyuIsle(Video video, String testName, String status) {
        try {
            if (!VideoManager.saklanmaliMi(status)) {
                video.delete();
                return;
            }

            Path videoPath = renameVideo(video.path(), testName, status);
            if (videoPath != null) {
                AllureArtifactCollector.registerVideo(videoPath);
            }
        } catch (Exception e) {
            System.err.println("Video işlenemedi: " + e.getMessage());
        }
    }

    /** Oluşturulmuş kaynakları ters sırayla güvenli biçimde kapatır. */
    private static void kaynaklariTemizle() {
        APIRequestContext apiRequestContext = apiRequestContextThread.get();
        if (apiRequestContext != null) {
            guvenliKapat("APIRequestContext", apiRequestContext::dispose);
            apiRequestContextThread.remove();
        }

        BrowserContext context = contextThread.get();
        if (context != null) {
            guvenliKapat("BrowserContext", context::close);
            contextThread.remove();
        }

        Browser browser = browserThread.get();
        if (browser != null) {
            guvenliKapat("Browser", browser::close);
            browserThread.remove();
        }

        Playwright playwright = playwrightThread.get();
        if (playwright != null) {
            guvenliKapat("Playwright", playwright::close);
            playwrightThread.remove();
        }

        pageThread.remove();
    }

    /** Kaydedilen videoyu test adı ve benzersiz kimlikle yeniden adlandırır. */
    private static Path renameVideo(
            Path generatedVideoPath,
            String testName,
            String status) throws IOException {

        if (generatedVideoPath == null || !Files.exists(generatedVideoPath)) {
            return null;
        }

        String fileName = sanitizeFileName(testName)
                + "_" + LocalDateTime.now().format(ZAMAN_BICIMI)
                + "_" + sanitizeFileName(status)
                + "_" + UUID.randomUUID().toString().substring(0, 8)
                + ".webm";

        Path targetDirectory = VideoManager.getVideoDirectory();
        Path targetVideoPath = targetDirectory.resolve(fileName);
        Files.move(generatedVideoPath, targetVideoPath, StandardCopyOption.REPLACE_EXISTING);
        return targetVideoPath;
    }

    /** Dosya adında kullanılamayan karakterleri temizler. */
    private static String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "test";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /** Bir kaynağın kapanma hatası diğer kaynakların kapanmasını engellemez. */
    private static void guvenliKapat(String ad, Runnable is) {
        try {
            is.run();
        } catch (Exception e) {
            System.err.println(ad + " kapatılırken hata: " + e.getMessage());
        }
    }
}