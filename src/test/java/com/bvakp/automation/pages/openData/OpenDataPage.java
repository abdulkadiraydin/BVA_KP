package com.bvakp.automation.pages.openData;

import com.bvakp.automation.pages.BasePage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class OpenDataPage extends BasePage {


    private final Locator acikVeriPortali;


    public OpenDataPage(Page page) {

        super(page);

        // =========================
        // Açık Veri / Kaynak
        // =========================

        this.acikVeriPortali = page.getByText(
                "Açık Veri Portalı",
                new Page.GetByTextOptions().setExact(true)
        );




        // =========================
        // Veri Seti Oluşturma
        // =========================

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
     * Kaynak araması sonrasında arama kriterini içeren
     * en az bir görünür kaynak kaydının yüklenmesini bekler
     * ve sonucu doğrular.
     *
     * @param aramaKriteri aranacak kaynak kriteri
     * @return kriteri içeren görünür kaynak varsa true
     */
    public boolean kaynakAramaSonuclariUygunMu(
            String aramaKriteri) {

        Locator eslesenKaynaklar =
                page.getByText(
                        aramaKriteri,
                        new Page.GetByTextOptions()
                                .setExact(false)
                );


        try {

            /*
             * Arama sonrasında tablo asenkron yüklendiği için
             * ilk eşleşen kaydın görünür olması beklenir.
             */
            eslesenKaynaklar
                    .first()
                    .waitFor(
                            new Locator.WaitForOptions()
                                    .setState(
                                            WaitForSelectorState.VISIBLE
                                    )
                                    .setTimeout(15000)
                    );

        } catch (PlaywrightException e) {

            ReportManager.info(
                    "Arama kriterini içeren görünür kaynak yüklenemedi"
                            + " | Kriter: "
                            + aramaKriteri
            );

            return false;
        }


        int gorunenKayitSayisi =
                0;


        for (int i = 0;
             i < eslesenKaynaklar.count();
             i++) {

            Locator kaynak =
                    eslesenKaynaklar.nth(i);


            if (!kaynak.isVisible()) {
                continue;
            }


            String kaynakMetni =
                    kaynak.innerText()
                            .trim();


            if (kaynakMetni
                    .toLowerCase()
                    .contains(
                            aramaKriteri
                                    .trim()
                                    .toLowerCase()
                    )) {

                gorunenKayitSayisi++;


                ReportManager.info(
                        "Arama kriteriyle eşleşen kaynak bulundu"
                                + " | Kaynak: "
                                + kaynakMetni
                );
            }
        }


        ReportManager.info(
                "Arama kriteriyle eşleşen görünür kaynak sayısı: "
                        + gorunenKayitSayisi
        );


        return gorunenKayitSayisi > 0;
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

    /**
     * Verilen kaynak kaydını Veri Seti Oluşturma
     * ekranında seçer.
     */
    public void kaynakSecme(
            String kaynakAdi) {

        Locator kaynakSatiri =
                kaynakSatiriBulma(
                        kaynakAdi
                );

        kaynakSatiri
                .getByLabel(
                        "Seçim"
                )
                .check();
    }


    /**
     * Kaynak kaydının seçili olduğunu kontrol eder.
     */
    public boolean kaynakSeciliMi(
            String kaynakAdi) {

        return kaynakSatiriBulma(
                kaynakAdi
        ).getByLabel(
                "Seçim"
        ).isChecked();
    }


    /**
     * Veri Seti Oluşturma ekranında
     * sonraki adıma geçer.
     */
    public void veriSetiIleriGitme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("İleri")
                        .setExact(true)
        ).click();
    }

    /**
     * yil alanına Maskele anonimleştirme
     * kuralını uygular.
     */
    public void yilAlaninaMaskeleKuraliUygulama() {

        kolonaMaskelemeKuraliUygulama("yil");
    }

    /**
     * Veri seti adını girer.
     */
    public void veriSetiAdiGirme(
            String veriSetiAdi) {

        page.getByLabel(
                "Veri Seti Adı *"
        ).fill(
                veriSetiAdi
        );
    }


    /**
     * Veri seti adı alanındaki değeri döndürür.
     */
    public String veriSetiAdiAlma() {

        return page.getByLabel(
                "Veri Seti Adı *"
        ).inputValue();
    }


    /**
     * Sorumlu birimi seçer.
     */
    public void sorumluBirimSecme(String sorumluBirim) {
        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Sorumlu Birim")).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(sorumluBirim))
                .click();
    }


    /**
     * Veri seti kategorisini seçer.
     */
    public void kategoriSecme(
            String kategori) {

        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Kategori")).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(kategori))
                .click();
    }


    /**
     * Kullanım lisansını seçer.
     */
    public void kullanimLisansiSecme(String lisans) {
        page.getByRole(
                AriaRole.COMBOBOX,
                new Page.GetByRoleOptions()
                        .setName("Kullanım Lisansı")).click();

        page.locator("[role='listbox'] [role='option']")
                .filter(new Locator.FilterOptions()
                        .setHasText(lisans))
                .click();
    }


    /**
     * Veri seti açıklamasını girer.
     */
    public void veriSetiAciklamasiGirme(
            String aciklama) {

        page.getByLabel(
                "Açıklama"
        ).fill(
                aciklama
        );
    }


    /**
     * Veri seti açıklamasını döndürür.
     */
    public String veriSetiAciklamasiAlma() {

        return page.getByLabel(
                "Açıklama"
        ).inputValue();
    }


    /**
     * Veri seti önizleme ekranına geçer.
     */
    public void veriSetiOnizleme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Önizle")
                        .setExact(true)
        ).click();
    }


    /**
     * Veri setini onaya gönderir.
     */
    public void veriSetiOnayaGonderme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Onaya Gönder")
                        .setExact(true)
        ).click();
    }


    /**
     * Onaya gönderme işlemindeki ikinci
     * onay butonuna tıklar.
     */
    public void veriSetiOnayaGondermeyiOnaylama() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Onaya Gönder")
                        .setExact(true)
        ).click();
    }


    /**
     * Veri setinin oluşturulup KVKK sürecine
     * gönderildiğine ait başarı mesajını kontrol eder.
     */
    public boolean veriSetiKvkkOnayinaGonderildiMi() {

        try {

            Locator mesaj =
                    page.getByText(
                            "Veri seti oluşturuldu ve KVKK",
                            new Page.GetByTextOptions()
                                    .setExact(false)
                    );

            mesaj.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return mesaj.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }
    /**
     * Veri Seti Adı zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean veriSetiAdiZorunluMesajiGoruntulendiMi() {

        try {

            Locator mesaj =
                    page.getByText(
                            "Veri seti adı zorunludur.",
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


    /**
     * Sorumlu Birim zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean sorumluBirimZorunluMesajiGoruntulendiMi() {

        try {

            Locator mesaj =
                    page.getByText(
                            "Sorumlu birim zorunludur.",
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


    /**
     * Kategori zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean kategoriZorunluMesajiGoruntulendiMi() {

        try {

            Locator mesaj =
                    page.getByText(
                            "Kategori zorunludur.",
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
    /**
     * Verilen kolona Maskele anonimleştirme
     * kuralını uygular.
     *
     * @param kolonAdi maskeleme uygulanacak kolon
     */
    public void kolonaMaskelemeKuraliUygulama(String kolonAdi) {

        Locator kolonSatiri = page.locator(
                "[role='row'][data-id='" + kolonAdi + "']"
        );

        kolonSatiri.locator(
                "[data-field='__rule'] [role='combobox']"
        ).click();

        page.locator("li[role='option'][data-value='MASK']").click();
    }


    /**
     * Önizleme ekranında beklenen kolonun
     * görüntülendiğini kontrol eder.
     */
    public boolean onizlemeKolonuGoruntulendiMi(
            String kolonAdi) {

        try {

            Locator kolon =
                    page.getByText(
                            kolonAdi,
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            kolon.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return kolon.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Önizleme ekranında Maskelendi bilgisinin
     * görüntülendiğini kontrol eder.
     */
    public boolean maskelendiBilgisiGoruntulendiMi() {

        try {

            Locator maskelendi =
                    page.getByText(
                            "Maskelendi",
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            maskelendi.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return maskelendi.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Anonimleştirilmiş önizleme tablosunda
     * yıldız karakterleriyle maskelenmiş veri
     * bulunup bulunmadığını kontrol eder.
     */
    public boolean maskelenmisVeriGoruntulendiMi() {

        Locator hucreler =
                page.getByRole(
                        AriaRole.CELL
                );

        int hucreSayisi =
                hucreler.count();


        for (int i = 0;
             i < hucreSayisi;
             i++) {

            Locator hucre =
                    hucreler.nth(i);

            if (!hucre.isVisible()) {
                continue;
            }

            String hucreMetni =
                    hucre.innerText()
                            .trim();

            /*
             * Maskelenmiş değerlerde en az
             * dört yıldız bulunması beklenir.
             */
            if (hucreMetni.contains("****")
            ) {

                ReportManager.info(
                        "Maskelenmiş veri bulundu"
                                + " | Değer: "
                                + hucreMetni
                );

                return true;
            }
        }

        return false;
    }
}