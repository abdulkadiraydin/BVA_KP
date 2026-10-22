package com.bvakp.automation.reporting;

import com.bvakp.automation.core.config.ConfigManager;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * TestNG koşusunun Allure raporlarını ve test kanıtlarını arşivler.
 *
 * <p>Kaynak dosyaları silmez; önceki koşuların dosyalarını yeni arşive katmaz.
 * Kalıcı arşiv varsayılan olarak reports/allure-runs altındadır ve mvn clean ile
 * silinen target klasöründen ayrı tutulur. Allure 2 HTML raporu ve history,
 * arşivleme sonrası scripts/Generate-AllureReport.ps1 ile otomatik üretilir.
 *
 * <p>Ayarlar (-D veya config.properties):
 * allure.report.auto.generate (varsayılan true), allure.report.timeout.seconds (varsayılan 300).
 *
 * <p>Arşivleme JVM kapanışında çalıştığı için bu sınıf, logger kapanmış olabileceğinden
 * bilinçli olarak System.out/err kullanır.
 */
public final class AllureRunOrganizer {

    private static final Path ALLURE_SOURCE = Paths.get(
            System.getProperty("allure.results.directory", "target/allure-results")
    ).toAbsolutePath().normalize();

    private static final Path RUNS_DIRECTORY = Paths.get(
            System.getProperty("report.archive.directory", "reports/allure-runs")
    ).toAbsolutePath().normalize();

    private static final Path SCREENSHOTS = Paths.get("target", "screenshots").toAbsolutePath().normalize();
    private static final Path VIDEOS = Paths.get("target", "videos").toAbsolutePath().normalize();
    private static final Path TRACES = Paths.get("target", "traces").toAbsolutePath().normalize();

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final AtomicBoolean RUN_FAILED = new AtomicBoolean(false);
    private static final AtomicBoolean SHUTDOWN_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean ORGANIZED = new AtomicBoolean(false);

    private static final long VARSAYILAN_RAPOR_ZAMAN_ASIMI = 300L;
    private static final long MINIMUM_RAPOR_ZAMAN_ASIMI = 30L;

