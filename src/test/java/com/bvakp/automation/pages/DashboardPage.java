package com.bvakp.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class DashboardPage extends BasePage {

    private final Locator gostergePanosuMetni;
    private final Locator acikVeriPortaliMenusu;

    /**
     * Giriş işleminden sonra açılan Dashboard ekranındaki
     * gerekli elementleri tanımlamak için kullanılır.
     *
     * @param page aktif Playwright sayfası
     */
    public DashboardPage(Page page) {
        super(page);

        gostergePanosuMetni = page
                .getByText(
                        "Gösterge Panosu",
                        new Page.GetByTextOptions().setExact(true)
                )
                .first();

        acikVeriPortaliMenusu = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Açık Veri Portalı")
        );
    }

    /**
     * Başarılı giriş sonrasında Gösterge Panosu ekranının
     * görüntülenip görüntülenmediğini kontrol etmek için kullanılır.
     *
     * @return Gösterge Panosu görünüyorsa true, görünmüyorsa false
     */
    public boolean gostergePanosuGoruntulenmeKontrolu() {

        gostergePanosuMetni.waitFor();

        return gostergePanosuMetni.isVisible();
    }

    /**
     * Dashboard ekranındaki Açık Veri Portalı menüsüne
     * geçiş yapmak için kullanılır.
     */
    public void acikVeriPortalinaGitme() {
        acikVeriPortaliMenusu.click();
    }

    /**
     * Gösterge Panosu alanının o anda görünür olup olmadığını
     * bekleme yapmadan kontrol etmek için kullanılır.
     *
     * @return Gösterge Panosu görünüyorsa true
     */
    public boolean gostergePanosuGorunuyorMu() {
        return gostergePanosuMetni.isVisible();
    }
}