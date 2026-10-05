
package com.bvakp.automation.utils;

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
 * Bir test başarısız olduğunda hata sayacı artırılır,
 * başarılı olduğunda ilgili testin sayacı sıfırlanır.
 *
 * Ayrıca aynı test için son hata bilgilendirme mailinin
 * hangi tarihte gönderildiği saklanır.
 */
public final class TestFailureTracker {

    private static final Path STATE_DIRECTORY =
            Paths.get(
                    ".automation-state"
            );

    private static final Path STATE_FILE =
            STATE_DIRECTORY.resolve(
                    "test-failure-state.properties"
            );

    private static final int MAIL_THRESHOLD = 5;

    private TestFailureTracker() {
    }

    /**
     * Test başarısız olduğunda ilgili testin ardışık
     * hata sayısını bir artırır.
     *
     * @param className test class adı
     * @param testName test metot adı
     * @return güncel ardışık hata sayısı
     */
    public static synchronized int hataKaydet(
            String className,
            String testName) {

        Properties properties =
                propertiesOku();

        String key =
                testKey(
                        className,
                        testName
                );

        int mevcutHataSayisi =
                Integer.parseInt(
                        properties.getProperty(
                                key + ".failureCount",
                                "0"
                        )
                );

        int yeniHataSayisi =
                mevcutHataSayisi + 1;

        properties.setProperty(
                key + ".failureCount",
                String.valueOf(
                        yeniHataSayisi
                )
        );

        propertiesYaz(
                properties
        );

        return yeniHataSayisi;
    }

    /**
     * Test başarılı olduğunda ilgili testin
     * ardışık hata sayısını sıfırlar.
     *
     * @param className test class adı
     * @param testName test metot adı
     */
    public static synchronized void basariliTestKaydet(
            String className,
            String testName) {

        Properties properties =
                propertiesOku();

        String key =
                testKey(
                        className,
                        testName
                );

        properties.setProperty(
                key + ".failureCount",
                "0"
        );

        propertiesYaz(
                properties
        );
    }

    /**
     * Testin mail gönderim eşiğine ulaşıp ulaşmadığını
     * ve aynı gün içerisinde daha önce uyarı maili
     * gönderilip gönderilmediğini kontrol eder.
     *
     * @param className test class adı
     * @param testName test metot adı
     * @param failureCount güncel ardışık hata sayısı
     * @return mail gönderilmesi gerekiyorsa true
     */
    public static synchronized boolean mailGonderilmeliMi(
            String className,
            String testName,
            int failureCount) {

        if (failureCount < MAIL_THRESHOLD) {
            return false;
        }

        Properties properties =
                propertiesOku();

        String key =
                testKey(
                        className,
                        testName
                );

        String sonMailTarihi =
                properties.getProperty(
                        key + ".lastAlertDate",
                        ""
                );

        String bugun =
                LocalDate.now()
                        .toString();

        return !bugun.equals(
                sonMailTarihi
        );
    }

    /**
     * Test için uyarı maili başarıyla gönderildikten sonra
     * mail gönderim tarihini kaydeder.
     *
     * Bu bilgi aynı test için aynı gün içerisinde
     * tekrar mail gönderilmesini engeller.
     */
    public static synchronized void mailGonderildiKaydet(
            String className,
            String testName) {

        Properties properties =
                propertiesOku();

        String key =
                testKey(
                        className,
                        testName
                );

        properties.setProperty(
                key + ".lastAlertDate",
                LocalDate.now().toString()
        );

        propertiesYaz(
                properties
        );
    }

    /**
     * Test class ve test metot adından benzersiz
     * takip anahtarı oluşturur.
     */
    private static String testKey(
            String className,
            String testName) {

        return className
                + "."
                + testName;
    }

    /**
     * Mevcut takip dosyasını okur.
     * Dosya henüz yoksa boş Properties nesnesi döndürür.
     */
    private static Properties propertiesOku() {

        Properties properties =
                new Properties();

        if (!Files.exists(
                STATE_FILE
        )) {
            return properties;
        }

        try (InputStream inputStream =
                     Files.newInputStream(
                             STATE_FILE
                     )) {

            properties.load(
                    inputStream
            );

            return properties;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Test hata takip dosyası okunamadı.",
                    e
            );
        }
    }

    /**
     * Güncel hata takip bilgilerini kalıcı dosyaya yazar.
     */
    private static void propertiesYaz(
            Properties properties) {

        try {

            Files.createDirectories(
                    STATE_DIRECTORY
            );

            try (OutputStream outputStream =
                         Files.newOutputStream(
                                 STATE_FILE
                         )) {

                properties.store(
                        outputStream,
                        "BVA KP Test Failure State"
                );
            }

        } catch (IOException e) {

            throw new RuntimeException(
                    "Test hata takip dosyası yazılamadı.",
                    e
            );
        }
    }
}