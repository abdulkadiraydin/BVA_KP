package com.bvakp.automation.mail;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.utils.ReportZipUtil;
import com.bvakp.automation.utils.TestResultSummaryUtil;
import com.bvakp.automation.utils.TestResultSummaryUtil.TestSonucu;
import com.bvakp.automation.utils.TestResultSummaryUtil.TestSummary;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Son test koşusunun rapor mailini hazırlar ve gönderir.
 *
 * <p>Mail gövdesinde sayıların yanında her testin sonucu, hatalı ve atlanan testlerin hata mesajı
 * yer alır; ekte ozet.html ve ekran görüntülerinin bulunduğu ZIP gönderilir. ZIP MAIL_MAX_EK_MB
 * sınırını aşarsa ek gönderilmez, mailde not düşülür. Jenkins'te koşuyorsa build ve Allure rapor
 * linkleri eklenir.
 *
 * <p>Test JVM'i kapandıktan (arşiv oluştuktan) sonra, ayrı bir süreçte çalıştırılmalıdır.
 */
public final class TestReportMailService {

  // ReportZipUtil'in ürettiği ad: BVA_KP_Test_Raporu_2026-10-22_19-41-05.zip
  private static final Pattern RAPOR_TARIH_DESENI =
      Pattern.compile("BVA_KP_Test_Raporu_(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})\\.zip");

  private static final DateTimeFormatter DOSYA_TARIH_FORMATI =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  private static final DateTimeFormatter MAIL_TARIH_FORMATI =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  private static final String BASARILI_RENGI = "#2E7D32";
  private static final String HATALI_RENGI = "#C62828";
  private static final String ATLANDI_RENGI = "#8A6100";

  /** Uzun stack/locator mesajları maili şişirmesin; tamamı ozet.html ve Allure'da. */
  private static final int MAX_HATA_UZUNLUGU = 1500;

  private static final String HUCRE = "padding: 8px 10px; border: 1px solid #dddddd;";
  private static final String BASLIK_HUCRE = HUCRE + " background: #f5f5f5; font-weight: bold;";

  private TestReportMailService() {}

  /** Son koşunun ZIP'ini oluşturur ve test sonuçlarıyla birlikte rapor maili gönderir. */
  public static void sonRaporuMailGonder() {

    System.out.println("Son otomasyon test raporu hazırlanıyor...");

    Path raporZip = ReportZipUtil.sonKosuyuZipOlustur();
    TestSummary testSummary = TestResultSummaryUtil.sonTestSonucunuAl();
    List<TestSonucu> sonuclar = TestResultSummaryUtil.testSonuclariniAl();

    String kosuTarihi = raporTarihiniAl(raporZip);
    String ortam = ConfigManager.ortam();
    String durum = testSummary.durum();

    long zipBoyutu = dosyaBoyutu(raporZip);
    long maxEkBoyutu = MailConfig.maxEkBoyutuMb() * 1024 * 1024;
    boolean ekGonderilsin = zipBoyutu <= maxEkBoyutu;

    String konu = "BVA KP Otomasyon Test Raporu - " + durum + " - " + ortam + " - " + kosuTarihi;

    String htmlMesaj =
        new StringBuilder()
            .append(
                "<html><body style=\"font-family: Arial, sans-serif; font-size: 14px;"
                    + " color: #333333; background-color: #ffffff;\">")
            .append("<p>Merhaba,</p>")
            .append(
                "<p>BVA KP otomasyon test koşusu tamamlanmıştır. Test sonuçları aşağıda yer"
                    + " almaktadır.</p>")
            .append(ozetTablosu(testSummary, durum, ortam, kosuTarihi))
            .append(sorunluTestlerBolumu(sonuclar))
            .append(tumTestlerBolumu(sonuclar))
            .append(ekBilgisi(raporZip, zipBoyutu, ekGonderilsin))
            .append("<p>İyi çalışmalar.</p>")
            .append(
                "<hr style=\"border: 0; border-top: 1px solid #eeeeee; margin-top: 25px;\">"
                    + "<p style=\"font-size: 11px; color: #888888;\">Bu e-posta BVA KP Test"
                    + " Otomasyon sistemi tarafından otomatik olarak oluşturulmuştur.</p>")
            .append("</body></html>")
            .toString();

    System.out.println("Rapor ZIP oluşturuldu: " + raporZip.toAbsolutePath());
    System.out.println("ZIP boyutu: " + mb(zipBoyutu) + " MB");
    System.out.println("Test durumu: " + durum + " | Ortam: " + ortam);

    if (!ekGonderilsin) {
      System.out.println(
          "ZIP, MAIL_MAX_EK_MB sınırını ("
              + MailConfig.maxEkBoyutuMb()
              + " MB) aştı; eksiz gidiyor.");
    }

    System.out.println("Mail gönderiliyor...");
    MailService.mailGonder(konu, htmlMesaj, ekGonderilsin ? raporZip : null);
    System.out.println("Test raporu mail gönderimi tamamlandı.");
  }

