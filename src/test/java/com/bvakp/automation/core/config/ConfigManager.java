package com.bvakp.automation.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private ConfigManager() {
    }

    private static void loadProperties() {

        try (InputStream inputStream =
                     ConfigManager.class
                             .getClassLoader()
                             .getResourceAsStream("config/config.properties")) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "config.properties dosyası bulunamadı."
                );
            }

            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Config dosyası okunurken hata oluştu.",
                    e
            );
        }
    }

    public static String get(String key) {

        String value = properties.getProperty(key);

        if (value == null) {
            throw new RuntimeException(
                    "Config değeri bulunamadı: " + key
            );
        }

        return value;
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}