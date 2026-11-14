package com.bvakp.automation.utils;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.auth.PortalRol;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage.VeriSetiSatiri;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Otomasyonun kapalı portaldaki Açık Veri Portalı'nda bıraktığı veri setlerini ve kaynakları siler.
 *
 * <p>Silinecek kayıt: adı cleanup.name.prefix (varsayılan "otomasyon_") ile başlar (büyük/küçük
 * harf duyarlı) ve adındaki tarih (TestDataUtil.dinamikAdOlusturma: ..._yyyyMMdd_HHmmss_SSS)
 * cleanup.min.age.hours saatten eskidir. Adından tarih okunamayan kayda dokunulmaz. Böylece o
 * sırada çalışan koşuların verisi silinmez; her koşu önceki günlerin artığını temizler.
 *
 * <p>Sıra: önce veri setleri (Yayında olan önce arşivlenir), sonra kaynaklar (kaynak, bağlı veri
 * seti kalmadığında silinebilir). Kayıtlar en eskiden başlanarak silinir. Silinemeyen kayıt
 * atlanır, temizlik durmaz; sonunda özet yazılır.
 *
 * <p>Güvenlik: base.url'in host'u cleanup.allowed.hosts listesinde değilse hiçbir şey silinmez.
 * Bütün işlemler TEMIZLIK rolünün kullanıcısıyla yapılır.
 *
 * <p>Ayarlar: cleanup.enabled, cleanup.allowed.hosts, cleanup.name.prefix, cleanup.min.age.hours
 */
public class OtomasyonKaynakTemizleyici extends AvpBaseTest {

  private static final String VARSAYILAN_ON_EK = "otomasyon_";
  private static final int VARSAYILAN_MINIMUM_SAAT = 48;

  private static final String YAYINDA = "Yayında";

  /** Sil butonu normal statüde hemen görünür; arşivden sonra statü değişimi beklenir. */
  private static final int SIL_BUTONU_TIMEOUT_MS = 10_000;

  private static final int ARSIV_SONRASI_TIMEOUT_MS = 120_000;

  /**
   * Addaki zaman damgası: otomasyon_kaynak_20261114_232219_575 → 20261114_232219. Damgadan sonra ek
   * olabilir (ör. düzenleme testi: ..._232030_320_duzenlendi).
   */
  private static final Pattern AD_TARIHI = Pattern.compile("_(\\d{8}_\\d{6})_\\d{3}(?=_|$)");

  private static final DateTimeFormatter AD_TARIH_BICIMI =
      DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  private static final DateTimeFormatter LOG_TARIH_BICIMI =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  /** Manuel çalıştırma: cleanup.enabled'dan bağımsızdır, ortam kilidi yine geçerlidir. */
  public static void main(String[] args) {
    kosuSonuTemizliginiCalistir();
  }

  /**
   * Temizliği testlerden bağımsız, yeni bir Playwright oturumunda çalıştırır. Ortam izinli değilse
   * tarayıcı açılmadan çıkar.
   */
  public static void kosuSonuTemizliginiCalistir() {

    if (!ortamIzinliMi()) {
      return;
    }

    OtomasyonKaynakTemizleyici temizleyici = new OtomasyonKaynakTemizleyici();

    ReportManager.info("OTOMASYON TEMİZLİĞİ İÇİN YENİ PLAYWRIGHT OTURUMU AÇILIYOR");

    try {
      // Testlerin Page'i kapanmış olabileceği için temizliğe özel oturum açılır.
      PlaywrightManager.initialize(temizleyici.authStateKullan());
      temizleyici.otomasyonKayitlariniTemizle();
    } finally {
      PlaywrightManager.close("otomasyonKaynaklariniTemizleme", "TEMIZLIK");
      ReportManager.info("OTOMASYON TEMİZLİĞİ PLAYWRIGHT OTURUMU KAPATILDI");
    }
  }

  /**
   * base.url'in host'u cleanup.allowed.hosts listesinde mi kontrol eder. Liste boşsa hiçbir ortamda
   * silme yapılmaz.
   */
  private static boolean ortamIzinliMi() {

    String host = URI.create(ConfigManager.get("base.url")).getHost();

    Set<String> izinliHostlar =
        Arrays.stream(ConfigManager.getOrDefault("cleanup.allowed.hosts", "").split(","))
            .map(String::trim)
            .filter(deger -> !deger.isEmpty())
            .map(deger -> deger.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());

    if (host == null || !izinliHostlar.contains(host.toLowerCase(Locale.ROOT))) {
      ReportManager.warn(
          "Temizlik ATLANDI: '"
              + host
              + "' cleanup.allowed.hosts listesinde değil. "
              + "Silme yalnızca izin verilen test ortamlarında yapılır.");
      return false;
    }

    return true;
  }

