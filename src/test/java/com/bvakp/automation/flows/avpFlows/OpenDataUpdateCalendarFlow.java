package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.pages.openData.OpenDataUpdateCalendarPage;
import com.bvakp.automation.reporting.ReportManager;

public class OpenDataUpdateCalendarFlow {

    private final OpenDataUpdateCalendarPage calendarPage;

    public OpenDataUpdateCalendarFlow(OpenDataUpdateCalendarPage calendarPage) {
        this.calendarPage = calendarPage;
    }

    public void donemSecme(String donem) {
        ReportManager.info("Dönem seçiliyor: " + donem);
        calendarPage.donemSecme(donem);
    }

    public void gecmisiFiltrele(String veriSetiAdi) {
        ReportManager.info("İşlem geçmişi veri setine göre filtreleniyor: " + veriSetiAdi);
        calendarPage.gecmisVeriSetiFiltreleme(veriSetiAdi);
    }

    public void gecmisSonucFiltresiSecme(String value) {
        ReportManager.info("İşlem geçmişi sonuç filtresi seçiliyor: " + value);
        calendarPage.gecmisSonucFiltresiSecme(value);
    }

    public void gunKaydiniAcma(String veriSetiAdi) {
        ReportManager.info("Seçili günün listesinden kayıt açılıyor: " + veriSetiAdi);
        calendarPage.gunListesindenKayitAcma(veriSetiAdi);
    }

    public void detaydanVeriSetineGitme() {
        ReportManager.info("Güncelleme ayrıntısında Veri setine git tıklanıyor.");
        calendarPage.detaydaVeriSetineGitTiklama();
    }

    public void detayiKapatma() {
        ReportManager.info("Güncelleme ayrıntısı kapatılıyor.");
        calendarPage.detayiKapatma();
    }

    public void planAramaYapma(String veriSetiAdi) {
        ReportManager.info("Güncelleme planında aranıyor: " + veriSetiAdi);
        calendarPage.planAramaYapma(veriSetiAdi);
    }

    public void detaySekmesineGecme(String sekme) {
        ReportManager.info("Veri seti detayında sekmeye geçiliyor: " + sekme);
        calendarPage.detaySekmesineGecme(sekme);
    }
}