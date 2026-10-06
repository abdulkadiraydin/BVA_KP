package com.bvakp.automation.companents;

import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class KaynakEkleModal {

    private final Page page;

    private final Locator adAlani;
    private final Locator tabloAlani;
    private final Locator aciklamaAlani;
    private final Locator veriOnizleme;
    private final Locator kaynakEkleButonu;


    /**
     * Kaynak Ekle modalındaki elementleri tanımlar.
     *
     * @param page aktif Playwright sayfası
     */
    public KaynakEkleModal(Page page) {

        this.page = page;

        this.adAlani =
                page.getByLabel(
                        "Ad *"
                );

        this.tabloAlani =
                page.getByLabel(
                        "Tablo *"
                );

        this.aciklamaAlani =
                page.getByLabel(
                        "Açıklama"
                );

        this.veriOnizleme =
                page.getByText(
                        "Veri Önizleme",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        this.kaynakEkleButonu =
                page.getByRole(
                        AriaRole.BUTTON,
                        new Page.GetByRoleOptions()
                                .setName("Kaynak Ekle")
                                .setExact(true)
                );
    }


    /**
     * Kaynak Ekle formundaki temel alanların
     * görüntülendiğini kontrol eder.
     *
     * @return form alanları görünüyorsa true
     */
    public boolean kaynakEkleFormuGoruntulendiMi() {

        try {

            adAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            tabloAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            aciklamaAlani.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return adAlani.isVisible()
                    && tabloAlani.isVisible()
                    && aciklamaAlani.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak adı alanına verilen değeri girer.
     *
     * @param kaynakAdi oluşturulacak kaynak adı
     */
    public void kaynakAdiGirme(
            String kaynakAdi) {

        adAlani.fill(
                kaynakAdi
        );
    }


    /**
     * Kaynak adı alanındaki mevcut değeri döndürür.
     *
     * @return kaynak adı alanındaki değer
     */
    public String kaynakAdiAlma() {

        return adAlani
                .inputValue()
                .trim();
    }


    /**
     * Tablo seçim alanını açar ve belirtilen
     * kaynak tablosunu seçer.
     *
     * @param tabloAdi seçilecek tablo adı
     */
    public void kaynakTablosuSecme(
            String tabloAdi) {

        tabloAlani.click();

        Locator tabloSecenegi =
                page.getByText(
                        tabloAdi,
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        tabloSecenegi.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        tabloSecenegi.click();
    }


    /**
     * Tablo alanındaki seçili değeri döndürür.
     *
     * @return seçili tablo adı
     */
    public String seciliTabloyuAlma() {

        return tabloAlani
                .inputValue()
                .trim();
    }


    /**
     * Kaynak açıklaması alanına verilen değeri girer.
     *
     * @param aciklama kaynak açıklaması
     */
    public void kaynakAciklamasiGirme(
            String aciklama) {

        aciklamaAlani.fill(
                aciklama
        );
    }


    /**
     * Kaynak açıklaması alanındaki mevcut değeri döndürür.
     *
     * @return açıklama alanındaki değer
     */
    public String kaynakAciklamasiAlma() {

        return aciklamaAlani
                .inputValue()
                .trim();
    }


    /**
     * Tablo seçimi sonrasında Veri Önizleme
     * bölümünün görüntülendiğini kontrol eder.
     *
     * @return Veri Önizleme görünüyorsa true
     */
    public boolean veriOnizlemeGoruntulendiMi() {

        try {

            veriOnizleme.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(15000)
            );

            return veriOnizleme.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Kaynak Ekle butonuna tıklayarak
     * kaynak oluşturma işlemini tamamlar.
     */
    public void kaynakEkleme() {

        kaynakEkleButonu.waitFor(
                new Locator.WaitForOptions()
                        .setTimeout(10000)
        );

        kaynakEkleButonu.click();
    }


    /**
     * Kaynak oluşturma sonrasında formun
     * kapandığını kontrol eder.
     *
     * @return form kapandıysa true
     */
    public boolean kaynakEkleFormuKapandiMi() {

        try {

            adAlani.waitFor(
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
     * Kaynak adı zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean kaynakAdiZorunluMesajiGoruntulendiMi() {

        return page.getByText(
                "Kaynak adı zorunludur.",
                new Page.GetByTextOptions()
                        .setExact(true)
        ).isVisible();
    }


    /**
     * Tablo zorunlu alan mesajının
     * görüntülendiğini kontrol eder.
     */
    public boolean tabloZorunluMesajiGoruntulendiMi() {

        return page.getByText(
                "Bir tablo seçin.",
                new Page.GetByTextOptions()
                        .setExact(true)
        ).isVisible();
    }
    /**
     * Kaynak Ekle formunu açar.
     */
    public void kaynakEkleButonunaTiklama() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Kaynak Ekle")
                        .setExact(true)
        ).click();
    }
    /**
     * Kaynak Ekle formunda Hazır Sorgu seçeneğine geçer.
     */
    public void hazirSorguyaGecme() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Hazır Sorgu")
                        .setExact(true)
        ).click();
    }


    /**
     * Hazır Sorgu alanına verilen SQL sorgusunu yazar.
     *
     * @param sorgu çalıştırılacak SQL sorgusu
     */
    public void hazirSorguGirme(
            String sorgu) {

        Locator sorguAlani =
                page.getByPlaceholder(
                        "SELECT a.*, b.ad\n"
                                + "FROM tablo_a a\n"
                                + "JOIN tablo_b b ON a.id = b.a_id"
                );

        sorguAlani.fill(
                sorgu
        );
    }


    /**
     * Girilen hazır sorgunun doğrulanmasını başlatır.
     */
    public void hazirSorguDogrulama() {

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions()
                        .setName("Doğrula")
                        .setExact(true)
        ).click();
    }


    /**
     * Sorgu doğrulaması sonrasında beklenen
     * kolonların görüntülendiğini kontrol eder.
     */
    public boolean hazirSorguKolonlariGoruntulendiMi() {

        String[] beklenenKolonlar = {
                "yil_yillik_hat_km",
                "bolge_kodu_yillik_hat_km",
                "bolge_adi_yillik_hat_km",
                "hat_kodu_yillik_hat_kapasitesi",
                "hat_adi_yillik_hat_kapasitesi"
        };


        for (String kolon : beklenenKolonlar) {

            try {

                Locator kolonLocator =
                        page.getByText(
                                kolon,
                                new Page.GetByTextOptions()
                                        .setExact(true)
                        );

                kolonLocator.waitFor(
                        new Locator.WaitForOptions()
                                .setTimeout(15000)
                );


                if (!kolonLocator.isVisible()) {

                    ReportManager.info(
                            "Sorgu sonucunda beklenen kolon görüntülenemedi"
                                    + " | Kolon: "
                                    + kolon
                    );

                    return false;
                }

            } catch (PlaywrightException e) {

                ReportManager.info(
                        "Sorgu sonucunda beklenen kolon yüklenemedi"
                                + " | Kolon: "
                                + kolon
                );

                return false;
            }
        }


        return true;
    }
    /**
     * SELECT dışındaki sorgularda Kullanıcı Kaynaklı Hata
     * alanının görüntülendiğini kontrol eder.
     */
    public boolean kullaniciKaynakliHataGoruntulendiMi() {

        try {

            Locator hataBasligi =
                    page.getByText(
                            "Kullanıcı Kaynaklı Hata",
                            new Page.GetByTextOptions()
                                    .setExact(true)
                    );

            hataBasligi.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return hataBasligi.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }

    /**
     * SELECT dışındaki veri değiştiren sorguların
     * engellendiğine ait hata mesajını kontrol eder.
     */
    public boolean sadeceSelectSorgusuHatasiGoruntulendiMi() {

        try {

            Locator hataMesaji =
                    page.getByText(
                            "Sorguyu yalnızca SELECT ile yazın.",
                            new Page.GetByTextOptions()
                                    .setExact(false)
                    );

            hataMesaji.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return hataMesaji.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }


    /**
     * Read-only sorgu kuralına ait hata kodunun
     * görüntülendiğini kontrol eder.
     */
    public boolean readOnlySorguHataKoduGoruntulendiMi() {

        try {

            Locator hataKodu =
                    page.getByText(
                            "OPEN_DATA.SOURCE.QUERY_NOT_READ_ONLY",
                            new Page.GetByTextOptions()
                                    .setExact(false)
                    );

            hataKodu.waitFor(
                    new Locator.WaitForOptions()
                            .setTimeout(10000)
            );

            return hataKodu.isVisible();

        } catch (PlaywrightException e) {

            return false;
        }
    }
}