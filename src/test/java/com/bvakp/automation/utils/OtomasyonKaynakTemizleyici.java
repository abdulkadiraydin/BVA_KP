package com.bvakp.automation.utils;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.core.playwright.PlaywrightManager;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;

public class OtomasyonKaynakTemizleyici extends AvpBaseTest {


    /**
     * Utility class'ı manuel olarak çalıştırır.
     *
     * Yalnızca adı "otomasyon" ile başlayan
     * kaynak kayıtları silinir.
     */
    public static void main(String[] args) {

        OtomasyonKaynakTemizleyici temizleyici =
                new OtomasyonKaynakTemizleyici();

        try {

            temizleyici.setUp();

            temizleyici
                    .otomasyonKaynaklariniTemizleme();

        } finally {

            PlaywrightManager.close(
                    "otomasyonKaynaklariniTemizleme",
                    "MANUEL"
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
         *
         * Utility main() üzerinden çalıştığı için
         * burada step yerine info kullanılır.
         */
        ReportManager.info(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Kaynaklar otomasyon kriteriyle filtrelenir.
         */
        ReportManager.info(
                "'otomasyon' kriteri ile kaynaklar filtreleniyor."
        );

        openDataPage
                .kaynakArama(
                        "otomasyon"
                );


        /*
         * Arama sonrasında tablo asenkron yüklendiği
         * için kayıtların gelmesi beklenir.
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
             * Her döngüde locator yeniden oluşturulur.
             *
             * Çünkü her silme işleminden sonra
             * tablo DOM'u yeniden render edilmektedir.
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


            Locator silinecekSatir =
                    null;

            String silinecekKaynakAdi =
                    null;


            int satirSayisi =
                    otomasyonSatirlari.count();
            ReportManager.info(
                    "Filtre sonrası bulunan otomasyon satırı sayısı: "
                            + satirSayisi
            );


            /*
             * Adı gerçekten "otomasyon" ile başlayan
             * ilk görünür kayıt bulunur.
             */
            for (int i = 0;
                 i < satirSayisi;
                 i++) {

                Locator satir =
                        otomasyonSatirlari.nth(i);


                if (!satir.isVisible()) {
                    continue;
                }


                Locator kaynakAdiHucreleri =
                        satir.getByRole(
                                AriaRole.GRIDCELL
                        );


                if (kaynakAdiHucreleri.count() == 0) {
                    continue;
                }


                String kaynakAdi =
                        kaynakAdiHucreleri
                                .nth(0)
                                .innerText()
                                .trim();



                /*
                 * Güvenlik kontrolü:
                 * yalnızca otomasyon ile başlayan
                 * kaynaklara dokunulur.
                 */
                if (!kaynakAdi
                        .toLowerCase()
                        .startsWith(
                                "otomasyon"
                        )) {

                    continue;
                }


                silinecekSatir =
                        satir;

                silinecekKaynakAdi =
                        kaynakAdi;

                break;
            }


            /*
             * Silinebilecek otomasyon kaydı
             * kalmadıysa döngü tamamlanır.
             */
            if (silinecekSatir == null) {

                break;
            }


            /*
             * Silme öncesinde aynı isimde kaç adet
             * görünür kayıt olduğu alınır.
             *
             * Aynı isimde birden fazla kayıt bulunması
             * durumunu da destekler.
             */
            int silmeOncesiKayitSayisi =
                    gorunurKaynakSayisi(
                            silinecekKaynakAdi
                    );


            ReportManager.info(
                    "Silinecek otomasyon kaynağı"
                            + " | "
                            + silinecekKaynakAdi
                            + " | Aynı isimde görünür kayıt: "
                            + silmeOncesiKayitSayisi
            );


            /*
             * Bulunan satırın Sil butonuna tıklanır.
             */
            silinecekSatir
                    .getByRole(
                            AriaRole.BUTTON,
                            new Locator.GetByRoleOptions()
                                    .setName("Sil")
                                    .setExact(true)
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
                            .setTimeout(10000)
            );


            /*
             * Silme işlemi onaylanır.
             */
            dialog.getByRole(
                    AriaRole.BUTTON,
                    new Locator.GetByRoleOptions()
                            .setName("Sil")
                            .setExact(true)
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
                            .setTimeout(15000)
            );


            /*
             * Kritik bekleme:
             *
             * Dialogun kapanması tablo verisinin
             * güncellendiği anlamına gelmeyebilir.
             *
             * Aynı isimdeki görünür kayıt sayısının
             * gerçekten azalması beklenir.
             */
            String kaynakAdiBeklenen =
                    silinecekKaynakAdi;

            int oncekiKayitSayisi =
                    silmeOncesiKayitSayisi;


            page.waitForCondition(
                    () ->
                            gorunurKaynakSayisi(
                                    kaynakAdiBeklenen
                            )
                                    < oncekiKayitSayisi,
                    new Page.WaitForConditionOptions()
                            .setTimeout(15000)
            );


            silinenKayitSayisi++;


            ReportManager.info(
                    "Kaynak silindi"
                            + " | "
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
     * yenilendiğini anlamak için kullanılır.
     */
    private int gorunurKaynakSayisi(
            String kaynakAdi) {

        Locator kaynakAdiLocator =
                page.getByText(
                        kaynakAdi,
                        new Page.GetByTextOptions()
                                .setExact(true)
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


        for (int i = 0;
             i < kaynakSatirlari.count();
             i++) {

            if (kaynakSatirlari
                    .nth(i)
                    .isVisible()) {

                gorunurKayitSayisi++;
            }
        }


        return gorunurKayitSayisi;
    }
    /**
     * Otomasyon araması sonrasında tablonun
     * sonuç üretmesini bekler.
     *
     * Ya en az bir otomasyon kaydı ya da
     * boş sonuç mesajı görüntülenmelidir.
     */
    private void otomasyonKayitlarininYuklenmesiniBekleme() {

        Locator otomasyonSatiri =
                page.getByRole(
                        AriaRole.ROW
                ).filter(
                        new Locator.FilterOptions()
                                .setHasText("otomasyon")
                ).first();

        Locator bosListeMesaji =
                page.getByText(
                        "Henüz kaynak eklenmemiş. \"Kaynak Ekle\" ile başlayın.",
                        new Page.GetByTextOptions()
                                .setExact(true)
                );

        page.waitForCondition(
                () ->
                        otomasyonSatiri.isVisible()
                                || bosListeMesaji.isVisible(),
                new Page.WaitForConditionOptions()
                        .setTimeout(15000)
        );
    }
}