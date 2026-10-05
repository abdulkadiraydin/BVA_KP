package com.bvakp.automation.mail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ardışık olarak belirlenen hata eşiğini geçen
 * otomasyon testleri için özel bilgilendirme maili gönderir.
 *
 * Mail içerisinde test bilgileri, hata detayı
 * ve son hata anında alınan screenshot gösterilir.
 */
public final class TestFailureAlertMailService {

    private TestFailureAlertMailService() {
    }

    /**
     * Belirtilen test için ardışık hata bilgilendirme
     * mailini tanımlı alıcılara gönderir.
     *
     * @param className      test class adı
     * @param testName       test metot adı
     * @param failureCount   ardışık hata sayısı
     * @param throwable      son test hatası
     * @param screenshotPath son hata sırasında alınan screenshot
     */
    public static void hataBilgilendirmeMailiGonder(
            String className,
            String testName,
            int failureCount,
            Throwable throwable,
            Path screenshotPath) {

        String tarih =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "dd.MM.yyyy HH:mm:ss"
                                )
                        );

        String hataDetayi =
                throwable != null
                        && throwable.getMessage() != null
                        ? throwable.getMessage()
                        : "Hata detayı bulunamadı.";

        String konu =
                "[UYARI] "
                        + testName
                        + " - Test Otomasyon Durumunda Hata Alınması";

        /*
         * Screenshot varsa body içerisinde gösterilir.
         * Screenshot oluşturulamamışsa kullanıcıya bilgi verilir.
         */
        String screenshotBolumu;

        if (screenshotPath != null
                && Files.exists(
                screenshotPath
        )) {

            screenshotBolumu =
                    """
                    <tr>
                        <td style="padding:0 28px 28px 28px;">

                            <div style="
                                font-size:16px;
                                font-weight:bold;
                                color:#1f2937;
                                margin-bottom:10px;
                            ">
                                Hata Anı Ekran Görüntüsü
                            </div>

                            <div style="
                                border:1px solid #dfe3e8;
                                background:#f8fafc;
                                padding:12px;
                                border-radius:8px;
                            ">

                                <img
                                    src="cid:failureScreenshot"
                                    alt="Hata ekran görüntüsü"
                                    style="
                                        display:block;
                                        width:100%%;
                                        max-width:820px;
                                        height:auto;
                                        border:0;
                                    "
                                />

                            </div>

                        </td>
                    </tr>
                    """;

        } else {

            screenshotBolumu =
                    """
                    <tr>
                        <td style="padding:0 28px 28px 28px;">

                            <div style="
                                padding:12px;
                                background:#f8fafc;
                                border:1px solid #dfe3e8;
                                color:#64748b;
                            ">
                                Hata anına ait ekran görüntüsü oluşturulamadı.
                            </div>

                        </td>
                    </tr>
                    """;
        }

        String mesaj =
                """
                <!DOCTYPE html>
                <html>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f3f4f6;
                    font-family:Arial, Helvetica, sans-serif;
                    color:#1f2937;
                ">

                    <table
                        width="100%%"
                        cellpadding="0"
                        cellspacing="0"
                        border="0"
                        style="background-color:#f3f4f6;"
                    >

                        <tr>

                            <td
                                align="center"
                                style="padding:30px 15px;"
                            >

                                <table
                                    width="850"
                                    cellpadding="0"
                                    cellspacing="0"
                                    border="0"
                                    style="
                                        width:100%%;
                                        max-width:850px;
                                        background:#ffffff;
                                        border:1px solid #e5e7eb;
                                    "
                                >

                                    <!-- Başlık -->
                                    <tr>

                                        <td style="
                                            background:#b91c1c;
                                            padding:24px 28px;
                                            color:#ffffff;
                                        ">

                                            <div style="
                                                font-size:22px;
                                                font-weight:bold;
                                            ">
                                                Test Otomasyon Hata Bildirimi
                                            </div>

                                            <div style="
                                                font-size:14px;
                                                margin-top:7px;
                                                color:#fee2e2;
                                            ">
                                                Ardışık başarısız test koşusu tespit edildi.
                                            </div>

                                        </td>

                                    </tr>


                                    <!-- Açıklama -->
                                    <tr>

                                        <td style="
                                            padding:26px 28px 18px 28px;
                                            font-size:14px;
                                            line-height:1.6;
                                        ">

                                            Merhaba,<br><br>

                                            <strong>%s</strong> otomasyon testi
                                            üst üste

                                            <strong style="color:#b91c1c;">
                                                %d
                                            </strong>

                                            koşuda başarısız olmuştur.

                                        </td>

                                    </tr>


                                    <!-- Durum -->
                                    <tr>

                                        <td style="
                                            padding:0 28px 22px 28px;
                                        ">

                                            <table
                                                width="100%%"
                                                cellpadding="0"
                                                cellspacing="0"
                                                border="0"
                                            >

                                                <tr>

                                                    <td style="
                                                        background:#fef2f2;
                                                        border-left:5px solid #dc2626;
                                                        padding:14px 16px;
                                                        color:#991b1b;
                                                        font-size:14px;
                                                    ">

                                                        <strong>
                                                            DURUM:
                                                        </strong>

                                                        İncelenmesi gereken tekrarlayan
                                                        otomasyon hatası tespit edilmiştir.

                                                    </td>

                                                </tr>

                                            </table>

                                        </td>

                                    </tr>


                                    <!-- Test bilgileri -->
                                    <tr>

                                        <td style="
                                            padding:0 28px 25px 28px;
                                        ">

                                            <table
                                                width="100%%"
                                                cellpadding="0"
                                                cellspacing="0"
                                                border="0"
                                                style="
                                                    border-collapse:collapse;
                                                    font-size:14px;
                                                "
                                            >

                                                <tr>

                                                    <td style="
                                                        width:210px;
                                                        padding:12px;
                                                        background:#f8fafc;
                                                        border:1px solid #dfe3e8;
                                                        font-weight:bold;
                                                    ">
                                                        Test Class
                                                    </td>

                                                    <td style="
                                                        padding:12px;
                                                        border:1px solid #dfe3e8;
                                                    ">
                                                        %s
                                                    </td>

                                                </tr>

                                                <tr>

                                                    <td style="
                                                        padding:12px;
                                                        background:#f8fafc;
                                                        border:1px solid #dfe3e8;
                                                        font-weight:bold;
                                                    ">
                                                        Test
                                                    </td>

                                                    <td style="
                                                        padding:12px;
                                                        border:1px solid #dfe3e8;
                                                    ">
                                                        %s
                                                    </td>

                                                </tr>

                                                <tr>

                                                    <td style="
                                                        padding:12px;
                                                        background:#f8fafc;
                                                        border:1px solid #dfe3e8;
                                                        font-weight:bold;
                                                    ">
                                                        Ardışık Hata Sayısı
                                                    </td>

                                                    <td style="
                                                        padding:12px;
                                                        border:1px solid #dfe3e8;
                                                        color:#dc2626;
                                                        font-size:16px;
                                                        font-weight:bold;
                                                    ">
                                                        %d
                                                    </td>

                                                </tr>

                                                <tr>

                                                    <td style="
                                                        padding:12px;
                                                        background:#f8fafc;
                                                        border:1px solid #dfe3e8;
                                                        font-weight:bold;
                                                    ">
                                                        Tarih
                                                    </td>

                                                    <td style="
                                                        padding:12px;
                                                        border:1px solid #dfe3e8;
                                                    ">
                                                        %s
                                                    </td>

                                                </tr>

                                            </table>

                                        </td>

                                    </tr>


                                    <!-- Son hata -->
                                    <tr>

                                        <td style="
                                            padding:0 28px 25px 28px;
                                        ">

                                            <div style="
                                                font-size:16px;
                                                font-weight:bold;
                                                margin-bottom:10px;
                                            ">
                                                Son Hata
                                            </div>

                                            <div style="
                                                background:#f8fafc;
                                                border:1px solid #dfe3e8;
                                                border-left:4px solid #dc2626;
                                                padding:14px;
                                                font-family:Consolas, monospace;
                                                font-size:13px;
                                                line-height:1.5;
                                                white-space:pre-wrap;
                                            ">%s</div>

                                        </td>

                                    </tr>


                                    <!-- Screenshot -->
                                    %s


                                    <!-- Alt bilgi -->
                                    <tr>

                                        <td style="
                                            background:#f8fafc;
                                            border-top:1px solid #e5e7eb;
                                            padding:18px 28px;
                                            font-size:12px;
                                            line-height:1.5;
                                            color:#64748b;
                                        ">

                                            Aynı test için bir gün içerisinde
                                            en fazla bir hata bilgilendirme maili
                                            gönderilmektedir.

                                            <br><br>

                                            Bu mesaj BVA KP otomasyon test
                                            altyapısı tarafından otomatik olarak
                                            oluşturulmuştur.

                                        </td>

                                    </tr>

                                </table>

                            </td>

                        </tr>

                    </table>

                </body>
                </html>
                """.formatted(
                        htmlEscape(
                                testName
                        ),
                        failureCount,
                        htmlEscape(
                                className
                        ),
                        htmlEscape(
                                testName
                        ),
                        failureCount,
                        tarih,
                        htmlEscape(
                                hataDetayi
                        ),
                        screenshotBolumu
                );

        MailService.inlineGorselliMailGonder(
                konu,
                mesaj,
                screenshotPath,
                null
        );
    }

    /**
     * Dinamik metinlerde bulunan HTML özel karakterlerinin
     * mail tasarımını bozmasını engeller.
     */
    private static String htmlEscape(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}