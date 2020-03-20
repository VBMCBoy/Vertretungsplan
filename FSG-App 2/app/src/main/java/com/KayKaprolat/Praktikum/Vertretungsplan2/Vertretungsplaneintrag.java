package com.KayKaprolat.Praktikum.Vertretungsplan2;

public class Vertretungsplaneintrag {

    private String clazz;
    private String change;


    private String insteadOf;

    public Vertretungsplaneintrag(String clazz, String change, String insteadOf) {
        if ((clazz == null) || (change == null))
            throw new NullPointerException("Klasse und Änderung dürfen nicht null sein.");
        this.clazz = clazz;
        this.change = change;
        this.insteadOf = insteadOf;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        if (clazz != null)
            this.clazz = clazz;
    }

    public String getChange() {
        return change;
    }

    public void setChange(String change) {
        if (change != null)
            this.change = change;
    }

    public String getInsteadOf() {
        return insteadOf;
    }

    public void setInsteadOf(String insteadOf) {
        this.insteadOf = insteadOf;
    }
}
