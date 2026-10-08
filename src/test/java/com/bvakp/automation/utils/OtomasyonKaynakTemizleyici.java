package com.bvakp.automation.utils;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

import java.util.Locale;

public class OtomasyonKaynakTemizleyici extends AvpBaseTest {


    /**
     * Utility sınıfını manuel olarak çalıştırır.
     *
     * Yalnızca adı "otomasyon" ile başlayan
     * kaynak kayıtları silinir.
     */
    public static void main(String[] args) {

        kosuSonuTemizliginiCalistir();
    }


    /**
     * Otomasyon kaynak temizliğini bağımsız bir
     * Playwright oturumu açarak çalıştırır.
     *
     * Testlerin kullandığı Page nesnesinden bağımsızdır.
     * Temizlik tamamlandığında açılan Playwright
     * oturumu kapatılır.
     */
    public static void kosuSonuTemizliginiCalistir() {

        OtomasyonKaynakTemizleyici temizleyici =
                new OtomasyonKaynakTemizleyici();


        ReportManager.info(
                "OTOMASYON KAYNAK TEMİZLİĞİ İÇİN "
                        + "YENİ PLAYWRIGHT OTURUMU AÇILIYOR"
        );


        try {

            /*
             * Testlerde kullanılan Page daha önce
             * kapatılmış olabileceği için temizliğe
             * özel yeni Playwright oturumu oluşturulur.
             */
            temizleyici.setUp();


            /*
             * Asıl kaynak temizleme işlemi çalıştırılır.
             */
            temizleyici
                    .otomasyonKaynaklariniTemizleme();


        } finally {

            /*
             * Yalnızca temizleme işlemi için açılan
             * Playwright oturumu kapatılır.
             */
            PlaywrightManager.close(
                    "otomasyonKaynaklariniTemizleme",
                    "TEMIZLIK"
            );


            ReportManager.info(
                    "OTOMASYON KAYNAK TEMİZLİĞİ "
                            + "PLAYWRIGHT OTURUMU KAPATILDI"
            );
        }
    }


