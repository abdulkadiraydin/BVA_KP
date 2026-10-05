package com.bvakp.automation.mail;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Otomasyon test raporlarının SMTP üzerinden e-posta olarak
 * gönderilmesini sağlar.
 *
 * Standart HTML mail, attachment ve mail gövdesi içerisinde
 * görüntülenecek inline screenshot gönderimini destekler.
 */
public final class MailService {

    private MailService() {
    }

    /**
     * Belirtilen konu, HTML mesaj ve isteğe bağlı attachment
     * ile standart mail gönderir.
     *
     * @param konu       mail konusu
     * @param mesaj      HTML mail içeriği
     * @param attachment eklenecek dosya
     */
    public static void mailGonder(
            String konu,
            String mesaj,
            Path attachment) {

        try {

            Session session =
                    sessionOlusturma();

            MimeMessage mimeMessage =
                    temelMailOlusturma(
                            session,
                            konu
                    );

            MimeMultipart multipart =
                    new MimeMultipart();

            /*
             * HTML mail gövdesi.
             */
            MimeBodyPart mesajBolumu =
                    new MimeBodyPart();

            mesajBolumu.setContent(
                    mesaj,
                    "text/html; charset=UTF-8"
            );

            multipart.addBodyPart(
                    mesajBolumu
            );

            /*
             * Attachment verilmişse mail'e eklenir.
             */
            if (attachment != null) {

                attachmentEkleme(
                        multipart,
                        attachment
                );
            }

            mimeMessage.setContent(
                    multipart
            );

            Transport.send(
                    mimeMessage
            );

            System.out.println(
                    "Mail başarıyla gönderildi."
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Mail gönderilirken hata oluştu.",
                    e
            );
        }
    }

    /**
     * HTML mail içeriği içerisinde screenshot görüntülenmesini sağlar.
     *
     * Screenshot mail'e inline olarak eklenir ve HTML içerisinde
     * cid:failureScreenshot referansı ile görüntülenir.
     *
     * @param konu           mail konusu
     * @param htmlMesaj      HTML mail içeriği
     * @param screenshotPath mail içerisinde gösterilecek screenshot
     * @param attachment     isteğe bağlı ek dosya
     */
    public static void inlineGorselliMailGonder(
            String konu,
            String htmlMesaj,
            Path screenshotPath,
            Path attachment) {

        try {

            Session session =
                    sessionOlusturma();

            MimeMessage mimeMessage =
                    temelMailOlusturma(
                            session,
                            konu
                    );

            /*
             * Dış multipart;
             * HTML + inline image ve attachment'ı birlikte tutar.
             */
            MimeMultipart mixedMultipart =
                    new MimeMultipart(
                            "mixed"
                    );

            /*
             * HTML body ile screenshot'ın birbirine bağlı
             * olmasını sağlayan related multipart.
             */
            MimeMultipart relatedMultipart =
                    new MimeMultipart(
                            "related"
                    );

            MimeBodyPart htmlPart =
                    new MimeBodyPart();

            htmlPart.setContent(
                    htmlMesaj,
                    "text/html; charset=UTF-8"
            );

            relatedMultipart.addBodyPart(
                    htmlPart
            );

            /*
             * Screenshot mevcutsa mail gövdesine inline eklenir.
             */
            if (screenshotPath != null
                    && Files.exists(
                    screenshotPath
            )) {

                MimeBodyPart imagePart =
                        new MimeBodyPart();

                DataSource imageSource =
                        new FileDataSource(
                                screenshotPath.toFile()
                        );

                imagePart.setDataHandler(
                        new DataHandler(
                                imageSource
                        )
                );

                /*
                 * HTML içerisinde:
                 *
                 * <img src="cid:failureScreenshot">
                 *
                 * şeklinde kullanılacaktır.
                 */
                imagePart.setHeader(
                        "Content-ID",
                        "<failureScreenshot>"
                );

                imagePart.setDisposition(
                        MimeBodyPart.INLINE
                );

                relatedMultipart.addBodyPart(
                        imagePart
                );
            }

            /*
             * Related multipart dış mail gövdesine eklenir.
             */
            MimeBodyPart relatedContainer =
                    new MimeBodyPart();

            relatedContainer.setContent(
                    relatedMultipart
            );

            mixedMultipart.addBodyPart(
                    relatedContainer
            );

            /*
             * Ek rapor verilmişse ayrıca attachment olarak eklenir.
             */
            if (attachment != null) {

                attachmentEkleme(
                        mixedMultipart,
                        attachment
                );
            }

            mimeMessage.setContent(
                    mixedMultipart
            );

            Transport.send(
                    mimeMessage
            );

            System.out.println(
                    "Inline screenshot içeren mail başarıyla gönderildi."
            );

        } catch (MessagingException e) {

            throw new RuntimeException(
                    "Inline screenshot içeren mail gönderilirken hata oluştu.",
                    e
            );
        }
    }

    /**
     * Mail'in gönderen, alıcı ve konu gibi
     * ortak temel bilgilerini oluşturur.
     */
    private static MimeMessage temelMailOlusturma(
            Session session,
            String konu)
            throws MessagingException {

        MimeMessage mimeMessage =
                new MimeMessage(
                        session
                );

        mimeMessage.setFrom(
                new InternetAddress(
                        MailConfig.from()
                )
        );

        alicilariEkleme(
                mimeMessage
        );

        mimeMessage.setSubject(
                konu,
                "UTF-8"
        );

        return mimeMessage;
    }

    /**
     * SMTP bağlantısı için gerekli mail session nesnesini oluşturur.
     */
    private static Session sessionOlusturma() {

        Properties properties =
                new Properties();

        properties.put(
                "mail.smtp.auth",
                "true"
        );

        properties.put(
                "mail.smtp.starttls.enable",
                String.valueOf(
                        MailConfig.startTlsEnabled()
                )
        );

        properties.put(
                "mail.smtp.host",
                MailConfig.host()
        );

        properties.put(
                "mail.smtp.port",
                String.valueOf(
                        MailConfig.port()
                )
        );

        return Session.getInstance(
                properties,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                    getPasswordAuthentication() {

                        return new PasswordAuthentication(
                                MailConfig.username(),
                                MailConfig.password()
                        );
                    }
                }
        );
    }

    /**
     * MAIL_TO içerisinde virgülle ayrılmış
     * bir veya birden fazla alıcıyı mail'e ekler.
     */
    private static void alicilariEkleme(
            MimeMessage message)
            throws MessagingException {

        String[] alicilar =
                MailConfig.to()
                        .split(",");

        for (String alici : alicilar) {

            String temizAlici =
                    alici.trim();

            if (!temizAlici.isBlank()) {

                message.addRecipient(
                        Message.RecipientType.TO,
                        new InternetAddress(
                                temizAlici
                        )
                );
            }
        }
    }

    /**
     * Belirtilen dosyayı mail attachment olarak ekler.
     */
    private static void attachmentEkleme(
            MimeMultipart multipart,
            Path attachment)
            throws MessagingException {

        if (!Files.exists(
                attachment
        )) {

            throw new IllegalArgumentException(
                    "Mail attachment dosyası bulunamadı: "
                            + attachment.toAbsolutePath()
            );
        }

        MimeBodyPart attachmentPart =
                new MimeBodyPart();

        DataSource source =
                new FileDataSource(
                        attachment.toFile()
                );

        attachmentPart.setDataHandler(
                new DataHandler(
                        source
                )
        );

        attachmentPart.setFileName(
                attachment
                        .getFileName()
                        .toString()
        );

        multipart.addBodyPart(
                attachmentPart
        );
    }
}