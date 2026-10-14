package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.AvpBaseTest;
<<<<<<< HEAD
import com.bvakp.automation.flows.OpenDataPublishFlow;
=======
import com.bvakp.automation.core.playwright.PlaywrightManager;
>>>>>>> origin/main
import com.bvakp.automation.flows.OpenDataCreateFlow;
import com.bvakp.automation.flows.OpenDataPublishFlow;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import com.bvakp.automation.reporting.ReportManager;
import com.bvakp.automation.utils.TestDataUtil;
import org.testng.Assert;
<<<<<<< HEAD
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
=======
>>>>>>> origin/main
import org.testng.annotations.Test;
import com.microsoft.playwright.Page;

public class OpenDataPublishTest extends AvpBaseTest {

<<<<<<< HEAD
    private final ThreadLocal<TestContext> testContextThread =
            new ThreadLocal<>();

    /**
     * Her test metodunun kullanacağı Page ve Page Object nesnelerini
     * thread bazında birlikte tutar.
     */
    private record TestContext(
            Page page,
            OpenDataPage openDataPage,
            OpenDataPublishPage publishPage,
            OpenDataPublishFlow publishFlow,
            OpenDataCreateFlow openDataCreateFlow) {
    }

    /**
     * Her test öncesinde aktif thread için AVP oturumunu hazırlar
     * ve yalnızca o teste ait Page Object/Flow nesnelerini oluşturur.
     */
    @BeforeMethod
    public void setUpPublish() {

        avpOturumuHazirlama();

        Page aktifPage =
                page();

        OpenDataPage openDataPage =
                new OpenDataPage(aktifPage);

        openDataPage.clickAcikVeriPortali();

        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(aktifPage);

        OpenDataCreateFlow openDataCreateFlow =
                new OpenDataCreateFlow(aktifPage);

        OpenDataPublishFlow publishFlow =
                new OpenDataPublishFlow(publishPage);

        testContextThread.set(
                new TestContext(
                        aktifPage,
                        openDataPage,
                        publishPage,
                        publishFlow,
                        openDataCreateFlow
                )
=======
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
>>>>>>> origin/main
        );
    }

    /**
     * Aktif test thread'ine ait context bilgisini döndürür.
     */
    private TestContext context() {

        TestContext context =
                testContextThread.get();

        if (context == null) {
            throw new IllegalStateException(
                    "OpenDataPublishTest context oluşturulmamış."
            );
        }

        return context;
    }

    /**
     * Aktif test thread'ine ait OpenDataPage nesnesini döndürür.
     */
    private OpenDataPage openDataPage() {

        return context().openDataPage();
    }

    /**
     * Aktif test thread'ine ait OpenDataPublishPage nesnesini döndürür.
     */
    private OpenDataPublishPage publishPage() {

        return context().publishPage();
    }

    /**
     * Aktif test thread'ine ait OpenDataPublishFlow nesnesini döndürür.
     */
    private OpenDataPublishFlow publishFlow() {

        return context().publishFlow();
    }

    /**
     * Aktif test thread'ine ait OpenDataCreateFlow nesnesini döndürür.
     */
    private OpenDataCreateFlow openDataCreateFlow() {

        return context().openDataCreateFlow();
    }

    /**
     * Test tamamlandığında thread üzerinde tutulan test context'ini temizler.
     */
    @AfterMethod(alwaysRun = true)
    public void publishContextTemizleme() {

        testContextThread.remove();
    }

