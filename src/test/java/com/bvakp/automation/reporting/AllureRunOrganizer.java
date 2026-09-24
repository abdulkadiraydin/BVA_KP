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

public final class AllureRunOrganizer {

    /*
     * Allure'un test çalışırken ham json ve attachment
     * dosyalarını oluşturduğu geçici klasör.
     */
    private static final Path ALLURE_SOURCE =
            Paths.get("allure-results");

    /*
     * Tamamlanan test koşularının arşivleneceği klasör.
     */
    private static final Path ALLURE_RUNS =
            Paths.get("target", "allure-runs");

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
     * Aynı koşudaki bütün testlerin aynı klasörde toplanması için
     * tarih/saat bir kere oluşturulur.
     */
    private static final String RUN_DATE_TIME =
            LocalDateTime.now()
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "yyyy-MM-dd_HH-mm-ss"
                            )
                    );

    private AllureRunOrganizer() {
    }


    public static void initialize() {

        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        AllureRunOrganizer::organizeResults,
                        "allure-run-organizer"
                )
        );
    }


    public static void markFailed() {

        RUN_FAILED.set(true);
    }


    private static String createRunFolderName() {

        String status =
                RUN_FAILED.get()
                        ? "HATALI"
                        : "BASARILI";

        return "kosu_"
                + RUN_DATE_TIME
                + "_"
                + status;
    }


    private static void organizeResults() {

        if (!Files.exists(ALLURE_SOURCE)) {
            return;
        }

        try (Stream<Path> stream =
                     Files.list(ALLURE_SOURCE)) {

            List<Path> files =
                    stream
                            .filter(Files::isRegularFile)
                            .toList();

            /*
             * Allure sonucu oluşmamışsa boş koşu klasörü oluşturma.
             */
            if (files.isEmpty()) {
                return;
            }

            /*
             * Örnek:
             *
             * target/allure-runs/
             * kosu_2026-09-24_11-30-15_BASARILI/
             */
            Path runDirectory =
                    ALLURE_RUNS.resolve(
                            createRunFolderName()
                    );

            /*
             * Allure teknik dosyalarının tutulacağı klasör.
             */
            Path resultsDirectory =
                    runDirectory.resolve("results");

            Files.createDirectories(
                    resultsDirectory
            );

            /*
             * JSON, PNG ve diğer attachment dosyalarını
             * results klasörüne taşı.
             */
            for (Path file : files) {

                Files.move(
                        file,
                        resultsDirectory.resolve(
                                file.getFileName()
                        ),
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            /*
             * İnsan tarafından okunabilir HTML özet raporunu
             * koşu klasörünün içine oluştur.
             */
            HtmlReportManager.generateReport(
                    runDirectory
            );

            /*
             * Geçici allure-results klasörü boş kaldıysa sil.
             */
            deleteSourceDirectoryIfEmpty();

        } catch (IOException e) {

            System.err.println(
                    "Allure sonuçları düzenlenirken hata oluştu: "
                            + e.getMessage()
            );
        }
    }


    private static void deleteSourceDirectoryIfEmpty()
            throws IOException {

        if (!Files.exists(ALLURE_SOURCE)) {
            return;
        }

        try (Stream<Path> stream =
                     Files.list(ALLURE_SOURCE)) {

            if (stream.findAny().isEmpty()) {

                Files.deleteIfExists(
                        ALLURE_SOURCE
                );
            }
        }
    }
}