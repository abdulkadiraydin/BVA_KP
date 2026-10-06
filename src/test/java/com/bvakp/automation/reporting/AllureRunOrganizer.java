package com.bvakp.automation.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Test koşusu tamamlandıktan sonra oluşturulan raporları
 * koşu ve test class bazında düzenler.
 *
 * Her TestNG koşusu için tarih ve genel test durumunu içeren
 * tek bir ana klasör oluşturulur.
 *
 * Ana koşu klasörünün altında her test class'ına ait
 * ayrı HTML rapor klasörü bulunur.
 */
public final class AllureRunOrganizer {

    private static final Path ALLURE_SOURCE =
            Paths.get(
                    System.getProperty(
                            "allure.results.directory",
                            Paths.get(
                                    "target",
                                    "allure-results"
                            ).toString()
                    )
            );

    private static final Path ALLURE_RUNS =
            Paths.get(
                    "target",
                    "allure-runs"
            );

    /*
     * Teknik Allure JSON ve attachment dosyaları
     * class bazlı HTML raporlardan ayrı tutulur.
     */
    private static final Path ALLURE_RAW_ARCHIVE =
            Paths.get(
                    "target",
                    "allure-results-archive"
            );

    /*
     * Tamamlanan test koşularının arşivleneceği klasör.
     */


    /*
     * Organizer'ın birden fazla kez initialize edilmesini engeller.
     */
    private static final AtomicBoolean INITIALIZED =
            new AtomicBoolean(false);

    /*
     * Koşuda herhangi bir hata oluşup oluşmadığını tutar.
     */
    private static final AtomicBoolean RUN_FAILED =
            new AtomicBoolean(false);

    /*
     * Aynı TestNG koşusundaki bütün raporlar
     * aynı tarih/saat bilgisini kullanır.
     */
    private static final String RUN_DATE_TIME =
            LocalDateTime.now()
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "yyyyMMdd_HHmmss"
                            )
                    );

    private AllureRunOrganizer() {
    }

    /**
     * Rapor düzenleme mekanizmasını yalnızca bir kez başlatır.
     *
     * JVM kapanırken mevcut koşuya ait raporların
     * düzenlenmesini sağlayan shutdown hook oluşturulur.
     */
    public static void initialize() {

        if (!INITIALIZED.compareAndSet(
                false,
                true
        )) {
            return;
        }

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                AllureRunOrganizer::organizeResults,
                                "allure-run-organizer"
                        )
                );
    }

    /**
     * Mevcut test koşusunda en az bir hata oluştuğunu işaretler.
     *
     * Bu bilgi ana koşu klasörünün durumunun
     * HATALI olarak belirlenmesinde kullanılır.
     */
    public static void markFailed() {

        RUN_FAILED.set(true);
    }

    /**
     * JVM kapanırken HTML raporlarını oluşturur
     * ve teknik Allure dosyalarını arşivler.
     */
    private static void organizeResults() {

        try {

            generateClassReports();

            archiveAllureTechnicalResults();

        } catch (IOException e) {

            System.err.println(
                    "Test sonuçları düzenlenirken hata oluştu: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Mevcut test koşusu için ana rapor klasörünü oluşturur.
     *
     * Ana klasör formatı:
     *
     * Kosu_20260925_184200_BASARILI
     * Kosu_20260925_184200_HATALI
     *
     * Ardından her test class'ı için bu klasör altında
     * ayrı rapor klasörü ve ozet.html dosyası oluşturulur.
     */
    private static void generateClassReports()
            throws IOException {

        Files.createDirectories(
                ALLURE_RUNS
        );

        List<String> classNames =
                HtmlReportManager
                        .getExecutedClassNames();

        if (classNames.isEmpty()) {

            System.out.println(
                    "Rapor oluşturulacak test class'ı bulunamadı."
            );

            return;
        }

        /*
         * Koşunun genel durumu belirlenir.
         *
         * RUN_FAILED true ise veya herhangi bir class
         * başarısız olmuşsa ana koşu HATALI kabul edilir.
         */
        boolean herhangiBirClassHatali =
                classNames.stream()
                        .anyMatch(
                                HtmlReportManager::classFailed
                        );

        String runStatus =
                RUN_FAILED.get()
                        || herhangiBirClassHatali
                        ? "HATALI"
                        : "BASARILI";

        String runFolderName =
                "Kosu_"
                        + RUN_DATE_TIME
                        + "_"
                        + runStatus;

        Path runDirectory =
                ALLURE_RUNS.resolve(
                        runFolderName
                );

        Files.createDirectories(
                runDirectory
        );

        /*
         * Her test class'ı ana koşu klasörü altında
         * kendi rapor klasörüne yazılır.
         */
        for (String className : classNames) {

            String classStatus =
                    HtmlReportManager
                            .classFailed(className)
                            ? "HATALI"
                            : "BASARILI";

            String classFolderName =
                    sanitizeFileName(className)
                            + "_"
                            + RUN_DATE_TIME
                            + "_"
                            + classStatus;

            Path classRunDirectory =
                    runDirectory.resolve(
                            classFolderName
                    );

            HtmlReportManager.generateReport(
                    classRunDirectory,
                    className
            );
        }

        System.out.println(
                "Test koşusu rapor klasörü oluşturuldu: "
                        + runDirectory.toAbsolutePath()
        );
    }

    /**
     * Allure tarafından oluşturulan ham JSON ve attachment
     * dosyalarını ayrı bir teknik arşiv klasörüne taşır.
     *
     * Böylece aynı teknik dosyaların her class raporuna
     * tekrar tekrar kopyalanması engellenir.
     */
    private static void archiveAllureTechnicalResults()
            throws IOException {

        if (!Files.exists(
                ALLURE_SOURCE
        )) {
            return;
        }

        try (Stream<Path> stream =
                     Files.list(
                             ALLURE_SOURCE
                     )) {

            List<Path> files =
                    stream
                            .filter(
                                    Files::isRegularFile
                            )
                            .toList();

            if (files.isEmpty()) {
                return;
            }

            String status =
                    RUN_FAILED.get()
                            ? "HATALI"
                            : "BASARILI";

            Path archiveDirectory =
                    ALLURE_RAW_ARCHIVE.resolve(
                            "AllureResults_"
                                    + RUN_DATE_TIME
                                    + "_"
                                    + status
                    );

            Files.createDirectories(
                    archiveDirectory
            );

            for (Path file : files) {

                Files.copy(
                        file,
                        archiveDirectory.resolve(
                                file.getFileName()
                        ),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        }
    }

    /**
     * allure-results klasörü içerisinde dosya kalmadıysa
     * boş kaynak klasörünü siler.
     */
    private static void deleteSourceDirectoryIfEmpty()
            throws IOException {

        if (!Files.exists(
                ALLURE_SOURCE
        )) {
            return;
        }

        try (Stream<Path> stream =
                     Files.list(
                             ALLURE_SOURCE
                     )) {

            if (stream.findAny()
                    .isEmpty()) {

                Files.deleteIfExists(
                        ALLURE_SOURCE
                );
            }
        }
    }

    /**
     * Dosya ve klasör adlarında kullanılması uygun olmayan
     * karakterleri "_" karakteriyle değiştirir.
     *
     * @param value düzenlenecek dosya veya klasör adı
     * @return dosya sisteminde güvenli hale getirilmiş değer
     */
    private static String sanitizeFileName(
            String value) {

        if (value == null
                || value.isBlank()) {

            return "TestClass";
        }

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }
}