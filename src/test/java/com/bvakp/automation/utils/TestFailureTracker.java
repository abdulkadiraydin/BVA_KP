package com.bvakp.automation.utils;

import com.bvakp.automation.core.config.ConfigManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Properties;

/**
 * Testlerin koşular arasındaki ardışık hata sayılarını takip eder.
 *
 * <p>Testi tekrar çalıştırmaz; her koşunun sonucunu kalıcı bir dosyaya not eder.
 * Başarısız koşuda sayaç artar, başarılı koşuda sıfırlanır. Sayaç eşiğe ulaştığında
 * aynı test için günde en fazla bir bilgilendirme maili gönderilmesine izin verir.
 *
 * <p>Ayarlar (-D veya config.properties):
 * failure.tracking.enabled (varsayılan false), failure.alert.threshold (varsayılan 5).
 */
public final class TestFailureTracker {

    private static final Path STATE_DIRECTORY = Paths.get(".automation-state");
    private static final Path STATE_FILE = STATE_DIRECTORY.resolve("test-failure-state.properties");

    private static final int VARSAYILAN_ESIK = 5;

    private TestFailureTracker() {
    }

    /**
     * Test başarısız olduğunda ardışık hata sayısını bir artırır.
     *
     * @return güncel ardışık hata sayısı; takip kapalıysa 0
     */
    public static synchronized int hataKaydet(String className, String testName) {

        if (!takipAcikMi()) {
            return 0;
        }

        Properties properties = propertiesOku();
        String key = testKey(className, testName);

        int mevcutHataSayisi;
        try {
            mevcutHataSayisi = Integer.parseInt(properties.getProperty(key + ".failureCount", "0"));
        } catch (NumberFormatException e) {
            mevcutHataSayisi = 0;
        }

        int yeniHataSayisi = mevcutHataSayisi + 1;
        properties.setProperty(key + ".failureCount", String.valueOf(yeniHataSayisi));
        propertiesYaz(properties);

        return yeniHataSayisi;
    }

    /** Test başarılı olduğunda ardışık hata sayısını sıfırlar. */
    public static synchronized void basariliTestKaydet(String className, String testName) {

        if (!takipAcikMi()) {
            return;
        }

        Properties properties = propertiesOku();
        properties.setProperty(testKey(className, testName) + ".failureCount", "0");
        propertiesYaz(properties);
    }

    /**
     * Eşiğe ulaşılmışsa ve bu test için bugün henüz mail gönderilmemişse true döner.
     */
    public static synchronized boolean mailGonderilmeliMi(
            String className,
            String testName,
            int failureCount) {

        if (!takipAcikMi() || failureCount < mailEsigi()) {
            return false;
        }

        String sonMailTarihi = propertiesOku()
                .getProperty(testKey(className, testName) + ".lastAlertDate", "");

        return !LocalDate.now().toString().equals(sonMailTarihi);
    }

    /**
     * Mail başarıyla gönderildikten sonra tarihi kaydeder;
     * aynı test için aynı gün tekrar mail gönderilmesini engeller.
     */
    public static synchronized void mailGonderildiKaydet(String className, String testName) {

        if (!takipAcikMi()) {
            return;
        }

        Properties properties = propertiesOku();
        properties.setProperty(testKey(className, testName) + ".lastAlertDate", LocalDate.now().toString());
        propertiesYaz(properties);
    }

    private static String testKey(String className, String testName) {
        return className + "." + testName;
    }

    /** Takip dosyasını okur; dosya henüz yoksa boş Properties döndürür. */
    private static Properties propertiesOku() {

        Properties properties = new Properties();

        if (!Files.exists(STATE_FILE)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(STATE_FILE)) {
            properties.load(inputStream);
            return properties;
        } catch (IOException e) {
            throw new RuntimeException("Test hata takip dosyası okunamadı.", e);
        }
    }

    private static void propertiesYaz(Properties properties) {
        try {
            Files.createDirectories(STATE_DIRECTORY);
            try (OutputStream outputStream = Files.newOutputStream(STATE_FILE)) {
                properties.store(outputStream, "BVA KP Test Failure State");
            }
        } catch (IOException e) {
            throw new RuntimeException("Test hata takip dosyası yazılamadı.", e);
        }
    }

    /**
     * Takip yalnızca CI gibi düzenli koşu yapılan ortamlarda açılmalıdır;
     * geliştirici denemeleri mail tetiklemesin.
     */
    private static boolean takipAcikMi() {
        return Boolean.parseBoolean(ConfigManager.getOrDefault("failure.tracking.enabled", "false"));
    }

    /** Hatalı veya 1'den küçük değerde varsayılan eşik kullanılır. */
    private static int mailEsigi() {
        String value = ConfigManager.getOrDefault("failure.alert.threshold", String.valueOf(VARSAYILAN_ESIK));
        try {
            int esik = Integer.parseInt(value);
            return esik >= 1 ? esik : VARSAYILAN_ESIK;
        } catch (NumberFormatException e) {
            return VARSAYILAN_ESIK;
        }
    }
}