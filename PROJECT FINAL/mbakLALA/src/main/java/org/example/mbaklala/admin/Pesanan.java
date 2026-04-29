package org.example.mbaklala.admin;

public class Pesanan {

    private String id;
    private String nama;
    private String layanan;
    private double berat;

    public Pesanan(String id, String nama, String layanan, double berat) {
        this.id = id;
        this.nama = nama;
        this.layanan = layanan;
        this.berat = berat;
    }

    public String getId() { return id; }
    public String getNama() { return nama; }
    public String getLayanan() { return layanan; }
    public double getBerat() { return berat; }

    @Override
    public String toString() {
        return id + " - " + nama + " - " + layanan + " (" + berat + " kg)";
    }
}