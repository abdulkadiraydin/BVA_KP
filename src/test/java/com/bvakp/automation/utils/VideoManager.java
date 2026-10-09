package com.bvakp.automation.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

public final class VideoManager {

    private static final Path VIDEO_DIRECTORY =
            Paths.get(
                    "target",
                    "videos"
            );

    private static final int RETENTION_DAYS = 2;

    private VideoManager() {
    }

    /**
     * Video kayıtlarının tutulacağı klasörü oluşturur
     * ve klasör yolunu döndürür.
     */
    public static Path getVideoDirectory() {

        try {

            Files.createDirectories(
                    VIDEO_DIRECTORY
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Video klasörü oluşturulamadı: "
                            + VIDEO_DIRECTORY.toAbsolutePath(),
                    e
            );
        }

        return VIDEO_DIRECTORY;
    }


/*
    public static void deleteOldVideos() {

        if (!Files.exists(VIDEO_DIRECTORY)) {
            return;
        }

        Instant cutoffDate =
                Instant.now().minus(
                        RETENTION_DAYS,
                        ChronoUnit.DAYS
                );

        try (Stream<Path> paths =
                     Files.walk(VIDEO_DIRECTORY)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            isOlderThan(
                                    path,
                                    cutoffDate
                            )
                    )
                    .forEach(
                            VideoManager::deleteVideo
                    );

        } catch (IOException e) {

            System.err.println(
                    "Eski video kayıtları temizlenirken hata oluştu: "
                            + e.getMessage()
            );
        }

        deleteEmptyDirectories();
    }
*/

    private static boolean isOlderThan(
            Path path,
            Instant cutoffDate) {

        try {

            return Files
                    .getLastModifiedTime(path)
                    .toInstant()
                    .isBefore(cutoffDate);

        } catch (IOException e) {

            return false;
        }
    }

    /**
     * Video dosyasını siler.
     */
    private static void deleteVideo(Path path) {

        try {

            Files.deleteIfExists(path);

        } catch (IOException e) {

            System.err.println(
                    "Video silinemedi: "
                            + path.toAbsolutePath()
            );
        }
    }

    /**
     * Video temizliği sonrasında boş kalan
     * klasörleri temizler.
     */
    private static void deleteEmptyDirectories() {

        if (!Files.exists(VIDEO_DIRECTORY)) {
            return;
        }

        try (Stream<Path> paths =
                     Files.walk(VIDEO_DIRECTORY)) {

            paths
                    .sorted(
                            Comparator.reverseOrder()
                    )
                    .filter(Files::isDirectory)
                    .filter(path ->
                            !path.equals(VIDEO_DIRECTORY)
                    )
                    .forEach(path -> {

                        try (Stream<Path> content =
                                     Files.list(path)) {

                            if (content.findAny().isEmpty()) {
                                Files.deleteIfExists(path);
                            }

                        } catch (IOException ignored) {
                        }
                    });

        } catch (IOException ignored) {
        }
    }
}