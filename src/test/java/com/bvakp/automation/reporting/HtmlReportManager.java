package com.bvakp.automation.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Allure'dan bağımsız, tek dosyalık HTML test raporunu (ozet.html) üretir.
 *
 * <p>Koşu boyunca her testin adımları, sonucu, hata mesajı ve ekran görüntüsü
 * bellekte toplanır; koşu sonunda AllureRunOrganizer bütün sınıfları içeren
 * tek bir ozet.html oluşturur. Paralel koşuda her thread kendi aktif testine yazar.
 */
public final class HtmlReportManager {

    private static final String BASARILI = "BASARILI";
    private static final String HATALI = "HATALI";
    private static final String ATLANDI = "ATLANDI";

    private static final Map<String, TestResultData> TEST_RESULTS = new ConcurrentHashMap<>();
    private static final List<String> TEST_ORDER = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<String> CURRENT_TEST = new ThreadLocal<>();

    private static final DateTimeFormatter EKRAN_TARIH_BICIMI =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private HtmlReportManager() {
    }

    // ------------------------------------------------------------------
    // Test kaydı
    // ------------------------------------------------------------------

    /** Test için yeni bir kayıt açar ve bu thread'in aktif testi yapar. */
    public static void startTest(String className, String testName) {

        // UUID, aynı testin bir koşuda birden fazla çalışmasında kayıtların çakışmasını önler.
        String testId = className + "_" + testName + "_" + UUID.randomUUID();

        TEST_RESULTS.put(testId, new TestResultData(className, testName, LocalDateTime.now()));
        TEST_ORDER.add(testId);
        CURRENT_TEST.set(testId);
    }

    public static void addStep(String step) {
        TestResultData test = getCurrentTest();
        if (test != null) {
            test.steps.add(step);
        }
    }

    public static void setScreenshot(Path screenshotPath) {
        TestResultData test = getCurrentTest();
        if (test != null) {
            test.screenshotPath = screenshotPath;
        }
    }

    public static void markPassed(long duration) {
        TestResultData test = getCurrentTest();
        if (test == null) {
            return;
        }
        test.status = BASARILI;
        test.duration = duration;
        CURRENT_TEST.remove();
    }

    public static void markFailed(long duration, Throwable throwable) {
        TestResultData test = getCurrentTest();
        if (test == null) {
            return;
        }
        test.status = HATALI;
        test.duration = duration;
        test.errorMessage = mesajAl(throwable);
        CURRENT_TEST.remove();
    }

    /**
     * Testi atlandı olarak kapatır.
     *
     * <p>Hazırlık metodu başarısız olduğunda TestNG testi onTestStart çağırmadan
     * atlayabilir; bu durumda kayıt burada açılır ki test raporda kaybolmasın.
     */
    public static void markSkipped(
            String className,
            String testName,
            long duration,
            Throwable sebep) {

        TestResultData test = getCurrentTest();
        if (test == null) {
            startTest(className, testName);
            test = getCurrentTest();
        }

        test.status = ATLANDI;
        test.duration = duration;
        test.errorMessage = mesajAl(sebep);
        CURRENT_TEST.remove();
    }

    // ------------------------------------------------------------------
    // Sorgulama (AllureRunOrganizer kullanır)
    // ------------------------------------------------------------------

    /** Bu koşuda çalışan class adlarını ilk çalıştırılma sırasına göre döndürür. */
    public static List<String> getExecutedClassNames() {

        Set<String> classNames = new LinkedHashSet<>();

        for (TestResultData test : getAllTestsInOrder()) {
            classNames.add(test.className);
        }

        return new ArrayList<>(classNames);
    }

    /**
     * Class'ta hatalı veya atlanan test varsa true döndürür.
     * Atlanan test, hazırlık hatasının işareti olabileceği için hatalı sayılır.
     */
    public static boolean classFailed(String className) {
        return getTestsForClass(className).stream().anyMatch(HtmlReportManager::sorunluMu);
    }

    // ------------------------------------------------------------------
    // Rapor üretimi
    // ------------------------------------------------------------------

