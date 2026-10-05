package com.bvakp.automation.reporting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

public final class HtmlReportManager {

    private static final Map<String, TestResultData> TEST_RESULTS =
            new ConcurrentHashMap<>();

    private static final List<String> TEST_ORDER =
            new CopyOnWriteArrayList<>();

    private static final ThreadLocal<String> CURRENT_TEST =
            new ThreadLocal<>();

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    private HtmlReportManager() {
    }

    public static void startTest(
            String className,
            String testName) {

        String testId =
                className
                        + "_"
                        + testName
                        + "_"
                        + UUID.randomUUID();

        TestResultData testResult =
                new TestResultData(
                        className,
                        testName,
                        LocalDateTime.now()
                );

        TEST_RESULTS.put(
                testId,
                testResult
        );

        TEST_ORDER.add(testId);
        CURRENT_TEST.set(testId);
    }

    public static void addStep(String step) {

        TestResultData test =
                getCurrentTest();

        if (test != null) {
            test.steps.add(step);
        }
    }

    public static void markPassed(long duration) {

        TestResultData test =
                getCurrentTest();

        if (test == null) {
            return;
        }

        test.status = "BASARILI";
        test.duration = duration;

        CURRENT_TEST.remove();
    }

    public static void markFailed(
            long duration,
            Throwable throwable) {

        TestResultData test =
                getCurrentTest();

        if (test == null) {
            return;
        }

        test.status = "HATALI";
        test.duration = duration;

        if (throwable != null) {
            test.errorMessage =
                    throwable.getMessage();
        }

        CURRENT_TEST.remove();
    }

    public static void markSkipped(long duration) {

        TestResultData test =
                getCurrentTest();

        if (test == null) {
            return;
        }

        test.status = "ATLANDI";
        test.duration = duration;

        CURRENT_TEST.remove();
    }

    public static void setScreenshot(
            Path screenshotPath) {

        TestResultData test =
                getCurrentTest();

        if (test != null) {
            test.screenshotPath = screenshotPath;
        }
    }

    /**
     * Bu test koşusunda rapora düşen class adlarını
     * ilk çalıştırılma sırasına göre döndürür.
     */
    public static List<String> getExecutedClassNames() {

        Set<String> classNames =
                new LinkedHashSet<>();

        for (String testId : TEST_ORDER) {

            TestResultData test =
                    TEST_RESULTS.get(testId);

            if (test != null) {
                classNames.add(test.className);
            }
        }

        return new ArrayList<>(classNames);
    }

    /**
     * Belirtilen class içerisindeki testlerden en az biri
     * hatalıysa true döndürür.
     */
    public static boolean classFailed(
            String className) {

        return getTestsForClass(className)
                .stream()
                .anyMatch(
                        test -> "HATALI".equals(test.status)
                );
    }

    /**
     * Geriye dönük uyumluluk için tüm testleri tek raporda üretir.
     */
    public static void generateReport(
            Path runDirectory) {

        generateReportInternal(
                runDirectory,
                null
        );
    }

    /**
     * Yalnızca belirtilen test class'ına ait testleri içeren
     * HTML raporunu üretir.
     */
    public static void generateReport(
            Path runDirectory,
            String className) {

        generateReportInternal(
                runDirectory,
                className
        );
    }

