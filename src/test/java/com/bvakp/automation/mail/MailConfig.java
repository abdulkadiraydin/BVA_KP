package com.bvakp.automation.mail;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Mail gönderiminde kullanılacak SMTP ayarlarını yönetir.
 *
 * Değerler öncelikle sistem Environment Variable üzerinden,
 * bulunamazsa proje kökündeki .env.mail dosyasından okunur.
 */
public final class MailConfig {

    private static final Dotenv DOTENV =
            Dotenv.configure()
                    .filename(".env.mail")
                    .ignoreIfMissing()
                    .load();

    private MailConfig() {
    }

    public static String host() {
        return getRequiredValue(
                "MAIL_HOST"
        );
    }

    public static int port() {

        return Integer.parseInt(
                getRequiredValue(
                        "MAIL_PORT"
                )
        );
    }

    public static String username() {
        return getRequiredValue(
                "MAIL_USERNAME"
        );
    }

    public static String password() {
        return getRequiredValue(
                "MAIL_PASSWORD"
        );
    }

    public static String from() {
        return getRequiredValue(
                "MAIL_FROM"
        );
    }

    public static String to() {
        return getRequiredValue(
                "MAIL_TO"
        );
    }

    public static boolean startTlsEnabled() {

        String value =
                getValue(
                        "MAIL_STARTTLS"
                );

        if (value == null
                || value.isBlank()) {

            return true;
        }

        return Boolean.parseBoolean(
                value
        );
    }

    /**
     * Değeri önce işletim sistemi environment variable
     * alanından, ardından .env.mail dosyasından arar.
     */
    private static String getValue(
            String variableName) {

        String systemValue =
                System.getenv(
                        variableName
                );

        if (systemValue != null
                && !systemValue.isBlank()) {

            return systemValue.trim();
        }

        String dotenvValue =
                DOTENV.get(
                        variableName
                );

        if (dotenvValue != null
                && !dotenvValue.isBlank()) {

            return dotenvValue.trim();
        }

        return null;
    }

    /**
     * Zorunlu mail ayarını döndürür.
     *
     * Değer environment variable veya .env.mail
     * içerisinde bulunamazsa hata oluşturur.
     */
    private static String getRequiredValue(
            String variableName) {

        String value =
                getValue(
                        variableName
                );

        if (value == null) {

            throw new IllegalStateException(
                    "Mail ayarı bulunamadı: "
                            + variableName
            );
        }

        return value;
    }
}