    /**
     * Açık Veri Portalındaki adı "otomasyon"
     * ile başlayan bütün kaynakları siler.
     */
    private void otomasyonKaynaklariniTemizleme() {

        OpenDataPage openDataPage =
                new OpenDataPage(page);


        ReportManager.info(
                "OTOMASYON KAYNAK TEMİZLİĞİ BAŞLADI"
        );


        /*
         * AVP oturumu hazırlanır.
         */
        avpOturumuHazirlama();


        /*
         * Açık Veri Portalına geçilir.
         */
        ReportManager.info(
                "Açık Veri Portalına geçiliyor."
        );


        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Kaynaklar "otomasyon" kriteriyle filtrelenir.
         */
        ReportManager.info(
                "'otomasyon' kriteri ile kaynaklar filtreleniyor."
        );


        openDataPage
                .kaynakArama(
                        "otomasyon"
                );


        /*
         * Arama sonrasında tablo asenkron olarak
         * güncellendiği için sonuçların yüklenmesi beklenir.
         */
        ReportManager.info(
                "Otomasyon kaynaklarının yüklenmesi bekleniyor."
        );


        otomasyonKayitlarininYuklenmesiniBekleme();


        int silinenKayitSayisi =
                0;


        /*
         * Silinebilecek otomasyon kaydı kalmayana
         * kadar işlem devam eder.
         */
        while (true) {

            /*
             * Her silme işleminden sonra tablo yeniden
             * render edildiği için locator her döngüde
             * yeniden oluşturulur.
             */
            Locator otomasyonSatirlari =
                    page.getByRole(
                            AriaRole.ROW
                    ).filter(
                            new Locator.FilterOptions()
                                    .setHasText(
                                            "otomasyon"
                                    )
                    );


            int satirSayisi =
                    otomasyonSatirlari.count();


            ReportManager.info(
                    "Filtre sonrası bulunan otomasyon satırı sayısı: "
                            + satirSayisi
            );


            Locator silinecekSatir =
                    null;

            String silinecekKaynakAdi =
                    null;


            /*
             * Adı gerçekten "otomasyon" ile başlayan
             * ilk görünür kaynak kaydı bulunur.
             */
            for (int i = 0;
                 i < satirSayisi;
                 i++) {

                Locator satir =
                        otomasyonSatirlari.nth(i);


                /*
                 * Görünür olmayan satırlar dikkate alınmaz.
                 */
                if (!satir.isVisible()) {

                    continue;
                }


                Locator hucreler =
                        satir.getByRole(
                                AriaRole.GRIDCELL
                        );


                String bulunanKaynakAdi =
                        null;


                /*
                 * Kaynak adının belirli bir kolon indexinde
                 * olduğu varsayılmaz.
                 *
                 * Satırdaki bütün hücreler kontrol edilerek
                 * "otomasyon" ile başlayan kaynak adı bulunur.
                 */
                for (int hucreIndex = 0;
                     hucreIndex < hucreler.count();
                     hucreIndex++) {

                    Locator hucre =
                            hucreler.nth(
                                    hucreIndex
                            );


                    if (!hucre.isVisible()) {

                        continue;
                    }


                    String hucreMetni =
                            hucre
                                    .innerText()
                                    .trim();


                    if (hucreMetni
                            .toLowerCase(
                                    Locale.ROOT
                            )
                            .startsWith(
                                    "otomasyon"
                            )) {

                        bulunanKaynakAdi =
                                hucreMetni;

                        break;
                    }
                }


                /*
                 * Satır içerisinde otomasyon ile başlayan
                 * kaynak adı bulunamamışsa sonraki satıra geçilir.
                 */
                if (bulunanKaynakAdi == null) {

                    continue;
                }


                /*
                 * Silinecek kayıt belirlenir.
                 */
                silinecekSatir =
                        satir;

                silinecekKaynakAdi =
                        bulunanKaynakAdi;


                ReportManager.info(
                        "Silinecek otomasyon kaynağı bulundu"
                                + " | "
                                + silinecekKaynakAdi
                );


                break;
            }


            /*
             * Silinebilecek otomasyon kaydı
             * kalmadıysa döngü tamamlanır.
             */
            if (silinecekSatir == null) {

                ReportManager.info(
                        "Silinebilecek otomasyon kaynağı kalmadı."
                );

                break;
            }


            /*
             * Silme öncesinde aynı isimde kaç adet
             * görünür kayıt olduğu alınır.
             *
             * Bu kontrol aynı isimde birden fazla
             * kayıt bulunması durumunu da destekler.
             */
            int silmeOncesiKayitSayisi =
                    gorunurKaynakSayisi(
                            silinecekKaynakAdi
                    );


            ReportManager.info(
                    "Kaynak silme işlemi başlatılıyor"
                            + " | Kaynak: "
                            + silinecekKaynakAdi
                            + " | Aynı isimde görünür kayıt: "
                            + silmeOncesiKayitSayisi
            );


            /*
             * İlgili satırdaki Sil butonuna tıklanır.
             */
            silinecekSatir
                    .getByRole(
                            AriaRole.BUTTON,
                            new Locator.GetByRoleOptions()
                                    .setName(
                                            "Sil"
                                    )
                                    .setExact(
                                            true
                                    )
                    )
                    .click();


            /*
             * Silme onay dialogunun açılması beklenir.
             */
            Locator dialog =
                    page.getByRole(
                            AriaRole.DIALOG
                    );


            dialog.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    WaitForSelectorState.VISIBLE
                            )
                            .setTimeout(
                                    10000
                            )
            );


            ReportManager.info(
                    "Kaynak silme onay dialogu açıldı"
                            + " | Kaynak: "
                            + silinecekKaynakAdi
            );


