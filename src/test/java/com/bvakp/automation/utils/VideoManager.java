package com.bvakp.automation.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class VideoManager {

    private static final Path VIDEO_DIRECTORY =
            Paths.get("target", "videos");

    private VideoManager() {
    }

    /**
     * Video kayıtlarının tutulacağı klasörü oluşturur
     * ve klasör yolunu döndürür.
     */
    public static Path getVideoDirectory() {

        try {
            Files.createDirectories(VIDEO_DIRECTORY);

        } catch (IOException e) {

            throw new RuntimeException(
                    "Video klasörü oluşturulamadı: "
                            + VIDEO_DIRECTORY.toAbsolutePath(),
                    e
            );
        }

        return VIDEO_DIRECTORY;
    }
}