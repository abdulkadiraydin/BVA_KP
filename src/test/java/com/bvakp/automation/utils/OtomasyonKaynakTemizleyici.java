package com.bvakp.automation.utils;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Testlerin Açık Veri Portalında oluşturduğu kaynakları siler.
 *
 * <p>Yalnızca adı tam olarak cleanup.name.prefix (varsayılan "otomasyon_") ile
 * başlayan kayıtlar silinir. Büyük/küçük harf duyarlıdır; açıklaması "Otomasyon ..."
 * olan gerçek bir kayıt eşleşmez.
 *
 * <p>Güvenlik: base.url'in host'u cleanup.allowed.hosts listesinde değilse
 * hiçbir şey silinmez. Bu, temizliğin yanlışlıkla canlı ortamda çalışmasını önler.
 *
 * <p>Ayarlar: cleanup.enabled, cleanup.allowed.hosts, cleanup.name.prefix, cleanup.max.delete
 */
public class OtomasyonKaynakTemizleyici extends AvpBaseTest {

    private static final String VARSAYILAN_ON_EK = "otomasyon_";
    private static final int VARSAYILAN_MAKSIMUM_SILME = 200;

    private static final String BOS_LISTE_MESAJI = "Henüz kaynak eklenmemiş. \"Kaynak Ekle\" ile başlayın.";

    /** Manuel çalıştırma: cleanup.enabled'dan bağımsızdır, ortam kilidi yine geçerlidir. */
    public static void main(String[] args) {
        kosuSonuTemizliginiCalistir();
    }

    /**
     * Temizliği testlerden bağımsız, yeni bir Playwright oturumunda çalıştırır.
     * Ortam izinli değilse tarayıcı açılmadan çıkar.
     */
    public static void kosuSonuTemizliginiCalistir() {

        if (!ortamIzinliMi()) {
            return;
        }

        OtomasyonKaynakTemizleyici temizleyici = new OtomasyonKaynakTemizleyici();

        ReportManager.info("OTOMASYON KAYNAK TEMİZLİĞİ İÇİN YENİ PLAYWRIGHT OTURUMU AÇILIYOR");

        try {
            // Testlerin Page'i kapanmış olabileceği için temizliğe özel oturum açılır.
            temizleyici.setUp();
            temizleyici.otomasyonKaynaklariniTemizle();
        } finally {
            PlaywrightManager.close("otomasyonKaynaklariniTemizleme", "TEMIZLIK");
            ReportManager.info("OTOMASYON KAYNAK TEMİZLİĞİ PLAYWRIGHT OTURUMU KAPATILDI");
        }
    }

    /**
     * base.url'in host'u cleanup.allowed.hosts listesinde mi kontrol eder.
     * Liste boşsa hiçbir ortamda silme yapılmaz.
     */
    private static boolean ortamIzinliMi() {

        String baseUrl = ConfigManager.get("base.url");
        String host = URI.create(baseUrl).getHost();

        Set<String> izinliHostlar = Arrays.stream(
                        ConfigManager.getOrDefault("cleanup.allowed.hosts", "").split(","))
                .map(String::trim)
                .filter(deger -> !deger.isEmpty())
                .map(deger -> deger.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        if (host == null || !izinliHostlar.contains(host.toLowerCase(Locale.ROOT))) {
            ReportManager.warn("Temizlik ATLANDI: '" + host + "' cleanup.allowed.hosts listesinde değil. "
                    + "Silme yalnızca izin verilen test ortamlarında yapılır.");
            return false;
        }

        return true;
    }

    private void otomasyonKaynaklariniTemizle() {

        String onEk = ConfigManager.getOrDefault("cleanup.name.prefix", VARSAYILAN_ON_EK);
        int maksimumSilme = maksimumSilmeSayisi();

        ReportManager.info("OTOMASYON KAYNAK TEMİZLİĞİ BAŞLADI | Ön ek: '" + onEk
                + "' | Üst sınır: " + maksimumSilme);

        avpOturumuHazirlama();

        Page aktifPage = page();
        OpenDataPage openDataPage = new OpenDataPage(aktifPage);

        ReportManager.info("Açık Veri Portalına geçiliyor.");
        openDataPage.acikVeriPortalinaGitme();

        ReportManager.info("'" + onEk + "' kriteri ile kaynaklar filtreleniyor.");
        openDataPage.kaynakArama(onEk);

        aramaSonuclariniBekle(aktifPage, onEk);

        int silinenKayitSayisi = 0;

        while (silinenKayitSayisi < maksimumSilme) {

            SilinecekKayit kayit = silinecekIlkKaydiBul(aktifPage, onEk);

            if (kayit == null) {
                ReportManager.info("Silinebilecek otomasyon kaynağı kalmadı.");
                break;
            }

            kaydiSil(aktifPage, kayit);
            silinenKayitSayisi++;

            ReportManager.info("Kaynak başarıyla silindi | Kaynak: " + kayit.ad()
                    + " | Toplam silinen: " + silinenKayitSayisi);
        }

        if (silinenKayitSayisi >= maksimumSilme) {
            ReportManager.warn("Silme üst sınırına ulaşıldı (" + maksimumSilme
                    + "). Kalan kayıtlar bir sonraki temizlikte silinecek.");
        }

        ReportManager.info("OTOMASYON KAYNAK TEMİZLİĞİ TAMAMLANDI | Toplam silinen kayıt: " + silinenKayitSayisi);
    }

    private record SilinecekKayit(Locator satir, String ad) { }

    /**
     * Adı ön ekle (büyük/küçük harf duyarlı) başlayan ilk görünür satırı bulur.
     * Kaynak adının sütun sırası varsayılmaz; satırdaki hücreler tek tek kontrol edilir.
     */
    private SilinecekKayit silinecekIlkKaydiBul(Page aktifPage, String onEk) {

        // Tablo her silmede yeniden çizildiği için locator her seferinde yeniden oluşturulur.
        Locator adaySatirlar = aktifPage.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(onEk));

        int satirSayisi = adaySatirlar.count();

        for (int i = 0; i < satirSayisi; i++) {

            Locator satir = adaySatirlar.nth(i);
            if (!satir.isVisible()) {
                continue;
            }

            Locator hucreler = satir.getByRole(AriaRole.GRIDCELL);
            int hucreSayisi = hucreler.count();

            for (int h = 0; h < hucreSayisi; h++) {

                Locator hucre = hucreler.nth(h);
                if (!hucre.isVisible()) {
                    continue;
                }

                String hucreMetni = hucre.innerText().trim();

                if (hucreMetni.startsWith(onEk)) {
                    ReportManager.info("Silinecek otomasyon kaynağı bulundu | " + hucreMetni);
                    return new SilinecekKayit(satir, hucreMetni);
                }
            }
        }

        return null;
    }