            /*
             * Dialog içerisindeki Sil butonuna
             * tıklanarak işlem onaylanır.
             */
            dialog.getByRole(
                    AriaRole.BUTTON,
                    new Locator.GetByRoleOptions()
                            .setName(
                                    "Sil"
                            )
                            .setExact(
                                    true
                            )
            ).click();


            /*
             * Dialog tamamen kapanmadan
             * tablo kontrolüne geçilmez.
             */
            dialog.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(
                                    15000
                            )
            );


            /*
             * Lambda içerisinde kullanılacak değerler
             * final / effectively final değişkenlere alınır.
             */
            String kaynakAdiBeklenen =
                    silinecekKaynakAdi;

            int oncekiKayitSayisi =
                    silmeOncesiKayitSayisi;


            /*
             * Dialogun kapanması kaydın tablodan gerçekten
             * silindiğini garanti etmez.
             *
             * Aynı isimdeki görünür kayıt sayısının
             * azalması beklenir.
             */
            page.waitForCondition(
                    () ->
                            gorunurKaynakSayisi(
                                    kaynakAdiBeklenen
                            )
                                    < oncekiKayitSayisi,
                    new Page.WaitForConditionOptions()
                            .setTimeout(
                                    15000
                            )
            );


            silinenKayitSayisi++;


            ReportManager.info(
                    "Kaynak başarıyla silindi"
                            + " | Kaynak: "
                            + silinecekKaynakAdi
                            + " | Toplam silinen: "
                            + silinenKayitSayisi
            );
        }


        ReportManager.info(
                "OTOMASYON KAYNAK TEMİZLİĞİ TAMAMLANDI"
                        + " | Toplam silinen kayıt: "
                        + silinenKayitSayisi
        );
    }


    /**
     * Belirtilen kaynak adına ait görünür
     * kayıt sayısını döndürür.
     *
     * Silme işleminden sonra tablonun gerçekten
     * güncellendiğini doğrulamak için kullanılır.
     *
     * @param kaynakAdi kontrol edilecek kaynak adı
     * @return görünür kaynak sayısı
     */
    private int gorunurKaynakSayisi(
            String kaynakAdi) {

        Locator kaynakAdiLocator =
                page.getByText(
                        kaynakAdi,
                        new Page.GetByTextOptions()
                                .setExact(
                                        true
                                )
                );


        Locator kaynakSatirlari =
                page.getByRole(
                        AriaRole.ROW
                ).filter(
                        new Locator.FilterOptions()
                                .setHas(
                                        kaynakAdiLocator
                                )
                );


        int gorunurKayitSayisi =
                0;


        int kaynakSatirSayisi =
                kaynakSatirlari.count();


        for (int i = 0;
             i < kaynakSatirSayisi;
             i++) {

            Locator kaynakSatiri =
                    kaynakSatirlari.nth(
                            i
                    );


            if (kaynakSatiri.isVisible()) {

                gorunurKayitSayisi++;
            }
        }


        return gorunurKayitSayisi;
    }


    /**
     * Otomasyon araması sonrasında tablonun
     * sonuç üretmesini bekler.
     *
     * En az bir otomasyon kaydı veya
     * boş sonuç bilgisi oluşana kadar beklenir.
     */
    private void otomasyonKayitlarininYuklenmesiniBekleme() {

        Locator otomasyonSatiri =
                page.getByRole(
                        AriaRole.ROW
                ).filter(
                        new Locator.FilterOptions()
                                .setHasText(
                                        "otomasyon"
                                )
                ).first();


        Locator bosListeMesaji =
                page.getByText(
                        "Henüz kaynak eklenmemiş. \"Kaynak Ekle\" ile başlayın.",
                        new Page.GetByTextOptions()
                                .setExact(
                                        true
                                )
                );


        page.waitForCondition(
                () ->
                        otomasyonSatiri.isVisible()
                                || bosListeMesaji.isVisible(),
                new Page.WaitForConditionOptions()
                        .setTimeout(
                                15000
                        )
        );


        ReportManager.info(
                "Otomasyon kaynak arama sonuçları yüklendi."
        );
    }
}