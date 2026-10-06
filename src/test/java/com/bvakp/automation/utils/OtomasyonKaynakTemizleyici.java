package com.bvakp.automation.utils;

import com.bvakp.automation.base.AvpBaseTest;
import com.bvakp.automation.pages.openData.OpenDataPage;
import com.bvakp.automation.reporting.ReportManager;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

public class OtomasyonKaynakTemizleyici extends AvpBaseTest {

    /**
     * Utility class'ı manuel olarak çalıştırır.
     *
     * Yalnızca "otomasyon" ile başlayan
     * kaynak kayıtları silinir.
     */
    public static void main(String[] args) {

        OtomasyonKaynakTemizleyici temizleyici =
                new OtomasyonKaynakTemizleyici();

        try {

            temizleyici.setUp();

            temizleyici.otomasyonKaynaklariniTemizleme();

        } finally {

            /*
             * TestNG çalışmadığı için teardown işlemi
             * manuel olarak PlaywrightManager üzerinden yapılabilir.
             */
            com.bvakp.automation.core.playwright.PlaywrightManager
                    .close(
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
         * Mevcut AVP auth state kullanılır.
         * State geçersizse normal login gerçekleştirilir.
         */
        avpOturumuHazirlama();


        ReportManager.step(
                "Açık Veri Portalına geçiliyor."
        );

        openDataPage
                .acikVeriPortalinaGitme();


        /*
         * Önce kaynak listesi yalnızca otomasyon
         * kayıtlarını gösterecek şekilde filtrelenir.
         */
        ReportManager.step(
                "'otomasyon' kriteri ile kaynaklar filtreleniyor."
        );

        openDataPage
                .kaynakArama(
                        "otomasyon"
                );


        int silinenKayitSayisi =
                0;


        /*
         * Her silme işleminden sonra liste yeniden
         * render edildiği için locator tekrar oluşturulur.
         */
        while (true) {

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


            /*
             * Yalnızca adı gerçekten "otomasyon"
             * ile başlayan satır seçilir.
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
             * Silinebilecek otomasyon kaydı kalmadıysa
             * işlem tamamlanır.
             */
            if (silinecekSatir == null) {

                break;
            }


            ReportManager.info(
                    "Silinecek otomasyon kaynağı | "
                            + silinecekKaynakAdi
            );


            /*
             * Sadece bulunan satırın Sil butonuna tıklanır.
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
             * Silme onay dialogundaki Sil butonuna tıklanır.
             */
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


            /*
             * Dialog kapanmadan sonraki kayda geçilmez.
             */
            dialog.waitFor(
                    new Locator.WaitForOptions()
                            .setState(
                                    com.microsoft.playwright.options
                                            .WaitForSelectorState.HIDDEN
                            )
                            .setTimeout(15000)
            );


            silinenKayitSayisi++;


            ReportManager.info(
                    "Kaynak silindi | "
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
}