    private static final String RUN_ID = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))
            + "_" + UUID.randomUUID().toString().substring(0, 8);

    // Testler başlamadan alınan envanter; eski dosyaları silmeden
    // yeni koşunun çıktılarını ayırmayı sağlar.
    private static final Map<Path, Set<Path>> INITIAL_FILES = new HashMap<>();

    private AllureRunOrganizer() {
    }

    /** Testler başlamadan BaseTest/TestListener tarafından çağrılır; yalnızca ilk çağrıda çalışır. */
    public static void initialize() {
        if (!INITIALIZED.compareAndSet(false, true)) {
            return;
        }

        for (Path directory : List.of(ALLURE_SOURCE, SCREENSHOTS, VIDEOS, TRACES)) {
            try {
                INITIAL_FILES.put(directory, snapshot(directory));
            } catch (IOException e) {
                // Boş kabul etmek eski dosyaları yeni koşuya karıştıracağı için
                // bu dizin arşivlemeden çıkarılır.
                INITIAL_FILES.put(directory, null);
                System.err.println("Arşiv başlangıç envanteri alınamadı: "
                        + directory + " | " + e.getMessage());
            }
        }

        // Allure JSON ve videolar tamamlansın diye asıl işlem JVM kapanışında yapılır.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SHUTDOWN_STARTED.set(true);
            organizeResults();
        }, "allure-run-organizer"));
    }

    /** Bu koşuda test veya TestNG configuration hatası olduğunu belirtir. */
    public static void markFailed() {
        RUN_FAILED.set(true);
    }

    /**
     * Main branch'indeki TestListener.onFinish çağrısı için public tutulur.
     * Erken çağrı rapor oluşturmaz; arşivleme JVM kapanış hook'unda yapılır.
     */
    public static void organizeResults() {
        if (!SHUTDOWN_STARTED.get()) {
            System.out.println("Allure arşivleme JVM kapanışına ertelendi.");
            return;
        }
        if (!ORGANIZED.compareAndSet(false, true)) {
            return;
        }

        List<String> classNames = HtmlReportManager.getExecutedClassNames();
        boolean hasNewResults = hasNewFiles(ALLURE_SOURCE);
        if (classNames.isEmpty() && !hasNewResults) {
            System.out.println("Bu koşu için arşivlenecek test sonucu bulunamadı.");
            return;
        }

        boolean classFailed = classNames.stream().anyMatch(HtmlReportManager::classFailed);
        String status = (RUN_FAILED.get() || classFailed) ? "HATALI" : "BASARILI";
        Path runDirectory = RUNS_DIRECTORY.resolve("Kosu_" + RUN_ID + "_" + status);

        try {
            Files.createDirectories(runDirectory);
        } catch (IOException e) {
            System.err.println("Koşu arşivi oluşturulamadı: " + e.getMessage());
            return;
        }

        for (String className : classNames) {
            String classStatus = HtmlReportManager.classFailed(className) ? "HATALI" : "BASARILI";

            // Koşu kimliği üst klasörde olduğu için class klasöründe tekrarlanmaz;
            // böylece Windows'ta uzun yol sorunları önlenir.
            Path classDirectory = runDirectory.resolve(safeName(className) + "_" + classStatus);

            try {
                HtmlReportManager.generateReport(classDirectory, className);
            } catch (RuntimeException e) {
                System.err.println("HTML class raporu oluşturulamadı: "
                        + className + " | " + e.getMessage());
            }
        }

        // Allure dosyaları tek klasörde kalır (result.json -> ek bağları dosya adına dayanır);
        // medya kanıtları her testin kendi klasörüne toplanır: kanitlar/<Sınıf>/<test>/.
        // ozet.html kendi ekran görüntüsü kopyasını sınıf klasöründe tutar.
        copyNewFiles(ALLURE_SOURCE, runDirectory.resolve("allure-results"));
        Path kanitKlasoru = runDirectory.resolve("kanitlar");
        copyTestArtifacts(SCREENSHOTS, "ekran", kanitKlasoru);
        copyTestArtifacts(VIDEOS, "video", kanitKlasoru);
        copyTestArtifacts(TRACES, "trace", kanitKlasoru);

        try {
            Files.writeString(runDirectory.resolve("run-info.txt"),
                    "run.id=" + RUN_ID + System.lineSeparator()
                            + "run.status=" + status + System.lineSeparator()
                            + "run.classes=" + classNames.size() + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Koşu özeti yazılamadı: " + e.getMessage());
        }

        System.out.println("Test koşusu rapor klasörü oluşturuldu: " + runDirectory);
        generateAllureReport(runDirectory);
    }

    /**
     * Arşiv tamamlandıktan sonra Allure CLI ile HTML ve History raporunu oluşturur.
     * Rapor hatası ham kanıtları silmez veya TestNG sonucunu değiştirmez.
     */
    private static void generateAllureReport(Path runDirectory) {
        if (!otomatikRaporAcikMi()) {
            System.out.println("Otomatik Allure raporu kapalı (allure.report.auto.generate=false).");
            return;
        }

        Path resultsDirectory = runDirectory.resolve("allure-results");
        try {
            if (!Files.isDirectory(resultsDirectory)) {
                System.err.println("Allure raporu atlandı: arşivlenmiş allure-results bulunamadı.");
                return;
            }
            try (Stream<Path> results = Files.list(resultsDirectory)) {
                if (results.noneMatch(path -> Files.isRegularFile(path)
                        && path.getFileName().toString().endsWith("-result.json"))) {
                    System.err.println("Allure raporu atlandı: bu koşuda result.json bulunamadı.");
                    return;
                }
            }
        } catch (IOException e) {
            System.err.println("Allure sonuç kontrolü yapılamadı: " + e.getMessage());
            return;
        }

        if (!System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("windows")) {
            System.err.println("Otomatik Allure raporu bu sürümde Windows gerektirir. "
                    + "Script manuel çalıştırılabilir.");
            return;
        }

        Path projectRoot = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path script = projectRoot.resolve("scripts/Generate-AllureReport.ps1");
        if (!Files.isRegularFile(script)) {
            System.err.println("Otomatik Allure script'i bulunamadı: " + script);
            return;
        }

        Path reportLog = runDirectory.resolve("allure-generation.log");
        long timeoutSeconds = raporZamanAsimi();
        Process process = null;

        try {
            // Bypass yalnızca bu alt süreç için geçerlidir;
            // Windows'un kalıcı ExecutionPolicy ayarı değiştirilmez.
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe",
                    "-NoProfile",
                    "-NonInteractive",
                    "-ExecutionPolicy", "Bypass",
                    "-File", script.toString(),
                    "-RunDirectory", runDirectory.toString()
            );
            builder.directory(projectRoot.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(reportLog.toFile()));

            System.out.println("Allure HTML raporu otomatik oluşturuluyor: " + runDirectory);
            process = builder.start();

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroy();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
                System.err.println("Allure raporu zaman aşımına uğradı: " + reportLog);
                return;
            }

            Path indexHtml = runDirectory.resolve("allure-report/index.html");
            if (process.exitValue() == 0 && Files.isRegularFile(indexHtml)) {
                System.out.println("Allure HTML raporu hazır: " + indexHtml);
                Path rapor = runDirectory.resolve("RAPOR.html");
                if (Files.isRegularFile(rapor)) {
                    System.out.println("Rapor (çift tıkla): " + rapor);
                    System.out.println("Son koşu kısayolu: " + projectRoot.resolve("reports/SON-KOSU.html"));
                }
                System.out.println("Allure History güncellendi: " + projectRoot.resolve("reports/allure-history"));
            } else {
                System.err.println("Allure raporu oluşturulamadı (çıkış kodu "
                        + process.exitValue() + "). Ayrıntılar: " + reportLog);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            System.err.println("Allure rapor işlemi kesildi: " + reportLog);
        } catch (IOException | SecurityException e) {
            System.err.println("Allure rapor script'i başlatılamadı: " + e.getMessage());
            System.err.println("Manuel komut: .\\scripts\\Generate-AllureReport.ps1");
        }
    }

    private static boolean otomatikRaporAcikMi() {
        return Boolean.parseBoolean(ConfigManager.getOrDefault("allure.report.auto.generate", "true"));
    }

    /** Hatalı veya çok küçük değerde varsayılana/minimuma döner. */
    private static long raporZamanAsimi() {
        String value = ConfigManager.getOrDefault(
                "allure.report.timeout.seconds", String.valueOf(VARSAYILAN_RAPOR_ZAMAN_ASIMI));
        try {
            return Math.max(MINIMUM_RAPOR_ZAMAN_ASIMI, Long.parseLong(value));
        } catch (NumberFormatException e) {
            System.err.println("Geçersiz allure.report.timeout.seconds '" + value
                    + "'; " + VARSAYILAN_RAPOR_ZAMAN_ASIMI + " kullanılıyor.");
            return VARSAYILAN_RAPOR_ZAMAN_ASIMI;
        }
    }

    /**
     * Yeni kanıtları test klasörüne sabit adla kopyalar: kanitlar/<Sınıf>/<test>/ekran.png.
     * Aynı testten ikinci dosya gelirse (ör. DataProvider) ekran_2.png olarak eklenir.
     * Teste eşleşmeyen dosyalar (ör. @BeforeClass kanıtları) özgün adıyla
     * kanitlar/_eslesmeyen altına alınır; kaynak dosyalar yerinde kalır.
     */
    private static void copyTestArtifacts(Path source, String sabitAd, Path kanitKlasoru) {

        Set<Path> initial = INITIAL_FILES.get(source);
        if (initial == null || !Files.isDirectory(source)) {
            return;
        }

        try (Stream<Path> files = Files.walk(source)) {
            List<Path> yeniDosyalar = files.filter(Files::isRegularFile)
                    .filter(file -> !initial.contains(source.relativize(file)))
                    .sorted()
                    .toList();

            for (Path file : yeniDosyalar) {
                Path destination = kanitHedefi(file, sabitAd, kanitKlasoru);
                try {
                    Files.createDirectories(destination.getParent());
                    Files.copy(file, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Kanıt kopyalanamadı: " + file + " | " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Test bazlı kanıt arşivleme başarısız: " + source + " | " + e.getMessage());
        }
    }

    private static Path kanitHedefi(Path file, String sabitAd, Path kanitKlasoru) {

        AllureArtifactCollector.KanitSahibi sahip = AllureArtifactCollector.findArtifactOwner(file);

        if (sahip == null || sahip.testAdi() == null) {
            System.err.println("Kanıtın testi eşleştirilemedi: " + file);
            String sinif = sahip == null ? "_eslesmeyen" : safeName(sahip.sinifAdi());
            return kanitKlasoru.resolve(sinif).resolve("_eslesmeyen").resolve(file.getFileName());
        }

        Path testKlasoru = kanitKlasoru.resolve(safeName(sahip.sinifAdi())).resolve(safeName(sahip.testAdi()));
        String uzanti = uzantiAl(file.getFileName().toString());

        Path hedef = testKlasoru.resolve(sabitAd + uzanti);
        for (int sira = 2; Files.exists(hedef); sira++) {
            hedef = testKlasoru.resolve(sabitAd + "_" + sira + uzanti);
        }
        return hedef;
    }

    private static String uzantiAl(String dosyaAdi) {
        int nokta = dosyaAdi.lastIndexOf('.');
        return nokta < 0 ? "" : dosyaAdi.substring(nokta);
    }

    /** Envanterde olmayan dosyaları klasör yapısını koruyarak kopyalar. */
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
                Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Koşu kanıtları arşivlenemedi: " + source + " | " + e.getMessage());
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
            System.err.println("Allure dosyaları kontrol edilemedi: " + e.getMessage());
            return false;
        }
    }

    private static Set<Path> snapshot(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return Set.of();
        }
        try (Stream<Path> files = Files.walk(directory)) {
            Set<Path> names = new HashSet<>();
            files.filter(Files::isRegularFile).forEach(file -> names.add(directory.relativize(file)));
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