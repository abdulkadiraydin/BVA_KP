package com.bvakp.automation.mail;

public class MailRunner {

    public static void main(String[] args) {

        System.out.println(
                "MAIL_HOST tanımlı mı: "
                        + (System.getenv("MAIL_HOST") != null)
        );

        System.out.println(
                "MAIL_USERNAME tanımlı mı: "
                        + (System.getenv("MAIL_USERNAME") != null)
        );

        System.out.println(
                "MAIL_PASSWORD tanımlı mı: "
                        + (System.getenv("MAIL_PASSWORD") != null)
        );

        TestReportMailService
                .sonRaporuMailGonder();
    }
}