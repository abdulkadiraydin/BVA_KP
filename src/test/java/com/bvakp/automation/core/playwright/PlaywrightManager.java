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


    /**
     * Utility class olduğu için nesne oluşturulmasını engeller.
     */
    private PlaywrightManager() {
    }


    /**
     * Config dosyasındaki browser ve headless ayarlarını kullanarak
     * auth state olmadan Playwright oturumu başlatır.
     */
    public static void initialize() {

        BrowserName browserName =
                BrowserName.from(
                        ConfigManager.get("browser")
                );

        boolean headless =
                ConfigManager.getBoolean(
                        "headless"
                );

        initialize(
                browserName,
                headless,
                false
        );
    }


    /**
     * Config dosyasındaki browser ve headless ayarlarını kullanarak
     * isteğe bağlı auth state ile Playwright oturumu başlatır.
     *
     * @param authStateKullan kayıtlı authentication state kullanılacaksa true
     */
    public static void initialize(
            boolean authStateKullan) {

        BrowserName browserName =
                BrowserName.from(
                        ConfigManager.get("browser")
                );

        boolean headless =
                ConfigManager.getBoolean(
                        "headless"
                );

        initialize(
                browserName,
                headless,
                authStateKullan
        );
    }


    /**
     * Belirtilen browser ve headless ayarlarıyla
     * auth state olmadan Playwright oturumu başlatır.
     *
     * @param browserName kullanılacak browser
     * @param headless browser headless çalışacaksa true
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
     * Browser, BrowserContext ve Page nesnelerini oluşturur.
     * Her thread kendi Playwright nesnelerini kullandığı için
     * paralel test çalıştırmaya uygundur.
     *
     * @param browserName kullanılacak browser
     * @param headless browser headless çalışacaksa true
     * @param authStateKullan kayıtlı auth state kullanılacaksa true
     */
    public static void initialize(
            BrowserName browserName,
            boolean headless,
            boolean authStateKullan) {

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


        /*
         * AVP testinde kayıtlı auth state isteniyorsa
         * mevcut storage state BrowserContext'e yüklenir.
         */
        if (authStateKullan
                && AvpAuthStateManager.authStateVarMi()) {

            contextOptions.setStorageStatePath(
                    AvpAuthStateManager.authStatePathAlma()
            );
        }


        BrowserContext context =
                browser.newContext(
                        contextOptions
                );

        Page page =
                context.newPage();


        /*
         * Her test thread'i kendi Playwright nesnelerini tutar.
         */
        playwrightThread.set(
                playwright
        );

        browserThread.set(
                browser
        );

        contextThread.set(
                context
        );

        pageThread.set(
                page
        );
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
     * Aktif thread'e ait Browser nesnesini döndürür.
     *
     * @return aktif Browser
     */
    public static Browser getBrowser() {

        return browserThread.get();
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
     * Test bilgisi verilmediğinde varsayılan değerlerle
     * Playwright oturumunu kapatır.
     */
    public static void close() {

        close(
                "test",
                "BILINMIYOR"
        );
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

        Page page =
                pageThread.get();


        /*
         * Page üzerinde video kaydı bulunuyorsa
         * Context kapanmadan önce referansı alınır.
         */
        if (page != null) {

            try {

                video =
                        page.video();

            } catch (Exception ignored) {

                /*
                 * Video aktif değilse teardown işlemi
                 * yine devam eder.
                 */
            }
        }


        /*
         * BrowserContext kapatılır.
         * Video dosyasının diske yazılması burada tamamlanır.
         */
        BrowserContext context =
                contextThread.get();

        if (context != null) {

            try {

                context.close();

            } finally {

                contextThread.remove();
            }
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


        /*
         * Browser kapatılır.
         */
        Browser browser =
                browserThread.get();

        if (browser != null) {

            try {

                browser.close();

            } finally {

                browserThread.remove();
            }
        }


        /*
         * Playwright instance kapatılır.
         */
        Playwright playwright =
                playwrightThread.get();

        if (playwright != null) {

            try {

                playwright.close();

            } finally {

                playwrightThread.remove();
            }
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


        String dateTime =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd_HHmmss_SSS"
                                )
                        );


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
}