    @Test
    public void veriSetiKvkkOnaylama() {
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_onay");

        ReportManager.info("KVKK onaylama testi başlatılıyor. Veri seti: " + dataSetName);

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK onay testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onay testi veri seti"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

<<<<<<< HEAD
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage().clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        publishPage().waitForStatus(
=======
        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        ctx.publishPage().clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında olması bekleniyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.pass("Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName);

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında olduğu doğrulanıyor.");
        publishPage().waitForStatus(
=======
        ctx.publishFlow().approveKvkk(dataSetName);
        ReportManager.info("Veri setinin Yayın Onayında olduğu doğrulanıyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.pass("KVKK onaylama testi başarıyla tamamlandı: " + dataSetName);
    }
    @Test
    public void veriSetiKvkkReddetme() {
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_red");

        ReportManager.info(
                "KVKK reddetme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK reddetme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");
        ReportManager.info(dataSetName);
        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK reddetme testi veri seti"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

<<<<<<< HEAD
        ReportManager.info("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage().clickPublishTab();

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        publishPage().waitForStatus(
=======
        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        ctx.publishPage().clickPublishTab();
        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().rejectKvkk(dataSetName);
=======
        ctx.publishFlow().rejectKvkk(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "KVKK reddi sonrası veri setinin Düzenleme Bekliyor durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD

        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
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

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayınlama testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti yayınlama testi"
        );
        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");
        ReportManager.info("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
<<<<<<< HEAD
        publishPage().clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında durumuna geçmesi bekleniyor.");
        publishPage().waitForStatus(
=======
        ctx.publishPage().clickPublishTab();

        ReportManager.info("Veri setinin KVKK Onayında durumuna geçmesi bekleniyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri setinin Yayın Onayında durumuna geçmesi bekleniyor.");
        publishPage().waitForStatus(
=======
        ctx.publishFlow().approveKvkk(dataSetName);
        ReportManager.info("Veri setinin Yayın Onayında durumuna geçmesi bekleniyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.pass("Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName);

        ReportManager.info("Veri seti yayınlama işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri setinin Yayında durumunda olduğu doğrulanıyor.");
        Assert.assertTrue(
                publishPage().isStatus(dataSetName, "Yayında"),
                "Veri seti Yayında durumuna geçmedi."
=======
        ctx.publishFlow().publishDataSet(dataSetName);
        ctx.publishPage().waitForStatus(
                dataSetName,
                "Yayında"
>>>>>>> origin/main
        );
        ReportManager.pass("Veri seti Yayında durumunda: " + dataSetName);

        ReportManager.info("Veri setinin portala aktarım durumu doğrulanıyor.");
<<<<<<< HEAD
        Assert.assertTrue(
                publishPage().isPortalStatus(dataSetName, "Aktarıldı"),
                "Veri seti portala aktarılmadı."
=======
        ctx.publishPage().waitForPortalStatus(dataSetName, "Aktarıldı"
>>>>>>> origin/main
        );
        ReportManager.pass("Veri seti portala başarıyla aktarıldı: " + dataSetName);
    }
    @Test
    public void veriSetiYayiniReddetme() {
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_red");

        ReportManager.info(
                "Veri seti yayın reddetme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
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

<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
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
<<<<<<< HEAD
        page().waitForTimeout(3000);

        ReportManager.info(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
        publishPage().waitForStatus(dataSetName, "KVKK Onayında");

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");

        publishFlow().approveKvkk(dataSetName);

        ReportManager.info(
                "Veri setinin Yayın Onayında statüsüne geçmesi bekleniyor."
        );

        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");
        ReportManager.info(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );
        ctx.publishFlow().approveKvkk(dataSetName);
        ReportManager.info(
                "Veri setinin Yayın Onayında statüsüne geçmesi bekleniyor."
        );
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.info(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayın reddetme işlemi başlatılıyor.");

<<<<<<< HEAD
        publishFlow().rejectPublication(dataSetName);
=======
        ctx.publishFlow().rejectPublication(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Yayın reddi sonrasında veri setinin Düzenleme Bekliyor "
                        + "statüsünde olduğu doğrulanıyor."
        );
        Assert.assertTrue(
<<<<<<< HEAD
                publishPage().isStatus(
=======
                ctx.publishPage().isStatus(
>>>>>>> origin/main
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
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kvkk_duzenleme");

        ReportManager.info(
                "KVKK onayı düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "KVKK düzenleme testi kaynağı"
        );

        ReportManager.info(
                "Kaynak oluşturma işlemi tamamlandı: " + kaynakAdi
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);

<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "KVKK onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");

<<<<<<< HEAD
        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage().clickPublishTab();
=======
        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        ctx.publishPage().clickPublishTab();
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin KVKK Onayında statüsüne geçmesi bekleniyor."
        );
<<<<<<< HEAD

        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK reddetme işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().rejectKvkk(dataSetName);

        ReportManager.info(
                "KVKK reddi sonrasında veri seti düzenleme işlemi başlatılıyor."
        );

        publishFlow().editKvkkPendingDataSet(dataSetName);
=======
        ctx.publishFlow().rejectKvkk(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.info(
                "KVKK reddi sonrasında veri seti düzenleme işlemi başlatılıyor."
        );
        ctx.publishFlow().editKvkkPendingDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Düzenleme sonrasında veri setinin KVKK Onayında "
                        + "statüsünde olduğu doğrulanıyor."
        );
        Assert.assertTrue(
<<<<<<< HEAD
                publishPage().isStatus(dataSetName, "KVKK Onayında"),
=======
                ctx.publishPage().isStatus(dataSetName, "KVKK Onayında"),
>>>>>>> origin/main
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
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_duzenleme");

        ReportManager.info(
                "Yayın onayı sonrası düzenleme testi başlatıldı. Veri seti: "
                        + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);

<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın onayı düzenleme testi kaynağı"
        );

        ReportManager.info("Kaynak oluşturma işlemi tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);

<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın onayı düzenleme testi veri seti"
        );

        ReportManager.info("Veri seti oluşturma metodu tamamlandı.");
<<<<<<< HEAD

        ReportManager.info(
                "Veri setinin kaydedilmesi için 3 saniye bekleniyor."
        );

        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        publishPage().clickPublishTab();

        ReportManager.info("KVKK Onayında statüsü bekleniyor.");
        publishPage().waitForStatus(
=======
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.info("Veri setinin kaydedilmesi için 3 saniye bekleniyor.");
        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
        ctx.publishPage().clickPublishTab();

        ReportManager.info("KVKK Onayında statüsü bekleniyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );

        ReportManager.info(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onay işlemi başlatılıyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Yayın Onayında statüsü bekleniyor.");
        publishPage().waitForStatus(
=======
        ctx.publishFlow().approveKvkk(dataSetName);
        ReportManager.info("Yayın Onayında statüsü bekleniyor.");
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );

        ReportManager.info(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );
<<<<<<< HEAD

        ReportManager.info("Yayın reddetme işlemi başlatılıyor.");
        publishFlow().rejectPublication(dataSetName);

        ReportManager.info(
                "Yayın reddi sonrasında düzenleme işlemi başlatılıyor."
        );
        publishFlow().editPublicationPendingDataSet(dataSetName);
=======
        ctx.publishFlow().rejectPublication(dataSetName);
        ReportManager.info(
                "Yayın reddi sonrasında düzenleme işlemi başlatılıyor."
        );
        ctx.publishFlow().editPublicationPendingDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Düzenleme sonrası veri setinin KVKK Onayında olduğu "
                        + "doğrulanıyor."
        );
        Assert.assertTrue(
<<<<<<< HEAD
                publishPage().isStatus(dataSetName, "KVKK Onayında"),
=======
                ctx.publishPage().isStatus(dataSetName, "KVKK Onayında"),
>>>>>>> origin/main
                "Düzenleme sonrası veri seti KVKK Onayında durumuna geçmedi. "
                        + "Veri seti: " + dataSetName
        );
        ReportManager.pass(
                "Düzenleme sonrası veri seti KVKK Onayında durumunda: "
                        + dataSetName
        );

<<<<<<< HEAD
        ReportManager.info(
                "Düzenleme sonrası portal aktarım durumu kontrol ediliyor."
        );

        Assert.assertTrue(
                publishPage().isPortalStatus(dataSetName, "Aktarılmadı"),
                "Düzenleme sonrası portal durumu Aktarılmadı olmadı. "
                        + "Veri seti: " + dataSetName
        );

        ReportManager.pass(
                "Düzenleme sonrası portal aktarım durumu Aktarılmadı: "
                        + dataSetName
        );
=======
>>>>>>> origin/main
    }
    @Test
    public void veriSetiYayinOncesiSilme() {
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_yayin_oncesi_silme");

        ReportManager.info(
                "Yayın öncesi silme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın öncesi silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
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
<<<<<<< HEAD
        page().waitForTimeout(3000);

        ReportManager.info(
                "Veri seti yayın öncesinde siliniyor: " + dataSetName
        );
        publishFlow().deleteBeforePublication(dataSetName);
=======
        PlaywrightManager.getPage().waitForTimeout(3000);
        ctx.publishPage().clickPublishTab();
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");
        ReportManager.info(
                "Veri seti yayın öncesinde siliniyor: " + dataSetName
        );

        ctx.publishFlow().deleteBeforePublication(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Silme işlemi sonrası veri setinin listede olmadığı kontrol ediliyor."
        );
        Assert.assertFalse(
<<<<<<< HEAD
                publishPage().isDataSetDisplayed(dataSetName),
=======
                ctx.publishPage().isDataSetDisplayed(dataSetName),
>>>>>>> origin/main
                "Veri seti yayın öncesinde silinemedi."
        );

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
                "Yayın sonrası silme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Yayın sonrası silme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Yayın sonrası silme testi veri seti"
        );

<<<<<<< HEAD
        page().waitForTimeout(3000);
=======
        PlaywrightManager.getPage().waitForTimeout(3000);
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().clickPublishTab();
        publishPage().waitForStatus(dataSetName, "KVKK Onayında");
=======
        ctx.publishPage().clickPublishTab();
        ctx.publishPage().waitForStatus(dataSetName, "KVKK Onayında");
>>>>>>> origin/main

        ReportManager.pass(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("KVKK onayı veriliyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);

        ReportManager.info("Veri seti yayınlanıyor.");
        publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Yayınlanan veri seti arşivleniyor.");
        publishFlow().archiveDataSet(dataSetName);

        ReportManager.info("Arşivlenen veri seti siliniyor.");
        publishFlow().deleteAfterPublication(dataSetName);
=======
        ctx.publishFlow().approveKvkk(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri seti yayınlanıyor.");
        ctx.publishFlow().publishDataSet(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Yayınlanan veri seti arşivleniyor.");
        ctx.publishFlow().archiveDataSet(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Arşivlenen veri seti siliniyor.");
        ctx.publishFlow().deleteAfterPublication(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Silme işlemi sonrası veri setinin listede olmadığı doğrulanıyor."
        );
        Assert.assertFalse(
<<<<<<< HEAD
                publishPage().isDataSetDisplayed(dataSetName),
=======
                ctx.publishPage().isDataSetDisplayed(dataSetName),
>>>>>>> origin/main
                "Arşivlenen veri seti silinemedi."
        );

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
                "Veri seti görüntüleme testi başlatıldı. Veri seti: " + dataSetName
        );

        ReportManager.info("Test kaynağı oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti görüntüleme testi kaynağı"
        );

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
                kaynakAdi,
                dataSetName,
                "İşletme Dairesi Başkanlığı",
                "Altyapı & Şebeke",
                "TCDD Taşımacılık Açık Veri Lisansı",
                "Veri seti görüntüleme testi"
        );

<<<<<<< HEAD
        page().waitForTimeout(3000);

        ReportManager.info("Veri seti görüntüleme işlemi başlatılıyor.");
        publishFlow().viewDataSet(dataSetName);
=======
        PlaywrightManager.getPage().waitForTimeout(3000);

        ReportManager.info("Veri seti görüntüleme işlemi başlatılıyor.");
        ctx.publishFlow().viewDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.pass(
                "Veri seti görüntüleme akışı tamamlandı: " + dataSetName
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
                "Veri seti arama testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arama testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
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
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
<<<<<<< HEAD
        publishPage().clickPublishTab();
=======
        ctx.publishPage().clickPublishTab();
>>>>>>> origin/main

        ReportManager.info(
                "Arama alanına veri seti adı yazılıyor: " + dataSetName
        );
<<<<<<< HEAD
        publishFlow().searchDataSet(dataSetName);
=======
        ctx.publishFlow().searchDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Arama sonuçlarının güncellenmesi için 3 saniye bekleniyor."
        );
<<<<<<< HEAD
        page().waitForTimeout(3000);

=======
>>>>>>> origin/main
        ReportManager.info(
                "Aranan veri setinin listede bulunduğu doğrulanıyor."
        );
        Assert.assertTrue(
<<<<<<< HEAD
                publishPage().isDataSetNameMatching(dataSetName),
=======
                ctx.publishPage().isDataSetNameMatching(dataSetName),
>>>>>>> origin/main
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
                "Manuel güncelleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Manuel güncelleme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
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
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
<<<<<<< HEAD
        publishPage().clickPublishTab();
=======
        ctx.publishPage().clickPublishTab();
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.info(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti için KVKK onayı veriliyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);
=======
        ctx.publishFlow().approveKvkk(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin Yayın Onayında durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.info(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayınlanıyor.");
<<<<<<< HEAD
        publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Güncelleme öncesi veri seti versiyonu alınıyor.");
        int oldVersion = Integer.parseInt(
                publishPage().getCurrentVersion(dataSetName).replace("v", "")
=======
        ctx.publishFlow().publishDataSet(dataSetName);
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.info("Güncelleme öncesi veri seti versiyonu alınıyor.");
        int oldVersion = Integer.parseInt(
                ctx.publishPage().getCurrentVersion(dataSetName).replace("v", "")
>>>>>>> origin/main
        );

        ReportManager.info("Güncelleme öncesi versiyon: v" + oldVersion);

        ReportManager.info("Veri seti manuel olarak güncelleniyor.");
<<<<<<< HEAD
        publishFlow().manuallyUpdateDataSet(dataSetName);
=======
        ctx.publishFlow().manuallyUpdateDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Manuel güncelleme sonrası portal aktarım durumu bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForPortalStatus(
=======
        ctx.publishPage().waitForPortalStatus(
>>>>>>> origin/main
                dataSetName,
                "Aktarıldı"
        );
        ReportManager.info(
                "Veri setinin portal aktarım durumu Aktarıldı: " + dataSetName
        );

        ReportManager.info(
                "Güncelleme işleminin tamamlanması için sayfa yenileniyor."
        );
        page().reload();

        ReportManager.info(
                "Sayfa yenilendi. Veri setinin güncel versiyonu okunuyor."
        );

        int newVersion = Integer.parseInt(
<<<<<<< HEAD
                publishPage().getCurrentVersion(dataSetName).replace("v", "")
=======
                ctx.publishPage().getCurrentVersion(dataSetName).replace("v", "")
>>>>>>> origin/main
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
        TestContext ctx = testHazirligi();
        String kaynakAdi =
                TestDataUtil.dinamikAdOlusturma("otomasyon_kaynak");

        String dataSetName =
                TestDataUtil.dinamikAdOlusturma("otomasyon_arsivleme");

        ReportManager.info(
                "Veri seti arşivleme testi başlatılıyor. Veri seti: " + dataSetName
        );

        ReportManager.info("Kaynak oluşturuluyor: " + kaynakAdi);
<<<<<<< HEAD
        openDataCreateFlow().createSource(
=======
        ctx.createFlow().createSource(
>>>>>>> origin/main
                kaynakAdi,
                "arac_bakim_is_yeri_sayisi",
                "Veri seti arşivleme testi kaynağı"
        );
        ReportManager.info("Kaynak oluşturma metodu tamamlandı.");

        ReportManager.info("Veri seti oluşturuluyor: " + dataSetName);
<<<<<<< HEAD
        openDataCreateFlow().createDataSetForApproval(
=======
        ctx.createFlow().createDataSetForApproval(
>>>>>>> origin/main
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
        page().waitForTimeout(3000);

        ReportManager.info("Veri Seti Yönetimi sekmesine geçiliyor.");
<<<<<<< HEAD
        publishPage().clickPublishTab();
=======
        ctx.publishPage().clickPublishTab();
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin KVKK Onayında durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "KVKK Onayında"
        );
        ReportManager.info(
                "Veri seti KVKK Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti için KVKK onayı veriliyor.");
<<<<<<< HEAD
        publishFlow().approveKvkk(dataSetName);
=======
        ctx.publishFlow().approveKvkk(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin Yayın Onayında durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Yayın Onayında"
        );
        ReportManager.pass(
                "Veri seti Yayın Onayında durumuna ulaştı: " + dataSetName
        );

        ReportManager.info("Veri seti yayınlanıyor.");
<<<<<<< HEAD
        publishFlow().publishDataSet(dataSetName);

        ReportManager.info("Veri seti arşivleniyor.");
        publishFlow().archiveDataSet(dataSetName);
=======
        ctx.publishFlow().publishDataSet(dataSetName);
        ctx.publishPage().waitForStatus(
                dataSetName,
                "Yayında"
        );
        ctx.publishPage().waitForPortalStatus(
                dataSetName,
                "Aktarıldı"
        );
        PlaywrightManager.getPage().waitForTimeout(3000);
        ReportManager.info("Veri seti arşivleniyor.");
        ctx.publishFlow().archiveDataSet(dataSetName);
>>>>>>> origin/main

        ReportManager.info(
                "Veri setinin Arşivlendi durumuna geçmesi bekleniyor."
        );
<<<<<<< HEAD
        publishPage().waitForStatus(
=======
        ctx.publishPage().waitForStatus(
>>>>>>> origin/main
                dataSetName,
                "Arşivlendi"
        );
        ReportManager.pass(
                "Veri seti Arşivlendi durumuna ulaştı: " + dataSetName
        );
<<<<<<< HEAD

        publishPage().waitForPortalStatus(
                dataSetName,
                "Aktarılmadı"
        );
        ReportManager.pass(
                "Arşivlenen veri setinin portal aktarım durumu Aktarılmadı: "
                        + dataSetName
        );

=======
>>>>>>> origin/main
        ReportManager.pass(
                "Veri seti arşivleme testi başarıyla tamamlandı."
        );
    }
}