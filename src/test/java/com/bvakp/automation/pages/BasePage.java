package com.bvakp.automation.pages;

import com.bvakp.automation.core.config.ConfigManager;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Bütün sayfa ve modal sınıflarının ortak temeli.
 *
 * <p>Görünürlük kontrolleri bu sınıftaki yardımcılarla yapılır; sayfa sınıflarında
 * try/waitFor/catch kalıbı tekrar yazılmaz.
 */
public abstract class BasePage {

    /** Element/ekran yüklenmesi için varsayılan bekleme süresi. */
    protected static final int VARSAYILAN_BEKLEME_MS = 15_000;

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    /** Config dosyasında tanımlanan base URL'i açar. */
    public void openBaseUrl() {
        page.navigate(ConfigManager.get("base.url"));
    }

    public String getCurrentUrl() {
        return page.url();
    }

    public String getTitle() {
        return page.title();
    }

    /**
     * Ekranda çıkan bildirimin (toast) görünmesini bekler.
     *
     * @param bildirimMetni bildirimde geçen metin (tamamı gerekmez)
     */
    public void bildirimBekleme(String bildirimMetni) {

        page.getByText(bildirimMetni)
                .first()
                .waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(VARSAYILAN_BEKLEME_MS));

        ReportManager.info("Bildirim görüldü: " + bildirimMetni);
    }

    /** Varsayılan süre içinde element görünür olursa true döner. */
    protected boolean gorunurMu(Locator locator) {
        return gorunurMu(locator, VARSAYILAN_BEKLEME_MS);
    }

    /**
     * Element verilen süre içinde görünür olursa true, olmazsa false döner.
     *
     * <p>Locator birden fazla elemente denk gelirse ilki beklenir. Aksi halde
     * Playwright'ın "birden fazla eşleşme" hatası, element hiç yokmuş gibi
     * false'a dönüşür ve test yanlış sebeple başarısız görünür.
     */
    protected boolean gorunurMu(Locator locator, int timeoutMs) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    /**
     * Element görünmüyorsa (veya hiç yoksa) hemen true döner; görünüyorsa
     * kaybolması için en fazla verilen süre kadar bekler.
     *
     * <p>"Bu mesaj çıkmamalı" kontrollerinde kullanılır. gorunurMu ile yapılan
     * negatif kontrol tam bekleme süresini boşa harcar.
     *
     * <p>Dikkat: işlemden hemen sonra çağrılırsa, mesaj henüz ekrana gelmeden
     * true dönebilir. Önce beklenen bir sonucun (örneğin diğer hata mesajının)
     * göründüğü doğrulanmalı, negatif kontrol ondan sonra yapılmalıdır.
     */
    protected boolean gorunmuyorMu(Locator locator, int timeoutMs) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(timeoutMs));
            return true;
        } catch (PlaywrightException e) {
            return false;
        }
    }

    /** Varsayılan süre ile kaybolmayı bekler. */
    protected boolean gorunmuyorMu(Locator locator) {
        return gorunmuyorMu(locator, VARSAYILAN_BEKLEME_MS);
    }
}