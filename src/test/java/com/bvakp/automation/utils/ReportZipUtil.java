package com.bvakp.automation.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Son otomasyon test koşusuna ait rapor klasörünü bulur
 * ve mail gönderiminde kullanılmak üzere ZIP dosyası oluşturur.
 */
public final class ReportZipUtil {

    private static final Path RAPOR_KLASORU =
            Paths.get(
                    "target",
                    "allure-runs"
            );

    private static final Path MAIL_ZIP_KLASORU =
            Paths.get(
                    "target",
                    "mail-reports"
            );

    /*
     * Beklenen ana koşu klasör formatı:
     *
     * Kosu_20260925_184200_BASARILI
     * Kosu_20260925_184200_HATALI
     */
    private static final Pattern KOSU_KLASOR_DESENI =
            Pattern.compile(
                    "^Kosu_(\\d{8}_\\d{6})_(BASARILI|HATALI)$"
            );

    private static final DateTimeFormatter TARIH_FORMATI =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd_HHmmss"
            );

    private ReportZipUtil() {
    }

    /**
     * En son test koşusuna ait ana rapor klasörünü bulur
     * ve içerisindeki bütün class raporlarıyla birlikte
     * tek ZIP dosyası oluşturur.
     *
     * @return oluşturulan ZIP dosyasının yolu
     */
    public static Path sonKosuyuZipOlustur() {

        if (!Files.exists(
                RAPOR_KLASORU
        )) {

            throw new IllegalStateException(
                    "Rapor klasörü bulunamadı: "
                            + RAPOR_KLASORU.toAbsolutePath()
            );
        }

        try {

            Path sonKosuKlasoru =
                    sonKosuKlasorunuBul();

            String kosuTarihi =
                    kosuTarihiAl(
                            sonKosuKlasoru
                                    .getFileName()
                                    .toString()
                    );

            Files.createDirectories(
                    MAIL_ZIP_KLASORU
            );

            Path zipDosyasi =
                    MAIL_ZIP_KLASORU.resolve(
                            "BVA_KP_Test_Raporu_"
                                    + kosuTarihi
                                    + ".zip"
                    );

            /*
             * Aynı koşu daha önce ZIP oluşturduysa
             * eski ZIP dosyası yenisiyle değiştirilir.
             */
            Files.deleteIfExists(
                    zipDosyasi
            );

            kosuKlasorunuZipOlustur(
                    sonKosuKlasoru,
                    zipDosyasi
            );

            return zipDosyasi;

        } catch (IOException e) {

            throw new RuntimeException(
                    "Test raporları ZIP oluşturulurken hata oluştu.",
                    e
            );
        }
    }

    /**
     * allure-runs altındaki en güncel
     * Kosu_... rapor klasörünü bulur.
     */
    private static Path sonKosuKlasorunuBul()
            throws IOException {

        try (Stream<Path> stream =
                     Files.list(
                             RAPOR_KLASORU
                     )) {

            return stream
                    .filter(
                            Files::isDirectory
                    )
                    .filter(
                            path ->
                                    KOSU_KLASOR_DESENI
                                            .matcher(
                                                    path.getFileName()
                                                            .toString()
                                            )
                                            .matches()
                    )
                    .max(
                            Comparator.comparing(
                                    path ->
                                            LocalDateTime.parse(
                                                    kosuTarihiAl(
                                                            path.getFileName()
                                                                    .toString()
                                                    ),
                                                    TARIH_FORMATI
                                            )
                            )
                    )
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            "ZIP oluşturulabilecek test koşusu bulunamadı."
                                    )
                    );
        }
    }

    /**
     * Koşu klasör adından tarih/saat bilgisini çıkarır.
     *
     * @param klasorAdi koşu klasörünün adı
     * @return yyyyMMdd_HHmmss formatındaki tarih
     */
    private static String kosuTarihiAl(
            String klasorAdi) {

        Matcher matcher =
                KOSU_KLASOR_DESENI.matcher(
                        klasorAdi
                );

        if (!matcher.matches()) {

            throw new IllegalArgumentException(
                    "Geçersiz koşu klasör adı: "
                            + klasorAdi
            );
        }

        return matcher.group(1);
    }

    /**
     * Son koşu klasörünün tamamını
     * tek ZIP dosyasına ekler.
     */
    private static void kosuKlasorunuZipOlustur(
            Path kosuKlasoru,
            Path zipDosyasi)
            throws IOException {

        try (OutputStream outputStream =
                     Files.newOutputStream(
                             zipDosyasi
                     );

             ZipOutputStream zipOutputStream =
                     new ZipOutputStream(
                             outputStream
                     );

             Stream<Path> paths =
                     Files.walk(
                             kosuKlasoru
                     )) {

            List<Path> dosyalar =
                    paths
                            .filter(
                                    Files::isRegularFile
                            )
                            .toList();

            Path parent =
                    kosuKlasoru.getParent();

            for (Path dosya : dosyalar) {

                String zipEntryAdi =
                        parent
                                .relativize(
                                        dosya
                                )
                                .toString()
                                .replace(
                                        "\\",
                                        "/"
                                );

                ZipEntry zipEntry =
                        new ZipEntry(
                                zipEntryAdi
                        );

                zipOutputStream.putNextEntry(
                        zipEntry
                );

                Files.copy(
                        dosya,
                        zipOutputStream
                );

                zipOutputStream.closeEntry();
            }
        }
    }
}