    private static void generateReportInternal(
            Path runDirectory,
            String className) {

        try {

            Files.createDirectories(
                    runDirectory
            );

            Path screenshotDirectory =
                    runDirectory.resolve(
                            "screenshots"
                    );

            Files.createDirectories(
                    screenshotDirectory
            );

            List<TestResultData> tests =
                    className == null
                            ? getAllTestsInOrder()
                            : getTestsForClass(className);

            String html =
                    createHtml(
                            screenshotDirectory,
                            tests,
                            className
                    );

            Path reportFile =
                    runDirectory.resolve(
                            "ozet.html"
                    );

            Files.writeString(
                    reportFile,
                    html,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            ReportManager.error(
                    "HTML raporu oluşturulurken hata oluştu.",
                    e
            );
        }
    }

    private static List<TestResultData> getAllTestsInOrder() {

        List<TestResultData> tests =
                new ArrayList<>();

        for (String testId : TEST_ORDER) {

            TestResultData test =
                    TEST_RESULTS.get(testId);

            if (test != null) {
                tests.add(test);
            }
        }

        return tests;
    }

    private static List<TestResultData> getTestsForClass(
            String className) {

        List<TestResultData> tests =
                new ArrayList<>();

        for (String testId : TEST_ORDER) {

            TestResultData test =
                    TEST_RESULTS.get(testId);

            if (test != null
                    && test.className.equals(className)) {

                tests.add(test);
            }
        }

        return tests;
    }

    private static String createHtml(
            Path screenshotDirectory,
            List<TestResultData> tests,
            String className)
            throws IOException {

        int total =
                tests.size();

        long passed =
                tests.stream()
                        .filter(
                                test ->
                                        "BASARILI"
                                                .equals(test.status)
                        )
                        .count();

        long failed =
                tests.stream()
                        .filter(
                                test ->
                                        "HATALI"
                                                .equals(test.status)
                        )
                        .count();

        long skipped =
                tests.stream()
                        .filter(
                                test ->
                                        "ATLANDI"
                                                .equals(test.status)
                        )
                        .count();

        String overallStatus =
                failed > 0
                        ? "HATALI"
                        : "BASARILI";

        StringBuilder testBlocks =
                new StringBuilder();

        for (TestResultData test : tests) {

            String statusClass =
                    switch (test.status) {

                        case "BASARILI" ->
                                "success";

                        case "HATALI" ->
                                "failure";

                        case "ATLANDI" ->
                                "skipped";

                        default ->
                                "unknown";
                    };

            StringBuilder steps =
                    new StringBuilder();

            for (String step : test.steps) {

                steps.append(
                        "<li>"
                                + escapeHtml(step)
                                + "</li>"
                );
            }

            String screenshotHtml = "";

            if (test.screenshotPath != null
                    && Files.exists(
                    test.screenshotPath
            )) {

                String fileName =
                        sanitizeFileName(test.testName)
                                + "_"
                                + test.startedAt.format(FILE_DATE_FORMAT)
                                + "_"
                                + test.status
                                + ".png";

                Path targetScreenshot =
                        screenshotDirectory.resolve(
                                fileName
                        );

                Files.copy(
                        test.screenshotPath,
                        targetScreenshot,
                        StandardCopyOption.REPLACE_EXISTING
                );

                screenshotHtml =
                        """
                        <div class="screenshot">
                            <p><strong>Screenshot</strong></p>
                            <a href="screenshots/%s" target="_blank">
                                <img src="screenshots/%s" alt="Screenshot">
                            </a>
                        </div>
                        """.formatted(
                                fileName,
                                fileName
                        );
            }

            String errorHtml = "";

            if (test.errorMessage != null) {

                errorHtml =
                        """
                        <div class="error">
                            <strong>Hata:</strong>
                            <pre>%s</pre>
                        </div>
                        """.formatted(
                                escapeHtml(
                                        test.errorMessage
                                )
                        );
            }

            testBlocks.append(
                    """
                    <div class="test-card">
                        <div class="test-header">
                            <div>
                                <h2>%s</h2>
                                <span class="class-name">%s</span>
                            </div>

                            <span class="status %s">
                                %s
                            </span>
                        </div>

                        <p>
                            <strong>Süre:</strong>
                            %.2f saniye
                        </p>

                        <h3>Test Adımları</h3>

                        <ol class="steps">
                            %s
                        </ol>

                        %s
                        %s
                    </div>
                    """.formatted(
                            escapeHtml(
                                    test.testName
                            ),
                            escapeHtml(
                                    test.className
                            ),
                            statusClass,
                            test.status,
                            test.duration / 1000.0,
                            steps,
                            errorHtml,
                            screenshotHtml
                    )
            );
        }

        String reportTime =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd.MM.yyyy HH:mm:ss"
                                )
                        );

