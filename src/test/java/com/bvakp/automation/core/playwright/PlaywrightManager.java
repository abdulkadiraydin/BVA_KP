package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.auth.AvpAuthStateManager;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.utils.VideoManager;
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

        initialize(
                browserName,
                headless,
                false
        );
    }

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

    public static void initialize(
            BrowserName browserName,
            boolean headless,
            boolean authStateKullan) {
/*
        VideoManager.deleteOldVideos();
*/
        Playwright playwright =
                Playwright.create();

        Browser browser =
                BrowserFactory.createBrowser(
                        playwright,
                        browserName,
                        headless
                );

        Browser.NewContextOptions contextOptions =
                new Browser.NewContextOptions()
                        .setRecordVideoDir(
                                VideoManager.getVideoDirectory()
                        )
                        .setRecordVideoSize(
                                1280,
                                720
                        );

        if (authStateKullan
                && AvpAuthStateManager.authStateVarMi()) {

            contextOptions.setStorageStatePath(
                    AvpAuthStateManager.authStatePathAlma()
            );
        }

        BrowserContext context =
                browser.newContext(contextOptions);

        Page page =
                context.newPage();

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

    /**
     * Eski kullanım için varsayılan close metodu.
     */
    public static void close() {

        close(
                "test",
                "BILINMIYOR"
        );
    }

    /**
     * BrowserContext kapatıldıktan sonra oluşan Playwright videosunu
     * test method adı + tarih + durum formatında yeniden adlandırır.
     *
     * Örnek:
     * kaynakSilmeKontrolu_20260925_133518_BASARILI.webm
     */
    public static void close(
            String testName,
            String status) {

        Video video = null;

        Page page =
                pageThread.get();

        if (page != null) {

            try {
                video = page.video();
            } catch (Exception ignored) {
                // Video aktif değilse teardown yine devam etsin.
            }
        }

        if (contextThread.get() != null) {

            contextThread.get().close();
            contextThread.remove();
        }

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

    private static void renameVideo(
            Path generatedVideoPath,
            String testName,
            String status)
            throws IOException {

        if (generatedVideoPath == null
                || !Files.exists(generatedVideoPath)) {
            return;
        }

        String dateTime =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd_HHmmss_SSS"
                                )
                        );

        String fileName =
                sanitizeFileName(testName)
                        + "_"
                        + dateTime
                        + "_"
                        + sanitizeFileName(status)
                        + ".webm";

        Path targetDirectory =
                VideoManager.getVideoDirectory();

        Files.createDirectories(
                targetDirectory
        );

        Path targetVideoPath =
                targetDirectory.resolve(
                        fileName
                );

        Files.move(
                generatedVideoPath,
                targetVideoPath,
                StandardCopyOption.REPLACE_EXISTING
        );
    }

    private static String sanitizeFileName(
            String value) {

        if (value == null || value.isBlank()) {
            return "test";
        }

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
}
