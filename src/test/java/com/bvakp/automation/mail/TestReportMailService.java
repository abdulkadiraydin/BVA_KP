package com.bvakp.automation.mail;

import com.bvakp.automation.utils.ReportZipUtil;
import com.bvakp.automation.utils.TestResultSummaryUtil;
import com.bvakp.automation.utils.TestResultSummaryUtil.TestSummary;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Son otomasyon test çalıştırmasına ait raporların mail gönderim sürecini yönetir.
 *
 * Son test koşusuna ait raporları ZIP haline getirir, TestNG sonuçlarından
 * test özet bilgilerini alır ve HTML formatında mail içeriği oluşturur.
 *
 * Test sonucuna göre genel durumu BASARILI veya HATALI olarak belirleyerek
 * hazırlanan raporu MailService aracılığıyla tanımlı alıcılara gönderir.
 */


public final class TestReportMailService {

    private static final Pattern RAPOR_TARIH_DESENI =
            Pattern.compile(
                    "BVA_KP_Test_Raporu_(\\d{8}_\\d{6})\\.zip"
            );

    private static final DateTimeFormatter DOSYA_TARIH_FORMATI =
            DateTimeFormatter.ofPattern(
                    "yyyyMMdd_HHmmss"
            );

    private static final DateTimeFormatter MAIL_TARIH_FORMATI =
            DateTimeFormatter.ofPattern(
                    "dd.MM.yyyy HH:mm:ss"
            );

    private TestReportMailService() {
    }

    public static void sonRaporuMailGonder() {

        System.out.println(
                "Son otomasyon test raporu hazırlanıyor..."
        );

        Path raporZip =
                ReportZipUtil
                        .sonKosuyuZipOlustur();

        TestSummary testSummary =
                TestResultSummaryUtil
                        .sonTestSonucunuAl();

        String kosuTarihi =
                raporTarihiniAl(
                        raporZip
                );

        String durum =
                testSummary.durum();

        String durumRengi =
                "BASARILI".equals(durum)
                        ? "#2E7D32"
                        : "#C62828";

        String konu =
                "BVA KP Otomasyon Test Raporu - "
                        + durum
                        + " - "
                        + kosuTarihi;

        String htmlMesaj =
                htmlMesajOlustur(
                        testSummary,
                        durum,
                        durumRengi,
                        kosuTarihi,
                        raporZip
                );

        System.out.println(
                "Rapor ZIP oluşturuldu: "
                        + raporZip.toAbsolutePath()
        );

        System.out.println(
                "Test durumu: "
                        + durum
        );

        System.out.println(
                "Mail gönderiliyor..."
        );

        MailService.mailGonder(
                konu,
                htmlMesaj,
                raporZip
        );

        System.out.println(
                "Test raporu mail gönderimi tamamlandı."
        );
    }

    private static String htmlMesajOlustur(
            TestSummary summary,
            String durum,
            String durumRengi,
            String kosuTarihi,
            Path raporZip) {

        return """
                <html>
                <body style="
                    font-family: Arial, sans-serif;
                    font-size: 14px;
                    color: #333333;
                    background-color: #ffffff;
                ">

                    <p>Merhaba,</p>

                    <p>
                        BVA KP otomasyon test koşusu tamamlanmıştır.
                        Test sonuçlarının özeti aşağıda yer almaktadır.
                    </p>

                    <table style="
                        border-collapse: collapse;
                        width: 520px;
                        margin-top: 20px;
                        margin-bottom: 20px;
                    ">

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                                font-weight: bold;
                            ">
                                Test Durumu
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                                font-weight: bold;
                                color: %s;
                            ">
                                %s
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Toplam Test
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                %d
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Başarılı
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                                color: #2E7D32;
                                font-weight: bold;
                            ">
                                %d
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Başarısız
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                                color: #C62828;
                                font-weight: bold;
                            ">
                                %d
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Atlanan
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                %d
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Koşu Tarihi
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                %s
                            </td>
                        </tr>

                        <tr>
                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                Rapor Dosyası
                            </td>

                            <td style="
                                padding: 10px;
                                border: 1px solid #dddddd;
                            ">
                                %s
                            </td>
                        </tr>

                    </table>

                    <p>
                        Detaylı test sonuçları, test adımları ve ekran görüntüleri
                        ekte bulunan ZIP dosyasında yer almaktadır.
                    </p>

                    <p>
                        İyi çalışmalar.
                    </p>

                    <hr style="
                        border: 0;
                        border-top: 1px solid #eeeeee;
                        margin-top: 25px;
                    ">

                    <p style="
                        font-size: 11px;
                        color: #888888;
                    ">
                        Bu e-posta BVA KP Test Otomasyon sistemi tarafından
                        otomatik olarak oluşturulmuştur.
                    </p>

                </body>
                </html>
                """.formatted(
                durumRengi,
                durum,
                summary.toplam(),
                summary.basarili(),
                summary.basarisiz(),
                summary.atlanan(),
                kosuTarihi,
                raporZip
                        .getFileName()
                        .toString()
        );
    }

    private static String raporTarihiniAl(
            Path raporZip) {

        String dosyaAdi =
                raporZip
                        .getFileName()
                        .toString();

        Matcher matcher =
                RAPOR_TARIH_DESENI.matcher(
                        dosyaAdi
                );

        if (!matcher.matches()) {

            return "Bilinmiyor";
        }

        LocalDateTime tarih =
                LocalDateTime.parse(
                        matcher.group(1),
                        DOSYA_TARIH_FORMATI
                );

        return tarih.format(
                MAIL_TARIH_FORMATI
        );
    }
}