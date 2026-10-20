
package com.bvakp.automation.core.playwright;

import com.bvakp.automation.core.config.ConfigManager;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Playwright Java trace kayıtlarının yaşam döngüsünü yönetir.
 * İşlemler, ilgili testin çalıştığı thread kapsamında yürütülür.
 *
 * <p>Çalışma modu "trace.mode" ayarından okunur (-Dtrace.mode veya config.properties):
 * off, on, retain-on-failure (varsayılan).
 *
 * <p>Trace dosyaları sayfa içeriklerini ve ağ verilerini barındırabilir.
 * Bu dosyalar hassas test çıktıları olarak değerlendirilmeli
 * ve kesinlikle kaynak kod deposuna (Git) eklenmemelidir.
 */
public final class TraceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TraceManager.class);
    private static final Path TRACE_DIRECTORY = Paths.get("target", "traces");
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
                    .withZone(ZoneOffset.UTC);
    private static final ThreadLocal<TraceSession> ACTIVE_SESSION = new ThreadLocal<>();
    private static final Set<String> NON_FAILURE_STATUSES = Set.of(
            "BASARILI", "SUCCESS", "PASSED", "ATLANDI", "SKIPPED", "HAZIRLIK", "TEMIZLIK"
    );

    private enum Mode { OFF, ON, RETAIN_ON_FAILURE }

    private record TraceSession(BrowserContext context, Mode mode) { }

    private TraceManager() { }

    /** BrowserContext oluşturulduktan sonra, Page açılmadan önce çağrılmalıdır. */
    public static void start(BrowserContext context) {
        if (context == null) {
            return;
        }

        Mode mode = configuredMode();
        if (mode == Mode.OFF) {
            return;
        }

        if (ACTIVE_SESSION.get() != null) {
            LOGGER.warn("A trace session already exists on this thread; tracing was not restarted.");
            return;
        }

        try {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(false));
            ACTIVE_SESSION.set(new TraceSession(context, mode));
            LOGGER.debug("Playwright tracing started (mode={}).", mode);
        } catch (RuntimeException e) {
            // Tanılama işlemlerindeki hatalar, işlevsel testlerin çalışmasını engellememelidir.
            LOGGER.warn("Playwright tracing could not be started: {}", e.getMessage());
        }
    }

    /**
     * BrowserContext.close() çağrılmadan ÖNCE trace kaydını durdurur.
     * Trace dosyasını yalnızca belirlenen saklama modunun izin verdiği
     * durumlarda kaydeder. Dosya kaydedilmişse dosya yolunu döndürür.
     */
    public static Path stop(BrowserContext context, String testName, String status) {
        TraceSession session = ACTIVE_SESSION.get();
        if (session == null || session.context() != context) {
            return null;
        }
        ACTIVE_SESSION.remove();

        try {
            if (!shouldRetain(session.mode(), status)) {
                context.tracing().stop();
                return null;
            }

            Files.createDirectories(TRACE_DIRECTORY);
            String filename = safeFilename(testName) + "_"
                    + safeFilename(status) + "_"
                    + TIMESTAMP.format(Instant.now()) + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + ".zip";
            Path savedPath = TRACE_DIRECTORY.resolve(filename).toAbsolutePath();

            context.tracing().stop(new Tracing.StopOptions().setPath(savedPath));
            if (!Files.isRegularFile(savedPath) || Files.size(savedPath) == 0) {
                LOGGER.warn("Trace file was not created correctly: {}", savedPath);
                return null;
            }

            LOGGER.info("Playwright trace saved: {}", savedPath);
            return savedPath;
        } catch (Exception e) {
            LOGGER.warn("Playwright trace could not be saved: {}", e.getMessage());
            try {
                context.tracing().stop();
            } catch (RuntimeException ignored) {
                // Trace işlemi başarısız olsa bile BrowserContext kapatma işlemi devam etmelidir.
            }
            return null;
        }
    }

    private static Mode configuredMode() {
        // Değer sırasıyla -Dtrace.mode, config.properties, varsayılandan gelir.
        String value = ConfigManager.getOrDefault("trace.mode", "retain-on-failure");

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "off" -> Mode.OFF;
            case "on" -> Mode.ON;
            case "retain-on-failure" -> Mode.RETAIN_ON_FAILURE;
            default -> {
                LOGGER.warn("Unknown trace.mode '{}'; using retain-on-failure.", value);
                yield Mode.RETAIN_ON_FAILURE;
            }
        };
    }

    private static boolean shouldRetain(Mode mode, String status) {
        if (mode == Mode.ON) {
            return true;
        }
        String normalizedStatus = status == null
                ? "BILINMIYOR"
                : status.trim().toUpperCase(Locale.ROOT);
        // Hata analizi sırasında kanıt kaybını önlemek için bilinmeyen test durumlarının trace kayıtları saklanır.
        return mode == Mode.RETAIN_ON_FAILURE
                && !NON_FAILURE_STATUSES.contains(normalizedStatus);
    }

    private static String safeFilename(String value) {
        String name = value == null ? "test" : value.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (name.isBlank() || name.equals(".") || name.equals("..")) {
            return "test";
        }
        return name.length() > 90 ? name.substring(0, 90) : name;
    }

}
