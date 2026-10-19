package com.bvakp.automation.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * Bir JVM/TestNG kosusunun HTML raporlarini ve Allure kanitlarini ayri arsivler.
 * Kaynak dosyalari silmez; onceki kosularin dosyalarini yeni arsive katmaz.
 * Kalici arsiv varsayilan olarak reports/allure-runs altindadir: mvn clean
 * ile silinen target klasorunden ayri tutulur.
 * Allure 2 HTML raporu ve history, arsivleme sonrasi otomatik olarak
 * scripts/Generate-AllureReport.ps1 kullanilarak uretilir.
 */
public final class AllureRunOrganizer {

    private static final Path ALLURE_SOURCE = Paths.get(
            System.getProperty("allure.results.directory", "target/allure-results")
    ).toAbsolutePath().normalize();

    private static final Path RUNS_DIRECTORY = Paths.get(
            System.getProperty("report.archive.directory", "reports/allure-runs")
    ).toAbsolutePath().normalize();

    private static final Path SCREENSHOTS = Paths.get("target", "screenshots")
            .toAbsolutePath().normalize();
    private static final Path VIDEOS = Paths.get("target", "videos")
            .toAbsolutePath().normalize();
    private static final Path TRACES = Paths.get("target", "traces")
            .toAbsolutePath().normalize();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final AtomicBoolean RUN_FAILED = new AtomicBoolean(false);

    // JVM property: -Dreport.auto.generate=false ile otomatik rapor kapatilabilir.
    private static final boolean AUTO_GENERATE_REPORT = Boolean.parseBoolean(
            System.getProperty("report.auto.generate", "true")
    );

    private static final String RUN_ID = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
            + "_" + UUID.randomUUID().toString().substring(0, 8);

    // Snapshot, testler baslamadan once alinir. Boylesi eski Allure dosyalarini
    // silmeden yeni kosunun teknik ciktilarini ayirmamizi saglar.
    private static final Map<Path, Set<Path>> INITIAL_FILES = new HashMap<>();

    private AllureRunOrganizer() {
    }

    /** Testler baslamadan, mevcut BaseTest/TestListener tarafindan cagirilir. */
    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        for (Path directory : List.of(ALLURE_SOURCE, SCREENSHOTS, VIDEOS, TRACES)) {
            try {
                INITIAL_FILES.put(directory, snapshot(directory));
            } catch (IOException e) {
                // Hata alinan dizini bos kabul etmek eski dosyalari yenisine
                // karistiracagi icin arsivlemeyi bu dizin icin devre disi birakir.
                INITIAL_FILES.put(directory, null);
                System.err.println("Arsiv baslangic envanteri alinamadi: "
                        + directory + " | " + e.getMessage());
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(
                AllureRunOrganizer::organizeResults,
                "allure-run-organizer"
        ));
    }

    /** Bu kosuda test veya TestNG configuration hatasi oldugunu belirtir. */
    public static void markFailed() {
        RUN_FAILED.set(true);
    }