  /** Durum, sayılar, ortam, tarih ve (Jenkins'teyse) linkler. */
  private static String ozetTablosu(
      TestSummary summary, String durum, String ortam, String kosuTarihi) {

    String durumRengi = "BASARILI".equals(durum) ? BASARILI_RENGI : HATALI_RENGI;

    StringBuilder tablo =
        new StringBuilder(
            "<table style=\"border-collapse: collapse; width: 520px; margin: 20px 0;\">");

    satir(tablo, "Test Durumu", renkli(durum, durumRengi));
    satir(tablo, "Ortam", escape(ortam));
    satir(tablo, "Koşu Tarihi", escape(kosuTarihi));
    satir(tablo, "Toplam Test", String.valueOf(summary.toplam()));
    satir(tablo, "Başarılı", renkli(String.valueOf(summary.basarili()), BASARILI_RENGI));
    satir(tablo, "Başarısız", renkli(String.valueOf(summary.basarisiz()), HATALI_RENGI));
    satir(tablo, "Atlanan", String.valueOf(summary.atlanan()));

    // Jenkins her build'e BUILD_URL verir; lokal koşuda yoktur.
    String buildUrl = System.getenv("BUILD_URL");
    if (buildUrl != null && !buildUrl.isBlank()) {
      String temelUrl = buildUrl.endsWith("/") ? buildUrl : buildUrl + "/";
      satir(tablo, "Jenkins", link(temelUrl, "Build sayfası"));
      satir(tablo, "Allure Raporu", link(temelUrl + "allure/", "Allure raporunu aç"));
    }

    return tablo.append("</table>").toString();
  }

  /** Hatalı ve atlanan testler, hata mesajlarıyla birlikte; yoksa boş. */
  private static String sorunluTestlerBolumu(List<TestSonucu> sonuclar) {

    List<TestSonucu> sorunlular =
        sonuclar.stream().filter(sonuc -> !"BASARILI".equals(sonuc.durum())).toList();

    if (sorunlular.isEmpty()) {
      return "";
    }

    StringBuilder bolum =
        new StringBuilder("<h3 style=\"color: " + HATALI_RENGI + ";\">Hatalı ve Atlanan Testler (")
            .append(sorunlular.size())
            .append(")</h3>");

    for (TestSonucu sonuc : sorunlular) {
      String baslik = "ATLANDI".equals(sonuc.durum()) ? "Atlanma sebebi" : "Hata";
      bolum
          .append("<div style=\"margin-bottom: 14px; border-left: 4px solid ")
          .append(durumRengi(sonuc.durum()))
          .append("; background: #fafafa; padding: 10px 12px;\">")
          .append("<div><strong>")
          .append(escape(sonuc.sinif()))
          .append(" › ")
          .append(escape(sonuc.test()))
          .append("</strong> &nbsp; ")
          .append(renkli(sonuc.durum(), durumRengi(sonuc.durum())))
          .append("</div>")
          .append("<div style=\"margin-top: 6px; font-size: 12px; color: #555555;\">")
          .append(baslik)
          .append(":</div>")
          .append(
              "<pre style=\"white-space: pre-wrap; word-break: break-word; margin: 4px 0 0 0;"
                  + " font-family: Consolas, monospace; font-size: 12px;\">")
          .append(escape(kisalt(sonuc.hata())))
          .append("</pre></div>");
    }

    return bolum.toString();
  }

