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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Son test koşusunun arşiv klasörünü bulur ve mail eki olarak ZIP oluşturur.
 *
 * <p>ZIP'e yalnızca maille paylaşılması uygun ve dosyadan açılabilen içerik eklenir:
 * class bazlı ozet.html raporları, ekran görüntüleri ve run-info.txt.
 * Video, trace, test bazlı kanıtlar ve Allure dosyaları boyut ve hassas veri nedeniyle eklenmez;
 * bunlar reports/allure-runs arşivinde kalır.
 */
public final class ReportZipUtil {

    // AllureRunOrganizer ile aynı kaynaktan okunmalıdır.
    private static final Path RAPOR_KLASORU = Paths.get(
            System.getProperty("report.archive.directory", "reports/allure-runs"));

    private static final Path MAIL_ZIP_KLASORU = Paths.get("target", "mail-reports");

    /*
     * AllureRunOrganizer'ın ürettiği koşu klasörü formatı:
     * Kosu_20261020_230900_123_ab12cd34_HATALI
     *      └──── tarih ──────┘ └ id ─┘ └ durum ┘
     */
    private static final Pattern KOSU_KLASOR_DESENI = Pattern.compile(
            "^Kosu_(\\d{8}_\\d{6}_\\d{3})_[0-9a-f]{8}_(BASARILI|HATALI)$");

    private static final DateTimeFormatter TARIH_FORMATI =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /** ZIP'e alınmayacak klasörler. */
    private static final Set<String> HARIC_KLASORLER = Set.of(
            "videos", "traces", "allure-results", "allure-report", "kanitlar");

    /** ZIP'e alınmayacak dosyalar. */
    private static final Set<String> HARIC_DOSYALAR = Set.of("allure-generation.log", "RAPOR.html");

    private ReportZipUtil() {
    }

    /**
     * En son koşu klasöründen mail eki ZIP'i oluşturur.
     *
     * @return oluşturulan ZIP dosyasının yolu
     */
    public static Path sonKosuyuZipOlustur() {

        if (!Files.isDirectory(RAPOR_KLASORU)) {
            throw new IllegalStateException(
                    "Rapor klasörü bulunamadı: " + RAPOR_KLASORU.toAbsolutePath());
        }

        try {
            Path sonKosuKlasoru = sonKosuKlasorunuBul();
            String kosuTarihi = kosuTarihiAl(sonKosuKlasoru.getFileName().toString());

            Files.createDirectories(MAIL_ZIP_KLASORU);

            Path zipDosyasi = MAIL_ZIP_KLASORU.resolve("BVA_KP_Test_Raporu_" + kosuTarihi + ".zip");

            // Aynı koşu için daha önce oluşturulan ZIP yenisiyle değiştirilir.
            Files.deleteIfExists(zipDosyasi);

            kosuKlasorunuZiple(sonKosuKlasoru, zipDosyasi);

            return zipDosyasi;

        } catch (IOException e) {
            throw new RuntimeException("Test raporları ZIP oluşturulurken hata oluştu.", e);
        }
    }

    /** Arşivdeki en güncel Kosu_... klasörünü bulur. */
    private static Path sonKosuKlasorunuBul() throws IOException {

        try (Stream<Path> stream = Files.list(RAPOR_KLASORU)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> KOSU_KLASOR_DESENI.matcher(path.getFileName().toString()).matches())
                    .max(Comparator.comparing(path ->
                            LocalDateTime.parse(kosuTarihiAl(path.getFileName().toString()), TARIH_FORMATI)))
                    .orElseThrow(() -> new IllegalStateException(
                            "ZIP oluşturulabilecek test koşusu bulunamadı: "
                                    + RAPOR_KLASORU.toAbsolutePath()));
        }
    }

    /** Koşu klasör adından yyyyMMdd_HHmmss_SSS formatındaki tarihi çıkarır. */
    private static String kosuTarihiAl(String klasorAdi) {

        Matcher matcher = KOSU_KLASOR_DESENI.matcher(klasorAdi);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Geçersiz koşu klasör adı: " + klasorAdi);
        }

        return matcher.group(1);
    }

    /** Koşu klasörünü, hariç tutulan içerik dışında ZIP'e ekler. */
    private static void kosuKlasorunuZiple(Path kosuKlasoru, Path zipDosyasi) throws IOException {

        Path parent = kosuKlasoru.getParent();

        try (OutputStream outputStream = Files.newOutputStream(zipDosyasi);
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
             Stream<Path> paths = Files.walk(kosuKlasoru)) {

            List<Path> dosyalar = paths
                    .filter(Files::isRegularFile)
                    .filter(dosya -> zipeEklensinMi(kosuKlasoru.relativize(dosya)))
                    .toList();

            for (Path dosya : dosyalar) {

                // ZIP standardı klasör ayırıcı olarak "/" bekler.
                String zipEntryAdi = parent.relativize(dosya).toString().replace("\\", "/");

                zipOutputStream.putNextEntry(new ZipEntry(zipEntryAdi));
                Files.copy(dosya, zipOutputStream);
                zipOutputStream.closeEntry();
            }
        }
    }

    private static boolean zipeEklensinMi(Path goreliYol) {

        for (Path parca : goreliYol) {
            if (HARIC_KLASORLER.contains(parca.toString())) {
                return false;
            }
        }

        return !HARIC_DOSYALAR.contains(goreliYol.getFileName().toString());
    }
}