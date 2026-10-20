package com.bvakp.automation.mail;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Mail gönderiminde kullanılacak SMTP ayarlarını yönetir.
 *
 * <p>Değerler önce işletim sistemi ortam değişkeninden, bulunamazsa proje
 * kökündeki .env.mail dosyasından okunur. Şifre içerdiği için bu ayarlar
 * config.properties'e yazılmaz; .env.mail Git'e eklenmemelidir.
 */
public final class MailConfig {

    private static final Dotenv DOTENV = Dotenv.configure()
            .filename(".env.mail")
            .ignoreIfMissing()
            .load();

    private MailConfig() {
    }

    public static String host() {
        return getRequiredValue("MAIL_HOST");
    }

    public static int port() {
        String value = getRequiredValue("MAIL_PORT");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Geçersiz MAIL_PORT değeri: " + value, e);
        }
    }

    public static String username() {
        return getRequiredValue("MAIL_USERNAME");
    }

    public static String password() {
        return getRequiredValue("MAIL_PASSWORD");
    }

    public static String from() {
        return getRequiredValue("MAIL_FROM");
    }

    /** Virgülle ayrılmış bir veya birden fazla alıcı. */
    public static String to() {
        return getRequiredValue("MAIL_TO");
    }

    /** Tanımlı değilse true (çoğu kurumsal SMTP sunucusu STARTTLS ister). */
    public static boolean startTlsEnabled() {
        String value = getValue("MAIL_STARTTLS");
        return value == null || Boolean.parseBoolean(value);
    }

    /** Önce ortam değişkeninde, sonra .env.mail dosyasında arar; bulamazsa null döner. */
    private static String getValue(String variableName) {

        String systemValue = System.getenv(variableName);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String dotenvValue = DOTENV.get(variableName);
        if (dotenvValue != null && !dotenvValue.isBlank()) {
            return dotenvValue.trim();
        }

        return null;
    }

    private static String getRequiredValue(String variableName) {

        String value = getValue(variableName);

        if (value == null) {
            throw new IllegalStateException(
                    "Mail ayarı bulunamadı: " + variableName
                            + " (ortam değişkeni veya .env.mail)");
        }

        return value;
    }
}