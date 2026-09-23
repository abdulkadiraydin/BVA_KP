package com.bvakp.automation.reporting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public final class AllureRunOrganizer {

    private static final Path ALLURE_RESULTS =
            Paths.get("target", "allure-results");

    private static final AtomicBoolean INITIALIZED =
            new AtomicBoolean(false);

    private static final AtomicBoolean RUN_FAILED =
            new AtomicBoolean(false);

    private static final Instant RUN_START =
            ProcessHandle.current()
                    .info()
                    .startInstant()
                    .orElse(Instant.now());

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

        String dateTime =
                LocalDateTime.ofInstant(
                        RUN_START,
                        ZoneId.systemDefault()
                ).format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd_HH-mm-ss"
                        )
                );

        String status =
                RUN_FAILED.get()
                        ? "HATALI"
                        : "BASARILI";

        return "kosu_" + dateTime + "_" + status;
    }

    private static void organizeResults() {

        if (!Files.exists(ALLURE_RESULTS)) {
            return;
        }

        Path runDirectory =
                ALLURE_RESULTS.resolve(
                        createRunFolderName()
                );

        try {

            Files.createDirectories(runDirectory);

            try (Stream<Path> files =
                         Files.list(ALLURE_RESULTS)) {

                files
                        .filter(Files::isRegularFile)
                        .filter(AllureRunOrganizer::belongsToCurrentRun)
                        .forEach(file ->
                                moveFile(file, runDirectory)
                        );
            }

        } catch (IOException e) {

            System.err.println(
                    "Allure sonuçları klasörlenirken hata oluştu: "
                            + e.getMessage()
            );
        }
    }

    private static boolean belongsToCurrentRun(Path file) {

        try {

            Instant modifiedTime =
                    Files.getLastModifiedTime(file)
                            .toInstant();

            return !modifiedTime.isBefore(RUN_START);

        } catch (IOException e) {
            return false;
        }
    }

    private static void moveFile(
            Path file,
            Path runDirectory) {

        try {

            Files.move(
                    file,
                    runDirectory.resolve(
                            file.getFileName()
                    ),
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (IOException e) {

            System.err.println(
                    "Dosya taşınamadı: "
                            + file
                            + " | "
                            + e.getMessage()
            );
        }
    }
}