    /** Testler ve AllureArtifactCollector islemleri tamamlandiktan sonra calisir. */
    private static void organizeResults() {
        List<String> classNames = HtmlReportManager.getExecutedClassNames();
        boolean hasNewResults = hasNewFiles(ALLURE_SOURCE);
        if (classNames.isEmpty() && !hasNewResults) {
            System.out.println("Bu kosu icin arsivlenecek test sonucu bulunamadi.");
            return;
        }

        boolean classFailed = classNames.stream().anyMatch(HtmlReportManager::classFailed);
        String status = (RUN_FAILED.get() || classFailed) ? "HATALI" : "BASARILI";
        Path runDirectory = RUNS_DIRECTORY.resolve("Kosu_" + RUN_ID + "_" + status);

        try {
            Files.createDirectories(runDirectory);
        } catch (IOException e) {
            System.err.println("Kosu arsivi olusturulamadi: " + e.getMessage());
            return;
        }

        // Class klasorlerinin tam yolunu saklayarak kanitlari dogru class'a
        // ayiririz; ayni isimli test metotlari birbirine karismaz.
        Map<String, Path> classDirectories = new LinkedHashMap<>();
        // Bir klasordeki kopyalama hatasi diger kanitlari kaybettirmemeli.
        for (String className : classNames) {
            String classStatus = HtmlReportManager.classFailed(className)
                    ? "HATALI" : "BASARILI";
            Path classDirectory = runDirectory.resolve(
                    safeName(className) + "_" + RUN_ID + "_" + classStatus
            );
            classDirectories.put(className, classDirectory);
            try {
                HtmlReportManager.generateReport(classDirectory, className);
            } catch (RuntimeException e) {
                System.err.println("HTML class raporu olusturulamadi: "
                        + className + " | " + e.getMessage());
            }
        }

        // Allure'un ham result.json ve attachment dosyalari tek yerde kalir.
        // Medya kanitlari ise TestNG sinifina gore arsivlenir.
        copyNewFiles(ALLURE_SOURCE, runDirectory.resolve("allure-results"));
        copyClassArtifacts(SCREENSHOTS, "screenshots", runDirectory, classDirectories);
        copyClassArtifacts(VIDEOS, "videos", runDirectory, classDirectories);
        copyClassArtifacts(TRACES, "traces", runDirectory, classDirectories);

        try {
            Files.writeString(runDirectory.resolve("run-info.txt"),
                    "run.id=" + RUN_ID + System.lineSeparator()
                            + "run.status=" + status + System.lineSeparator()
                            + "run.classes=" + classNames.size() + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Kosu ozeti yazilamadi: " + e.getMessage());
        }

        System.out.println("Test kosusu rapor klasoru olusturuldu: " + runDirectory);
        generateAllureReport(runDirectory);
    }

    /**
     * Koşu kanitlari arşivlendikten sonra Allure CLI raporunu oluşturur.
     * Raporlama hatası TestNG sonucunu değiştirmez ve kanitlari silmez.
     * Ayrı bir shutdown hook kullanılmaz; arşivleme ve raporlama sıralıdır.
     */
    private static void generateAllureReport(Path runDirectory) {
        if (!AUTO_GENERATE_REPORT) {
            System.out.println("Otomatik Allure raporu kapali (report.auto.generate=false).");
            return;
        }

        Path resultsDirectory = runDirectory.resolve("allure-results");
        try {
            if (!Files.isDirectory(resultsDirectory)) {
                System.err.println("Allure raporu atlandi: arsivlenmis allure-results bulunamadi.");
                return;
            }
            try (Stream<Path> results = Files.list(resultsDirectory)) {
                if (results.noneMatch(path -> Files.isRegularFile(path)
                        && path.getFileName().toString().endsWith("-result.json"))) {
                    System.err.println("Allure raporu atlandi: bu kosuda result.json bulunamadi.");
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Allure sonuc kontrolu yapilamadi: " + e.getMessage());
            return;
        }

        // Kullanici Windows/IntelliJ PowerShell uzerinden calisiyor.
        if (!System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT)
                .contains("windows")) {
            System.err.println("Otomatik Allure raporu bu surumde Windows gerektirir. "
                    + "Script manuel calistirilabilir.");
            return;
        }

        Path projectRoot = Paths.get(System.getProperty("user.dir", "."))
                .toAbsolutePath().normalize();
        Path script = projectRoot.resolve("scripts/Generate-AllureReport.ps1");
        if (!Files.isRegularFile(script)) {
            System.err.println("Otomatik Allure script'i bulunamadi: " + script);
            return;
        }

        Path reportLog = runDirectory.resolve("allure-generation.log");
        long timeoutSeconds = Math.max(30L,
                Long.getLong("report.auto.timeout.seconds", 300L));
        Process process = null;
        try {
            // -File, var olan kurumsal PowerShell calistirma politikalarina uyar.
            // Yalnızca bu rapor oluşturma işlemi için PowerShell politikası belirlenir.
// Sistemin genel ExecutionPolicy ayarları değiştirilmez.
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    script.toString(),
                    "-RunDirectory",
                    runDirectory.toString()
            );
            builder.directory(projectRoot.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(reportLog.toFile()));
            System.out.println("Allure HTML raporu otomatik olusturuluyor: " + runDirectory);
            process = builder.start();

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroy();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
                System.err.println("Allure raporu zaman asimina ugradi: " + reportLog);
                return;
            }

            if (process.exitValue() == 0
                    && Files.isRegularFile(runDirectory.resolve("allure-report/index.html"))) {
                System.out.println("Allure HTML raporu hazir: "
                        + runDirectory.resolve("allure-report/index.html"));
                System.out.println("Allure History guncellendi: "
                        + projectRoot.resolve("reports/allure-history"));
            } else {
                System.err.println("Allure raporu olusturulamadi (cikis kodu "
                        + process.exitValue() + "). Ayrintilar: " + reportLog);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            System.err.println("Allure rapor islemi kesildi: " + reportLog);
        } catch (IOException | SecurityException e) {
            System.err.println("Allure rapor script'i baslatilamadi: " + e.getMessage());
            System.err.println("Manuel komut: .\\scripts\\Generate-AllureReport.ps1");
        }
    }

    /**
     * Kaynak dosyalari yerinde birakir; her yeni kaniti ait oldugu class
     * klasorune kopyalar. Allure'in kullandigi attachment dosyalarina dokunmaz.
     *
     * Sinifi bilinmeyen dosyalar yanlis bir test sinifina yazilmaz; ayri bir
     * Sinifi_Belirlenemeyen klasorune alinarak veri kaybi onlenir.
     */
    private static void copyClassArtifacts(
            Path source,
            String mediaFolder,
            Path runDirectory,
            Map<String, Path> classDirectories) {

        Set<Path> initial = INITIAL_FILES.get(source);
        if (initial == null || !Files.isDirectory(source)) {
            return;
        }

        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path relative = source.relativize(file);
                if (initial.contains(relative)) {
                    continue;
                }

                String className = AllureArtifactCollector.findArtifactClass(file);
                Path classDirectory = classDirectories.get(className);
                if (classDirectory == null) {
                    classDirectory = runDirectory.resolve("Sinifi_Belirlenemeyen");
                    System.err.println("Kanitin test sinifi eslestirilemedi: " + file);
                }

                Path destination = classDirectory.resolve(mediaFolder).resolve(relative);
                try {
                    Files.createDirectories(destination.getParent());
                    Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Kanit kopyalanamadi: " + file
                            + " | " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Class bazli kanit arsivleme basarisiz: "
                    + source + " | " + e.getMessage());
        }
    }

