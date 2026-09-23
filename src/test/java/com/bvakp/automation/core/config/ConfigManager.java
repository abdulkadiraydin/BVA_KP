package com.bvakp.automation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class ConfigManager {

    private static final Properties properties = new Properties();

    private static final String CONFIG_RESOURCE =
            "config/config.properties";

    private static final Path CONFIG_FILE =
            Paths.get(
                    "src",
                    "test",
                    "resources",
                    "config",
                    "config.properties"
            );

    static {
        loadProperties();
    }

    private ConfigManager() {
    }

    private static void loadProperties() {

        InputStream inputStream =
                ConfigManager.class
                        .getClassLoader()
                        .getResourceAsStream(CONFIG_RESOURCE);

        try {

            // Önce classpath üzerinden dene
            if (inputStream != null) {

                try (InputStream stream = inputStream) {
                    properties.load(stream);
                }

                return;
            }

            // Classpath'te yoksa fiziksel dosyadan oku
            if (Files.exists(CONFIG_FILE)) {

                try (InputStream stream =
                             Files.newInputStream(CONFIG_FILE)) {

                    properties.load(stream);
                }

                return;
            }

            throw new RuntimeException(
                    "config.properties bulunamadı. Aranan yollar: "
                            + CONFIG_RESOURCE
                            + " | "
                            + CONFIG_FILE.toAbsolutePath()
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Config dosyası okunurken hata oluştu.",
                    e
            );
        }
    }

    public static String get(String key) {

        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new RuntimeException(
                    "Config değeri bulunamadı: " + key
            );
        }

        return value.trim();
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}