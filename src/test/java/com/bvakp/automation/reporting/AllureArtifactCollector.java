package com.bvakp.automation.reporting;

import com.bvakp.automation.core.config.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Video ve trace dosyalarını testin Allure UUID'siyle eşleştirir.
 *
 * <p>Allure TestNG, test sonucunu @AfterMethod tamamlanmadan önce yazabildiği için
 * video/trace dosyaları suite sonunda doğru result.json içine eklenir.
 * Hiçbir test senaryosu veya Page Object bu sınıfı kullanmak zorunda değildir.
 */
public final class AllureArtifactCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllureArtifactCollector.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path RESULTS_DIRECTORY = Paths.get(
            System.getProperty("allure.results.directory", "target/allure-results")
    );

    private static final ThreadLocal<String> CURRENT_TEST_UUID = new ThreadLocal<>();

    // TestNG test class'i ve metodu, Allure attachment'larindan bagimsiz olarak gercek
    // ekran goruntusu / video / trace dosyalarinin arsiv klasorunu belirler.
    private static final ThreadLocal<String> CURRENT_TEST_CLASS = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_TEST_NAME = new ThreadLocal<>();
    private static final ConcurrentMap<Path, KanitSahibi> ARTIFACT_OWNERS = new ConcurrentHashMap<>();

    /**
     * Kanıt dosyasının ait olduğu test.
     *
     * @param testAdi bilinmiyorsa null
     * @param ad      arşivdeki dosya adı (uzantısız); null ise türün sabit adı kullanılır (ekran, video, trace)
     */
    public record KanitSahibi(String sinifAdi, String testAdi, String ad) {
    }
    private static final ConcurrentMap<String, ConcurrentMap<ArtifactType, Path>> PENDING =
            new ConcurrentHashMap<>();

    private enum ArtifactType {
        VIDEO("Playwright Video", "video/webm", ".webm"),
        TRACE("Playwright Trace", "application/zip", ".zip");

        private final String name;
        private final String mimeType;
        private final String extension;

        ArtifactType(String name, String mimeType, String extension) {
            this.name = name;
            this.mimeType = mimeType;
            this.extension = extension;
        }
    }

    private AllureArtifactCollector() {
    }

    /** Test metodu sona ererken, Allure bağlamı hâlâ aktifken çağrılır. */
    public static void bindCurrentTest(String className) {
        bindCurrentTest(className, null);
    }

    /** Sınıfın yanında test adını da saklar; kanıtlar arşivde test klasörüne ayrılır. */
    public static void bindCurrentTest(String className, String testName) {
        // Allure UUID bulunmasa bile dosyanin test sinifi belli olabilir.
        if (className != null && !className.isBlank()) {
            CURRENT_TEST_CLASS.set(className);
        }
        if (testName != null && !testName.isBlank()) {
            CURRENT_TEST_NAME.set(testName);
        }
        Allure.getLifecycle().getCurrentTestCase()
                .ifPresentOrElse(
                        CURRENT_TEST_UUID::set,
                        () -> LOGGER.warn("Allure test UUID'si bulunamadı; video/trace diskte saklanacak.")
                );
    }

    /** Testin sonunda alınan ekran görüntüsü; arşivde ekran.png olur. */
    public static void registerScreenshot(Path path) {
        rememberArtifactClass(path);
    }

    /**
     * Test sırasında elle alınan ekran görüntüsünü teste bağlar; arşivde kendi adıyla
     * (ör. kaynak_arama_pozitif.png) testin klasörüne gider.
     */
    public static void registerTestScreenshot(Path path, String sinifAdi, String testAdi, String ad) {
        if (path != null && sinifAdi != null && testAdi != null) {
            ARTIFACT_OWNERS.put(path.toAbsolutePath().normalize(), new KanitSahibi(sinifAdi, testAdi, ad));
        }
    }

    /** Arsivleyicinin fiziksel kanit dosyalarini siniflara ayirmasi icindir. */
    public static String findArtifactClass(Path path) {
        KanitSahibi sahip = findArtifactOwner(path);
        return sahip == null ? null : sahip.sinifAdi();
    }

    /** Dosyanın ait olduğu sınıf ve test; eşleşme yoksa null. */
    public static KanitSahibi findArtifactOwner(Path path) {
        if (path == null) {
            return null;
        }
        return ARTIFACT_OWNERS.get(path.toAbsolutePath().normalize());
    }

    private static void rememberArtifactClass(Path path) {
        String className = CURRENT_TEST_CLASS.get();
        if (className != null && !className.isBlank() && path != null) {
            ARTIFACT_OWNERS.put(path.toAbsolutePath().normalize(),
                    new KanitSahibi(className, CURRENT_TEST_NAME.get(), null));
        }
    }

    public static void registerTrace(Path path) {
        register(ArtifactType.TRACE, path);
    }

    public static void registerVideo(Path path) {
        register(ArtifactType.VIDEO, path);
    }

    private static void register(ArtifactType type, Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }

        // Arşivde doğru class klasörüne gitmesi için her durumda hatırlanır.
        rememberArtifactClass(path);

        if (!allureyaEklensinMi(type)) {
            LOGGER.debug("{} Allure'a eklenmedi (ayar kapalı), dosya korundu: {}", type.name, path);
            return;
        }

        String testUuid = CURRENT_TEST_UUID.get();
        if (testUuid == null || testUuid.isBlank()) {
            LOGGER.warn("{} Allure'a eşleştirilemedi, dosya korundu: {}", type.name, path);
            return;
        }

        PENDING.computeIfAbsent(testUuid, ignored -> new ConcurrentHashMap<>())
                .put(type, path.toAbsolutePath());

        LOGGER.info("{} Allure testine ayrıldı | UUID={} | {}", type.name, testUuid, path);
    }

    /** allure.attach.video / allure.attach.trace ayarlarına göre karar verir. */
    private static boolean allureyaEklensinMi(ArtifactType type) {
        return switch (type) {
            case VIDEO -> Boolean.parseBoolean(
                    ConfigManager.getOrDefault("allure.attach.video", "false"));
            case TRACE -> Boolean.parseBoolean(
                    ConfigManager.getOrDefault("allure.attach.trace", "true"));
        };
    }

    /** @AfterMethod içinde BrowserContext ve video kapandıktan sonra thread kaydı temizlenir. */
    public static void clearCurrentTest() {
        CURRENT_TEST_UUID.remove();
        CURRENT_TEST_CLASS.remove();
        CURRENT_TEST_NAME.remove();
    }

    /**
     * Bütün testler tamamlandığında çağrılır. Allure'ın ürettiği JSON korunur;
     * yalnızca UUID ile eşleşen testin attachments alanına iki medya eklenir.
     */
    public static void publishPendingArtifacts() {
        for (Map.Entry<String, ConcurrentMap<ArtifactType, Path>> entry : PENDING.entrySet()) {
            String uuid = entry.getKey();
            Path resultJson = RESULTS_DIRECTORY.resolve(uuid + "-result.json");
            if (!Files.isRegularFile(resultJson)) {
                LOGGER.warn("Allure result.json bulunamadı: {}. Medya dosyaları diskte korundu.", resultJson);
                continue;
            }
            try {
                ObjectNode result = (ObjectNode) MAPPER.readTree(resultJson.toFile());
                ArrayNode attachments = result.withArray("attachments");
                boolean changed = false;

                for (Map.Entry<ArtifactType, Path> artifact : entry.getValue().entrySet()) {
                    ArtifactType type = artifact.getKey();
                    Path source = artifact.getValue();
                    if (!Files.isRegularFile(source) || Files.size(source) == 0) {
                        LOGGER.warn("Boş veya eksik {} dosyası: {}", type.name, source);
                        continue;
                    }
                    if (containsNamedAttachment(attachments, type.name)) {
                        continue;
                    }

                    String attachmentName = UUID.randomUUID() + "-attachment" + type.extension;
                    Files.copy(source, RESULTS_DIRECTORY.resolve(attachmentName));
                    attachments.addObject()
                            .put("name", type.name)
                            .put("source", attachmentName)
                            .put("type", type.mimeType);
                    changed = true;
                    LOGGER.info("{} Allure sonucuna bağlandı: {}", type.name, resultJson.getFileName());
                }

                if (changed) {
                    writeAtomically(resultJson, result);
                }
                PENDING.remove(uuid, entry.getValue());
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Allure medya ekleme işlemi başarısız: {}", resultJson, e);
            }
        }
    }

    private static boolean containsNamedAttachment(ArrayNode attachments, String name) {
        for (JsonNode attachment : attachments) {
            if (name.equals(attachment.path("name").asText())) {
                return true;
            }
        }
        return false;
    }

    private static void writeAtomically(Path resultJson, ObjectNode result) throws IOException {
        Path temporary = Files.createTempFile(RESULTS_DIRECTORY, "allure-media-", ".tmp");
        try {
            MAPPER.writeValue(temporary.toFile(), result);
            try {
                Files.move(temporary, resultJson,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, resultJson, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}