    private static void copyNewFiles(Path source, Path destination) {
        Set<Path> initial = INITIAL_FILES.get(source);
        if (initial == null || !Files.isDirectory(source)) {
            return;
        }

        try (Stream<Path> files = Files.walk(source)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                Path relative = source.relativize(file);
                if (initial.contains(relative)) {
                    continue;
                }
                Path target = destination.resolve(relative);
                Files.createDirectories(target.getParent());
                // Kaynaklar korunur; Allure'in result.json -> attachment baglari
                // ayni klasor altindaki dosya adlari korundugu icin bozulmaz.
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Kosu kanitlari arsivlenemedi: "
                    + source + " | " + e.getMessage());
        }
    }

    private static boolean hasNewFiles(Path source) {
        Set<Path> initial = INITIAL_FILES.get(source);
        if (initial == null || !Files.isDirectory(source)) {
            return false;
        }
        try (Stream<Path> files = Files.walk(source)) {
            return files.filter(Files::isRegularFile)
                    .map(source::relativize)
                    .anyMatch(relative -> !initial.contains(relative));
        } catch (IOException e) {
            System.err.println("Allure dosyalari kontrol edilemedi: " + e.getMessage());
            return false;
        }
    }

    private static Set<Path> snapshot(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return Set.of();
        }
        try (Stream<Path> files = Files.walk(directory)) {
            Set<Path> names = new HashSet<>();
            files.filter(Files::isRegularFile)
                    .forEach(file -> names.add(directory.relativize(file)));
            return Set.copyOf(names);
        }
    }

    private static String safeName(String value) {
        if (value == null || value.isBlank()) {
            return "TestClass";
        }
        return value.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
