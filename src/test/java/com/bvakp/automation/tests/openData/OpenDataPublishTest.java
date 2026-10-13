package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.flows.OpenDataCreateFlow.CreateDataSetForApproval;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.flows.OpenDataSourceFlow.CreateSource;
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
    private CreateDataSetForApproval createDataSetForApproval;
    private CreateSource createSource;

    @BeforeMethod
    public void setUpPublish() {
        avpOturumuHazirlama();

        openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        publishPage = new OpenDataPublishPage(page);

        createSource = new CreateSource(page);

        publishFlow = new OpenDataPublishFlow(publishPage);

        createDataSetForApproval = new CreateDataSetForApproval(page);

    }

    @Test
    public void veriSetiKvkkOnaylama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_onay");

        ReportManager.step("KVKK onaylama testi başlatılıyor. Veri seti: " + dataSetName);

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK onay testi kaynağı"
        );
        ReportManager.step("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onay testi veri seti"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Veri setinin KVKK Onayında olması bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step("KVKK onay işlemi başlatılıyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.step("Veri setinin Yayın Onayında olduğu doğrulanıyor.");
        Assert.assertTrue(
                publishPage.isStatus(
                        dataSetName,
                        "Yayın Onayında"
                ),
                "Veri seti KVKK onaylanamadı. Veri seti: " + dataSetName
        );

        ReportManager.step("KVKK onaylama testi başarıyla tamamlandı.");
    }
    @Test
    public void veriSetiKvkkReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_red");

        ReportManager.step(
                "KVKK reddetme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK reddetme testi kaynağı"
        );
        ReportManager.step("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK reddetme testi veri seti"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step("KVKK reddetme işlemi başlatılıyor.");
        publishFlow.rejectKvkk(dataSetName);

        ReportManager.step(
                "Veri setinin Düzenleme Bekliyor durumunda olduğu doğrulanıyor."
        );
        ReportManager.step(
                "KVKK reddi sonrası veri setinin Düzenleme Bekliyor durumuna geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Düzenleme Bekliyor"
        );

        ReportManager.step(
                "KVKK reddi doğrulandı. Veri seti Düzenleme Bekliyor durumunda."
        );
    }
    @Test
    public void veriSetiYayinlama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayinlama");

        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayınlama testi kaynağı"
        );

        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti yayınlama testi"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");
        ReportManager.step("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        publishPage.clickPublishTab();

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        publishFlow.approveKvkk(dataSetName);

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        publishFlow.publishDataSet(dataSetName);

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "Yayında"),
                "Veri seti Yayında durumuna geçmedi."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarıldı"),
                "Veri seti portala aktarılmadı."
        );
    }
    @Test
    public void veriSetiYayiniReddetme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_red");

        ReportManager.step(
                "Veri seti yayın reddetme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);

        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın reddetme testi kaynağı"
        );

        ReportManager.step(
                "Kaynak oluşturma işlemi tamamlandı: " + kaynakAdi
        );

        ReportManager.step(
                "Yayın reddetme testi için veri seti oluşturuluyor: "
                        + dataSetName
        );

        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın reddetme testi veri seti"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı: " + dataSetName);
        ReportManager.step("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor.");
        publishPage.waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.step("Veri setinin KVKK Onayında olduğu doğrulandı.");

        ReportManager.step("KVKK onay işlemi başlatılıyor.");

        publishFlow.approveKvkk(dataSetName);

        ReportManager.step(
                "Veri setinin Yayın Onayında statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.step(
                "Veri setinin Yayın Onayında olduğu doğrulandı."
        );

        ReportManager.step("Veri seti yayın reddetme işlemi başlatılıyor.");

        publishFlow.rejectPublication(dataSetName);

        ReportManager.step(
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

        ReportManager.step(
                "Veri seti yayın reddetme testi başarıyla tamamlandı."
        );
    }
    @Test
    public void veriSetiKvkkOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_duzenleme");

        ReportManager.step(
                "KVKK onayı düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);

        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK düzenleme testi kaynağı"
        );

        ReportManager.step(
                "Kaynak oluşturma işlemi tamamlandı: " + kaynakAdi
        );

        ReportManager.step(
                "Veri seti oluşturuluyor: " + dataSetName
        );

        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onayı düzenleme testi veri seti"
        );

        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step(
                "Veri setinin KVKK Onayında olduğu doğrulandı."
        );

        ReportManager.step("KVKK reddetme işlemi başlatılıyor.");
        publishFlow.rejectKvkk(dataSetName);

        ReportManager.step(
                "KVKK reddi sonrasında veri seti düzenleme işlemi başlatılıyor."
        );

        publishFlow.editKvkkPendingDataSet(dataSetName);

        ReportManager.step(
                "Düzenleme sonrasında veri setinin KVKK Onayında "
                        + "statüsünde olduğu doğrulanıyor."
        );

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Veri seti KVKK Onayında durumuna geçmedi. Veri seti: "
                        + dataSetName
        );

        ReportManager.step(
                "KVKK onayı düzenleme testi başarıyla tamamlandı."
        );
    }
    @Test
    public void veriSetiYayinOnayiDuzenleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_duzenleme");

        ReportManager.step(
                "Yayın onayı sonrası düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);

        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın onayı düzenleme testi kaynağı"
        );

        ReportManager.step("Kaynak oluşturma işlemi tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);

        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın onayı düzenleme testi veri seti"
        );

        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("KVKK Onayında statüsü bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step("KVKK onay işlemi başlatılıyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.step("Yayın Onayında statüsü bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.step("Yayın reddetme işlemi başlatılıyor.");
        publishFlow.rejectPublication(dataSetName);

        ReportManager.step(
                "Yayın reddi sonrasında düzenleme işlemi başlatılıyor."
        );
        publishFlow.editPublicationPendingDataSet(dataSetName);

        ReportManager.step(
                "Düzenleme sonrası veri setinin KVKK Onayında olduğu "
                        + "doğrulanıyor."
        );

        Assert.assertTrue(
                publishPage.isStatus(dataSetName, "KVKK Onayında"),
                "Düzenleme sonrası veri seti KVKK Onayında durumuna geçmedi. "
                        + "Veri seti: " + dataSetName
        );

        ReportManager.step(
                "Düzenleme sonrası portal aktarım durumu kontrol ediliyor."
        );

        Assert.assertTrue(
                publishPage.isPortalStatus(dataSetName, "Aktarılmadı"),
                "Düzenleme sonrası portal durumu Aktarılmadı olmadı. "
                        + "Veri seti: " + dataSetName
        );

        ReportManager.step(
                "Yayın onayı sonrası düzenleme testi başarıyla tamamlandı."
        );
    }
    @Test
    public void veriSetiYayinOncesiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_oncesi_silme");

        ReportManager.step("Yayın öncesi silme testi başlatıldı. Veri seti: " + dataSetName);

        ReportManager.step("Test kaynağı oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın öncesi silme testi kaynağı"
        );

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın öncesi silme testi veri seti"
        );

        ReportManager.step(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri seti yayın öncesinde siliniyor: " + dataSetName);
        publishFlow.deleteBeforePublication(dataSetName);

        ReportManager.step("Silme işlemi sonrası veri setinin listede olmadığı kontrol ediliyor.");
        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Veri seti yayın öncesinde silinemedi."
        );

        ReportManager.step("Yayın öncesi silme testi başarıyla tamamlandı.");
    }
    @Test
    public void veriSetiYayinSonrasiSilme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_sonrasi_silme");

        ReportManager.step(
                "Yayın sonrası silme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.step("Test kaynağı oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın sonrası silme testi kaynağı"
        );

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın sonrası silme testi veri seti"
        );

        page.waitForTimeout(3000);

        ReportManager.step("Veri setinin KVKK Onayında durumuna geçmesi bekleniyor.");
        publishPage.clickPublishTab();
        publishPage.waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.step("KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.step("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.step("Yayınlanan veri seti arşivleniyor.");
        publishFlow.archiveDataSet(dataSetName);

        ReportManager.step("Arşivlenen veri seti siliniyor.");
        publishFlow.deleteAfterPublication(dataSetName);

        ReportManager.step("Silme işlemi sonrası veri setinin listede olmadığı doğrulanıyor.");
        Assert.assertFalse(
                publishPage.isDataSetDisplayed(dataSetName),
                "Arşivlenen veri seti silinemedi."
        );

        ReportManager.step("Yayın sonrası silme testi başarıyla tamamlandı.");
    }
    @Test
    public void veriSetiGoruntuleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme");

        ReportManager.step(
                "Veri seti görüntüleme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.step("Test kaynağı oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti görüntüleme testi kaynağı"
        );

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti görüntüleme testi"
        );
        page.waitForTimeout(3000);
        ReportManager.step("Veri seti görüntüleme işlemi başlatılıyor.");
        publishFlow.viewDataSet(dataSetName);

        ReportManager.step(
                "Veri seti görüntüleme akışı tamamlandı: " + dataSetName
        );
    }
    @Test
    public void veriSetiArama() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arama");

        ReportManager.step(
                "Veri seti arama testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arama testi kaynağı"
        );
        ReportManager.step("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arama testi"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Arama alanına veri seti adı yazılıyor: " + dataSetName
        );
        publishFlow.searchDataSet(dataSetName);
        ReportManager.step(
                "Arama sonuçlarının güncellenmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.step(
                "Aranan veri setinin listede bulunduğu doğrulanıyor."
        );
        Assert.assertTrue(
                publishPage.isDataSetNameMatching(dataSetName),
                "Aranan veri seti listede bulunamadı. Veri seti: " + dataSetName
        );

        ReportManager.step(
                "Veri seti arama testi başarıyla tamamlandı."
        );
    }
    @Test
    public void veriSetiManuelGuncelleme() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_manuel_guncelleme");

        ReportManager.step(
                "Manuel güncelleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Manuel güncelleme testi kaynağı"
        );
        ReportManager.step("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Manuel güncelleme testi veri seti"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step("Veri setinin KVKK Onayında durumuna geçmesi bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step("Veri seti için KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.step("Veri setinin Yayın Onayında durumuna geçmesi bekleniyor.");
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.step("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.step("Güncelleme öncesi veri seti versiyonu alınıyor.");
        int oldVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );

        ReportManager.step("Güncelleme öncesi versiyon: v" + oldVersion);

        ReportManager.step("Veri seti manuel olarak güncelleniyor.");
        publishFlow.manuallyUpdateDataSet(dataSetName);
        ReportManager.step(
                "Manuel güncelleme sonrası portal aktarım durumu bekleniyor."
        );

        publishPage.waitForPortalStatus(
                dataSetName,
                "Aktarıldı"
        );
        ReportManager.step(
                "Güncelleme işleminin tamamlanması için sayfa yenileniyor."
        );

        PlaywrightManager.getPage().reload();

        ReportManager.step(
                "Sayfa yenilendi. Veri setinin güncel versiyonu okunuyor."
        );

        int newVersion = Integer.parseInt(
                publishPage.getCurrentVersion(dataSetName).replace("v", "")
        );
        ReportManager.step("Güncelleme sonrası versiyon: v" + newVersion);
        ReportManager.step("Versiyon artışı doğrulanıyor.");
        Assert.assertEquals(
                newVersion,
                oldVersion + 1,
                "Manuel güncelleme sonrası veri seti versiyonu 1 artmadı."
        );

        ReportManager.step("Manuel güncelleme testi başarıyla tamamlandı.");
    }
    @Test
    public void veriSetiArsiveAlma() {

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arsivleme");

        ReportManager.step(
                "Veri seti arşivleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.step("Kaynak oluşturuluyor: " + kaynakAdi);
        createSource.createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arşivleme testi kaynağı"
        );
        ReportManager.step("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.step("Veri seti oluşturuluyor: " + dataSetName);
        createDataSetForApproval.createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arşivleme testi"
        );
        ReportManager.step("Veri seti oluşturma metodu tamamlandı.");

        ReportManager.step(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.step("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage.clickPublishTab();

        ReportManager.step(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.step("Veri seti için KVKK onayı veriliyor.");
        publishFlow.approveKvkk(dataSetName);

        ReportManager.step(
                "Veri setinin Yayın Onayında durumuna geçmesi bekleniyor."
        );
        publishPage.waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.step("Veri seti yayınlanıyor.");
        publishFlow.publishDataSet(dataSetName);

        ReportManager.step("Veri seti arşivleniyor.");
        publishFlow.archiveDataSet(dataSetName);

        ReportManager.step(
                "Veri setinin Arşivlendi durumuna geçmesi bekleniyor."
        );

        publishPage.waitForStatus(
                dataSetName,
                "Arşivlendi"
        );

        ReportManager.step(
                "Veri setinin Arşivlendi durumu doğrulandı."
        );
        publishPage.waitForPortalStatus(
                dataSetName,
                "Aktarılmadı"
        );

        ReportManager.step(
                "Veri seti arşivleme testi başarıyla tamamlandı."
        );
    }
}