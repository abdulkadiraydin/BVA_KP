package com.bvakp.automation.api.publicportal;

import com.bvakp.automation.api.core.ApiClient;
import com.bvakp.automation.api.core.ApiYanit;
import com.bvakp.automation.core.config.ConfigManager;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

/**
 * Kamuya açık Açık Veri Portalı'nın CKAN Action API'si (/api/3/action/...).
 *
 * <p>Ham yanıt dönen metotlar ({@link ApiYanit}) durum kodu ve gövde testleri içindir; tipli
 * metotlar karşılaştırma testleri içindir. Doğrulama (assert) testlerde yapılır.
 */
public class PublicPortalApi {

  private static final String ACTION = "/api/3/action/";

  /** Sistem alanı; veri sözlüğünde ve karşılaştırmalarda yer almaz. */
  private static final String SISTEM_ALANI = "_id";

  /** Portal yoklama aralığı; API'de beklenecek bir olay olmadığı için belirli aralıkla sorulur. */
  private static final long YOKLAMA_ARALIGI_MS = 3_000;

  private final ApiClient client;

  public PublicPortalApi(ApiClient client) {
    this.client = client;
  }

  /** Taban adresi config'teki public.data.url olan, aktif test oturumunu kullanan istemci. */
  public static PublicPortalApi portaldan() {
    return new PublicPortalApi(ApiClient.aktifOturumdan(ConfigManager.get("public.data.url")));
  }

  // =====================================================================
  // Ham çağrılar
  // =====================================================================
  /**
   * Kullanıcının API anahtar listesini, verilen anahtarla kimlik doğrulayarak ister. Anahtar
   * geçerliyse başarılı, iptal edilmişse yetki hatası döner.
   */
  public ApiYanit anahtarIleAnahtarListesiGetir(String apiAnahtari, String kullaniciAdi) {
    return client.get(
        ACTION + "api_token_list",
        Map.of("user_id", kullaniciAdi),
        Map.of("Authorization", apiAnahtari));
  }

  /**
   * @param idVeyaAd veri setinin id'si veya URL'deki adı (slug)
   */
  public ApiYanit paketGetir(String idVeyaAd) {
    return client.get(ACTION + "package_show", Map.of("id", idVeyaAd));
  }

  /** package_search ile paket arar. */
  public ApiYanit paketAra(String sorgu, int adet) {
    return client.get(ACTION + "package_search", Map.of("q", sorgu, "rows", adet));
  }

  /** datastore_search ile kaynağın kayıtlarını sayfa sayfa getirir. */
  public ApiYanit datastoreAra(String resourceId, int limit, int offset) {
    return client.get(
        ACTION + "datastore_search",
        Map.of("resource_id", resourceId, "limit", limit, "offset", offset));
  }

  /** CKAN başarı ölçütü: HTTP 2xx ve gövdede "success": true. */
  public static boolean apiBasariliMi(ApiYanit yanit) {
    return yanit.basariliMi() && yanit.alan("/success").asBoolean(false);
  }

  // =====================================================================
  // Tipli yardımcılar
  // =====================================================================

  /**
   * Başlığı (veri seti adı) tam eşleşen veri setini arar. CKAN araması bulanık eşleştiği için ilk
   * sonuç değil, başlığı birebir aynı olan sonuç seçilir.
   */
  public Optional<JsonNode> paketBaslikIleBul(String baslik) {

    ApiYanit yanit = paketAra("\"" + baslik + "\"", 50);
    basariliOlmali(yanit);

    for (JsonNode paket : yanit.alan("/result/results")) {
      if (baslik.equals(paket.path("title").asText())) {
        return Optional.of(paket);
      }
    }
    return Optional.empty();
  }

  /**
   * Veri seti aramada görünene ve verisi DataStore'a tamamen yüklenene kadar bekler; süre dolarsa
   * hata verir. Yayından hemen sonra kaynak aktif görünse de satırlar arkadan, parça parça
   * yüklenir; bu yüzden kayıt sayısı sıfırdan büyük olup iki yoklamada aynı kalana kadar beklenir.
   */
  public JsonNode paketHazirOlanaKadarBekleme(String baslik, int sureSn) {

    long bitis = System.currentTimeMillis() + sureSn * 1000L;
    int oncekiToplam = -1;

    while (true) {
      Optional<JsonNode> paket = paketBaslikIleBul(baslik);
      Optional<String> kaynakId = paket.flatMap(PublicPortalApi::datastoreKaynakId);
      int toplam = kaynakId.map(this::kayitSayisiOkuma).orElse(0);

      if (toplam > 0 && toplam == oncekiToplam) {
        return paket.get();
      }
      if (System.currentTimeMillis() > bitis) {
        throw new IllegalStateException(
            "Veri seti "
                + sureSn
                + " sn içinde portalda hazır olmadı: "
                + baslik
                + hazirOlmamaSebebi(paket, kaynakId, toplam));
      }
      oncekiToplam = toplam;
      yoklamaAraligiBekleme();
    }
  }

