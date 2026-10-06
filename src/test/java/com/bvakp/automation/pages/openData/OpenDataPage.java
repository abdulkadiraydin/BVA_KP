package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class OpenDataPage extends BasePage {

    // =========================
    // Açık Veri / Kaynak
    // =========================

    private final Locator acikVeriPortali;
    private final Locator kaynakEkleButton;
    private final Locator adInput;
    private final Locator tabloInput;
    private final Locator ilkTablo;
    private final Locator aciklamaInput;
    private final Locator kaynakEkleSubmitButton;
    private final Locator ileriButton;

    // =========================
    // Veri Seti Oluşturma
    // =========================

    private final Locator dataSetNameInput;
    private final Locator sorumluBirimSelect;
    private final Locator kategoriSelect;
    private final Locator lisansSelect;
    private final Locator dataSetAciklamaInput;
    private final Locator guncellemeTipiSelect;
    private final Locator takvimBaslangiciInput;
    private final Locator siklikInput;
    private final Locator aralikSelect;
    private final Locator onizleButton;
    private final Locator onayaGonderButton;
    private final Locator onayaGonderConfirmButton;

    public OpenDataPage(Page page) {

        super(page);

        // =========================
        // Açık Veri / Kaynak
        // =========================

        this.acikVeriPortali = page.getByText(
                "Açık Veri Portalı",
                new Page.GetByTextOptions().setExact(true)
        );

        this.kaynakEkleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        );

        this.adInput = page.locator("input[name='name']");

        this.tabloInput = page.locator(
                "input[role='combobox']"
        );

        this.ilkTablo = page.locator(
                "[role='option']"
        ).first();

        this.aciklamaInput = page.locator(
                "textarea[name='description']"
        );

        this.kaynakEkleSubmitButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Kaynak Ekle")
        ).last();

        this.ileriButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("İleri")
        );

        // =========================
        // Veri Seti Oluşturma
        // =========================

        this.dataSetNameInput = page.locator(
                "input[name='name']"
        );

        this.sorumluBirimSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Sorumlu Birim")
        );

        this.kategoriSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Kategori")
        );

        this.lisansSelect = page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Lisans")
        );

        this.dataSetAciklamaInput = page.locator(
                "textarea[name='description']"
        );

        this.guncellemeTipiSelect = page.locator(
                "div[role='combobox']"
        ).filter(
                new Locator.FilterOptions().setHasText("Manuel")
        );

        this.takvimBaslangiciInput = page.locator(
                "input[placeholder='DD.MM.YYYY']"
        );

        this.siklikInput = page.locator(
                "input[type='number'][min='1']"
        );

        this.aralikSelect = page.locator(
                "div[role='combobox'][aria-labelledby^='_r_bd_-label']"
        );

        this.onizleButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Önizle")
        );

        this.onayaGonderButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        );

        this.onayaGonderConfirmButton = page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Onaya Gönder")
        ).last();
    }

    // =========================
    // Açık Veri
    // =========================

    public void clickAcikVeriPortali() {
        acikVeriPortali.click();
    }

    // =========================
    // Kaynak Oluşturma
    // =========================

    public void clickKaynakEkle() {
        kaynakEkleButton.click();
    }

    public void enterAd(String ad) {
        adInput.fill(ad);
    }

    public void clickTablo() {
        tabloInput.click();
    }

    public void selectFirstTable() {
        ilkTablo.click();
    }

    public void enterAciklama(String aciklama) {
        aciklamaInput.fill(aciklama);
    }

    public void clickKaynakEkleSubmit() {
        kaynakEkleSubmitButton.click();
    }

    public boolean isSourceDisplayed(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        row.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10_000)
        );

        return row.isVisible();
    }

    public void deleteSource(String sourceName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sourceName));

        // Satırdaki Sil
        row.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Sil")
        ).click();

        // Popup'ın açılmasını bekle
        page.waitForTimeout(500);

        // Popup'taki Sil
        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Sil")
                        .setExact(true)
        ).last().click();

        // Backend işleminin tamamlanmasını bekle
        page.waitForTimeout(2000);

        page.reload();
    }

    public boolean isSourceDeleted(String sourceName) {

        Locator source = page.locator(
                "[role='gridcell'][data-field='name']"
        ).getByText(
                sourceName,
                new Locator.GetByTextOptions()
                        .setExact(true)
        );

        return source.count() == 0;
    }

    // =========================
    // Mevcut Veri Setini Seçme
    // =========================

    public void selectDataSet(String dataSetName) {

        Locator row = page.locator("[role='row']")
                .filter(new Locator.FilterOptions()
                        .setHasText(dataSetName));

        row.getByRole(
                AriaRole.CHECKBOX,
                new Locator.GetByRoleOptions()
                        .setName("Seçim")
        ).check();
    }

    public void clickIleri() {
        ileriButton.click();
    }

    // =========================
    // Veri Seti Oluşturma
    // =========================

    public void enterDataSetName(String name) {
        dataSetNameInput.fill(name);
    }

    public void selectSorumluBirim(String birim) {

        sorumluBirimSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(birim))
                .click();
    }

    public void selectKategori(String kategori) {

        kategoriSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(kategori))
                .click();
    }

    public void selectLisans(String lisans) {

        lisansSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(lisans))
                .click();
    }

    public void enterDataSetAciklama(String aciklama) {
        dataSetAciklamaInput.fill(aciklama);
    }

    public void selectGuncellemeTipi(String tip) {

        guncellemeTipiSelect.click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(tip))
                .click();
    }

    public void enterTakvimBaslangici(String tarih) {
        takvimBaslangiciInput.fill(tarih);
    }

    public void enterSiklik(String siklik) {
        siklikInput.fill(siklik);
    }

    public void selectAralik(String aralikValue) {

        Locator aralikLabel = page.locator("label")
                .filter(new Locator.FilterOptions()
                        .setHasText("Aralık"));

        String forId = aralikLabel.getAttribute("for");

        page.locator("#" + forId).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(aralikValue))
                .click();
    }

    public void clickOnizle() {
        onizleButton.click();
    }

    public void clickOnayaGonder() {
        onayaGonderButton.click();
    }

    public void confirmOnayaGonder() {
        onayaGonderConfirmButton.click();
    }

    public void veriSetiOlusturmaSekmesineGitme() {

        page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions()
                        .setName("Veri Seti Oluşturma")
        ).click();
    }

    /**
     * Kaynak Ekle ekranını açar.
     */
    public void kaynakEkleEkraniniAcma() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
        ).click();
    }

    /**
     * Oluşturulan kaynağın kaynak listesinde görüntülendiğini kontrol eder.
     *
     * @param kaynakAdi doğrulanacak kaynak adı
     * @return kaynak listede görünüyorsa true
     */
    public boolean kaynakListesindeGoruntulendiMi(
            String kaynakAdi) {

        Locator kaynak =
                page.getByRole(
                        AriaRole.GRIDCELL,
                        new Page.GetByRoleOptions()
                                .setName(kaynakAdi)
                                .setExact(true)
                );

        kaynak.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return kaynak.isVisible();
    }