  /** Bütün testlerin çalışma sırasıyla tablosu. */
  private static String tumTestlerBolumu(List<TestSonucu> sonuclar) {

    if (sonuclar.isEmpty()) {
      return "<p style=\"color: #888888;\">Test bazında sonuç dosyası bulunamadı; ayrıntılar"
          + " ekteki ozet.html dosyasındadır.</p>";
    }

    StringBuilder tablo =
        new StringBuilder("<h3>Tüm Testler</h3>")
            .append("<table style=\"border-collapse: collapse; margin-bottom: 20px;\">")
            .append("<tr>")
            .append(baslikHucresi("#"))
            .append(baslikHucresi("Sınıf"))
            .append(baslikHucresi("Test"))
            .append(baslikHucresi("Durum"))
            .append(baslikHucresi("Süre (sn)"))
            .append("</tr>");

    int sira = 1;
    for (TestSonucu sonuc : sonuclar) {
      tablo
          .append("<tr>")
          .append(hucre(String.valueOf(sira++)))
          .append(hucre(escape(sonuc.sinif())))
          .append(hucre(escape(sonuc.test())))
          .append(hucre(renkli(sonuc.durum(), durumRengi(sonuc.durum()))))
          .append(hucre(String.format(Locale.ROOT, "%.1f", sonuc.sureMs() / 1000.0)))
          .append("</tr>");
    }

    return tablo.append("</table>").toString();
  }

  /** Ek gönderildiyse içeriğini, gönderilmediyse nedenini açıklar. */
  private static String ekBilgisi(Path raporZip, long zipBoyutu, boolean ekGonderildi) {

    if (ekGonderildi) {
      return "<p>Test adımları ve ekran görüntüleri ekteki <strong>"
          + escape(raporZip.getFileName().toString())
          + "</strong> dosyasındadır ("
          + mb(zipBoyutu)
          + " MB). ZIP'i açtıktan sonra ozet.html dosyasını tarayıcıda açabilirsiniz.</p>";
    }

    return "<p style=\"color: "
        + HATALI_RENGI
        + ";\">Rapor ZIP'i ("
        + mb(zipBoyutu)
        + " MB) mail eki sınırını ("
        + MailConfig.maxEkBoyutuMb()
        + " MB) aştığı için eklenmedi. Ekran görüntüleri Jenkins build sayfasındaki arşivde"
        + " (Build Artifacts) yer almaktadır.</p>";
  }

  // ------------------------------------------------------------------
  // Yardımcılar
  // ------------------------------------------------------------------

  private static void satir(StringBuilder tablo, String etiket, String degerHtml) {
    tablo.append("<tr>").append(hucre(etiket)).append(hucre(degerHtml)).append("</tr>");
  }

  private static String hucre(String icerikHtml) {
    return "<td style=\"" + HUCRE + "\">" + icerikHtml + "</td>";
  }

  private static String baslikHucresi(String metin) {
    return "<td style=\"" + BASLIK_HUCRE + "\">" + metin + "</td>";
  }

  private static String renkli(String metin, String renk) {
    return "<span style=\"color: " + renk + "; font-weight: bold;\">" + escape(metin) + "</span>";
  }

  private static String link(String url, String metin) {
    return "<a href=\"" + escape(url) + "\">" + escape(metin) + "</a>";
  }

  private static String durumRengi(String durum) {
    if ("BASARILI".equals(durum)) {
      return BASARILI_RENGI;
    }
    return "ATLANDI".equals(durum) ? ATLANDI_RENGI : HATALI_RENGI;
  }

  private static String kisalt(String hata) {
    if (hata == null || hata.isBlank()) {
      return "Hata mesajı yok.";
    }
    return hata.length() <= MAX_HATA_UZUNLUGU
        ? hata
        : hata.substring(0, MAX_HATA_UZUNLUGU) + "\n... (devamı ozet.html ve Allure raporunda)";
  }

  private static long dosyaBoyutu(Path dosya) {
    try {
      return Files.size(dosya);
    } catch (IOException e) {
      throw new RuntimeException("Rapor ZIP boyutu okunamadı: " + dosya.toAbsolutePath(), e);
    }
  }

  private static String mb(long bayt) {
    return String.format(Locale.ROOT, "%.1f", bayt / (1024.0 * 1024.0));
  }

  /** Hata mesajlarındaki HTML karakterlerinin mail tasarımını bozmasını engeller. */
  private static String escape(String value) {
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

  /** ZIP adındaki tarihi mailde gösterilecek formata çevirir. */
  private static String raporTarihiniAl(Path raporZip) {

    Matcher matcher = RAPOR_TARIH_DESENI.matcher(raporZip.getFileName().toString());

    if (!matcher.matches()) {
      return "Bilinmiyor";
    }

    return LocalDateTime.parse(matcher.group(1), DOSYA_TARIH_FORMATI).format(MAIL_TARIH_FORMATI);
  }
}