  /** Önce veri setlerini, sonra kaynakları temizler ve özeti yazar. */
  private void otomasyonKayitlariniTemizle() {

    String onEk = ConfigManager.getOrDefault("cleanup.name.prefix", VARSAYILAN_ON_EK);
    int minimumSaat = minimumSaat();
    LocalDateTime sinir = LocalDateTime.now().minusHours(minimumSaat);

    ReportManager.info(
        "OTOMASYON TEMİZLİĞİ BAŞLADI | Ön ek: '"
            + onEk
            + "' | "
            + minimumSaat
            + " saatten eski kayıtlar ("
            + sinir.format(LOG_TARIH_BICIMI)
            + " öncesi)");

    avpOturumuHazirlama(PortalRol.TEMIZLIK);

    Page aktifPage = page();
    OpenDataPage openDataPage = new OpenDataPage(aktifPage);
    OpenDataPublishPage publishPage = new OpenDataPublishPage(aktifPage);
    OpenDataPublishFlow publishFlow = new OpenDataPublishFlow(publishPage);

    Sayac veriSetleri = veriSetleriniTemizle(aktifPage, publishPage, publishFlow, onEk, sinir);
    Sayac kaynaklar = kaynaklariTemizle(aktifPage, openDataPage, onEk, sinir);

    ReportManager.info(
        "OTOMASYON TEMİZLİĞİ TAMAMLANDI | Veri seti: " + veriSetleri + " | Kaynak: " + kaynaklar);
  }

  // =====================================================================
  // Veri setleri
  // =====================================================================

  /** Eski otomasyon veri setlerini en eskiden başlayarak siler; Yayında olanı önce arşivler. */
  private Sayac veriSetleriniTemizle(
      Page aktifPage,
      OpenDataPublishPage publishPage,
      OpenDataPublishFlow publishFlow,
      String onEk,
      LocalDateTime sinir) {

    Sayac sayac = new Sayac();

    aktifPage.navigate(acikVeriAdresi());
    publishPage.clickPublishTab();

    List<VeriSetiSatiri> bulunanlar = publishPage.veriSetleriniListeleme(onEk);
    adlariRaporla("Veri seti", bulunanlar.stream().map(VeriSetiSatiri::ad).toList());

    List<VeriSetiSatiri> silinecekler =
        bulunanlar.stream()
            .filter(satir -> eskiMi(satir.ad(), sinir))
            .sorted(Comparator.comparing(satir -> adTarihi(satir.ad()).orElseThrow()))
            .toList();

    ReportManager.info("Silinecek veri seti sayısı: " + silinecekler.size());

    for (VeriSetiSatiri satir : silinecekler) {
      try {
        int silButonuBekleme = SIL_BUTONU_TIMEOUT_MS;

        if (YAYINDA.equals(satir.durum())) {
          publishFlow.archiveDataSet(satir.ad(), PortalRol.TEMIZLIK);
          silButonuBekleme = ARSIV_SONRASI_TIMEOUT_MS;
        }

        if (!publishPage.silinebilirMi(satir.ad(), silButonuBekleme)) {
          sayac.atla(satir.ad(), "Sil butonu yok (statü: " + satir.durum() + ")");
          continue;
        }

        publishFlow.deleteDataSet(satir.ad(), PortalRol.TEMIZLIK);
        sayac.sil(satir.ad());

      } catch (RuntimeException e) {
        sayac.atla(satir.ad(), e.getMessage());
        ekraniToparla(aktifPage);
      }
    }

    return sayac;
  }

  // =====================================================================
  // Kaynaklar
  // =====================================================================

  /** Eski otomasyon kaynaklarını en eskiden başlayarak siler. */
  private Sayac kaynaklariTemizle(
      Page aktifPage, OpenDataPage openDataPage, String onEk, LocalDateTime sinir) {

    Sayac sayac = new Sayac();

    aktifPage.navigate(acikVeriAdresi());

    List<String> bulunanlar = openDataPage.kaynakAdlariniListeleme(onEk);
    adlariRaporla("Kaynak", bulunanlar);

    List<String> silinecekler =
        bulunanlar.stream()
            .filter(ad -> eskiMi(ad, sinir))
            .sorted(Comparator.comparing(ad -> adTarihi(ad).orElseThrow()))
            .toList();

    ReportManager.info("Silinecek kaynak sayısı: " + silinecekler.size());

    for (String ad : silinecekler) {
      try {
        if (openDataPage.kaynakSilme(ad)) {
          sayac.sil(ad);
        } else {
          sayac.atla(ad, "listeden kalkmadı (bağlı veri seti olabilir)");
          ekraniToparla(aktifPage);
        }
      } catch (RuntimeException e) {
        sayac.atla(ad, e.getMessage());
        ekraniToparla(aktifPage);
      }
    }

    return sayac;
  }

