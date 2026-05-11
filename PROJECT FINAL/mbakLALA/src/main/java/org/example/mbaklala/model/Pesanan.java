package org.example.mbaklala.model;

public class Pesanan {
    private String id; private String nama; private String layanan; private double berat; private String status;
    public Pesanan(String id, String nama, String layanan, double berat, String status) {
        this.id = id; this.nama = nama; this.layanan = layanan; this.berat = berat; this.status = status;
    }
    public String getId() { return id; }
    public String getStatus() { return status; }
    @Override public String toString() { return id + " - " + nama + " [" + status + "]"; }
}