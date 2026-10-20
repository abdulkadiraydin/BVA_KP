package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.flows.avpFlows.OpenDataCreateFlow;
import com.bvakp.automation.flows.avpFlows.OpenDataPublishFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenDataPublishTest extends AvpBaseTest {

    private record TestContext(
            OpenDataPublishPage publishPage,
            OpenDataPublishFlow publishFlow,
            OpenDataCreateFlow createFlow) {
    }
    private TestContext testHazirligi() {

        avpOturumuHazirlama();

        new OpenDataPage(page()).clickAcikVeriPortali();

        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(page());

        return new TestContext(
                publishPage,
                new OpenDataPublishFlow(publishPage),
                new OpenDataCreateFlow(page())
        );
    }
    @Test
    public void veriSetiKvkkOnaylama1() {
        TestContext ctx = testHazirligi();
    }
    @Test
    public void veriSetiKvkkOnaylama() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_onay");

        ReportManager.info(
                "KVKK onaylama testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK onay testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onay testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.pass(
                "KVKK onaylama doğrulandı. Veri seti Yayın Onayında: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiKvkkReddetme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_red");

        ReportManager.info(
                "KVKK reddetme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK reddetme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK reddetme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
        ctx.publishFlow().rejectKvkk(dataSetName);

        ReportManager.info("Veri setinin Düzenleme Bekliyor olması bekleniyor.");
        ctx.publishPage().waitForStatus(
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
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayinlama");

        ReportManager.info(
                "Veri seti yayınlama testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayınlama testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti yayınlama testi"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
        ctx.publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(
                dataSetName,
                "Yayında"
        );

        ReportManager.info("Veri setinin portala aktarılması bekleniyor.");
        ctx.publishPage().waitForPortalStatus(
                dataSetName,
                "Aktarıldı"
        );

        ReportManager.pass(
                "Veri seti yayınlandı ve portala aktarıldı: " + dataSetName
        );
    }
    @Test
    public void veriSetiYayiniReddetme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_red");

        ReportManager.info(
                "Yayın reddetme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın reddetme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın reddetme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.info("Yayın reddetme işlemi başlatılıyor.");
        ctx.publishFlow().rejectPublication(dataSetName);

        ReportManager.info("Veri setinin Düzenleme Bekliyor olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Düzenleme Bekliyor");

        ReportManager.pass(
                "Yayın reddi doğrulandı. Veri seti Düzenleme Bekliyor durumunda: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiKvkkOnayiDuzenleme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_duzenleme");

        ReportManager.info(
                "KVKK onayı düzenleme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK düzenleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
        ctx.publishFlow().rejectKvkk(dataSetName);

        ReportManager.info("Veri setinin Düzenleme Bekliyor olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Düzenleme Bekliyor");

        ReportManager.info("Veri seti düzenleniyor.");
        ctx.publishFlow().editKvkkPendingDataSet(dataSetName);

        ReportManager.info("Düzenleme sonrası KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.pass(
                "Düzenleme sonrasında veri seti KVKK Onayında durumunda: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiYayinOnayiDuzenleme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_duzenleme");

        ReportManager.info(
                "Yayın onayı düzenleme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın onayı düzenleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.info("Yayın reddetme işlemi başlatılıyor.");
        ctx.publishFlow().rejectPublication(dataSetName);

        ReportManager.info("Veri setinin Düzenleme Bekliyor olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Düzenleme Bekliyor");

        ReportManager.info("Veri seti düzenleniyor.");
        ctx.publishFlow().editPublicationPendingDataSet(dataSetName);

        ReportManager.info("Düzenleme sonrası KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.pass(
                "Düzenleme sonrası veri seti KVKK Onayında durumunda: "
                        + dataSetName
        );
    }
    @Test
    public void veriSetiYayinOncesiSilme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_oncesi_silme");

        ReportManager.info(
                "Yayın öncesi silme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın öncesi silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın öncesi silme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("Veri seti silme işlemi başlatılıyor.");
        ctx.publishFlow().deleteBeforePublication(dataSetName);

        ReportManager.info("Veri setinin listeden kalkması bekleniyor.");
        ctx.publishPage().waitForDataSetRemoval(dataSetName);

        ReportManager.pass(
                "Veri seti yayın öncesinde başarıyla silindi: " + dataSetName
        );
    }
    @Test
    public void veriSetiYayinSonrasiSilme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_sonrasi_silme");

        ReportManager.info(
                "Yayın sonrası silme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın sonrası silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın sonrası silme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
        ctx.publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayında");

        ReportManager.info("Veri setinin portala aktarılması bekleniyor.");
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        ReportManager.info("Veri seti arşivleme işlemi başlatılıyor.");
        ctx.publishFlow().archiveDataSet(dataSetName);

        ReportManager.info("Veri setinin Arşivlendi olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Arşivlendi");

        ReportManager.info("Veri seti silme işlemi başlatılıyor.");
        ctx.publishFlow().deleteAfterPublication(dataSetName);

        ReportManager.info("Veri setinin listeden kalkması bekleniyor.");
        ctx.publishPage().waitForDataSetRemoval(dataSetName);

        ReportManager.pass(
                "Arşivlenen veri seti başarıyla silindi: " + dataSetName
        );
    }
    @Test
    public void veriSetiGoruntuleme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_goruntuleme");

        ReportManager.info(
                "Veri seti görüntüleme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti görüntüleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti görüntüleme testi"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("Veri seti görüntüleniyor.");
        ctx.publishFlow().viewDataSet(dataSetName);

        ReportManager.info("Detay ekranının açılması bekleniyor.");
        ctx.publishPage().waitForDetailScreen(dataSetName);

        ReportManager.info("Önceki ekrana geri dönülüyor.");
        ctx.publishPage().clickGeriDon();

        ReportManager.pass(
                "Veri seti detay ekranı açıldı ve geri dönüldü: " + dataSetName
        );
    }
    @Test
    public void veriSetiArama() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arama");

        ReportManager.info(
                "Veri seti arama testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arama testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arama testi"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("Veri seti aranıyor.");
        ctx.publishFlow().searchDataSet(dataSetName);

        ReportManager.info("Aranan veri setinin listede bulunduğu doğrulanıyor.");
        Assert.assertTrue(
                ctx.publishPage().isDataSetNameMatching(dataSetName),
                "Aranan veri seti listede bulunamadı. Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Aranan veri seti listede bulundu: " + dataSetName
        );
    }
    @Test
    public void veriSetiManuelGuncelleme() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_manuel_guncelleme");

        ReportManager.info(
                "Manuel güncelleme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Manuel güncelleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Manuel güncelleme testi veri seti"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
        ctx.publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayında");

        ReportManager.info("Veri setinin portala aktarılması bekleniyor.");
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        int oldVersion = Integer.parseInt(
                ctx.publishPage().getCurrentVersion(dataSetName).replace("v", "")
        );
        ReportManager.info("Güncelleme öncesi versiyon: v" + oldVersion);

        ReportManager.info("Manuel güncelleme işlemi başlatılıyor.");
        ctx.publishFlow().manuallyUpdateDataSet(dataSetName);

        ReportManager.info("Versiyonun v" + (oldVersion + 1) + " olması bekleniyor.");
        ctx.publishPage().waitForVersion(dataSetName, oldVersion + 1);

        ReportManager.info("Güncel versiyonun portala aktarılması bekleniyor.");
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        ReportManager.pass(
                "Manuel güncelleme sonrası versiyon bir arttı ve portala aktarıldı: v"
                        + oldVersion + " → v" + (oldVersion + 1)
        );
    }
    @Test
    public void veriSetiArsiveAlma() {
        TestContext ctx = testHazirligi();

        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arsivleme");

        ReportManager.info(
                "Veri seti arşivleme testi başlatılıyor. Kaynak: " + kaynakAdi
                        + " | Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor.");
        ctx.createFlow().createSource(
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arşivleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor.");
        ctx.createFlow().createDataSetForApproval(
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti arşivleme testi"
        );

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
        ctx.publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayın Onayında");

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
        ctx.publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Yayında");

        ReportManager.info("Veri setinin portala aktarılması bekleniyor.");
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı");

        ReportManager.info("Veri seti arşivleme işlemi başlatılıyor.");
        ctx.publishFlow().archiveDataSet(dataSetName);

        ReportManager.info("Veri setinin Arşivlendi olması bekleniyor.");
        ctx.publishPage().waitForStatus(dataSetName, "Arşivlendi");

        ReportManager.pass(
                "Veri seti arşivleme doğrulandı. Veri seti Arşivlendi: "
                        + dataSetName
        );
    }
}