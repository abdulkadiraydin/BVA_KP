package com.bvakp.automation.tests.openData;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.pages.openData.OpenDataPublishPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OpenDataPublishTest extends BaseTest {

    @Test
    public void veriSetiYayınlama() {
        // Login
        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        // Açık Veri Portalı
        OpenDataPage openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        // Veri Seti Yönetimi
        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(page);

        publishPage.clickPublishTab();

        // KVKK Onayla
        publishPage.clickKvkkOnayla("Test Veri Seti");

        publishPage.enterKvkkAciklama(
                "Test KVKK onay açıklaması"
        );

        publishPage.confirmKvkkOnay();

        // KVKK onayı sonrası durum kontrolü
        Assert.assertTrue(
                publishPage.isStatus(
                        "Test Veri Seti",
                        "Yayın Onayında"
                ),
                "Veri seti KVKK onayından sonra Yayın Onayında durumuna geçmedi."
        );

        // Yayınla
        publishPage.clickYayinla("Test Veri Seti");

        publishPage.enterYayinAciklama(
                "Test yayın açıklaması"
        );

        publishPage.confirmYayinla();

        // Yayın durumunu kontrol et
        Assert.assertTrue(
                publishPage.isStatus(
                        "Test Veri Seti",
                        "Yayında"
                ),
                "Veri seti yayınlandı durumuna geçmedi."
        );
        publishPage.waitForPortalTransfer("Test Veri Seti");

        Assert.assertTrue(
                publishPage.isPortalStatus(
                        "Test Veri Seti",
                        "Aktarıldı"
                ),
                "Veri seti portala aktarılmadı."
        );
    }

    @Test
    public void veriSetiKvkkReddedilir() {

        LoginPage loginPage = new LoginPage(page);

        loginPage.open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        OpenDataPage openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        OpenDataPublishPage publishPage = new OpenDataPublishPage(page);
        publishPage.clickPublishTab();

        // KVKK Reddet popup'ını aç
        publishPage.clickKvkkReddet("Test Veri Seti");

        // İlk açılan popup'tan vazgeç
        publishPage.cancelKvkkRed();

        // Tekrar KVKK Reddet popup'ını aç
        publishPage.clickKvkkReddet("Test Veri Seti");

        // Red nedeni gir
        publishPage.enterKvkkRedAciklama(
                "KVKK kapsamında uygun bulunmamıştır."
        );

        // Red işlemini onayla
        publishPage.confirmKvkkRed();

        // Reddedildi durumunu bekle
        publishPage.waitForStatus(
                "Test Veri Seti",
                "Düzenleme Bekliyor"
        );
    }

    @Test
    public void duzenlemeBekleyenVeriSetiniDuzenleme() {

        LoginPage loginPage = new LoginPage(page);
        loginPage.open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        OpenDataPage openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        OpenDataPublishPage publishPage = new OpenDataPublishPage(page);
        publishPage.clickPublishTab();
        publishPage.clickDuzenle("Test Veri Seti");

        publishPage.selectDropdown(
                "Sorumlu Birim",
                "Yol Dairesi Başkanlığı"
        );
        publishPage.selectDropdown(
                "Kategori",
                "Filo & Araçlar"
        );
        publishPage.enterDuzenlemeAciklama("Güncellenmiş test veri seti açıklaması");
        publishPage.clickDuzenlemeKaydet();
        publishPage.assertDuzenlemeAlanlari(
                "Yol Dairesi Başkanlığı",
                "Filo & Araçlar",
                "Güncellenmiş test veri seti açıklaması"
        );
        publishPage.clickDegisiklikOnayaGonder();
        publishPage.confirmDegisiklikOnayaGonder();
        openDataPage.clickAcikVeriPortali();
        publishPage.clickPublishTab();

        Assert.assertTrue(
                publishPage.isStatus(
                        "Test Veri Seti",
                        "KVKK Onayında"
                ),
                "Veri seti KVKK Onayında durumuna geçmedi."
        );
    }

    @Test
    public void yayinKaldirma() {

        // Login
        LoginPage loginPage = new LoginPage(page);

        loginPage
                .open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        // Açık Veri Portalı
        OpenDataPage openDataPage = new OpenDataPage(page);

        openDataPage.clickAcikVeriPortali();

        // Veri Seti Yönetimi
        OpenDataPublishPage publishPage =
                new OpenDataPublishPage(page);

        publishPage.clickPublishTab();

        // Test veri setini arşivle
        publishPage.clickArsivle("Test Veri Seti");

        publishPage.enterArsivAciklama(
                "Test veri seti yayından kaldırılarak arşivlenmiştir."
        );
        publishPage.confirmArsivle();

        publishPage.waitForStatus(
                "Test Veri Seti",
                "Arşivlendi"
        );
    }

    @Test
    public void arsivlenmisVeriSetiniSilme() {

        LoginPage loginPage = new LoginPage(page);

        loginPage.open()
                .enterUsername(ConfigManager.get("username"))
                .enterPassword(ConfigManager.get("password"))
                .clickLogin();

        Assert.assertEquals(
                loginPage.getTitle(),
                "Büyük Veri Analitiği Kaynak Planlama"
        );

        OpenDataPage openDataPage = new OpenDataPage(page);
        openDataPage.clickAcikVeriPortali();

        OpenDataPublishPage publishPage = new OpenDataPublishPage(page);
        publishPage.clickPublishTab();

        // Arşivlenmiş veri setini sil
        publishPage.clickArsivlenmisVeriSetiniSil("Test Veri Seti");
        publishPage.confirmSil();

        Assert.assertFalse(
                publishPage.isDataSetDisplayed("Test Veri Seti"),
                "Veri seti silinemedi."
        );
    }
}

    /*@Test
    public void yayinlananVeriSetiIcerikKontrolu() {

        // Şimdilik boş
    }
}
*/
