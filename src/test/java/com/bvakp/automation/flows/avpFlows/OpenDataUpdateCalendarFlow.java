package com.bvakp.automation.flows.avpFlows;

import com.bvakp.automation.pages.openData.OpenDataUpdateCalendarPage;
import com.bvakp.automation.reporting.ReportManager;

/** Güncelleme Takvimi ekranındaki adımları rapora yazarak çalıştırır. */
public class OpenDataUpdateCalendarFlow {

  private final OpenDataUpdateCalendarPage calendarPage;

  public OpenDataUpdateCalendarFlow(OpenDataUpdateCalendarPage calendarPage) {
    this.calendarPage = calendarPage;
  }

  /** Dönem seçicide verilen dönemi seçer. */
  public void donemSecme(String donem) {
    ReportManager.step("Dönem seçiliyor: " + donem);
    calendarPage.donemSecme(donem);
  }

  /** İşlem Geçmişini veri seti adına göre filtreler. */
  public void gecmisiFiltrele(String veriSetiAdi) {
    ReportManager.step("İşlem geçmişi veri setine göre filtreleniyor: " + veriSetiAdi);
    calendarPage.gecmisVeriSetiFiltreleme(veriSetiAdi);
  }

  /** İşlem Geçmişinde sonuç filtresini seçer (ör. Başarılı). */
  public void gecmisSonucFiltresiSecme(String value) {
    ReportManager.step("İşlem geçmişi sonuç filtresi seçiliyor: " + value);
    calendarPage.gecmisSonucFiltresiSecme(value);
  }

  /** Seçili günün listesinden veri setinin kaydını açar. */
  public void gunKaydiniAcma(String veriSetiAdi) {
    ReportManager.step("Seçili günün listesinden kayıt açılıyor: " + veriSetiAdi);
    calendarPage.gunListesindenKayitAcma(veriSetiAdi);
  }

  /** Güncelleme Ayrıntısından veri seti detayına gider. */
  public void detaydanVeriSetineGitme() {
    ReportManager.step("Güncelleme ayrıntısında Veri setine git tıklanıyor.");
    calendarPage.detaydaVeriSetineGitTiklama();
  }

  /** Güncelleme Ayrıntısı çekmecesini kapatır. */
  public void detayiKapatma() {
    ReportManager.step("Güncelleme ayrıntısı kapatılıyor.");
    calendarPage.detayiKapatma();
  }

  /** Güncelleme Planında veri setini arar. */
  public void planAramaYapma(String veriSetiAdi) {
    ReportManager.step("Güncelleme planında aranıyor: " + veriSetiAdi);
    calendarPage.planAramaYapma(veriSetiAdi);
  }

  /** Veri seti detayında verilen sekmeye geçer. */
  public void detaySekmesineGecme(String sekme) {
    ReportManager.step("Veri seti detayında sekmeye geçiliyor: " + sekme);
    calendarPage.detaySekmesineGecme(sekme);
  }
}
