package com.bvakp.automation.utils;

import com.bvakp.automation.core.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;

/**
 * Playwright video kayıtlarının klasörünü ve saklama kuralını yönetir.
 *
 * <p>Mod "video.mode" ayarından okunur (-Dvideo.mode veya config.properties):
 * off, on, retain-on-failure (varsayılan).
 */
public final class VideoManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(VideoManager.class);

    private static final Path VIDEO_DIRECTORY = Paths.get("target", "videos");

    // TraceManager ile aynı küme: bunlar dışındaki her durum hata kabul edilir.
    private static final Set<String> NON_FAILURE_STATUSES = Set.of(
            "BASARILI", "SUCCESS", "PASSED", "ATLANDI", "SKIPPED", "HAZIRLIK", "TEMIZLIK"
    );

    private enum Mode { OFF, ON, RETAIN_ON_FAILURE }

    private VideoManager() {
    }

    /** Video kayıtlarının tutulacağı klasörü oluşturur ve yolunu döndürür. */
    public static Path getVideoDirectory() {
        try {
            Files.createDirectories(VIDEO_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Video klasörü oluşturulamadı: " + VIDEO_DIRECTORY.toAbsolutePath(), e);
        }
        return VIDEO_DIRECTORY;
    }

    /** Tarayıcı açılırken video kaydının başlatılıp başlatılmayacağını belirtir. */
    public static boolean kayitAcikMi() {
        return configuredMode() != Mode.OFF;
    }

    /** Test kapanırken videonun saklanıp saklanmayacağını belirtir. */
    public static boolean saklanmaliMi(String status) {

        Mode mode = configuredMode();

        if (mode == Mode.ON) {
            return true;
        }
        if (mode == Mode.OFF) {
            return false;
        }

        String normalizedStatus = status == null
                ? "BILINMIYOR"
                : status.trim().toUpperCase(Locale.ROOT);

        // Bilinmeyen durumlarda kanıt kaybını önlemek için video saklanır.
        return !NON_FAILURE_STATUSES.contains(normalizedStatus);
    }

    private static Mode configuredMode() {

        String value = ConfigManager.getOrDefault("video.mode", "retain-on-failure");

        return switch (value.toLowerCase(Locale.ROOT)) {
            case "off" -> Mode.OFF;
            case "on" -> Mode.ON;
            case "retain-on-failure" -> Mode.RETAIN_ON_FAILURE;
            default -> {
                LOGGER.warn("Unknown video.mode '{}'; using retain-on-failure.", value);
                yield Mode.RETAIN_ON_FAILURE;
            }
        };
    }
}