package com.bvakp.automation.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Son test koşusundan mail eki ZIP'i oluşturur.
 *
 * <p>ZIP'e yalnızca maille paylaşılması uygun içerik girer: ozet.html ve kanitlar altındaki bütün
 * ekran görüntüleri (ozet.html test sonu görüntülerini bağıl yolla gösterir). Aynı içerikli
 * görüntüler bir kez eklenir. RAPOR.html, video, trace ve teknik klasörü boyut ve hassas veri
 * nedeniyle eklenmez; bunlar reports/kosular arşivinde kalır.
 */
public final class ReportZipUtil {

  // AllureRunOrganizer ile aynı kaynaktan okunmalıdır.
  private static final Path RAPOR_KLASORU =
      Paths.get(System.getProperty("report.archive.directory", "reports/kosular"));

  private static final Path MAIL_ZIP_KLASORU = Paths.get("target", "mail-reports");

  private static final String OZET_DOSYASI = "ozet.html";

  /*
   * AllureRunOrganizer'ın ürettiği koşu klasörü adı:
   * 2026-10-22_19-41-05_HATALI   (aynı saniyede ikinci koşu: 2026-10-22_19-41-05_2_HATALI)
   * └────── tarih ──────┘
   */
  private static final Pattern KOSU_KLASOR_DESENI =
      Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})(_\\d+)?_(BASARILI|HATALI)$");

  private ReportZipUtil() {}

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

  /**
   * Arşivdeki en güncel koşu klasörünü bulur; ad tarihle başladığı için alfabetik en büyük olandır.
   */
  private static Path sonKosuKlasorunuBul() throws IOException {

    try (Stream<Path> stream = Files.list(RAPOR_KLASORU)) {
      return stream
          .filter(Files::isDirectory)
          .filter(path -> KOSU_KLASOR_DESENI.matcher(path.getFileName().toString()).matches())
          .max(Comparator.comparing(path -> path.getFileName().toString()))
          .orElseThrow(
              () ->
                  new IllegalStateException(
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

  /**
   * ozet.html ve ekran görüntülerini, koşu klasörü adıyla birlikte ZIP'e ekler.
   *
   * <p>Aynı içerikli görüntüler ZIP'e bir kez girer (ör. retry'da iki deneme de aynı hata ekranında
   * kalır, ya da birçok test aynı giriş ekranında kırılır). ozet.html'de ZIP'e girmeyen kopyaya
   * verilen yollar, ZIP'teki kopyaya çevrilir; böylece görüntüler açılmaya devam eder.
   */
  private static void kosuKlasorunuZiple(Path kosuKlasoru, Path zipDosyasi) throws IOException {

    Path parent = kosuKlasoru.getParent();

    List<Path> dosyalar;
    try (Stream<Path> paths = Files.walk(kosuKlasoru)) {
      // Sıralama, ekran.jpg'nin ekran_2.jpg'den önce gelip ZIP'te kalmasını sağlar.
      dosyalar =
          paths
              .filter(Files::isRegularFile)
              .filter(dosya -> zipeEklensinMi(kosuKlasoru.relativize(dosya)))
              .sorted()
              .toList();
    }

    // İçerik özeti -> ZIP'e giren ilk kopyanın koşu klasörüne göre yolu
    Map<String, String> ilkKopyalar = new HashMap<>();
    // ZIP'e girmeyen kopyanın yolu -> ozet.html'de yerine gösterilecek yol
    Map<String, String> yonlendirmeler = new HashMap<>();
    Path ozetDosyasi = null;

    try (OutputStream outputStream = Files.newOutputStream(zipDosyasi);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {

      for (Path dosya : dosyalar) {

        String goreliYol = kosuKlasoru.relativize(dosya).toString().replace("\\", "/");

        if (OZET_DOSYASI.equals(goreliYol)) {
          ozetDosyasi = dosya;
          continue;
        }

        String ilkKopya = ilkKopyalar.putIfAbsent(icerikOzeti(dosya), goreliYol);
        if (ilkKopya != null) {
          yonlendirmeler.put(goreliYol, ilkKopya);
          continue;
        }

        zipeYaz(zipOutputStream, zipEntryAdi(parent, dosya), Files.readAllBytes(dosya));
      }

      if (ozetDosyasi != null) {
        String html = Files.readString(ozetDosyasi, StandardCharsets.UTF_8);
        for (Map.Entry<String, String> yonlendirme : yonlendirmeler.entrySet()) {
          html =
              html.replace(
                  "\"" + yonlendirme.getKey() + "\"", "\"" + yonlendirme.getValue() + "\"");
        }
        zipeYaz(
            zipOutputStream,
            zipEntryAdi(parent, ozetDosyasi),
            html.getBytes(StandardCharsets.UTF_8));
      }
    }

    if (!yonlendirmeler.isEmpty()) {
      System.out.println(
          "Aynı içerikli " + yonlendirmeler.size() + " görüntü ZIP'e eklenmedi (kopya).");
    }
  }

  /** ZIP standardı klasör ayırıcı olarak "/" bekler. */
  private static String zipEntryAdi(Path parent, Path dosya) {
    return parent.relativize(dosya).toString().replace("\\", "/");
  }

  private static void zipeYaz(ZipOutputStream zipOutputStream, String ad, byte[] icerik)
      throws IOException {
    zipOutputStream.putNextEntry(new ZipEntry(ad));
    zipOutputStream.write(icerik);
    zipOutputStream.closeEntry();
  }

  /** Dosya içeriğinin SHA-256 özeti; aynı görüntüleri ayırt etmek için. */
  private static String icerikOzeti(Path dosya) throws IOException {
    try {
      byte[] ozet = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(dosya));
      return HexFormat.of().formatHex(ozet);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 desteklenmiyor.", e);
    }
  }

  /**
   * Yalnızca kökteki ozet.html ve kanitlar altındaki bütün PNG'ler: test sonu ekran görüntüleri
   * (ekran.png), test içinde elle alınanlar (kaynak_arama_pozitif.png) ve teste bağlanamayanlar
   * (_eslesmeyen).
   */
  private static boolean zipeEklensinMi(Path goreliYol) {

    String ad = goreliYol.getFileName().toString();

    if (goreliYol.getNameCount() == 1) {
      return OZET_DOSYASI.equals(ad);
    }

    return "kanitlar".equals(goreliYol.getName(0).toString()) && ad.endsWith(".png");
  }
}
