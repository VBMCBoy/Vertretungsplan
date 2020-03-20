package com.KayKaprolat.Praktikum.Vertretungsplan2;

import java.util.Date;
import java.util.List;

public class Vertretungsplan {

    private List<Vertretungsplaneintrag> eintraege;
    private Date date;
    private List<String> aufsichtsLehrer;
    private List<String> fehlendeKlassen;
    private String baseHtml;

    public Vertretungsplan(String baseHtml) {
        this.baseHtml = baseHtml;
        // TODO hier mit Jsoup schön parsen
    }

    public Vertretungsplan(List<Vertretungsplaneintrag> eintraege, Date date, List<String> aufsichtsLehrer, List<String> fehlendeKlassen) {
        if (eintraege == null || date == null || aufsichtsLehrer == null || fehlendeKlassen == null)
            throw new NullPointerException("Im Konstruktor von Vertretungsplan dürfen keine null-Werte übergeben werden.");
        this.eintraege = eintraege;
        this.date = date;
        this.aufsichtsLehrer = aufsichtsLehrer;
        this.fehlendeKlassen = fehlendeKlassen;
    }

    public String getMarkedHtml() {
        // TODO hier mit Jsoup die entsprechenden Zeilen gelb markieren
        return null;
    }

    public List<Vertretungsplaneintrag> getEintraege() {
        return eintraege;
    }

    public void setEintraege(List<Vertretungsplaneintrag> eintraege) {
        if (eintraege == null) throw new NullPointerException("Einträge dürfen nicht null sein.");
        this.eintraege = eintraege;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<String> getAufsichtsLehrer() {
        return aufsichtsLehrer;
    }

    public void setAufsichtsLehrer(List<String> aufsichtsLehrer) {
        if (aufsichtsLehrer == null) throw new NullPointerException("Aufsichtsführende Lehrer dürfen nicht null sein.");
        this.aufsichtsLehrer = aufsichtsLehrer;
    }

    public List<String> getFehlendeKlassen() {
        return fehlendeKlassen;
    }

    public void setFehlendeKlassen(List<String> fehlendeKlassen) {
        if (fehlendeKlassen == null) throw new NullPointerException("Fehlende Klassen dürfen nicht null sein.");
        this.fehlendeKlassen = fehlendeKlassen;
    }

    public String getBaseHtml() {
        return baseHtml;
    }

    public void setBaseHtml(String baseHtml) {
        if (baseHtml == null) throw new NullPointerException("Basis-HTML darf nicht null sein.");
        this.baseHtml = baseHtml;
    }

}
