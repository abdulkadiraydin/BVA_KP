package com.bvakp.automation.tests.publicPortal;

import com.bvakp.automation.base.BaseTest;
import com.bvakp.automation.core.auth.PublicPortalRol;
import com.bvakp.automation.pages.publicPortal.PublicPortalLoginPage;
import com.bvakp.automation.reporting.ReportManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Kamuya açık portalın (CKAN) giriş, çıkış ve girişsiz erişim testleri.
 *
 * <p>Testler talep rolünün ({@link PublicPortalRol#TALEP}) kullanıcısıyla çalışır. BaseTest'ten
 * türer: her test temiz bir tarayıcıyla, giriş yapılmamış olarak başlar. Hatalı giriş tek
 * senaryodur; art arda hatalı girişler kullanıcıyı kilitleyebilir.
 */
public class PublicPortalLoginTest extends BaseTest {

  private static final String HATALI_SIFRE = "HataliSifre123!";
  private static final String GIRIS_HATA_MESAJI = "Giriş hatası. Kullanıcı adı ya da şifre hatalı.";

  /** Ana sayfayı açar ve giriş ekranına geçer. */
  private PublicPortalLoginPage girisEkraniniAcma() {

    PublicPortalLoginPage loginPage = new PublicPortalLoginPage(page());

    ReportManager.step("Açık portal açılıyor, giriş ekranına geçiliyor.");
    loginPage.anaSayfaAcma();
    loginPage.girisEkraniniAcma();
    Assert.assertTrue(loginPage.girisEkraniAcikMi(), "Ön koşul: giriş ekranı açılmadı.");

    return loginPage;
  }

  /** Geçerli bilgilerle giriş yapılınca hesap menüsünde kullanıcının göründüğünü doğrular. */
  @Test
  public void basariliGirisYapma() {

    PublicPortalLoginPage loginPage = girisEkraniniAcma();
    String kullanici = PublicPortalRol.TALEP.kullaniciAdi();

    ReportManager.step("Giriş yapılıyor: " + kullanici);
    loginPage.girisYapma(kullanici, PublicPortalRol.TALEP.parola());

    Assert.assertTrue(
        loginPage.girisYapildiMi(kullanici), "Giriş yapılamadı; hesap menüsü görünmedi.");

    ReportManager.pass("Açık portala giriş yapıldı: " + kullanici);
  }

  /** Hatalı şifreyle giriş yapılamadığını ve hata mesajının gösterildiğini doğrular. */
  @Test
  public void hataliSifreIleGirisEngellenir() {

    PublicPortalLoginPage loginPage = girisEkraniniAcma();

    ReportManager.step("Hatalı şifreyle giriş deneniyor.");
    loginPage.girisYapma(PublicPortalRol.TALEP.kullaniciAdi(), HATALI_SIFRE);

    Assert.assertEquals(
        loginPage.girisHataMesajiAlma(), GIRIS_HATA_MESAJI, "Giriş hata mesajı farklı.");
    Assert.assertTrue(loginPage.girisEkraniAcikMi(), "Hatalı şifreyle giriş ekranından çıkıldı.");

    ReportManager.pass("Hatalı şifreyle giriş engellendi.");
  }

  /** Çıkış yapılınca hesap menüsünün kalkıp "Giriş Yap" linkinin göründüğünü doğrular. */
  @Test
  public void cikisYapma() {

    PublicPortalLoginPage loginPage = new PublicPortalLoginPage(page());
    String kullanici = PublicPortalRol.TALEP.kullaniciAdi();

    loginPage.rolIleGirisYapma(PublicPortalRol.TALEP);
    Assert.assertTrue(loginPage.girisYapildiMi(kullanici), "Ön koşul: giriş yapılamadı.");

    ReportManager.step("Çıkış yapılıyor.");
    loginPage.cikisYapma();

    Assert.assertTrue(loginPage.cikisYapildiMi(), "Çıkış yapılamadı; hesap menüsü hâlâ görünüyor.");

    ReportManager.pass("Açık portaldan çıkış yapıldı.");
  }

  /** Giriş yapmadan "Veri Talebi Oluştur"a tıklanınca giriş ekranına yönlendirildiğini doğrular. */
  @Test
  public void girisYapmadanTalepGirisEkraninaYonlendirir() {

    PublicPortalLoginPage loginPage = new PublicPortalLoginPage(page());

    ReportManager.step("Giriş yapmadan Veri Talebi Oluştur'a tıklanıyor.");
    loginPage.anaSayfaAcma();
    loginPage.anaSayfadanTalepOlusturma();

    Assert.assertTrue(loginPage.girisEkraniAcikMi(), "Giriş ekranına yönlendirilmedi.");
    Assert.assertTrue(
        loginPage.adresAlma().contains("/user/login"),
        "Adres giriş sayfası değil: " + loginPage.adresAlma());

    ReportManager.pass("Girişsiz talep isteği giriş ekranına yönlendirildi.");
  }
}
