package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.model.YayinAyarlari;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage.DuzenlemeAlanlari;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;

/**
 * Var olan bir veri seti üzerinde Veri Seti Yönetimi'ndeki tek bir iş adımını yapar: KVKK onay/red,
 * yayınlama/yayın reddi, düzenleme, arşivleme, silme, görüntüleme, arama, güncelleme.
 *
 * <p>Her metot işlemi yapar ve işlemin bildirimini bekler; sonrasında oluşan statü değişikliğini
 * doğrulamak testin işidir.
 */
public class OpenDataPublishFlow {

  private static final String DUZENLEME_SORUMLU_BIRIM = "Yolcu Taşımacılığı Daire Başkanlığı";
  private static final String DUZENLEME_KATEGORI = "İstatistik";
  private static final String YAYIN_ACIKLAMASI =
      "Otomasyon testi kapsamında yayınlama işlemi gerçekleştirilmiştir.";

  private final OpenDataPublishPage publishPage;

  public OpenDataPublishFlow(OpenDataPublishPage publishPage) {
    this.publishPage = publishPage;
  }

  // =====================================================================
  // KVKK ve yayın onay süreçleri
  // =====================================================================

  /** Veri setine KVKK onayı verir ve bildirimi bekler. */
  public void approveKvkk(String dataSetName) {

    ReportManager.step("KVKK onayı veriliyor: " + dataSetName);

    publishPage.clickKvkkOnayla(dataSetName);
    publishPage.enterKvkkAciklama("Otomasyon testi kapsamında KVKK onayı verilmiştir.");
    publishPage.confirmKvkkOnay();
    publishPage.bildirimBekleme("Veri seti yayın onay kuyruğuna gönderildi");
  }

  /** Veri setinin KVKK onayını reddeder ve bildirimi bekler. */
  public void rejectKvkk(String dataSetName) {

    ReportManager.step("KVKK reddediliyor: " + dataSetName);

    publishPage.clickKvkkReddet(dataSetName);
    publishPage.enterKvkkRedAciklama("Otomasyon testi kapsamında KVKK reddi verilmiştir.");
    publishPage.confirmKvkkRed();
    publishPage.bildirimBekleme("Veri seti düzenlenmek üzere geri gönderildi");
  }

