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
 * Otomasyon e-postalarını SMTP üzerinden gönderir.
 *
 * <p>Standart HTML mail, dosya eki ve mail gövdesinde görüntülenen
 * inline ekran görüntüsünü destekler. SMTP ayarları MailConfig'ten okunur.
 */
public final class MailService {

    /** HTML içinde <img src="cid:failureScreenshot"> ile referans verilir. */
    public static final String INLINE_SCREENSHOT_ID = "failureScreenshot";

    private static final String BAGLANTI_ZAMAN_ASIMI_MS = "10000";
    private static final String OKUMA_YAZMA_ZAMAN_ASIMI_MS = "30000";

    private MailService() {
    }

    /**
     * HTML mail gönderir; attachment verilmişse ek olarak ekler.
     */
    public static void mailGonder(String konu, String htmlMesaj, Path attachment) {

        try {
            MimeMessage mimeMessage = temelMailOlustur(konu);

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlBolumuOlustur(htmlMesaj));

            if (attachment != null) {
                attachmentEkle(multipart, attachment);
            }

            mimeMessage.setContent(multipart);
            Transport.send(mimeMessage);

            System.out.println("Mail başarıyla gönderildi.");

        } catch (MessagingException e) {
            throw new RuntimeException("Mail gönderilirken hata oluştu.", e);
        }
    }

    /**
     * Ekran görüntüsünü mail gövdesinde gösteren HTML mail gönderir.
     * Ekran görüntüsü yoksa mail görselsiz gönderilir.
     */
    public static void inlineGorselliMailGonder(
            String konu,
            String htmlMesaj,
            Path screenshotPath,
            Path attachment) {

        try {
            MimeMessage mimeMessage = temelMailOlustur(konu);

            // "related": HTML ve içinde kullanılan görsel birbirine bağlıdır.
            MimeMultipart relatedMultipart = new MimeMultipart("related");
            relatedMultipart.addBodyPart(htmlBolumuOlustur(htmlMesaj));

            if (screenshotPath != null && Files.exists(screenshotPath)) {
                MimeBodyPart imagePart = new MimeBodyPart();
                imagePart.setDataHandler(new DataHandler(new FileDataSource(screenshotPath.toFile())));
                imagePart.setHeader("Content-ID", "<" + INLINE_SCREENSHOT_ID + ">");
                imagePart.setDisposition(MimeBodyPart.INLINE);
                relatedMultipart.addBodyPart(imagePart);
            }

            MimeBodyPart relatedContainer = new MimeBodyPart();
            relatedContainer.setContent(relatedMultipart);

            // "mixed": gövde (HTML + görsel) ve ayrı dosya ekleri birlikte taşınır.
            MimeMultipart mixedMultipart = new MimeMultipart("mixed");
            mixedMultipart.addBodyPart(relatedContainer);

            if (attachment != null) {
                attachmentEkle(mixedMultipart, attachment);
            }

            mimeMessage.setContent(mixedMultipart);
            Transport.send(mimeMessage);

            System.out.println("Inline screenshot içeren mail başarıyla gönderildi.");

        } catch (MessagingException e) {
            throw new RuntimeException("Inline screenshot içeren mail gönderilirken hata oluştu.", e);
        }
    }

    /** Gönderen, alıcılar ve konu gibi ortak bilgileri içeren mesajı oluşturur. */
    private static MimeMessage temelMailOlustur(String konu) throws MessagingException {

        MimeMessage mimeMessage = new MimeMessage(sessionOlustur());
        mimeMessage.setFrom(new InternetAddress(MailConfig.from()));
        alicilariEkle(mimeMessage);
        mimeMessage.setSubject(konu, "UTF-8");

        return mimeMessage;
    }

    private static Session sessionOlustur() {

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(MailConfig.startTlsEnabled()));
        properties.put("mail.smtp.host", MailConfig.host());
        properties.put("mail.smtp.port", String.valueOf(MailConfig.port()));

        // Varsayılan sonsuz beklemedir; hata maili test thread'inde gönderildiği için
        // sunucu cevap vermezse tüm koşu kilitlenmesin.
        properties.put("mail.smtp.connectiontimeout", BAGLANTI_ZAMAN_ASIMI_MS);
        properties.put("mail.smtp.timeout", OKUMA_YAZMA_ZAMAN_ASIMI_MS);
        properties.put("mail.smtp.writetimeout", OKUMA_YAZMA_ZAMAN_ASIMI_MS);

        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MailConfig.username(), MailConfig.password());
            }
        });
    }

    private static MimeBodyPart htmlBolumuOlustur(String htmlMesaj) throws MessagingException {
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlMesaj, "text/html; charset=UTF-8");
        return htmlPart;
    }

    /** MAIL_TO içindeki virgülle ayrılmış alıcıları ekler. */
    private static void alicilariEkle(MimeMessage message) throws MessagingException {

        for (String alici : MailConfig.to().split(",")) {
            String temizAlici = alici.trim();
            if (!temizAlici.isBlank()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(temizAlici));
            }
        }
    }

    private static void attachmentEkle(MimeMultipart multipart, Path attachment) throws MessagingException {

        if (!Files.exists(attachment)) {
            throw new IllegalArgumentException(
                    "Mail attachment dosyası bulunamadı: " + attachment.toAbsolutePath());
        }

        DataSource source = new FileDataSource(attachment.toFile());

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(attachment.getFileName().toString());

        multipart.addBodyPart(attachmentPart);
    }
}