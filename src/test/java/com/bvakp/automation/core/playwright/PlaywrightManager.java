
package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.utils.VideoManager;
import com.microsoft.playwright.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class PlaywrightManager {

    private static final ThreadLocal<Playwright> playwrightThread =
            new ThreadLocal<>();

    private static final ThreadLocal<Browser> browserThread =
            new ThreadLocal<>();

    private static final ThreadLocal<BrowserContext> contextThread =
            new ThreadLocal<>();

    private static final ThreadLocal<Page> pageThread =
            new ThreadLocal<>();

    private static final ThreadLocal<APIRequestContext> apiRequestContextThread =
            new ThreadLocal<>();
    private static final DateTimeFormatter ZAMAN_BICIMI =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private PlaywrightManager()
    {
    }
    /**
     * Config dosyasındaki browser ve headless ayarlarını kullanarak
     * isteğe bağlı auth state ile Playwright oturumu başlatır.
     *
     * @param authStateKullan kayıtlı authentication state kullanılacaksa true
     */
    public static void initialize(boolean authStateKullan) {

        initialize(
                BrowserName.from(ConfigManager.get("browser")),
                ConfigManager.getBoolean("headless"),
                authStateKullan
        );
    }

    public static void initialize(
            BrowserName browserName,
            boolean headless,
            boolean authStateKullan) {

        Locale.setDefault(Locale.ENGLISH);

        int genislik = Integer.parseInt(
                ConfigManager.getOrDefault("viewport.width", "1280"));
        int yukseklik = Integer.parseInt(
                ConfigManager.getOrDefault("viewport.height", "720"));

        // Her nesne oluşturulur oluşturulmaz kaydedilir:
        // sonraki adım hata verirse close() elindekini temizleyebilsin.
        Playwright playwright = Playwright.create();
        playwrightThread.set(playwright);

        Browser browser = BrowserFactory.createBrowser(
                playwright, browserName, headless);
        browserThread.set(browser);

        Browser.NewContextOptions contextOptions =
                new Browser.NewContextOptions()
                        .setViewportSize(genislik, yukseklik)
                        .setRecordVideoDir(VideoManager.getVideoDirectory())
                        .setRecordVideoSize(genislik, yukseklik);

        /*
         * AVP testinde kayıtlı auth state isteniyorsa
         * mevcut storage state BrowserContext'e yüklenir.
         */
        if (authStateKullan && AvpAuthStateManager.authStateVarMi()) {
            contextOptions.setStorageStatePath(
                    AvpAuthStateManager.authStatePathAlma());
        }

        BrowserContext context = browser.newContext(contextOptions);
        contextThread.set(context);

        if (traceAcikMi()) {
            context.tracing().start(
                    new Tracing.StartOptions()
                            .setScreenshots(true)
                            .setSnapshots(true)
                            .setSources(true));
        }

        pageThread.set(context.newPage());
        apiRequestContextThread.set(playwright.request().newContext());
    }


    /**
     * Aktif thread'e ait Page nesnesini döndürür.
     *
     * @return aktif Playwright Page
     */
    public static Page getPage() {
        return pageThread.get();
    }


    /**
     * Aktif thread'e ait BrowserContext nesnesini döndürür.
     *
     * @return aktif BrowserContext
     */
    public static BrowserContext getContext() {
        return contextThread.get();
    }
    /**
     * Aktif thread'e ait Playwright nesnesini döndürür.
     *
     * @return aktif Playwright
     */
    public static Playwright getPlaywright() {

        return playwrightThread.get();
    }

    /**
     * Aktif thread'e ait APIRequestContext nesnesini döndürür.
     *
     * @return aktif APIRequestContext
     */
    public static APIRequestContext getApiRequestContext() {
        return apiRequestContextThread.get();
    }
    /**
     * Aktif BrowserContext, Browser ve Playwright nesnelerini kapatır.
     * Oluşturulan video dosyasını test adı ve durum bilgisiyle yeniden adlandırır.
     *
     * @param testName çalıştırılan test method adı
     * @param status test sonucu
     */
    public static void close(
            String testName,
            String status) {

        Video video = null;

        Page page = pageThread.get();
        /*
         * Page üzerinde video kaydı bulunuyorsa
         * Context kapanmadan önce referansı alınır.
         */
        if (page != null) {
            try {
                video = page.video();
            } catch (Exception ignored) {

                /*
                 * Video aktif değilse teardown işlemi
                 * yine devam eder.
                 */
            }
        }
        APIRequestContext apiRequestContext = apiRequestContextThread.get();
        if (apiRequestContext != null) {

            guvenliKapat("APIRequestContext", apiRequestContext::dispose);
            apiRequestContextThread.remove();
        }
        BrowserContext context = contextThread.get();
        /*
         * Trace durdurulur. Test hatalıysa zip olarak kaydedilir,
         * değilse kaydedilmeden atılır. Context kapanmadan önce yapılmalıdır.
         */
        if (context != null && traceAcikMi()) {
            try {
                if ("HATALI".equals(status)) {
                    Path traceDosyasi = Paths.get("target", "traces",
                            sanitizeFileName(testName) + "_"
                                    + LocalDateTime.now().format(ZAMAN_BICIMI)
                                    + ".zip");
                    Files.createDirectories(traceDosyasi.getParent());
                    context.tracing().stop(
                            new Tracing.StopOptions().setPath(traceDosyasi));
                } else {
                    context.tracing().stop(); // kaydetmeden at
                }
            } catch (Exception e) {
                System.err.println("Trace kaydedilemedi: " + e.getMessage());
            }
        }
        /*
         * BrowserContext kapatılır.
         * Video dosyasının diske yazılması burada tamamlanır.
         */
        if (context != null) {
            guvenliKapat("BrowserContext", context::close);
            contextThread.remove();
        }
        /*
         * Oluşan video dosyası anlamlı bir isimle yeniden adlandırılır.
         */
        if (video != null) {
            try {
                Path generatedVideoPath =
                        video.path();
                renameVideo(
                        generatedVideoPath,
                        testName,
                        status
                );

            } catch (Exception e) {
                System.err.println(
                        "Video yeniden adlandırılamadı: "
                                + e.getMessage()
                );
            }
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


        /*
         * Thread üzerinde kalan Page referansı temizlenir.
         */
        pageThread.remove();
    }


    /**
     * Playwright tarafından oluşturulan video dosyasını
     * test adı, tarih ve test sonucu ile yeniden adlandırır.
     *
     * @param generatedVideoPath Playwright video dosyası
     * @param testName test method adı
     * @param status test sonucu
     * @throws IOException dosya taşıma işlemi başarısız olursa
     */
    private static void renameVideo(
            Path generatedVideoPath,
            String testName,
            String status)
            throws IOException {

        if (generatedVideoPath == null
                || !Files.exists(
                generatedVideoPath
        )) {

            return;
        }
        String dateTime = LocalDateTime.now().format(ZAMAN_BICIMI);

        String fileName =
                sanitizeFileName(
                        testName
                )
                        + "_"
                        + dateTime
                        + "_"
                        + sanitizeFileName(
                        status
                )
                        + ".webm";


        Path targetDirectory = VideoManager.getVideoDirectory();
        Files.createDirectories(targetDirectory);
        Path targetVideoPath = targetDirectory.resolve(fileName);
        Files.move(
                generatedVideoPath,
                targetVideoPath,
                StandardCopyOption.REPLACE_EXISTING
        );
    }


    /**
     * Dosya isminde kullanılması sorun oluşturabilecek
     * karakterleri alt çizgi ile değiştirir.
     *
     * @param value temizlenecek değer
     * @return dosya adına uygun değer
     */
    private static String sanitizeFileName(
            String value) {
        if (value == null
                || value.isBlank()) {
            return "test";
        }
        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
    private static boolean traceAcikMi() {
        return Boolean.parseBoolean(
                ConfigManager.getOrDefault("trace.enabled", "true"));
    }
    private static void guvenliKapat(String ad, Runnable is) {

        try {
            is.run();

        } catch (Exception e) {

            System.err.println(
                    ad + " kapatılırken hata: " + e.getMessage()
            );
        }
    }
}
