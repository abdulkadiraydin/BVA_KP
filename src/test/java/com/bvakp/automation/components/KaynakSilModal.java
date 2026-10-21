package com.bvakp.automation.components;

import com.bvakp.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

/**
 * Açık Veri Portalı "Kaynağı Sil" onay modalı.
 *
 * <p>Locator'lar modal içine sınırlıdır; arka plandaki her satırda bulunan
 * "Sil" butonlarıyla karışmaz.
 */
public class KaynakSilModal extends BasePage {

    private static final int MODAL_TIMEOUT_MS = 10_000;

    private final Locator modal;
    private final Locator modalBasligi;
    private final Locator silButonu;

    public KaynakSilModal(Page page) {

        super(page);

        this.modal = page.getByRole(AriaRole.DIALOG);
        this.modalBasligi = modal.getByRole(AriaRole.HEADING,
                new Locator.GetByRoleOptions().setName("Kaynağı Sil").setExact(true));
        this.silButonu = modal.getByRole(AriaRole.BUTTON,
                new Locator.GetByRoleOptions().setName("Sil").setExact(true));
    }

    public boolean kaynakSilModalGoruntulendiMi() {
        return gorunurMu(modalBasligi, MODAL_TIMEOUT_MS);
    }

    /**
     * Modal açıldıysa ve içinde silinecek kaynağın adı yazıyorsa true.
     * Yanlış kaynağın silinmesini önlemek için onaydan önce kullanılır.
     */
    public boolean kaynakSilModalGoruntulendiMi(String kaynakAdi) {
        return kaynakSilModalGoruntulendiMi()
                && modal.getByText(kaynakAdi).isVisible();
    }

    public void silmeOnaylama() {
        silButonu.click();
    }

    public boolean kaynakSilModalKapandiMi() {
        return gorunmuyorMu(modalBasligi, MODAL_TIMEOUT_MS);
    }
}