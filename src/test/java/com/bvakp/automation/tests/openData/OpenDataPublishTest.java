package com.bvakp.automation.tests.openData;

import com.bvakp.automation.api.publicportal.PublicPortalApi;
import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.auth.PortalRol;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Veri Seti Yönetimi yaşam döngüsü testleri.
 *
 * <p>Her test kendi veri setini oluşturur. Testin konusu olmayan ön adımlar (KVKK onayı, yayınlama)
 * yardımcı metotlarla yapılır; her test yalnızca kendi adımını açıkça yazar. Testler birbirinden
 * bağımsızdır ve paralel çalışabilir.
 */
public class OpenDataPublishTest extends AvpBaseTest {

  private static final String KAYNAK_TABLOSU = "d_istasyon";
  private static final String SORUMLU_BIRIM = "Strateji Geliştirme Daire Başkanlığı";
  private static final String KATEGORI = "Altyapı ve Hat";
  private static final String KULLANIM_LISANSI = "Creative Commons Atıf-AynıLisans (CC BY-SA)";
  private static final List<String> MASKELENECEK_KOLONLAR = List.of("istasyon_kaynak_id");

  /** Detay ekranının Genel Bilgi sekmesinde bulunması gereken alanlar. */
  private static final List<String> DETAY_ALANLARI =
      List.of("Açıklama", "Sorumlu Birim", "Kategori", "Versiyon", "Lisans");

  /** Kamuya açık portala yansıma süresi (arama indeksi birkaç saniye geride kalabilir). */
  private static final int PORTAL_BEKLEME_SN = 60;

  /** Tek bir testin kullandığı sayfa ve flow nesneleri. */
  private record TestContext(
      OpenDataPublishPage publishPage,
      OpenDataPublishFlow publishFlow,
      OpenDataCreateFlow createFlow) {}

  /** Oturumu açar, Açık Veri Portalına geçer ve testin kullanacağı nesneleri oluşturur. */
  private TestContext testHazirligi() {

    avpOturumuHazirlama(PortalRol.HAZIRLAYAN);
    new OpenDataPage(page()).acikVeriPortalinaGitme();

    OpenDataPublishPage publishPage = new OpenDataPublishPage(page());

    return new TestContext(
        publishPage, new OpenDataPublishFlow(publishPage), new OpenDataCreateFlow(page()));
  }

  // =====================================================================
  // KVKK
  // =====================================================================

  /** KVKK onayı verilen veri setinin Yayın Onayında durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiKvkkOnaylama() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_kvkk_onay");

    ctx.publishFlow().approveKvkk(veriSeti);
    durumDogrulama(ctx, veriSeti, "Yayın Onayında");

    ReportManager.pass("KVKK onayı sonrası veri seti Yayın Onayında: " + veriSeti);
  }

  /** KVKK onayı reddedilen veri setinin Düzenleme Bekliyor durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiKvkkReddetme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_kvkk_red");

    ctx.publishFlow().rejectKvkk(veriSeti);
    durumDogrulama(ctx, veriSeti, "Düzenleme Bekliyor");

    ReportManager.pass("KVKK reddi sonrası veri seti Düzenleme Bekliyor: " + veriSeti);
  }

  /**
   * KVKK'da reddedilip düzenlenen veri setinin tekrar KVKK Onayında durumuna döndüğünü doğrular.
   */
  @Test
  public void veriSetiKvkkOnayiDuzenleme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_kvkk_duzenleme");

    ctx.publishFlow().rejectKvkk(veriSeti);
    durumDogrulama(ctx, veriSeti, "Düzenleme Bekliyor");

