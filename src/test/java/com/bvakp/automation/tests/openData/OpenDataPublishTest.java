package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
<<<<<<< HEAD
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataCreateFlow;
=======
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;
>>>>>>> rahmi
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenDataPublishTest extends AvpBaseTest {
    private OpenDataPage openDataPage;
    private OpenDataPublishPage publishPage;
    private OpenDataPublishFlow publishFlow;
    private OpenDataCreateFlow OpenDataCreateFlow;

    @BeforeMethod
    public void setUpPublish() {
        avpOturumuHazirlama();

        openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        publishPage = new OpenDataPublishPage(page);

        OpenDataCreateFlow = new OpenDataCreateFlow(page);

        publishFlow = new OpenDataPublishFlow(publishPage);

    }
    @Test
    public void veriSetiKvkkOnaylama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_onay");

        ReportManager.info("KVKK onaylama testi başlatılıyor. Veri seti: " + dataSetName);

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK onay testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onay testi veri seti"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.pass("Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName);

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olduğu doğrulanıyor.");
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.pass("KVKK onaylama testi başarıyla tamamlandı: " + dataSetName);
    }
    @Test
    public void veriSetiKvkkReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_red");

        ReportManager.info(
                "KVKK reddetme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK reddetme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK reddetme testi veri seti"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
        publishFlow.rejectKvkk(dataSetName);

        ReportManager.info(
                "KVKK reddi sonrası veri setinin Düzenleme Bekliyor durumuna geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

        ReportManager.pass(
                "KVKK reddi doğrulandı. Veri seti Düzenleme Bekliyor durumunda: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiYayinlama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayinlama");

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayınlama testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti yayınlama testi"
        );

        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");
        ReportManager.info("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında durumuna geçmesi bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında durumuna geçmesi bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.pass("Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName);

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında durumunda olduğu doğrulanıyor.");
        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "Yayında"),
                "Veri seti Yayında durumuna geçmedi."
        );
        ReportManager.pass("Veri seti Yayında durumunda: " + dataSetName);

        ReportManager.info("Veri setinin portala aktarım durumu doğrulanıyor.");
        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarıldı"),
                "Veri seti portala aktarılmadı."
        );
        ReportManager.pass("Veri seti portala başarıyla aktarıldı: " + dataSetName);
    }
    @Test
    public void veriSetiYayiniReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_red");

        ReportManager.info(
                "Veri seti yayın reddetme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın reddetme testi kaynağı"
        );

        ReportManager.info(
                "Kaynak oluşturma işlemi tamamlandı: " + kaynakAdi
        );

        ReportManager.info(
                "Yayın reddetme testi için veri seti oluşturuluyor: "
                        + dataSetName
        );

        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın reddetme testi veri seti"
        );

        ReportManager.info(
                "Veri seti oluşturma metodu tamamlandı: " + dataSetName
        );
        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
        publishPage.waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");

        publishFlow.approveKvkk(dataSetName);

        ReportManager.info(
                "Veri setinin Yayın Onayında statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.pass(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayın reddetme işlemi başlatılıyor.");

        publishFlow.rejectPublication(dataSetName);

        ReportManager.info(
                "Yayın reddi sonrasında veri setinin Düzenleme Bekliyor "
                        + "statüsünde olduğu doğrulanıyor."
        );

        Assert.assertTrue(
                publishPage.isStatus(
                        dataSetName,
                        "Düzenleme Bekliyor"
                ),
                "Veri seti yayın reddi sonrasında Düzenleme Bekliyor "
                        + "durumuna geçmedi. Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Veri seti yayın reddi sonrasında Düzenleme Bekliyor "
                        + "durumuna geçti: " + dataSetName
        );
    }
    @Test
    public void veriSetiKvkkOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_duzenleme");

        ReportManager.info(
                "KVKK onayı düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK düzenleme testi kaynağı"
        );

        ReportManager.info(
                "Kaynak oluşturma işlemi tamamlandı: " + kaynakAdi
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);

        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
        publishFlow.rejectKvkk(dataSetName);

        ReportManager.info(
                "KVKK reddi sonrasında veri seti düzenleme işlemi başlatılıyor."
        );

        publishFlow.editKvkkPendingDataSet(dataSetName);

        ReportManager.info(
                "Düzenleme sonrasında veri setinin KVKK Onayında "
                        + "statüsünde olduğu doğrulanıyor."
        );

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Veri seti KVKK Onayında durumuna geçmedi. Veri seti: "
                        + dataSetName
        );

        ReportManager.pass(
                "Düzenleme sonrasında veri seti KVKK Onayında durumunda: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiYayinOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_duzenleme");

        ReportManager.info(
                "Yayın onayı sonrası düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın onayı düzenleme testi kaynağı"
        );

        ReportManager.info("Kaynak oluşturma işlemi tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);

        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info("KVKK Onayında statüsü bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info("Yayın Onayında statüsü bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.pass(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Yayın reddetme işlemi başlatılıyor.");
        publishFlow.rejectPublication(dataSetName);

        ReportManager.info(
                "Yayın reddi sonrasında düzenleme işlemi başlatılıyor."
        );
        publishFlow.editPublicationPendingDataSet(dataSetName);

        ReportManager.info(
                "Düzenleme sonrası veri setinin KVKK Onayında olduğu "
                        + "doğrulanıyor."
        );

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Düzenleme sonrası veri seti KVKK Onayında durumuna geçmedi. "
                        + "Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Düzenleme sonrası veri seti KVKK Onayında durumunda: "
                        + dataSetName
        );

        ReportManager.info(
                "Düzenleme sonrası portal aktarım durumu kontrol ediliyor."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarılmadı"),
                "Düzenleme sonrası portal durumu Aktarılmadı olmadı. "
                        + "Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Düzenleme sonrası portal aktarım durumu Aktarılmadı: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiYayinOncesiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_oncesi_silme");

        ReportManager.info(
                "Yayın öncesi silme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın öncesi silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın öncesi silme testi veri seti"
        );

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info(
                "Veri seti yayın öncesinde siliniyor: " + dataSetName
        );
        publishFlow.deleteBeforePublication(dataSetName);

        ReportManager.info(
                "Silme işlemi sonrası veri setinin listede olmadığı kontrol ediliyor."
        );
        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Veri seti yayın öncesinde silinemedi."
        );

        ReportManager.pass(
                "Veri seti yayın öncesinde başarıyla silindi: " + dataSetName
        );
    }
    @Test
    public void veriSetiYayinSonrasiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_sonrasi_silme");

        ReportManager.info(
                "Yayın sonrası silme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın sonrası silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın sonrası silme testi veri seti"
        );

        page.waitForTimeout(3000);

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.clickPublishTab();
        publishPage.waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.info("Yayınlanan veri seti arşivleniyor.");
        publishFlow.archiveDataSet(dataSetName);

        ReportManager.info("Arşivlenen veri seti siliniyor.");
        publishFlow.deleteAfterPublication(dataSetName);

        ReportManager.info(
                "Silme işlemi sonrası veri setinin listede olmadığı doğrulanıyor."
        );
        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Arşivlenen veri seti silinemedi."
        );

        ReportManager.pass(
                "Arşivlenen veri seti başarıyla silindi: " + dataSetName
        );
    }
    @Test
    public void veriSetiGoruntuleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme");

        ReportManager.info(
                "Veri seti görüntüleme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti görüntüleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti görüntüleme testi"
        );

        page.waitForTimeout(3000);

        ReportManager.info("Veri seti görüntüleme işlemi başlatılıyor.");
        publishFlow.viewDataSet(dataSetName);

        ReportManager.pass(
                "Veri seti görüntüleme akışı tamamlandı: " + dataSetName
        );
    }
    @Test
    public void veriSetiArama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arama");

        ReportManager.info(
                "Veri seti arama testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arama testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arama testi"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Arama alanına veri seti adı yazılıyor: " + dataSetName
        );
        publishFlow.searchDataSet(dataSetName);

        ReportManager.info(
                "Arama sonuçlarının güncellenmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info(
                "Aranan veri setinin listede bulunduğu doğrulanıyor."
        );
        Assert.assertTrue(
                publishPage.isDataSetNameMatching(dataSetName),
                "Aranan veri seti listede bulunamadı. Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Aranan veri seti listede bulundu: " + dataSetName
        );
    }
    @Test
    public void veriSetiManuelGuncelleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_manuel_guncelleme");

        ReportManager.info(
                "Manuel güncelleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Manuel güncelleme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Manuel güncelleme testi veri seti"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti için KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info(
                "Veri setinin Yayın Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.pass(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.info("Güncelleme öncesi veri seti versiyonu alınıyor.");
        int oldVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );

        ReportManager.info("Güncelleme öncesi versiyon: v" + oldVersion);

        ReportManager.info("Veri seti manuel olarak güncelleniyor.");
        publishFlow.manuallyUpdateDataSet(dataSetName);

        ReportManager.info(
                "Manuel güncelleme sonrası portal aktarım durumu bekleniyor."
        );
        publishPage.waitForPortalStatus(
                dataSetName,
                "Aktarıldı"
        );
        ReportManager.pass(
                "Veri setinin portal aktarım durumu Aktarıldı: " + dataSetName
        );

        ReportManager.info(
                "Güncelleme işleminin tamamlanması için sayfa yenileniyor."
        );
        PlaywrightManager.getPage().reload();

        ReportManager.info(
                "Sayfa yenilendi. Veri setinin güncel versiyonu okunuyor."
        );

        int newVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );

        ReportManager.info("Güncelleme sonrası versiyon: v" + newVersion);
        ReportManager.info("Versiyon artışı doğrulanıyor.");

        Assert.assertEquals(
                newVersion,
                oldVersion + 1,
                "Manuel güncelleme sonrası veri seti versiyonu 1 artmadı."
        );

        ReportManager.pass(
                "Manuel güncelleme sonrası versiyon bir arttı: v"
                        + oldVersion + " → v" + newVersion
        );
    }
    @Test
    public void veriSetiArsiveAlma() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arsivleme");

        ReportManager.info(
                "Veri seti arşivleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
        OpenDataCreateFlow.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arşivleme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
        OpenDataCreateFlow.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arşivleme testi"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti için KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.info(
                "Veri setinin Yayın Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.pass(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.info("Veri seti arşivleniyor.");
        publishFlow.archiveDataSet(dataSetName);

        ReportManager.info(
                "Veri setinin Arşivlendi durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Arşivlendi"
        );
        ReportManager.pass(
                "Veri seti Arşivlendi durumuna ulaştı: " + dataSetName
        );

        publishPage.waitForPortalStatus(
                dataSetName,
                "Aktarılmadı"
        );
        ReportManager.pass(
                "Arşivlenen veri setinin portal aktarım durumu Aktarılmadı: "
                        + dataSetName
        );

        ReportManager.pass(
                "Veri seti arşivleme testi başarıyla tamamlandı."
        );
    }
}