package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
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
 * <p>Her test kendi veri setini oluşturup KVKK onayına göndererek başlar ({@link
 * #veriSetiOlustur}); sonraki her adım testin içinde açıkça yazılıdır. Testler birbirinden
 * bağımsızdır ve paralel çalışabilir.
 */
public class OpenDataPublishTest extends AvpBaseTest {

  private static final String KAYNAK_TABLOSU = "d_istasyon";
  private static final String SORUMLU_BIRIM = "Strateji Geliştirme Daire Başkanlığı";
  private static final String KATEGORI = "Altyapı ve Hat";
  private static final String KULLANIM_LISANSI = "Creative Commons Atıf-AynıLisans (CC BY-SA)";
  private static final List<String> MASKELENECEK_KOLONLAR = List.of("anket_yapilan_kisi");

  /** Tek bir testin kullandığı sayfa ve flow nesneleri. */
  private record TestContext(
      OpenDataPublishPage publishPage,
      OpenDataPublishFlow publishFlow,
      OpenDataCreateFlow createFlow) {}

  /** Oturumu açar, Açık Veri Portalına geçer ve testin kullanacağı nesneleri oluşturur. */
  private TestContext testHazirligi() {

    avpOturumuHazirlama();
    new OpenDataPage(page()).acikVeriPortalinaGitme();

    OpenDataPublishPage publishPage = new OpenDataPublishPage(page());

    return new TestContext(
        publishPage, new OpenDataPublishFlow(publishPage), new OpenDataCreateFlow(page()));
  }

  /**
   * Her testin ortak başlangıcı: kaynak ve veri seti oluşturup KVKK onayına gönderir, veri seti
   * adını döner.
   */
  private String veriSetiOlustur(TestContext ctx, String adOnEki) {

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

    return veriSeti;
  }

  // =====================================================================
  // KVKK
  // =====================================================================

  /** KVKK onayı verilen veri setinin Yayın Onayında durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiKvkkOnaylama() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_kvkk_onay");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ReportManager.pass("KVKK onayı sonrası veri seti Yayın Onayında: " + veriSeti);
  }

  /** KVKK onayı reddedilen veri setinin Düzenleme Bekliyor durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiKvkkReddetme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_kvkk_red");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().rejectKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Düzenleme Bekliyor");

    ReportManager.pass("KVKK reddi sonrası veri seti Düzenleme Bekliyor: " + veriSeti);
  }

  /**
   * KVKK'da reddedilip düzenlenen veri setinin tekrar KVKK Onayında durumuna döndüğünü doğrular.
   */
  @Test
  public void veriSetiKvkkOnayiDuzenleme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_kvkk_duzenleme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().rejectKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Düzenleme Bekliyor");

    ctx.publishFlow().editKvkkPendingDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ReportManager.pass("Düzenleme sonrası veri seti tekrar KVKK Onayında: " + veriSeti);
  }

  // =====================================================================
  // Yayın
  // =====================================================================

  /** KVKK onayından geçen veri setinin yayınlandığını ve portala aktarıldığını doğrular. */
  @Test
  public void veriSetiYayinlama() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_yayinlama");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().publishDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayında");
    ctx.publishPage().waitForPortalStatus(veriSeti, "Aktarıldı");

    ReportManager.pass("Veri seti yayınlandı ve portala aktarıldı: " + veriSeti);
  }

  /** Yayını reddedilen veri setinin Düzenleme Bekliyor durumuna geçtiğini doğrular. */
  @Test
  public void veriSetiYayiniReddetme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_yayin_red");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().rejectPublication(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Düzenleme Bekliyor");

    ReportManager.pass("Yayın reddi sonrası veri seti Düzenleme Bekliyor: " + veriSeti);
  }

  /**
   * Yayında reddedilip düzenlenen veri setinin tekrar KVKK Onayında durumuna döndüğünü doğrular.
   */
  @Test
  public void veriSetiYayinOnayiDuzenleme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_yayin_duzenleme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().rejectPublication(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Düzenleme Bekliyor");

    ctx.publishFlow().editPublicationPendingDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

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

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_manuel_guncelleme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().publishDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayında");
    ctx.publishPage().waitForPortalStatus(veriSeti, "Aktarıldı");

    int eskiVersiyon =
        Integer.parseInt(ctx.publishPage().getCurrentVersion(veriSeti).replace("v", "").trim());
    int yeniVersiyon = eskiVersiyon + 1;

    ctx.publishFlow().manuallyUpdateDataSet(veriSeti);
    ctx.publishPage().waitForVersion(veriSeti, yeniVersiyon);
    ctx.publishPage().waitForPortalStatus(veriSeti, "Aktarıldı");

    ReportManager.pass(
        "Manuel güncelleme sonrası versiyon arttı ve portala aktarıldı: v"
            + eskiVersiyon
            + " → v"
            + yeniVersiyon);
  }

  // =====================================================================
  // Arşivleme ve silme
  // =====================================================================

  /** Yayındaki veri setinin arşivlendiğini (Arşivlendi durumu) doğrular. */
  @Test
  public void veriSetiArsiveAlma() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_arsivleme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().publishDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayında");
    ctx.publishPage().waitForPortalStatus(veriSeti, "Aktarıldı");

    ctx.publishFlow().archiveDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Arşivlendi");

    ReportManager.pass("Veri seti arşivlendi: " + veriSeti);
  }

  /** KVKK Onayındaki (yayınlanmamış) veri setinin silinip listeden kalktığını doğrular. */
  @Test
  public void veriSetiYayinOncesiSilme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_yayin_oncesi_silme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().deleteBeforePublication(veriSeti);
    ctx.publishPage().waitForDataSetRemoval(veriSeti);

    ReportManager.pass("Veri seti yayın öncesinde silindi: " + veriSeti);
  }

  /** Yayınlanıp arşivlenen veri setinin silinip listeden kalktığını doğrular. */
  @Test
  public void veriSetiYayinSonrasiSilme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_yayin_sonrasi_silme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().approveKvkk(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayın Onayında");

    ctx.publishFlow().publishDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Yayında");
    ctx.publishPage().waitForPortalStatus(veriSeti, "Aktarıldı");

    // Yayındaki veri seti doğrudan silinemez; önce arşivlenir.
    ctx.publishFlow().archiveDataSet(veriSeti);
    ctx.publishPage().waitForStatus(veriSeti, "Arşivlendi");

    ctx.publishFlow().deleteAfterPublication(veriSeti);
    ctx.publishPage().waitForDataSetRemoval(veriSeti);

    ReportManager.pass("Arşivlenen veri seti silindi: " + veriSeti);
  }

  // =====================================================================
  // Görüntüleme ve arama
  // =====================================================================

  /** Veri seti detay ekranının açıldığını ve Geri Dön ile listeye dönüldüğünü doğrular. */
  @Test
  public void veriSetiGoruntuleme() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_goruntuleme");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().viewDataSet(veriSeti);
    ctx.publishPage().waitForDetailScreen(veriSeti);

    ctx.publishPage().clickGeriDon();
    ctx.publishPage().waitForVeriSetiYonetimi();

    ReportManager.pass("Veri seti detay ekranı açıldı ve listeye geri dönüldü: " + veriSeti);
  }

  /** Veri seti adıyla aramada ilgili kaydın listede bulunduğunu doğrular. */
  @Test
  public void veriSetiArama() {

    TestContext ctx = testHazirligi();

    String veriSeti = veriSetiOlustur(ctx, "otomasyon_arama");
    ctx.publishPage().waitForStatus(veriSeti, "KVKK Onayında");

    ctx.publishFlow().searchDataSet(veriSeti);

    Assert.assertTrue(
        ctx.publishPage().isDataSetNameMatching(veriSeti),
        "Aranan veri seti listede bulunamadı: " + veriSeti);

    ReportManager.pass("Aranan veri seti listede bulundu: " + veriSeti);
  }
}