  /** Kaynağın DataStore'daki kayıt sayısı; yükleme sürerken veya çağrı başarısızsa 0 döner. */
  private int kayitSayisiOkuma(String kaynakId) {
    ApiYanit yanit = datastoreAra(kaynakId, 1, 0);
    return apiBasariliMi(yanit) ? yanit.alan("/result/total").asInt(0) : 0;
  }

  /** Bekleme süresi dolduğunda hata mesajına eklenecek sebep. */
  private static String hazirOlmamaSebebi(
      Optional<JsonNode> paket, Optional<String> kaynakId, int toplam) {
    if (paket.isEmpty()) {
      return " (paket yok)";
    }
    if (kaynakId.isEmpty()) {
      return " (paket var, DataStore kaynağı yok)";
    }
    return " (kayıtlar yüklenmedi ya da yükleme sürüyor, son kayıt sayısı: " + toplam + ")";
  }

  /** Başlığı verilen veri seti süre içinde portal aramasında görünürse true döner. */
  public boolean paketGorunenKadarBekleme(String baslik, int sureSn) {
    return kosulOlanaKadarBekleme(() -> paketBaslikIleBul(baslik).isPresent(), sureSn);
  }

  /**
   * Başlığı verilen veri seti süre içinde portal aramasından kalkarsa true döner. Arşivleme ve
   * silme sonrası kontrol içindir.
   */
  public boolean paketKalkanaKadarBekleme(String baslik, int sureSn) {
    return kosulOlanaKadarBekleme(() -> paketBaslikIleBul(baslik).isEmpty(), sureSn);
  }

  /** Koşul sağlanana kadar portalı yoklar; süre dolarsa false döner. */
  private static boolean kosulOlanaKadarBekleme(BooleanSupplier kosul, int sureSn) {
    long bitis = System.currentTimeMillis() + sureSn * 1000L;
    while (!kosul.getAsBoolean()) {
      if (System.currentTimeMillis() > bitis) {
        return false;
      }
      yoklamaAraligiBekleme();
    }
    return true;
  }

  /** İki portal sorgusu arasında bekler. */
  @SuppressWarnings("java:S2925") // Bilinçli: portalda beklenecek bir sayfa/olay yok.
  private static void yoklamaAraligiBekleme() {
    try {
      Thread.sleep(YOKLAMA_ARALIGI_MS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Portal beklemesi kesildi.", e);
    }
  }

  /** Paketteki DataStore'u aktif ilk kaynağın id'si (önizleme ve API bu kaynağı kullanır). */
  public static Optional<String> datastoreKaynakId(JsonNode paket) {
    for (JsonNode kaynak : paket.path("resources")) {
      if (kaynak.path("datastore_active").asBoolean(false)) {
        return Optional.of(kaynak.path("id").asText());
      }
    }
    return Optional.empty();
  }

  /** İndirme linkini çağırır (ör. /datastore/dump/{id}?format=csv). */
  public ApiYanit dosyaIndirme(String link) {
    return client.get(link);
  }

  /** Kaynaktaki kayıtları ve alan tanımlarını tipli olarak döndürür. */
  public DatastoreSonuc kayitlariGetir(String resourceId, int limit) {

    ApiYanit yanit = datastoreAra(resourceId, limit, 0);
    basariliOlmali(yanit);

    JsonNode sonuc = yanit.alan("/result");

    Map<String, String> alanlar = new LinkedHashMap<>();
    for (JsonNode alan : sonuc.path("fields")) {
      String ad = alan.path("id").asText();
      if (!SISTEM_ALANI.equals(ad)) {
        alanlar.put(ad, alan.path("type").asText());
      }
    }

    List<Map<String, String>> kayitlar = new ArrayList<>();
    for (JsonNode kayit : sonuc.path("records")) {
      Map<String, String> satir = new LinkedHashMap<>();
      for (String ad : alanlar.keySet()) {
        JsonNode deger = kayit.path(ad);
        satir.put(ad, deger.isNull() || deger.isMissingNode() ? null : deger.asText());
      }
      kayitlar.add(satir);
    }

    return new DatastoreSonuc(sonuc.path("total").asInt(), alanlar, kayitlar);
  }

  /**
   * Tipli yardımcıların ön koşulu: çağrı başarısızsa sebebiyle durur. Ham yanıtı test etmek isteyen
   * testler bunun yerine ham metotları kullanıp kendisi doğrular.
   */
  private static void basariliOlmali(ApiYanit yanit) {
    if (!apiBasariliMi(yanit)) {
      throw new IllegalStateException(
          "Portal API çağrısı başarısız | "
              + yanit.metot()
              + " "
              + yanit.url()
              + " | Durum: "
              + yanit.durumKodu()
              + " | Gövde: "
              + yanit.kisaGovde());
    }
  }
}
