package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.components.KaynakEkleModal;
import com.bvakp.automation.core.auth.AvpOturum;
import com.bvakp.automation.core.auth.Rol;
import com.bvakp.automation.model.KolonTanimi;
import com.bvakp.automation.model.VeriSetiTanimi;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;
import java.util.List;
import java.util.Map;
import org.testng.Assert;

/**
 * Açık Veri Portalı'nda kaynak oluşturma ve veri setini KVKK onayına gönderme adımları.
 *
 * <p>Testlerin ön koşulu olarak kullanılır. Kaynak oluşturmanın ve veri setini onaya göndermenin
 * ayrıntılı testleri OpenDataCreateTest'tedir; o testler bu flow'u kullanmaz.
 *
 * <p>Bütün adımlar hazırlayan rolünün kullanıcısıyla yapılır (bkz. {@link Rol}).
 */
public class OpenDataCreateFlow {

  private final Page page;
  private final OpenDataPage openDataPage;
  private final KaynakEkleModal kaynakEkleModal;

  public OpenDataCreateFlow(Page page) {
    this.page = page;
    this.openDataPage = new OpenDataPage(page);
    this.kaynakEkleModal = new KaynakEkleModal(page);
  }

  /** Gerekirse hazırlayan kullanıcıya geçer; kullanıcı değişirse Açık Veri Portalı'na döner. */
  private void hazirlayanIleDevamEtme() {
    if (AvpOturum.rolIleGirisYapma(page, Rol.HAZIRLAYAN)) {
      openDataPage.acikVeriPortalinaGitme();
    }
  }

  /** Kaynak Ekle formunu doldurarak yeni kaynak oluşturur ve listede göründüğünü doğrular. */
  public void createSource(String kaynakAdi, String tabloAdi, String aciklama) {

    ReportManager.step("Ön koşul: kaynak oluşturuluyor | " + kaynakAdi + " | Tablo: " + tabloAdi);
    hazirlayanIleDevamEtme();

    openDataPage.kaynakEklemeEkraniniAcma();
    Assert.assertTrue(
        kaynakEkleModal.kaynakEkleFormuGoruntulendiMi(), "Ön koşul: Kaynak Ekle formu açılamadı.");

    kaynakEkleModal.kaynakAdiGirme(kaynakAdi);
    kaynakEkleModal.kaynakTablosuSecme(tabloAdi);
    kaynakEkleModal.kaynakAciklamasiGirme(aciklama);

    Assert.assertTrue(
        kaynakEkleModal.veriOnizlemeGoruntulendiMi(), "Ön koşul: Veri Önizleme görüntülenemedi.");

    kaynakEkleModal.kaynakEkleme();

    Assert.assertTrue(
        kaynakEkleModal.kaynakEkleFormuKapandiMi(),
        "Ön koşul: Kaynak oluşturulduktan sonra form kapanmadı.");
    Assert.assertTrue(
        openDataPage.kaynakListedeMi(kaynakAdi),
        "Ön koşul: Kaynak listede bulunamadı: " + kaynakAdi);

    ReportManager.info("Ön koşul kaynağı hazır | " + kaynakAdi);
  }

  /**
   * Verilen kaynaktan veri seti oluşturur (verilen kolonlara maskeleme uygular), KVKK onayına
   * gönderir ve ekranlardan toplanan değerleri döndürür.
   *
   * <p>Dönüş değeri isteğe bağlıdır; sadece onaya göndermek isteyen çağıranlar yok sayabilir.
   *
   * @param maskelenecekKolonlar Maskele kuralı uygulanacak kolonlar; en az bir kolon olmalı
   * @return veri setinin girilen ve ekranlardan okunan değerleri (yayın ayarları henüz yok)
   */
  public VeriSetiTanimi createDataSetForApproval(
      String kaynakAdi,
      String veriSetiAdi,
      String sorumluBirim,
      String kategori,
      String kullanimLisansi,
      String aciklama,
      List<String> maskelenecekKolonlar) {

    // Maskesiz veri setinde maskeleme testleri hiçbir şey kontrol etmeden geçer.
    Assert.assertFalse(
        maskelenecekKolonlar.isEmpty(), "Ön koşul: en az bir maskelenecek kolon verilmeli.");

    ReportManager.step(
        "Ön koşul: veri seti oluşturulup KVKK onayına gönderiliyor | "
            + veriSetiAdi
            + " | Maskelenecek: "
            + maskelenecekKolonlar);
    hazirlayanIleDevamEtme();

    openDataPage.kaynakSecme(kaynakAdi);
    openDataPage.veriSetiIleriGitme();

    for (String kolon : maskelenecekKolonlar) {
      openDataPage.kolonaMaskelemeKuraliUygulama(kolon);
    }
    List<KolonTanimi> kolonlar = openDataPage.alanTanimlariniOkuma();

    openDataPage.veriSetiAdiGirme(veriSetiAdi);
    openDataPage.sorumluBirimSecme(sorumluBirim);
    openDataPage.kategoriSecme(kategori);
    openDataPage.kullanimLisansiSecme(kullanimLisansi);
    openDataPage.veriSetiAciklamasiGirme(aciklama);

    openDataPage.veriSetiOnizleme();
    List<Map<String, String>> onizlemeKayitlari = openDataPage.onizlemeKayitlariniOkuma();

    openDataPage.veriSetiOnayaGonderme();
    openDataPage.onayaGondermeyiOnaylama();
    openDataPage.bildirimBekleme("Veri seti oluşturuldu ve KVKK onay kuyruğuna gönderildi");

    return new VeriSetiTanimi(
        veriSetiAdi,
        aciklama,
        kaynakAdi,
        sorumluBirim,
        kategori,
        kullanimLisansi,
        kolonlar,
        onizlemeKayitlari,
        null);
  }
}