/**
 * Verilen kaynak adına ait Görüntüle butonuna tıklar.
 *
 * @param kaynakAdi görüntülenecek kaynak adı
 */


/**yeni**/
    /**
     * Sol menüden Açık Veri Portalı ekranına geçer.
     */
    public void acikVeriPortalinaGitme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Açık Veri Portalı")
                        .setExact(true)
        ).click();
    }


    /**
     * Kaynak Tablo / Sorgu bölümünün görüntülendiğini kontrol eder.
     *
     * @return bölüm görüntüleniyorsa true
     */
    public boolean kaynakTabloSorguGoruntulendiMi() {

        try {

            Locator baslik =
                    page.getByText(
                            "Kaynak Tablo / Sorgu",
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            baslik.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return baslik.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak Ekle ekranını açar.
     */
    public void kaynakEklemeEkraniniAcma() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
        ).click();
    }


    /**
     * Belirtilen kaynak adına ait tablo satırını bulur.
     *
     * Kaynak adı exact olarak eşleştirilir.
     *
     * @param kaynakAdi aranacak kaynak adı
     * @return kaynak satırı
     */
    private Locator kaynakSatiriBulma(
            String kaynakAdi) {

        Locator kaynakAdiLocator =
                page.getByText(
                        kaynakAdi,
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        Locator kaynakSatiri =
                page.getByRole(
                        AriaRole.ROW
                ).filter(
                        new Locator.FilterOptions()
                                .setHas(kaynakAdiLocator)
                );

        kaynakSatiri.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(15000)
        );

        return kaynakSatiri;
    }


    /**
     * Oluşturulan kaynağın listede bulunduğunu kontrol eder.
     *
     * @param kaynakAdi kontrol edilecek kaynak adı
     * @return kaynak listede görünüyorsa true
     */
    public boolean kaynakListedeMi(
            String kaynakAdi) {

        try {

            return kaynakSatiriBulma(
                    kaynakAdi
            ).isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak satırında oluşturulan ad ve açıklamanın
     * birlikte görüntülendiğini kontrol eder.
     *
     * @param kaynakAdi kaynak adı
     * @param kaynakAciklamasi kaynak açıklaması
     * @return bilgiler doğruysa true
     */
    public boolean kaynakBilgileriDogruMu(
            String kaynakAdi,
            String kaynakAciklamasi) {

        try {

            Locator kaynakSatiri =
                    kaynakSatiriBulma(
                            kaynakAdi
                    );

            String satirMetni =
                    kaynakSatiri
                            .innerText();

            return satirMetni.contains(
                    kaynakAdi
            )
                    && satirMetni.contains(
                    kaynakAciklamasi
            );

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Belirtilen kaynak satırındaki
     * Sil butonuna tıklar.
     *
     * Güvenlik amacıyla yalnızca otomasyon tarafından
     * oluşturulan kaynakların silinmesine izin verilir.
     *
     * @param kaynakAdi silinecek kaynak adı
     */
    public void kaynakSilmeButonunaTiklama(
            String kaynakAdi) {

        if (!kaynakAdi.startsWith(
                "otomasyon_"
        )) {

            throw new IllegalArgumentException(
                    "Otomasyon tarafından oluşturulmayan kaynak silinemez: "
                            + kaynakAdi
            );
        }

        Locator kaynakSatiri =
                kaynakSatiriBulma(
                        kaynakAdi
                );

        kaynakSatiri
                .getByRole(
                        AriaRole.BUTTON,
                        new Locator.GetByRoleOptions()
                                .setName("Sil")
                                .setExact(true)
                )
                .click();
    }


    /**
     * Kaynağı Sil dialogunun açıldığını
     * ve doğru kaynak adını içerdiğini doğrular.
     *
     * @param kaynakAdi silinmesi beklenen kaynak adı
     * @return doğru silme dialogu görüntüleniyorsa true
     */
    public boolean kaynakSilmeOnayiGoruntulendiMi(
            String kaynakAdi) {

        try {

            Locator dialog =
                    page.getByRole(
                            AriaRole.DIALOG
                    );

            dialog.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            Locator baslik =
                    dialog.getByText(
                            "Kaynağı Sil",
                            new Locator.GetByTextOptions()
                                    .setExact(true)
                    );

            Locator kaynakAdiMetni =
                    dialog.getByText(
                            kaynakAdi,
                            new Locator.GetByTextOptions()
                                    .setExact(false)
                    );

            return dialog.isVisible()
                    && baslik.isVisible()
                    && kaynakAdiMetni.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynağı Sil dialogundaki
     * Sil butonuna tıklar.
     */
    public void kaynakSilmeOnayiVerme() {

        Locator dialog =
                page.getByRole(
                        AriaRole.DIALOG
                );

        dialog.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        dialog.getByRole(
                AriaRole.BUTTON,
                new Locator.GetByRoleOptions()
                        .setName("Sil")
                        .setExact(true)
        ).click();
    }


    /**
     * Silme işleminden sonra dialogun
     * kapandığını doğrular.
     *
     * @return dialog kapandıysa true
     */
    public boolean kaynakSilmeDialoguKapandiMi() {

        try {

            page.getByRole(
                    AriaRole.DIALOG
            ).waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(15000)
            );

            return true;

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Silinen kaynağın artık listede
     * görüntülenmediğini doğrular.
     *
     * @param kaynakAdi silinen kaynak adı
     * @return kaynak artık listede değilse true
     */
    public boolean kaynakListedenSilindiMi(
            String kaynakAdi) {

        Locator kaynak =
                page.getByText(
                        kaynakAdi,
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        try {

            kaynak.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(15000)
            );

        } catch (PlaywrightException e) {

            /*
             * Silme sonrasında kayıt tamamen DOM'dan
             * kaldırılmış olabilir.
             */
        }

        return kaynak.count() == 0
                || !kaynak.isVisible();
    }
    /**
     * Kaynak Tablo / Sorgu listesinde görüntülenebilir
     * herhangi bir kaynak bulunup bulunmadığını kontrol eder.
     *
     * @return görüntülenebilir kaynak varsa true
     */
    public boolean kaynakKaydiVarMi() {

        Locator satirlar =
                page.getByRole(
                        AriaRole.ROW
                );

        int satirSayisi =
                satirlar.count();

        for (int i = 0;
             i < satirSayisi;
             i++) {

            Locator satir =
                    satirlar.nth(i);

            Locator goruntuleButonu =
                    satir.getByLabel(
                            "Görüntüle"
                    );

            if (goruntuleButonu.count() > 0) {

                return true;
            }
        }

        return false;
    }


    /**
     * Kaynak listesindeki ilk görüntülenebilir
     * kaydın Görüntüle butonuna tıklar.
     */
    public void ilkKaynagiGoruntuleme() {

        Locator satirlar =
                page.getByRole(
                        AriaRole.ROW
                );

        int satirSayisi =
                satirlar.count();

        for (int i = 0;
             i < satirSayisi;
             i++) {

            Locator satir =
                    satirlar.nth(i);

            Locator goruntuleButonu =
                    satir.getByLabel(
                            "Görüntüle"
                    );

            if (goruntuleButonu.count() > 0) {

                goruntuleButonu.click();

                return;
            }
        }

        throw new IllegalStateException(
                "Görüntülenebilecek kaynak bulunamadı."
        );
    }


    /**
     * Belirtilen kaynak satırındaki
     * Görüntüle butonuna tıklar.
     *
     * @param kaynakAdi görüntülenecek kaynak adı
     */
    public void kaynakGoruntulemeButonunaTiklama(
            String kaynakAdi) {

        Locator kaynakSatiri =
                kaynakSatiriBulma(
                        kaynakAdi
                );

        kaynakSatiri
                .getByLabel(
                        "Görüntüle"
                )
                .click();
    }


    /**
     * Genel Bilgi sekmesine geçer.
     */
    public void genelBilgiSekmesineTiklama() {

        page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions()
                        .setName("Genel Bilgi")
                        .setExact(true)
        ).click();
    }


    /**
     * Kaynak detay ekranındaki Genel Bilgi
     * bölümünün açıldığını doğrular.
     *
     * Kaynak Bilgileri ve Kayıt Bilgileri
     * başlıklarının birlikte görüntülenmesi beklenir.
     *
     * @return Genel Bilgi ekranı doğru açıldıysa true
     */
    public boolean genelBilgiSayfasiGoruntulendiMi() {

        try {

            Locator kaynakBilgileri =
                    page.getByRole(
                            AriaRole.HEADING,
                            new Page.GetByRoleOptions()
                                    .setName("Kaynak Bilgileri")
                                    .setExact(true)
                    );

            Locator kayitBilgileri =
                    page.getByRole(
                            AriaRole.HEADING,
                            new Page.GetByRoleOptions()
                                    .setName("Kayıt Bilgileri")
                                    .setExact(true)
                    );

            kaynakBilgileri.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            kayitBilgileri.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return kaynakBilgileri.isVisible()
                    && kayitBilgileri.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Önizleme sekmesine geçer.
     */
    public void onizlemeSekmesineTiklama() {

        page.getByRole(
                AriaRole.TAB,
                new Page.GetByRoleOptions()
                        .setName("Önizleme")
                        .setExact(true)
        ).click();
    }


    /**
     * Kaynak Önizleme ekranının açıldığını doğrular.
     *
     * Recorder çıktısındaki sabit açıklama metni ile
     * en az bir tablo hücresinin oluşması kontrol edilir.
     *
     * @return Önizleme ekranı doğru açıldıysa true
     */
    public boolean onizlemeSayfasiGoruntulendiMi() {

        try {

            Locator aciklama =
                    page.getByText(
                            "Kaynaktan gelen ham veridir; anonimleştirme kuralları henüz uygulanmamıştır.",
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            aciklama.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            Locator tabloHucreleri =
                    page.locator(
                            "td"
                    );

            return aciklama.isVisible()
                    && tabloHucreleri.count() > 0;

        } catch (PlaywrightException e) {

            return false;
        }
    }
    /**
     * Belirtilen kaynak satırındaki
     * Düzenle butonuna tıklar.
     *
     * @param kaynakAdi düzenlenecek kaynak adı
     */
    public void kaynakDuzenlemeButonunaTiklama(
            String kaynakAdi) {

        Locator kaynakSatiri =
                kaynakSatiriBulma(
                        kaynakAdi
                );

        kaynakSatiri
                .getByLabel(
                        "Düzenle"
                )
                .click();
    }


    /**
     * Kaynak düzenleme ekranının açıldığını
     * temel form alanları üzerinden doğrular.
     *
     * @return düzenleme ekranı açıldıysa true
     */
    public boolean kaynakDuzenlemeEkraniGoruntulendiMi() {

        try {

            Locator adAlani =
                    page.getByLabel(
                            "Ad *"
                    );

            Locator aciklamaAlani =
                    page.getByLabel(
                            "Açıklama"
                    );

            Locator kaydetButonu =
                    page.getByRole(
                            AriaRole.BUTTON,
                            new Page.GetByRoleOptions()
                                    .setName("Kaydet")
                                    .setExact(true)
                    );

            adAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return adAlani.isVisible()
                    && aciklamaAlani.isVisible()
                    && kaydetButonu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Düzenleme ekranındaki mevcut kaynak adını döndürür.
     *
     * @return mevcut kaynak adı
     */
    public String duzenlemeKaynakAdiAlma() {

        return page.getByLabel(
                        "Ad *"
                )
                .inputValue()
                .trim();
    }


    /**
     * Düzenleme ekranındaki mevcut açıklamayı döndürür.
     *
     * @return mevcut açıklama
     */
    public String duzenlemeKaynakAciklamasiAlma() {

        return page.getByLabel(
                        "Açıklama"
                )
                .inputValue()
                .trim();
    }


    /**
     * Kaynak adını yeni değerle değiştirir.
     *
     * @param yeniKaynakAdi yeni kaynak adı
     */
    public void kaynakAdiniGuncelleme(
            String yeniKaynakAdi) {

        page.getByLabel(
                "Ad *"
        ).fill(
                yeniKaynakAdi
        );
    }


    /**
     * Kaynak açıklamasını yeni değerle değiştirir.
     *
     * @param yeniAciklama yeni açıklama
     */
    public void kaynakAciklamasiniGuncelleme(
            String yeniAciklama) {

        page.getByLabel(
                "Açıklama"
        ).fill(
                yeniAciklama
        );
    }


    /**
     * Kaynak düzenleme işlemini kaydeder.
     */
    public void kaynakDuzenlemeyiKaydetme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaydet")
                        .setExact(true)
        ).click();
    }
    /**
     * Kaynak ara alanına verilen kriteri yazar
     * ve arama işlemini başlatır.
     *
     * @param aramaKriteri aranacak kaynak değeri
     */
    public void kaynakArama(
            String aramaKriteri) {

        Locator kaynakAramaAlani =
                page.getByPlaceholder(
                        "Kaynak ara"
                );

        kaynakAramaAlani.fill(
                aramaKriteri
        );

        kaynakAramaAlani.press(
                "Enter"
        );
    }


    /**
     * Arama sonucunda görüntülenen kaynak kayıtlarının
     * arama kriteriyle uyumlu olduğunu kontrol eder.
     *
     * Yalnızca görünür ve Görüntüle aksiyonu bulunan
     * gerçek kaynak satırları değerlendirilir.
     *
     * @param aramaKriteri aranacak kaynak kriteri
     * @return bütün görünür kaynak satırları kriteri içeriyorsa true
     */
    public boolean kaynakAramaSonuclariUygunMu(
            String aramaKriteri) {

        Locator satirlar =
                page.getByRole(
                        AriaRole.ROW
                );

        int bulunanKayitSayisi =
                0;

        String kriter =
                aramaKriteri
                        .trim()
                        .toLowerCase();


        for (int i = 0;
             i < satirlar.count();
             i++) {

            Locator satir =
                    satirlar.nth(i);


            /*
             * Görünür olmayan satırlar değerlendirilmez.
             */
            if (!satir.isVisible()) {
                continue;
            }


            /*
             * Görüntüle butonu bulunan satırlar
             * gerçek kaynak kayıtlarıdır.
             */
            Locator goruntuleButonu =
                    satir.getByLabel(
                            "Görüntüle"
                    );

            if (goruntuleButonu.count() == 0) {
                continue;
            }


            bulunanKayitSayisi++;


            /*
             * HTML td varsayımı yapılmaz.
             * Satırın erişilebilir/görünür metni doğrudan alınır.
             */
            String satirMetni =
                    satir.innerText()
                            .trim()
                            .toLowerCase();


            ReportManager.info(
                    "Arama sonucu kontrol edilen satır | "
                            + satirMetni
            );


            if (!satirMetni.contains(
                    kriter
            )) {

                ReportManager.info(
                        "Arama kriteriyle uyuşmayan satır bulundu"
                                + " | Kriter: "
                                + aramaKriteri
                                + " | Satır: "
                                + satirMetni
                );

                return false;
            }
        }


        ReportManager.info(
                "Arama kriteriyle uyumlu görünür kaynak sayısı: "
                        + bulunanKayitSayisi
        );


        return bulunanKayitSayisi > 0;
    }
    /**
     * Arama sonucunda kaynak bulunamadığında
     * boş liste mesajının görüntülendiğini kontrol eder.
     *
     * @return boş liste mesajı görünüyorsa true
     */
    public boolean kaynakBulunamadiMesajiGoruntulendiMi() {

        try {

            Locator mesaj =
                    page.getByText(
                            "Henüz kaynak eklenmemiş. \"Kaynak Ekle\" ile başlayın.",
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            mesaj.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return mesaj.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }
}