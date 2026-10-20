package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.reporting.AllureArtifactCollector;
import com.bvakp.automation.utils.VideoManager;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Video;

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

    private PlaywrightManager() {
    }

    /** İstenirse kayıtlı authentication state kullanılarak tarayıcıyı başlatır. */
    public static void initialize(boolean authStateKullan) {
        initialize(
                BrowserName.from(ConfigManager.get("browser")),
                ConfigManager.getBoolean("headless"),
                authStateKullan
        );
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

        int genislik = Integer.parseInt(
                ConfigManager.getOrDefault("viewport.width", "1280"));
        int yukseklik = Integer.parseInt(
                ConfigManager.getOrDefault("viewport.height", "720"));

        try {
            Playwright playwright = Playwright.create();
            playwrightThread.set(playwright);

            Browser browser = BrowserFactory.createBrowser(playwright, browserName, headless);
            browserThread.set(browser);

            Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                    .setViewportSize(genislik, yukseklik)
                    .setRecordVideoDir(VideoManager.getVideoDirectory())
                    .setRecordVideoSize(genislik, yukseklik);

            if (authStateKullan && AvpAuthStateManager.authStateVarMi()) {
                contextOptions.setStorageStatePath(AvpAuthStateManager.authStatePathAlma());
            }

            BrowserContext context = browser.newContext(contextOptions);
            contextThread.set(context);

            // TraceManager modu (off/on/retain-on-failure) tek yetkili mekanizmadır.
            // Trace, ilk sayfa oluşturulmadan önce başlatılmalıdır.
            TraceManager.start(context);

            Page page = context.newPage();
            pageThread.set(page);

            APIRequestContext apiRequestContext = playwright.request().newContext();
            apiRequestContextThread.set(apiRequestContext);
        } catch (RuntimeException | Error e) {
            // Kurulum yarıda kaldıysa açılan Playwright kaynaklarını güvenli temizle.
            kaynaklariTemizle();
            throw e;
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
     * Önce Trace'i kaydeder, ardından Context'i kapatıp videoyu arşivler.
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
                try {
                    Path videoPath = renameVideo(video.path(), testName, status);
                    if (videoPath != null) {
                        AllureArtifactCollector.registerVideo(videoPath);
                    }
                } catch (Exception e) {
                    System.err.println("Video arşivlenemedi: " + e.getMessage());
                }
            }
        } finally {
            // Beklenmedik bir hata oluşsa bile kaynaklar ve ThreadLocal'lar temizlenir.
            kaynaklariTemizle();
            AllureArtifactCollector.clearCurrentTest();
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

        String dateTime = LocalDateTime.now().format(ZAMAN_BICIMI);
        String fileName = sanitizeFileName(testName)
                + "_" + dateTime
                + "_" + sanitizeFileName(status)
                + "_" + UUID.randomUUID().toString().substring(0, 8)
                + ".webm";

        Path targetDirectory = VideoManager.getVideoDirectory();
        Files.createDirectories(targetDirectory);
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
