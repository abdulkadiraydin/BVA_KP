package com.bvakp.automation.core.auth;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.pages.LoginPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Page;

/** AVP'ye rol kullanıcısıyla giriş yapar ve gerektiğinde kullanıcıyı değiştirir. */
public final class AvpOturum {

  /**
   * Keycloak brute force koruması, aynı kullanıcıyla 1 sn'den kısa aralıklı girişleri şüpheli sayıp
   * kullanıcıyı geçici kilitliyor (user_temporarily_disabled). Testler paralel çalışır; girişler
   * sırayla ve aralarında en az bu süre bırakılarak yapılır.
   */
  private static final long GIRISLER_ARASI_MIN_SURE_MS = 1_500;

  private static final Object GIRIS_KILIDI = new Object();
  private static long sonGirisZamaniMs = 0;

  /** Her test kendi iş parçacığında koşar; o testte açık olan kullanıcı. */
  private static final ThreadLocal<String> AKTIF_KULLANICI = new ThreadLocal<>();

  private AvpOturum() {}

  /** Giriş ekranı açıkken verilen rolün kullanıcısıyla giriş yapar. */
  public static void girisYapma(Page page, PortalRol rol) {

    LoginPage loginPage = new LoginPage(page);

    synchronized (GIRIS_KILIDI) {
      girisAraliginiBekle();

      loginPage.girisYapma(rol.kullaniciAdi(), rol.parola());
      sonGirisZamaniMs = System.currentTimeMillis();

      if (!loginPage.basariliGirisYapildiMi()) {
        throw new IllegalStateException(
            "AVP kullanıcı girişi başarısız ("
                + rol
                + "). Gösterge Panosu görüntülenemedi. "
                + "Keycloak brute force kilidi olabilir (Events: user_temporarily_disabled).");
      }
    }

    AKTIF_KULLANICI.set(rol.kullaniciAdi());
    ReportManager.info("Giriş yapıldı | Rol: " + rol + " | Kullanıcı: " + rol.kullaniciAdi());
  }

  /**
   * Açık kullanıcı rolün kullanıcısı değilse oturumu kapatıp o kullanıcıyla giriş yapar. Bütün
   * roller aynı kullanıcıysa hiçbir şey yapmaz.
   *
   * @return kullanıcı değiştiyse true; sayfa Gösterge Panosu'na döner
   */
  public static boolean rolIleGirisYapma(Page page, PortalRol rol) {

    if (rol.kullaniciAdi().equals(AKTIF_KULLANICI.get())) {
      return false;
    }

    ReportManager.step("Kullanıcı değiştiriliyor | Rol: " + rol + " | " + rol.kullaniciAdi());

    oturumuKapatma(page);
    page.navigate(ConfigManager.get("base.url"));

    if (new LoginPage(page).oturumDurumunuBekle() != LoginPage.OturumDurumu.LOGIN_EKRANI) {
      throw new IllegalStateException("Oturum kapatılamadı; giriş ekranı açılmadı.");
    }

    girisYapma(page, rol);
    return true;
  }

  /**
   * Açık kullanıcı bilgisini siler. Kayıtlı oturumla açılan testlerde oturumun kime ait olduğu
   * bilinmediği için çağrılır; böylece bir önceki testten kalan bilgi yanlış yönlendirmez.
   */
  public static void aktifKullaniciSifirlama() {
    AKTIF_KULLANICI.remove();
  }

  /** Çerezleri ve tarayıcı depolamasını silerek oturumu kapatır. */
  private static void oturumuKapatma(Page page) {
    page.context().clearCookies();
    page.evaluate("() => { localStorage.clear(); sessionStorage.clear(); }");
  }

  /**
   * Bir önceki girişten bu yana en az GIRISLER_ARASI_MIN_SURE_MS geçmesini bekler.
   *
   * <p>Bilinçli sabit bekleme: bir olayın hazır olmasını değil, Keycloak'un hızlı giriş kuralına
   * uyumu sağlar.
   */
  @SuppressWarnings("java:S2925")
  private static void girisAraliginiBekle() {
    long bekle = GIRISLER_ARASI_MIN_SURE_MS - (System.currentTimeMillis() - sonGirisZamaniMs);
    if (bekle > 0) {
      try {
        Thread.sleep(bekle);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