    ctx.publishFlow().editKvkkPendingDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "KVKK Onayında");

    ReportManager.pass("Düzenleme sonrası veri seti tekrar KVKK Onayında: " + veriSeti);
  }

  // =====================================================================
  // Yayın
  // =====================================================================

  /**
   * KVKK onayından geçen veri setinin yayınlandığını, portala aktarıldığını ve kamuya açık portalda
   * göründüğünü doğrular.
   */
  @Test
  public void veriSetiYayinlama() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_yayinlama");
    yayinOnayinaKadarGetirme(ctx, veriSeti);

    ctx.publishFlow().publishDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "Yayında");
    portalAktarimiDogrulama(ctx, veriSeti);
    Assert.assertTrue(
        PublicPortalApi.portaldan().paketGorunenKadarBekleme(veriSeti, PORTAL_BEKLEME_SN),
        "Yayınlanan veri seti kamuya açık portalda bulunamadı: " + veriSeti);

    ReportManager.pass("Veri seti yayınlandı ve portala aktarıldı: " + veriSeti);
  }

  /** Yayını reddedilen veri setinin Düzenleme Bekliyor durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiYayiniReddetme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_yayin_red");
    yayinOnayinaKadarGetirme(ctx, veriSeti);

    ctx.publishFlow().rejectPublication(veriSeti);
    durumDogrulama(ctx, veriSeti, "Düzenleme Bekliyor");

    ReportManager.pass("Yayın reddi sonrası veri seti Düzenleme Bekliyor: " + veriSeti);
  }

  /**
   * Yayında reddedilip düzenlenen veri setinin tekrar KVKK Onayında durumuna döndüğünü doğrular.
   */
  @Test
  public void veriSetiYayinOnayiDuzenleme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_yayin_duzenleme");
    yayinOnayinaKadarGetirme(ctx, veriSeti);

    ctx.publishFlow().rejectPublication(veriSeti);
    durumDogrulama(ctx, veriSeti, "Düzenleme Bekliyor");

    ctx.publishFlow().editPublicationPendingDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "KVKK Onayında");

    ReportManager.pass(
        "Yayın reddi ve düzenleme sonrası veri seti tekrar KVKK Onayında: " + veriSeti);
  }

  /**
   * Yayındaki veri seti manuel güncellenince versiyonun bir arttığını ve portala aktarıldığını
   * doğrular.
   */
  @Test
  public void veriSetiManuelGuncelleme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_manuel_guncelleme");
    yayinaKadarGetirme(ctx, veriSeti);

    int eskiVersiyon =
        Integer.parseInt(ctx.publishPage().getCurrentVersion(veriSeti).replace("v", "").trim());
    int yeniVersiyon = eskiVersiyon + 1;

    ctx.publishFlow().manuallyUpdateDataSet(veriSeti);
    Assert.assertTrue(
        ctx.publishPage().versiyonaGectiMi(veriSeti, yeniVersiyon),
        veriSeti + " manuel güncelleme sonrası v" + yeniVersiyon + " olmadı.");
    portalAktarimiDogrulama(ctx, veriSeti);

    ReportManager.pass(
        "Manuel güncelleme sonrası versiyon arttı ve portala aktarıldı: v"
            + eskiVersiyon
            + " → v"
            + yeniVersiyon);
  }

  // =====================================================================
  // Arşivleme ve silme
  // =====================================================================

  /** Yayındaki veri setinin arşivlendiğini ve kamuya açık portaldan kalktığını doğrular. */
  @Test
  public void veriSetiArsiveAlma() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_arsivleme");
    yayinaKadarGetirme(ctx, veriSeti);

    ctx.publishFlow().archiveDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "Arşivlendi");
    Assert.assertTrue(
        PublicPortalApi.portaldan().paketKalkanaKadarBekleme(veriSeti, PORTAL_BEKLEME_SN),
        "Arşivlenen veri seti hâlâ kamuya açık portalda: " + veriSeti);

    ReportManager.pass("Veri seti arşivlendi: " + veriSeti);
  }

  /** KVKK Onayındaki (yayınlanmamış) veri setinin silinip listeden kalktığını doğrular. */
  @Test
  public void veriSetiYayinOncesiSilme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_yayin_oncesi_silme");

    ctx.publishFlow().deleteBeforePublication(veriSeti);
    Assert.assertTrue(
        ctx.publishPage().listedenKalktiMi(veriSeti),
        "Silinen veri seti listeden kalkmadı: " + veriSeti);

    ReportManager.pass("Veri seti yayın öncesinde silindi: " + veriSeti);
  }

  /**
   * Yayınlanıp arşivlenen veri setinin silinip listeden ve kamuya açık portaldan kalktığını
   * doğrular.
   */
  @Test
  public void veriSetiYayinSonrasiSilme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_yayin_sonrasi_silme");
    yayinaKadarGetirme(ctx, veriSeti);

    // Yayındaki veri seti doğrudan silinemez; önce arşivlenir.
    ctx.publishFlow().archiveDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "Arşivlendi");

    ctx.publishFlow().deleteAfterPublication(veriSeti);
    Assert.assertTrue(
        ctx.publishPage().listedenKalktiMi(veriSeti),
        "Silinen veri seti listeden kalkmadı: " + veriSeti);
    Assert.assertTrue(
        PublicPortalApi.portaldan().paketKalkanaKadarBekleme(veriSeti, PORTAL_BEKLEME_SN),
        "Silinen veri seti hâlâ kamuya açık portalda: " + veriSeti);

    ReportManager.pass("Arşivlenen veri seti silindi: " + veriSeti);
  }

  // =====================================================================
  // Görüntüleme ve arama
  // =====================================================================

  /**
   * Veri seti detay ekranının temel alanlarıyla açıldığını ve Geri Dön ile listeye dönüldüğünü
   * doğrular.
   */
  @Test
  public void veriSetiGoruntuleme() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_goruntuleme");

    ctx.publishFlow().viewDataSet(veriSeti);
    Assert.assertTrue(
        ctx.publishPage().detayEkraniAcikMi(veriSeti),
        "Veri seti detay ekranı açılmadı: " + veriSeti);
    for (String alan : DETAY_ALANLARI) {
      Assert.assertTrue(
          ctx.publishPage().detayAlaniGorunurMu(alan),
          "Detay ekranında '" + alan + "' alanı görüntülenmedi.");
    }

    ctx.publishPage().clickGeriDon();
    Assert.assertTrue(
        ctx.publishPage().veriSetiYonetimiAcikMi(), "Veri Seti Yönetimi ekranına dönülemedi.");

    ReportManager.pass("Veri seti detay ekranı açıldı ve listeye geri dönüldü: " + veriSeti);
  }

  /** Veri seti adıyla aramada ilgili kaydın listede bulunduğunu doğrular. */
  @Test
  public void veriSetiArama() {

    TestContext ctx = testHazirligi();
    String veriSeti = kvkkOnayindaVeriSetiOlusturma(ctx, "otomasyon_arama");

    ctx.publishFlow().searchDataSet(veriSeti);

    Assert.assertTrue(
        ctx.publishPage().isDataSetNameMatching(veriSeti),
        "Aranan veri seti listede bulunamadı: " + veriSeti);

    ReportManager.pass("Aranan veri seti listede bulundu: " + veriSeti);
  }

  // =====================================================================
  // Yardımcılar: ön adımlar ve tekrar eden doğrulamalar
  // =====================================================================

  /**
   * Kaynak ve veri seti oluşturup KVKK onayına gönderir, KVKK Onayında durumunu doğrular ve veri
   * seti adını döner. Her testin ortak başlangıcıdır.
   */
  private String kvkkOnayindaVeriSetiOlusturma(TestContext ctx, String adOnEki) {

    String kaynakAdi = TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");
    String veriSeti = TestDataUtil.dinamikAdOlusturma(adOnEki);

    ctx.createFlow().createSource(kaynakAdi, KAYNAK_TABLOSU, "Otomasyon kaynağı - " + kaynakAdi);
    ctx.createFlow()
        .createDataSetForApproval(
            kaynakAdi,
            veriSeti,
            SORUMLU_BIRIM,
            KATEGORI,
            KULLANIM_LISANSI,
            "Otomasyon veri seti - " + veriSeti,
            MASKELENECEK_KOLONLAR);

    durumDogrulama(ctx, veriSeti, "KVKK Onayında");
    return veriSeti;
  }

  /** KVKK onayı verir ve Yayın Onayında durumunu doğrular. */
  private void yayinOnayinaKadarGetirme(TestContext ctx, String veriSeti) {
    ctx.publishFlow().approveKvkk(veriSeti);
    durumDogrulama(ctx, veriSeti, "Yayın Onayında");
  }

  /** KVKK onayı verip yayınlar; Yayında durumunu ve portala aktarımı doğrular. */
  private void yayinaKadarGetirme(TestContext ctx, String veriSeti) {
    yayinOnayinaKadarGetirme(ctx, veriSeti);
    ctx.publishFlow().publishDataSet(veriSeti);
    durumDogrulama(ctx, veriSeti, "Yayında");
    portalAktarimiDogrulama(ctx, veriSeti);
  }

  /** Veri setinin beklenen statüye geçtiğini doğrular; son görülen değer logdadır. */
  private void durumDogrulama(TestContext ctx, String veriSeti, String durum) {
    Assert.assertTrue(
        ctx.publishPage().durumaGectiMi(veriSeti, durum),
        veriSeti + " '" + durum + "' durumuna geçmedi (son görülen değer logda).");
  }

  /** Veri setinin kamuya açık portala aktarıldığını doğrular. */
  private void portalAktarimiDogrulama(TestContext ctx, String veriSeti) {
    Assert.assertTrue(
        ctx.publishPage().portalDurumunaGectiMi(veriSeti, "Aktarıldı"),
        veriSeti + " portala aktarılmadı (son görülen değer logda).");
  }
}
