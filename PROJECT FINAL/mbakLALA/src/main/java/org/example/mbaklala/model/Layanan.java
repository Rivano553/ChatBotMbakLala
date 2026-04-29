package org.example.mbaklala.model;

public class Layanan {
    private int id;
    private String nama;
    private double harga;
    private String estimasi;

    public Layanan(int id, String nama, double harga, String estimasi) {
        this.id = id;
        this.nama = nama;
        this.harga = harga;
        this.estimasi = estimasi;
    }

    public int getId() { return id; }
    public double getHarga() { return harga; }
    public String getEstimasi() { return estimasi; }

    @Override
    public String toString() {
        return nama;
    }
}