    /**
     * Koşudaki bütün sınıfları içeren tek ozet.html dosyasını koşu klasörüne yazar.
     *
     * @param kosuKlasoru    reports/kosular/&lt;koşu&gt;; ozet.html buraya yazılır
     * @param kosuBaslangici başlıkta gösterilen koşu zamanı
     * @param ekranlar       target'taki ekran görüntüsü → kanitlar altındaki kopyası;
     *                       ozet.html görüntüleri bu kopyalardan gösterir
     */
    public static void generateSummary(Path kosuKlasoru, LocalDateTime kosuBaslangici, Map<Path, Path> ekranlar) {

        try {
            String html = ozetHtmlOlustur(kosuKlasoru, kosuBaslangici, ekranlar);
            Files.createDirectories(kosuKlasoru);
            Files.writeString(kosuKlasoru.resolve("ozet.html"), html, StandardCharsets.UTF_8);

        } catch (IOException e) {
            ReportManager.error("ozet.html oluşturulurken hata oluştu.", e);
        }
    }

    private static String ozetHtmlOlustur(Path kosuKlasoru, LocalDateTime kosuBaslangici, Map<Path, Path> ekranlar) {

        List<TestResultData> tests = getAllTestsInOrder();

        long passed = tests.stream().filter(test -> BASARILI.equals(test.status)).count();
        long failed = tests.stream().filter(test -> HATALI.equals(test.status)).count();
        long skipped = tests.stream().filter(test -> ATLANDI.equals(test.status)).count();

        String overallStatus = tests.stream().anyMatch(HtmlReportManager::sorunluMu) ? HATALI : BASARILI;

        StringBuilder siniflar = new StringBuilder();
        for (String className : getExecutedClassNames()) {
            siniflar.append(sinifBolumuOlustur(className, kosuKlasoru, ekranlar));
        }

        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                    <meta charset="UTF-8">
                    <title>Otomasyon Test Özeti</title>
                    <style>
                        body { font-family: Arial, sans-serif; background: #f5f6f8; margin: 0; padding: 30px; color: #252525; }
                        .container { max-width: 1100px; margin: auto; }
                        .header { background: white; padding: 25px; border-radius: 10px; margin-bottom: 20px; }
                        .summary { display: flex; gap: 15px; margin-top: 20px; flex-wrap: wrap; }
                        .summary-card { background: #f1f2f4; padding: 15px 25px; border-radius: 8px; min-width: 120px; }
                        details.sinif { background: white; border-radius: 10px; margin-bottom: 16px; padding: 0 25px; }
                        details.sinif > summary { cursor: pointer; padding: 18px 0; font-size: 18px; font-weight: bold;
                                                  display: flex; justify-content: space-between; align-items: center; }
                        .test-card { border-top: 1px solid #eee; padding: 20px 0; }
                        .test-header { display: flex; justify-content: space-between; align-items: center; }
                        .test-header h3 { margin: 0; }
                        .status { padding: 6px 12px; border-radius: 20px; font-weight: bold; font-size: 13px; }
                        .success { background: #dff5e5; color: #19763a; }
                        .failure { background: #fde2e2; color: #b42318; }
                        .skipped { background: #fff0c2; color: #8a6100; }
                        .unknown { background: #e5e7eb; }
                        .steps li { margin-bottom: 6px; }
                        .error { background: #fff1f1; padding: 15px; border-radius: 8px; margin-top: 15px; }
                        .error pre { white-space: pre-wrap; margin: 0; }
                        .screenshot img { max-width: 700px; max-height: 450px; border: 1px solid #ddd; border-radius: 6px; margin-top: 15px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Otomasyon Test Özeti</h1>
                            <p><strong>Koşu Tarihi:</strong> %s</p>
                            <p><strong>Genel Durum:</strong> %s</p>
                            <div class="summary">
                                <div class="summary-card"><strong>Toplam</strong><br>%d</div>
                                <div class="summary-card"><strong>Başarılı</strong><br>%d</div>
                                <div class="summary-card"><strong>Hatalı</strong><br>%d</div>
                                <div class="summary-card"><strong>Atlandı</strong><br>%d</div>
                            </div>
                        </div>
                        %s
                    </div>
                </body>
                </html>
                """.formatted(
                kosuBaslangici.format(EKRAN_TARIH_BICIMI),
                overallStatus,
                tests.size(),
                passed,
                failed,
                skipped,
                siniflar
        );
    }

    /** Sınıf başlığı ve testleri; sorunlu sınıflar açık, diğerleri kapalı gelir. */
    private static String sinifBolumuOlustur(String className, Path kosuKlasoru, Map<Path, Path> ekranlar) {

        List<TestResultData> tests = getTestsForClass(className);
        long gecen = tests.stream().filter(test -> BASARILI.equals(test.status)).count();
        boolean sorunlu = tests.stream().anyMatch(HtmlReportManager::sorunluMu);

        StringBuilder kartlar = new StringBuilder();
        for (TestResultData test : tests) {
            kartlar.append(testKartiOlustur(test, kosuKlasoru, ekranlar));
        }

        return """
                <details class="sinif"%s>
                    <summary><span>%s</span><span class="status %s">%d / %d</span></summary>
                    %s
                </details>
                """.formatted(
                sorunlu ? " open" : "",
                escapeHtml(className),
                sorunlu ? "failure" : "success",
                gecen,
                tests.size(),
                kartlar
        );
    }

    private static String testKartiOlustur(TestResultData test, Path kosuKlasoru, Map<Path, Path> ekranlar) {

        String statusClass = switch (test.status) {
            case BASARILI -> "success";
            case HATALI -> "failure";
            case ATLANDI -> "skipped";
            default -> "unknown";
        };

        StringBuilder steps = new StringBuilder();
        for (String step : test.steps) {
            steps.append("<li>").append(escapeHtml(step)).append("</li>");
        }
        String adimlarHtml = steps.isEmpty() ? "" : "<ol class=\"steps\">" + steps + "</ol>";

        String errorHtml = "";
        if (test.errorMessage != null) {
            String baslik = ATLANDI.equals(test.status) ? "Atlanma sebebi:" : "Hata:";
            errorHtml = """
                    <div class="error">
                        <strong>%s</strong>
                        <pre>%s</pre>
                    </div>
                    """.formatted(baslik, escapeHtml(test.errorMessage));
        }

        return """
                <div class="test-card">
                    <div class="test-header">
                        <h3>%s</h3>
                        <span class="status %s">%s</span>
                    </div>
                    <p><strong>Süre:</strong> %.2f saniye</p>
                    %s
                    %s
                    %s
                </div>
                """.formatted(
                escapeHtml(test.testName),
                statusClass,
                test.status,
                test.duration / 1000.0,
                adimlarHtml,
                errorHtml,
                ekranHtmlOlustur(test, kosuKlasoru, ekranlar)
        );
    }

    /**
     * Ekran görüntüsünü kanitlar altındaki kopyasından, ozet.html'e göre bağıl yolla gösterir.
     * Böylece koşu klasörü (veya mail ZIP'i) taşındığında da görüntüler açılır.
     */
    private static String ekranHtmlOlustur(TestResultData test, Path kosuKlasoru, Map<Path, Path> ekranlar) {

        if (test.screenshotPath == null) {
            return "";
        }

        Path kopya = ekranlar.get(test.screenshotPath.toAbsolutePath().normalize());
        if (kopya == null) {
            return "";
        }

        String goreliYol = kosuKlasoru.relativize(kopya).toString().replace("\\", "/");

        return """
                <div class="screenshot">
                    <a href="%s" target="_blank"><img src="%s" alt="Ekran görüntüsü"></a>
                </div>
                """.formatted(goreliYol, goreliYol);
    }

    // ------------------------------------------------------------------
    // Yardımcılar
    // ------------------------------------------------------------------

    private static TestResultData getCurrentTest() {
        String testId = CURRENT_TEST.get();
        return testId == null ? null : TEST_RESULTS.get(testId);
    }

    private static List<TestResultData> getAllTestsInOrder() {

        List<TestResultData> tests = new ArrayList<>();

        for (String testId : TEST_ORDER) {
            TestResultData test = TEST_RESULTS.get(testId);
            if (test != null) {
                tests.add(test);
            }
        }

        return tests;
    }

    private static List<TestResultData> getTestsForClass(String className) {
        return getAllTestsInOrder().stream()
                .filter(test -> test.className.equals(className))
                .toList();
    }

    private static boolean sorunluMu(TestResultData test) {
        return HATALI.equals(test.status) || ATLANDI.equals(test.status);
    }

    private static String mesajAl(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
    }

    /** Hata mesajlarındaki HTML parçalarının sayfa yapısını bozmasını önler. */
    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class TestResultData {

        private final String className;
        private final String testName;
        private final LocalDateTime startedAt;
        private final List<String> steps = new CopyOnWriteArrayList<>();

        private volatile String status = "CALISIYOR";
        private volatile long duration;
        private volatile String errorMessage;
        private volatile Path screenshotPath;

        private TestResultData(String className, String testName, LocalDateTime startedAt) {
            this.className = className;
            this.testName = testName;
            this.startedAt = startedAt;
        }
    }
}