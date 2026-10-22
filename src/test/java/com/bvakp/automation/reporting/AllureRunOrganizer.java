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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Test koşusunun çıktılarını reports/kosular altında tek bir koşu klasöründe toplar.
 *
 * <pre>
 * reports/
 *   SON-KOSU.html                    en son koşunun raporu (script yazar)
 *   allure-history/                  Allure Trend/History verisi (script yazar)
 *   kosular/
 *     2026-10-22_19-41-05_HATALI/
 *       RAPOR.html                   tek dosyalık Allure raporu (script yazar)
 *       ozet.html                    sade özet, mailde giden
 *       kanitlar/&lt;Sınıf&gt;/&lt;test&gt;/   ekran.png, video.webm, trace.zip
 *       kanitlar/_hazirlik/          teste bağlanamayan kanıtlar (ör. @BeforeClass)
 *       teknik/                      allure-results, allure-report, kosu-bilgisi.txt, allure-uretim.log
 * </pre>
 *
 * <p>target altındaki kaynak dosyalar silinmez; yalnızca bu koşuda oluşanlar kopyalanır.
 * En yeni kosu.saklama.adedi kadar koşu saklanır, eskiler silinir (0 = hiç silinmez).
 *
 * <p>Ayarlar (-D veya config.properties): allure.report.auto.generate (true),
 * allure.report.timeout.seconds (300), kosu.saklama.adedi (20).
 *
 * <p>JVM kapanışında çalıştığı için logger yerine bilinçli olarak System.out/err kullanır.
 */
public final class AllureRunOrganizer {

    private static final Path ALLURE_SOURCE = Paths.get(
            System.getProperty("allure.results.directory", "target/allure-results")
    ).toAbsolutePath().normalize();

    private static final Path RUNS_DIRECTORY = Paths.get(
            System.getProperty("report.archive.directory", "reports/kosular")
    ).toAbsolutePath().normalize();

    private static final Path SCREENSHOTS = Paths.get("target", "screenshots").toAbsolutePath().normalize();
    private static final Path VIDEOS = Paths.get("target", "videos").toAbsolutePath().normalize();
    private static final Path TRACES = Paths.get("target", "traces").toAbsolutePath().normalize();