        String classInfo =
                className == null
                        ? "Tüm Test Class'ları"
                        : className;

        return """
                <!DOCTYPE html>
                <html lang="tr">

                <head>

                    <meta charset="UTF-8">

                    <title>
                        Otomasyon Test Raporu
                    </title>

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            background: #f5f6f8;
                            margin: 0;
                            padding: 30px;
                            color: #252525;
                        }

                        .container {
                            max-width: 1100px;
                            margin: auto;
                        }

                        .header {
                            background: white;
                            padding: 25px;
                            border-radius: 10px;
                            margin-bottom: 20px;
                        }

                        .summary {
                            display: flex;
                            gap: 15px;
                            margin-top: 20px;
                            flex-wrap: wrap;
                        }

                        .summary-card {
                            background: #f1f2f4;
                            padding: 15px 25px;
                            border-radius: 8px;
                            min-width: 120px;
                        }

                        .test-card {
                            background: white;
                            padding: 25px;
                            border-radius: 10px;
                            margin-bottom: 20px;
                        }

                        .test-header {
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                        }

                        .class-name {
                            color: #777;
                            font-size: 13px;
                        }

                        .status {
                            padding: 8px 14px;
                            border-radius: 20px;
                            font-weight: bold;
                        }

                        .success {
                            background: #dff5e5;
                            color: #19763a;
                        }

                        .failure {
                            background: #fde2e2;
                            color: #b42318;
                        }

                        .skipped {
                            background: #fff0c2;
                            color: #8a6100;
                        }

                        .unknown {
                            background: #e5e7eb;
                        }

                        .steps li {
                            margin-bottom: 10px;
                        }

                        .error {
                            background: #fff1f1;
                            padding: 15px;
                            border-radius: 8px;
                            margin-top: 15px;
                        }

                        .error pre {
                            white-space: pre-wrap;
                        }

                        .screenshot {
                            margin-top: 20px;
                        }

                        .screenshot img {
                            max-width: 700px;
                            max-height: 450px;
                            border: 1px solid #ddd;
                            border-radius: 6px;
                        }

                    </style>

                </head>

                <body>

                    <div class="container">

                        <div class="header">

                            <h1>
                                Otomasyon Test Raporu
                            </h1>

                            <p>
                                <strong>Test Class:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Koşu Tarihi:</strong>
                                %s
                            </p>

                            <p>
                                <strong>Genel Durum:</strong>
                                %s
                            </p>

                            <div class="summary">

                                <div class="summary-card">
                                    <strong>Toplam</strong>
                                    <br>
                                    %d
                                </div>

                                <div class="summary-card">
                                    <strong>Başarılı</strong>
                                    <br>
                                    %d
                                </div>

                                <div class="summary-card">
                                    <strong>Hatalı</strong>
                                    <br>
                                    %d
                                </div>

                                <div class="summary-card">
                                    <strong>Atlandı</strong>
                                    <br>
                                    %d
                                </div>

                            </div>

                        </div>

                        %s

                    </div>

                </body>

                </html>
                """.formatted(
                escapeHtml(classInfo),
                reportTime,
                overallStatus,
                total,
                passed,
                failed,
                skipped,
                testBlocks
        );
    }

    private static TestResultData getCurrentTest() {

        String testId =
                CURRENT_TEST.get();

        if (testId == null) {
            return null;
        }

        return TEST_RESULTS.get(testId);
    }

    private static String sanitizeFileName(
            String value) {

        if (value == null || value.isBlank()) {
            return "test";
        }

        return value.replaceAll(
                "[^a-zA-Z0-9._-]",
                "_"
        );
    }

    private static String escapeHtml(
            String value) {

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

        private final List<String> steps =
                new CopyOnWriteArrayList<>();

        private String status =
                "CALISIYOR";

        private long duration;
        private String errorMessage;
        private Path screenshotPath;

        private TestResultData(
                String className,
                String testName,
                LocalDateTime startedAt) {

            this.className = className;
            this.testName = testName;
            this.startedAt = startedAt;
        }
    }
}