  /** Veri setinin yayınını reddeder ve bildirimi bekler. */
  public void rejectPublication(String dataSetName) {

    ReportManager.step("Yayın reddediliyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickYayinReddet(dataSetName);
    publishPage.enterYayinRedAciklama("Otomasyon testi kapsamında yayın reddedilmiştir.");
    publishPage.confirmYayinRed();
    publishPage.bildirimBekleme("Veri seti düzenlenmek üzere geri gönderildi");
  }

  // =====================================================================
  // Düzenleme
  // =====================================================================

  /**
   * KVKK reddi sonrası "Düzenleme Bekliyor" durumundaki veri setini düzenleyip tekrar onaya
   * gönderir.
   */
  public void editKvkkPendingDataSet(String dataSetName) {
    veriSetiniDuzenleVeOnayaGonder(dataSetName, "Güncellenmiş test veri seti açıklaması");
  }

  /**
   * Yayın reddi sonrası "Düzenleme Bekliyor" durumundaki veri setini düzenleyip tekrar onaya
   * gönderir.
   */
  public void editPublicationPendingDataSet(String dataSetName) {
    veriSetiniDuzenleVeOnayaGonder(
        dataSetName, "Yayın reddi sonrası güncellenmiş test veri seti açıklaması");
  }

  /**
   * Sorumlu birim, kategori ve açıklamayı değiştirir, kaydeder, değerleri doğrular, değişikliği
   * onaya gönderir ve listeye geri döner.
   */
  private void veriSetiniDuzenleVeOnayaGonder(String dataSetName, String yeniAciklama) {

    ReportManager.step("Veri seti düzenleniyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickDuzenle(dataSetName);

    publishPage.selectDropdown("Sorumlu Birim", DUZENLEME_SORUMLU_BIRIM);
    publishPage.selectDropdown("Kategori", DUZENLEME_KATEGORI);
    publishPage.enterDuzenlemeAciklama(yeniAciklama);

    publishPage.clickDuzenlemeKaydet();
    publishPage.bildirimBekleme("Veri seti güncellendi");

    DuzenlemeAlanlari alanlar = publishPage.duzenlemeAlanlariniOkuma();
    Assert.assertEquals(
        alanlar.sorumluBirim(), DUZENLEME_SORUMLU_BIRIM, "Ön koşul: Sorumlu Birim kaydedilmedi.");
    Assert.assertEquals(alanlar.kategori(), DUZENLEME_KATEGORI, "Ön koşul: Kategori kaydedilmedi.");
    Assert.assertEquals(alanlar.aciklama(), yeniAciklama, "Ön koşul: Açıklama kaydedilmedi.");

    ReportManager.step("Değişiklikler onaya gönderiliyor: " + dataSetName);

    publishPage.clickDegisiklikOnayaGonder();
    publishPage.confirmDegisiklikOnayaGonder();
    publishPage.bildirimBekleme("Veri seti onay kuyruğuna gönderildi.");

    publishPage.clickGeriDon();
    Assert.assertTrue(
        publishPage.veriSetiYonetimiAcikMi(), "Ön koşul: Veri Seti Yönetimi ekranına dönülemedi.");
  }

  // =====================================================================
  // Arşivleme ve silme
  // =====================================================================

  /** Yayındaki veri setini arşivler ve bildirimi bekler. */
  public void archiveDataSet(String dataSetName) {

    ReportManager.step("Veri seti arşivleniyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickArsivle(dataSetName);
    publishPage.confirmArsivle();
    publishPage.bildirimBekleme("Veri seti arşivlendi");
  }

  /** Yayınlanmadan önce veri setini siler. */
  public void deleteBeforePublication(String dataSetName) {
    veriSetiniSil(dataSetName);
  }

  /** Arşivlendikten sonra veri setini siler. */
  public void deleteAfterPublication(String dataSetName) {
    veriSetiniSil(dataSetName);
  }

  /** Veri setini siler ve bildirimi bekler. */
  private void veriSetiniSil(String dataSetName) {

    ReportManager.step("Veri seti siliniyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickSil(dataSetName);
    publishPage.confirmSil();
    publishPage.bildirimBekleme("Veri seti silindi");
  }

  // =====================================================================
  // Görüntüleme, arama, güncelleme
  // =====================================================================

  /** Veri setinin detay ekranını açar. */
  public void viewDataSet(String dataSetName) {

    ReportManager.step("Veri seti detayı açılıyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickGoruntule(dataSetName);
  }

  /** Veri Seti Yönetimi listesinde veri setini arar. */
  public void searchDataSet(String dataSetName) {

    ReportManager.step("Veri seti aranıyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.searchDataSet(dataSetName);
  }

  /** Veri setinin manuel güncellemesini başlatır ve bildirimi bekler. */
  public void manuallyUpdateDataSet(String dataSetName) {

    ReportManager.step("Veri seti manuel güncelleniyor: " + dataSetName);

    publishPage.clickPublishTab();
    publishPage.clickGuncelle(dataSetName);
    publishPage.confirmGuncelle();
    publishPage.bildirimBekleme("Güncelleme başlatıldı");
  }

  /**
   * Veri setini yayınlar; yayınla butonuna basmadan önce penceredeki ayarları (formatlar, Data API,
   * KVKK notu) okuyup döndürür.
   */
  public YayinAyarlari publishDataSet(String dataSetName) {

    ReportManager.step("Veri seti yayınlanıyor: " + dataSetName);

    publishPage.clickYayinla(dataSetName);
    publishPage.enterYayinAciklama(YAYIN_ACIKLAMASI);

    YayinAyarlari ayarlar = publishPage.yayinAyarlariniOkuma(YAYIN_ACIKLAMASI);

    publishPage.confirmYayinla();
    publishPage.bildirimBekleme("Veri seti yayınlandı");

    return ayarlar;
  }
}