    private void kaydiSil(Page aktifPage, SilinecekKayit kayit) {

        // Aynı isimde birden fazla kayıt olabilir; silme, sayının azalmasıyla doğrulanır.
        int silmeOncesiSayi = gorunurKaynakSayisi(aktifPage, kayit.ad());

        ReportManager.info("Kaynak silme işlemi başlatılıyor | Kaynak: " + kayit.ad()
                + " | Aynı isimde görünür kayıt: " + silmeOncesiSayi);

        kayit.satir().getByRole(AriaRole.BUTTON, silButonu()).click();

        Locator dialog = aktifPage.getByRole(AriaRole.DIALOG);
        dialog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(10_000));

        dialog.getByRole(AriaRole.BUTTON, silButonu()).click();

        dialog.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(15_000));

        // Dialogun kapanması kaydın silindiğini garanti etmez; tablonun güncellenmesi beklenir.
        aktifPage.waitForCondition(
                () -> gorunurKaynakSayisi(aktifPage, kayit.ad()) < silmeOncesiSayi,
                new Page.WaitForConditionOptions().setTimeout(15_000));
    }

    private static Locator.GetByRoleOptions silButonu() {
        return new Locator.GetByRoleOptions().setName("Sil").setExact(true);
    }

    private int gorunurKaynakSayisi(Page aktifPage, String kaynakAdi) {

        Locator kaynakAdiLocator = aktifPage.getByText(kaynakAdi, new Page.GetByTextOptions().setExact(true));
        Locator kaynakSatirlari = aktifPage.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHas(kaynakAdiLocator));

        int gorunurSayi = 0;
        int satirSayisi = kaynakSatirlari.count();

        for (int i = 0; i < satirSayisi; i++) {
            if (kaynakSatirlari.nth(i).isVisible()) {
                gorunurSayi++;
            }
        }

        return gorunurSayi;
    }

    /** Arama sonrası en az bir eşleşen satır veya boş liste mesajı görünene kadar bekler. */
    private void aramaSonuclariniBekle(Page aktifPage, String onEk) {

        Locator eslesenSatir = aktifPage.getByRole(AriaRole.ROW)
                .filter(new Locator.FilterOptions().setHasText(onEk))
                .first();

        Locator bosListeMesaji = aktifPage.getByText(BOS_LISTE_MESAJI, new Page.GetByTextOptions().setExact(true));

        aktifPage.waitForCondition(
                () -> eslesenSatir.isVisible() || bosListeMesaji.isVisible(),
                new Page.WaitForConditionOptions().setTimeout(15_000));

        ReportManager.info("Otomasyon kaynak arama sonuçları yüklendi.");
    }

    private static int maksimumSilmeSayisi() {
        String value = ConfigManager.getOrDefault("cleanup.max.delete", String.valueOf(VARSAYILAN_MAKSIMUM_SILME));
        try {
            int sayi = Integer.parseInt(value);
            return sayi >= 1 ? sayi : VARSAYILAN_MAKSIMUM_SILME;
        } catch (NumberFormatException e) {
            return VARSAYILAN_MAKSIMUM_SILME;
        }
    }
}