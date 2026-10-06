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
     * Login sayfasının kullanıcı adı, şifre ve
     * Sign In butonu ile görüntülendiğini doğrular.
     */
    @Test(priority = 1)
    public void girisSayfasiAcma() {

        LoginPage loginPage =
                new LoginPage(page);


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage
                .girisSayfasiAcma();


        ReportManager.step(
                "Login ekranındaki kullanıcı adı, şifre ve Sign In alanları doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Login ekranı gerekli alanlarla birlikte görüntülenemedi."
        );


        ReportManager.info(
                "LOGIN SAYFASI AÇILIŞ KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Geçerli kullanıcı adı ve şifre ile
     * sisteme başarılı giriş yapılabildiğini doğrular.
     */
    @Test(priority = 2)
    public void basariliGirisYapma() {

        LoginPage loginPage =
                new LoginPage(page);


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage
                .girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Geçerli kullanıcı adı giriliyor."
        );

        loginPage
                .kullaniciAdiGirme(
                        ConfigManager.get("username")
                );


        ReportManager.step(
                "Geçerli kullanıcı şifresi giriliyor."
        );

        loginPage
                .sifreGirme(
                        ConfigManager.get("password")
                );


        ReportManager.step(
                "Sign In butonuna tıklanıyor."
        );

        loginPage
                .girisButonunaTiklama();


        ReportManager.step(
                "Başarılı giriş sonrasında ana uygulama ekranının açıldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .basariliGirisYapildiMi(),
                "Geçerli kullanıcı bilgileri ile giriş yapılamadı."
        );


        ReportManager.info(
                "BAŞARILI GİRİŞ KONTROLÜ TAMAMLANDI"
        );
    }


    /**
     * Hatalı kullanıcı adı ve doğru şifre ile
     * sisteme giriş yapılamadığını doğrular.
     */
    @Test(priority = 3)
    public void hataliKullaniciAdiIleGirisYapma() {

        LoginPage loginPage =
                new LoginPage(page);


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage
                .girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Hatalı kullanıcı adı giriliyor."
        );

        loginPage
                .kullaniciAdiGirme(
                        HATALI_KULLANICI_ADI
                );


        ReportManager.step(
                "Geçerli kullanıcı şifresi giriliyor."
        );

        loginPage
                .sifreGirme(
                        ConfigManager.get("password")
                );


        ReportManager.step(
                "Sign In butonuna tıklanıyor."
        );

        loginPage
                .girisButonunaTiklama();


        ReportManager.step(
                "Hatalı kullanıcı adı için giriş hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisHataMesajiGoruntulendiMi(),
                "Hatalı kullanıcı adı girişinde hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Görüntülenen giriş hata mesajının içeriği doğrulanıyor."
        );

        Assert.assertEquals(
                loginPage
                        .girisHataMesajiAlma(),
                GIRIS_HATA_MESAJI,
                "Beklenen giriş hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Başarısız giriş sonrasında kullanıcının login ekranında kaldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Hatalı kullanıcı adı sonrasında kullanıcı login ekranında kalmadı."
        );


        ReportManager.info(
                "HATALI KULLANICI ADI KONTROLÜ BAŞARILI"
        );
    }


    /**
     * Hem kullanıcı adı hem de şifre hatalı olduğunda
     * sisteme giriş yapılamadığını doğrular.
     */
    @Test(priority = 4)
    public void hataliKullaniciAdiVeSifreIleGirisYapma() {

        LoginPage loginPage =
                new LoginPage(page);


        ReportManager.step(
                "Login sayfası açılıyor."
        );

        loginPage
                .girisSayfasiAcma();


        ReportManager.step(
                "Login ekranının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Login ekranı görüntülenemedi."
        );


        ReportManager.step(
                "Hatalı kullanıcı adı giriliyor."
        );

        loginPage
                .kullaniciAdiGirme(
                        HATALI_KULLANICI_ADI
                );


        ReportManager.step(
                "Hatalı kullanıcı şifresi giriliyor."
        );

        loginPage
                .sifreGirme(
                        HATALI_SIFRE
                );


        ReportManager.step(
                "Sign In butonuna tıklanıyor."
        );

        loginPage
                .girisButonunaTiklama();


        ReportManager.step(
                "Hatalı kullanıcı adı ve şifre için giriş hata mesajının görüntülendiği doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisHataMesajiGoruntulendiMi(),
                "Hatalı kullanıcı bilgileri için hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Görüntülenen giriş hata mesajının içeriği doğrulanıyor."
        );

        Assert.assertEquals(
                loginPage
                        .girisHataMesajiAlma(),
                GIRIS_HATA_MESAJI,
                "Beklenen giriş hata mesajı görüntülenmedi."
        );


        ReportManager.step(
                "Başarısız giriş sonrasında kullanıcının login ekranında kaldığı doğrulanıyor."
        );

        Assert.assertTrue(
                loginPage
                        .girisEkraniGoruntuleniyorMu(),
                "Hatalı kullanıcı bilgileri sonrasında kullanıcı login ekranında kalmadı."
        );


        ReportManager.info(
                "HATALI KULLANICI ADI VE ŞİFRE KONTROLÜ BAŞARILI"
        );
    }
}