  // =====================================================================
  // Yardımcılar
  // =====================================================================

  /**
   * Bulunan kayıtların tarih aralığını ve adından tarih okunamayanları loga yazar. Tarihi
   * okunamayan kayda dokunulmaz; bu log, neden silinmediğini görmek içindir.
   */
  private static void adlariRaporla(String tur, List<String> adlar) {

    List<LocalDateTime> tarihler =
        adlar.stream()
            .map(OtomasyonKaynakTemizleyici::adTarihi)
            .flatMap(Optional::stream)
            .sorted()
            .toList();

    if (!tarihler.isEmpty()) {
      ReportManager.info(
          tur
              + " tarih aralığı | En eski: "
              + tarihler.get(0).format(LOG_TARIH_BICIMI)
              + " | En yeni: "
              + tarihler.get(tarihler.size() - 1).format(LOG_TARIH_BICIMI));
    }

    List<String> tarihsizler = adlar.stream().filter(ad -> adTarihi(ad).isEmpty()).toList();
    if (!tarihsizler.isEmpty()) {
      ReportManager.warn(
          tur
              + " adından tarih okunamadı, dokunulmayacak ("
              + tarihsizler.size()
              + "): "
              + String.join(", ", tarihsizler));
    }
  }

  /** Adındaki tarih sınırdan eskiyse true; tarih okunamazsa false (kayda dokunulmaz). */
  private static boolean eskiMi(String ad, LocalDateTime sinir) {
    return adTarihi(ad).map(tarih -> tarih.isBefore(sinir)).orElse(false);
  }

  /** Ad sonundaki zaman damgasını okur: ..._20261114_232219_575 → 14.11.2026 23:22:19. */
  private static Optional<LocalDateTime> adTarihi(String ad) {
    Matcher eslesme = AD_TARIHI.matcher(ad);
    if (!eslesme.find()) {
      return Optional.empty();
    }
    try {
      return Optional.of(LocalDateTime.parse(eslesme.group(1), AD_TARIH_BICIMI));
    } catch (DateTimeParseException e) {
      return Optional.empty();
    }
  }

  /** Kapalı portaldaki Açık Veri Portalı adresi (ör. http://host:port/opendata). */
  private static String acikVeriAdresi() {
    return URI.create(ConfigManager.get("base.url")).resolve("opendata").toString();
  }

  /**
   * Hata sonrası açık kalan pencereyi kapatıp Açık Veri Portalı'na döner; sonraki kayıt temiz
   * başlar.
   */
  private static void ekraniToparla(Page aktifPage) {
    try {
      aktifPage.keyboard().press("Escape");
      aktifPage.navigate(acikVeriAdresi());
    } catch (RuntimeException e) {
      ReportManager.warn("Temizlik ekranı toparlanamadı: " + e.getMessage());
    }
  }

  /** cleanup.min.age.hours ayarını okur; geçersiz veya 1'den küçükse varsayılanı döndürür. */
  private static int minimumSaat() {
    String deger =
        ConfigManager.getOrDefault(
            "cleanup.min.age.hours", String.valueOf(VARSAYILAN_MINIMUM_SAAT));
    try {
      int saat = Integer.parseInt(deger.trim());
      return saat >= 1 ? saat : VARSAYILAN_MINIMUM_SAAT;
    } catch (NumberFormatException e) {
      return VARSAYILAN_MINIMUM_SAAT;
    }
  }

  /** Bir ekrandaki silinen ve atlanan kayıt sayısı; her kayıt loga yazılır. */
  private static final class Sayac {

    private int silinen;
    private int atlanan;

    /** Silinen kaydı sayar ve loga yazar. */
    void sil(String ad) {
      silinen++;
      ReportManager.info("Temizlik: silindi | " + ad);
    }

    /** Atlanan kaydı sebebiyle sayar ve loga yazar. */
    void atla(String ad, String sebep) {
      atlanan++;
      ReportManager.warn("Temizlik: atlandı | " + ad + " | " + sebep);
    }

    @Override
    public String toString() {
      return silinen + " silindi, " + atlanan + " atlandı";
    }
  }
}
