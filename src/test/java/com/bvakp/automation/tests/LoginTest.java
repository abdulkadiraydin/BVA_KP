package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Keycloak giriş ekranı testleri.
 *
 * <p>BaseTest'ten türer: kayıtlı oturum kullanılmaz, her test temiz bir giriş ekranıyla başlar.
 */
public class LoginTest extends BaseTest {

    private static final String HATALI_KULLANICI_ADI = "hatali.kullaniciadi";
    private static final String HATALI_SIFRE = "HataliSifre123!";
    private static final String GIRIS_HATA_MESAJI = "Kullanıcı adı veya şifre hatalı.";

    /** Giriş ekranını açar ve temel alanların görüntülendiğini doğrular. */
    private LoginPage girisEkraniniAc() {

        LoginPage loginPage = new LoginPage(page());

        ReportManager.step("Login sayfası açılıyor.");
        loginPage.girisSayfasiAcma();

        Assert.assertTrue(loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı gerekli alanlarla birlikte görüntülenemedi.");

        return loginPage;
    }

    /** Login sayfasının kullanıcı adı, şifre ve giriş butonuyla açıldığını doğrular. */
    @Test
    public void girisSayfasiAcma() {

        girisEkraniniAc();

        ReportManager.info("LOGIN SAYFASI AÇILIŞ KONTROLÜ BAŞARILI");
    }

    /** Geçerli kullanıcı bilgileriyle başarılı giriş yapılabildiğini doğrular. */
    @Test
    public void basariliGirisYapma() {

        LoginPage loginPage = girisEkraniniAc();

        ReportManager.step("Geçerli kullanıcı bilgileri ile giriş yapılıyor.");
        loginPage.girisYapma(ConfigManager.get("username"), ConfigManager.get("password"));

        Assert.assertTrue(loginPage.basariliGirisYapildiMi(),
                "Geçerli kullanıcı bilgileri ile giriş yapılamadı.");

        ReportManager.info("BAŞARILI GİRİŞ KONTROLÜ TAMAMLANDI");
    }

    /**
     * Hatalı giriş senaryoları: senaryo adı, kullanıcı adı, şifre.
     * İlk değer raporlarda hangi senaryonun çalıştığını göstermek içindir.
     */
    @DataProvider(name = "hataliGirisBilgileri")
    public Object[][] hataliGirisBilgileri() {

        String gecerliKullaniciAdi = ConfigManager.get("username");
        String gecerliSifre = ConfigManager.get("password");

        return new Object[][]{
                {"Hatalı kullanıcı adı", HATALI_KULLANICI_ADI, gecerliSifre},
                {"Hatalı şifre", gecerliKullaniciAdi, HATALI_SIFRE},
                {"Hatalı kullanıcı adı ve şifre", HATALI_KULLANICI_ADI, HATALI_SIFRE}
        };
    }

    /** Hatalı bilgilerle giriş yapılamadığını ve doğru hata mesajının gösterildiğini doğrular. */
    @Test(dataProvider = "hataliGirisBilgileri")
    public void hataliBilgilerleGirisYapilamamali(String senaryo, String kullaniciAdi, String sifre) {

        LoginPage loginPage = girisEkraniniAc();

        ReportManager.step("Senaryo: " + senaryo + " ile giriş deneniyor.");
        loginPage.girisYapma(kullaniciAdi, sifre);

        Assert.assertTrue(loginPage.girisHataMesajiGoruntulendiMi(),
                senaryo + ": hata mesajı görüntülenmedi.");
        Assert.assertEquals(loginPage.girisHataMesajiAlma(), GIRIS_HATA_MESAJI,
                senaryo + ": beklenen hata mesajı görüntülenmedi.");
        Assert.assertTrue(loginPage.girisEkraniGoruntuleniyorMu(),
                senaryo + ": başarısız girişten sonra login ekranında kalınmadı.");

        ReportManager.info("HATALI GİRİŞ KONTROLÜ BAŞARILI | " + senaryo);
    }
}