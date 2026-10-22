package com.bvakp.automation.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Son test koşusundan mail eki ZIP'i oluşturur.
 *
 * <p>ZIP'e yalnızca maille paylaşılması uygun içerik girer: ozet.html ve
 * kanitlar altındaki ekran görüntüleri (ozet.html bunları bağıl yolla gösterir).
 * RAPOR.html, video, trace ve teknik klasörü boyut ve hassas veri nedeniyle eklenmez;
 * bunlar reports/kosular arşivinde kalır.
 */
public final class ReportZipUtil {

    // AllureRunOrganizer ile aynı kaynaktan okunmalıdır.
    private static final Path RAPOR_KLASORU = Paths.get(
            System.getProperty("report.archive.directory", "reports/kosular"));

    private static final Path MAIL_ZIP_KLASORU = Paths.get("target", "mail-reports");

    /*
     * AllureRunOrganizer'ın ürettiği koşu klasörü adı:
     * 2026-10-22_19-41-05_HATALI   (aynı saniyede ikinci koşu: 2026-10-22_19-41-05_2_HATALI)
     * └────── tarih ──────┘
     */
    private static final Pattern KOSU_KLASOR_DESENI = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})(_\\d+)?_(BASARILI|HATALI)$");

    private ReportZipUtil() {
    }

    /**
     * En son koşu klasöründen mail eki ZIP'i oluşturur.
     *
     * @return oluşturulan ZIP dosyasının yolu (ör. BVA_KP_Test_Raporu_2026-10-22_19-41-05.zip)
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

    /** Arşivdeki en güncel koşu klasörünü bulur; ad tarihle başladığı için alfabetik en büyük olandır. */
    private static Path sonKosuKlasorunuBul() throws IOException {

        try (Stream<Path> stream = Files.list(RAPOR_KLASORU)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> KOSU_KLASOR_DESENI.matcher(path.getFileName().toString()).matches())
                    .max(Comparator.comparing(path -> path.getFileName().toString()))
                    .orElseThrow(() -> new IllegalStateException(
                            "ZIP oluşturulabilecek test koşusu bulunamadı: "
                                    + RAPOR_KLASORU.toAbsolutePath()));
        }
    }

    /** Koşu klasör adından 2026-10-22_19-41-05 biçimindeki tarihi çıkarır. */
    private static String kosuTarihiAl(String klasorAdi) {

        Matcher matcher = KOSU_KLASOR_DESENI.matcher(klasorAdi);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Geçersiz koşu klasör adı: " + klasorAdi);
        }

        return matcher.group(1);
    }

    /** ozet.html ve ekran görüntülerini, koşu klasörü adıyla birlikte ZIP'e ekler. */
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

    /** Yalnızca kökteki ozet.html ve kanitlar altındaki ekran*.png dosyaları. */
    private static boolean zipeEklensinMi(Path goreliYol) {

        String ad = goreliYol.getFileName().toString();

        if (goreliYol.getNameCount() == 1) {
            return "ozet.html".equals(ad);
        }

        return "kanitlar".equals(goreliYol.getName(0).toString())
                && ad.startsWith("ekran")
                && ad.endsWith(".png");
    }
}