    /** Koşu klasörü adı: 2026-10-22_19-41-05_HATALI (aynı saniyede ikinci koşu: ..._2_HATALI). */
    private static final DateTimeFormatter KOSU_ADI_BICIMI = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Pattern KOSU_KLASOR_DESENI =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}(_\\d+)?_(BASARILI|HATALI)$");

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);
    private static final AtomicBoolean RUN_FAILED = new AtomicBoolean(false);
    private static final AtomicBoolean SHUTDOWN_STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean ORGANIZED = new AtomicBoolean(false);

    private static final long VARSAYILAN_RAPOR_ZAMAN_ASIMI = 300L;
    private static final long MINIMUM_RAPOR_ZAMAN_ASIMI = 30L;
    private static final int VARSAYILAN_SAKLAMA_ADEDI = 20;

    private static final LocalDateTime KOSU_BASLANGICI = LocalDateTime.now();

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
     * TestListener.onFinish çağrısı için public tutulur.
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

        Path runDirectory;
        try {
            runDirectory = kosuKlasoruOlustur(status);
        } catch (IOException e) {
            System.err.println("Koşu arşivi oluşturulamadı: " + e.getMessage());
            return;
        }
        Path teknik = runDirectory.resolve("teknik");

        // Allure dosyaları tek klasörde kalır (result.json -> ek bağları dosya adına dayanır).
        copyNewFiles(ALLURE_SOURCE, teknik.resolve("allure-results"));

        // Kanıtlar test klasörlerine; ozet.html ekran görüntülerini buradan gösterir.
        Path kanitKlasoru = runDirectory.resolve("kanitlar");
        Map<Path, Path> ekranlar = copyTestArtifacts(SCREENSHOTS, "ekran", kanitKlasoru);
        copyTestArtifacts(VIDEOS, "video", kanitKlasoru);
        copyTestArtifacts(TRACES, "trace", kanitKlasoru);

        try {
            HtmlReportManager.generateSummary(runDirectory, KOSU_BASLANGICI, ekranlar);
        } catch (RuntimeException e) {
            System.err.println("ozet.html oluşturulamadı: " + e.getMessage());
        }

        try {
            Files.createDirectories(teknik);
            Files.writeString(teknik.resolve("kosu-bilgisi.txt"),
                    "kosu.adi=" + runDirectory.getFileName() + System.lineSeparator()
                            + "kosu.durumu=" + status + System.lineSeparator()
                            + "kosu.sinif.sayisi=" + classNames.size() + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Koşu bilgisi yazılamadı: " + e.getMessage());
        }

        System.out.println("Koşu klasörü: " + runDirectory);
        System.out.println("Özet (çift tıkla): " + runDirectory.resolve("ozet.html"));

        generateAllureReport(runDirectory);
        eskiKosulariSilme(runDirectory);
    }

    /** reports/kosular/2026-10-22_19-41-05_HATALI; aynı ad varsa _2, _3 eklenir. */
    private static Path kosuKlasoruOlustur(String status) throws IOException {
        String tarih = KOSU_BASLANGICI.format(KOSU_ADI_BICIMI);
        Path klasor = RUNS_DIRECTORY.resolve(tarih + "_" + status);
        for (int sira = 2; Files.exists(klasor); sira++) {
            klasor = RUNS_DIRECTORY.resolve(tarih + "_" + sira + "_" + status);
        }
        Files.createDirectories(klasor);
        return klasor;
    }

    /**
     * Arşiv tamamlandıktan sonra Allure CLI ile RAPOR.html, Allure raporu ve history'yi üretir.
     * Rapor hatası kanıtları silmez veya TestNG sonucunu değiştirmez.
     */
    private static void generateAllureReport(Path runDirectory) {
        if (!otomatikRaporAcikMi()) {
            System.out.println("Otomatik Allure raporu kapalı (allure.report.auto.generate=false).");
            return;
        }

        Path teknik = runDirectory.resolve("teknik");
        Path resultsDirectory = teknik.resolve("allure-results");
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

        Path reportLog = teknik.resolve("allure-uretim.log");
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

            System.out.println("Allure raporu oluşturuluyor...");
            process = builder.start();

            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroy();
                if (!process.waitFor(5, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
                System.err.println("Allure raporu zaman aşımına uğradı: " + reportLog);
                return;
            }

            Path rapor = runDirectory.resolve("RAPOR.html");
            if (process.exitValue() == 0 && Files.isRegularFile(rapor)) {
                System.out.println("Rapor (çift tıkla): " + rapor);
                System.out.println("Son koşu kısayolu: " + projectRoot.resolve("reports/SON-KOSU.html"));
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

    /**
     * En yeni kosu.saklama.adedi kadar koşu klasörünü bırakır, eskileri siler.
     * Yalnızca koşu adı biçimine uyan klasörlere dokunur; 0 değeri silmeyi kapatır.
     */
    private static void eskiKosulariSilme(Path buKosu) {

        int saklanacak = saklamaAdedi();
        if (saklanacak <= 0) {
            return;
        }

        List<Path> kosular;
        try (Stream<Path> klasorler = Files.list(RUNS_DIRECTORY)) {
            // Ad tarihle başladığı için alfabetik sıra kronolojik sıradır.
            kosular = klasorler.filter(Files::isDirectory)
                    .filter(klasor -> KOSU_KLASOR_DESENI.matcher(klasor.getFileName().toString()).matches())
                    .sorted(Comparator.comparing((Path klasor) -> klasor.getFileName().toString()).reversed())
                    .toList();
        } catch (IOException e) {
            System.err.println("Eski koşular listelenemedi: " + e.getMessage());
            return;
        }

        int silinen = 0;
        for (Path kosu : kosular.subList(Math.min(saklanacak, kosular.size()), kosular.size())) {
            if (kosu.equals(buKosu)) {
                continue;
            }
            try (Stream<Path> icerik = Files.walk(kosu)) {
                for (Path yol : icerik.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(yol);
                }
                silinen++;
            } catch (IOException e) {
                System.err.println("Eski koşu silinemedi: " + kosu + " | " + e.getMessage());
            }
        }

        if (silinen > 0) {
            System.out.println("Eski koşular silindi: " + silinen + " (saklanan: " + saklanacak + ")");
        }
    }

    private static boolean otomatikRaporAcikMi() {
        return Boolean.parseBoolean(ConfigManager.getOrDefault("allure.report.auto.generate", "true"));
    }

    private static int saklamaAdedi() {
        String value = ConfigManager.getOrDefault("kosu.saklama.adedi", String.valueOf(VARSAYILAN_SAKLAMA_ADEDI));
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            System.err.println("Geçersiz kosu.saklama.adedi '" + value
                    + "'; " + VARSAYILAN_SAKLAMA_ADEDI + " kullanılıyor.");
            return VARSAYILAN_SAKLAMA_ADEDI;
        }
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
     * Yeni kanıtları test klasörüne sabit adla kopyalar: kanitlar/&lt;Sınıf&gt;/&lt;test&gt;/ekran.png.
     * Aynı testten ikinci dosya gelirse (ör. DataProvider) ekran_2.png olarak eklenir.
     * Teste bağlanamayan dosyalar özgün adıyla kanitlar/_hazirlik altına alınır.
     *
     * @return kaynak dosya (mutlak yol) → arşivdeki kopyası
     */
    private static Map<Path, Path> copyTestArtifacts(Path source, String sabitAd, Path kanitKlasoru) {

        Map<Path, Path> kopyalar = new HashMap<>();
        Set<Path> initial = INITIAL_FILES.get(source);
        if (initial == null || !Files.isDirectory(source)) {
            return kopyalar;
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
                    kopyalar.put(file.toAbsolutePath().normalize(), destination);
                } catch (IOException e) {
                    System.err.println("Kanıt kopyalanamadı: " + file + " | " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Test bazlı kanıt arşivleme başarısız: " + source + " | " + e.getMessage());
        }
        return kopyalar;
    }

    private static Path kanitHedefi(Path file, String sabitAd, Path kanitKlasoru) {

        AllureArtifactCollector.KanitSahibi sahip = AllureArtifactCollector.findArtifactOwner(file);

        if (sahip == null || sahip.testAdi() == null) {
            return kanitKlasoru.resolve("_hazirlik").resolve(file.getFileName());
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