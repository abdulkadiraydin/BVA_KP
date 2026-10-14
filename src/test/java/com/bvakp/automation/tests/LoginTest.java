package com.bvakp.automation.tests;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    private static final String HATALI_KULLANICI_ADI =
            "hatali.kullaniciadi";

    private static final String HATALI_SIFRE =
            "HataliSifre123!";

    private static final String GIRIS_HATA_MESAJI =
            "Invalid username or password.";


    /**
     * Aktif test thread'ine ait Page üzerinden
     * yeni LoginPage nesnesi oluşturmak için kullanılır.
     *
     * @return aktif teste ait LoginPage
     */
    private LoginPage loginPageOlusturma() {

        return new LoginPage(
                page()
        );
    }


    /**
     * Login sayfasının temel giriş alanları ile
     * açıldığını doğrulamak için kullanılır.
     */
    @Test(priority = 1)
    public void girisSayfasiAcma() {

        LoginPage loginPage =
                loginPageOlusturma();


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage.girisSayfasiAcma();


        ReportManager.step(
                "Login ekranındaki kullanıcı adı, şifre ve Giriş Yap alanları doğrulanıyor."
        );
        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı gerekli alanlarla birlikte görüntülenemedi."
        );


        ReportManager.info(
                "LOGIN SAYFASI AÇILIŞ KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Geçerli kullanıcı adı ve şifre ile
     * sisteme başarılı giriş yapılabildiğini doğrulamak için kullanılır.
     */
    @Test(priority = 2)
    public void basariliGirisYapma() {

        LoginPage loginPage =
                loginPageOlusturma();


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage.girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Geçerli kullanıcı bilgileri ile giriş yapılıyor."
        );

        loginPage.girisYapma(
                ConfigManager.get("username"),
                ConfigManager.get("password")
        );


        ReportManager.step(
                "Başarılı giriş sonrasında ana uygulama ekranının açıldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.basariliGirisYapildiMi(),
                "Geçerli kullanıcı bilgileri ile giriş yapılamadı."
        );


        ReportManager.info(
                "BAŞARILI GİRİŞ KONTROLÜ TAMAMLANDI"
        );
    }


    /**
     * Hatalı kullanıcı adı ve geçerli şifre ile
     * sisteme giriş yapılamadığını doğrulamak için kullanılır.
     */
    @Test(priority = 3)
    public void hataliKullaniciAdiIleGirisYapma() {

        LoginPage loginPage =
                loginPageOlusturma();


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage.girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Hatalı kullanıcı adı ve geçerli şifre ile giriş yapılıyor."
        );

        loginPage.girisYapma(
                HATALI_KULLANICI_ADI,
                ConfigManager.get("password")
        );


        ReportManager.step(
                "Giriş hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisHataMesajiGoruntulendiMi(),
                "Hatalı kullanıcı adı girişinde hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Görüntülenen giriş hata mesajının içeriği doğrulanıyor."
        );

        Assert.assertEquals(
                loginPage.girisHataMesajiAlma(),
                GIRIS_HATA_MESAJI,
                "Beklenen giriş hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Başarısız giriş sonrasında kullanıcının login ekranında kaldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Hatalı kullanıcı adı sonrasında kullanıcı login ekranında kalmadı."
        );


        ReportManager.info(
                "HATALI KULLANICI ADI KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Geçerli kullanıcı adı ve hatalı şifre ile
     * sisteme giriş yapılamadığını doğrulamak için kullanılır.
     */
    @Test(priority = 4)
    public void hataliSifreIleGirisYapma() {

        LoginPage loginPage =
                loginPageOlusturma();


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage.girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Geçerli kullanıcı adı ve hatalı şifre ile giriş yapılıyor."
        );

        loginPage.girisYapma(
                ConfigManager.get("username"),
                HATALI_SIFRE
        );


        ReportManager.step(
                "Giriş hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisHataMesajiGoruntulendiMi(),
                "Hatalı şifre girişinde hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Görüntülenen giriş hata mesajının içeriği doğrulanıyor."
        );

        Assert.assertEquals(
                loginPage.girisHataMesajiAlma(),
                GIRIS_HATA_MESAJI,
                "Beklenen giriş hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Başarısız giriş sonrasında kullanıcının login ekranında kaldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Hatalı şifre sonrasında kullanıcı login ekranında kalmadı."
        );


        ReportManager.info(
                "HATALI ŞİFRE KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Hatalı kullanıcı adı ve hatalı şifre ile
     * sisteme giriş yapılamadığını doğrulamak için kullanılır.
     */
    @Test(priority = 5)
    public void hataliKullaniciAdiVeSifreIleGirisYapma() {

        LoginPage loginPage =
                loginPageOlusturma();


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage.girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Hatalı kullanıcı adı ve hatalı şifre ile giriş yapılıyor."
        );

        loginPage.girisYapma(
                HATALI_KULLANICI_ADI,
                HATALI_SIFRE
        );


        ReportManager.step(
                "Giriş hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisHataMesajiGoruntulendiMi(),
                "Hatalı kullanıcı bilgileri için hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Görüntülenen giriş hata mesajının içeriği doğrulanıyor."
        );

        Assert.assertEquals(
                loginPage.girisHataMesajiAlma(),
                GIRIS_HATA_MESAJI,
                "Beklenen giriş hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Başarısız giriş sonrasında kullanıcının login ekranında kaldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage.girisEkraniGoruntuleniyorMu(),
                "Hatalı kullanıcı bilgileri sonrasında kullanıcı login ekranında kalmadı."
        );


        ReportManager.info(
                "HATALI KULLANICI ADI VE ŞİFRE KONTROLÜ BAŞARILI"
        );
    }
}