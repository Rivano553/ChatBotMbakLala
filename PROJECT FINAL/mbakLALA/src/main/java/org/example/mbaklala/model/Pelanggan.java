package org.example.mbaklala.model;

public class Pelanggan {
    private int id;
    private String nama;
    private String maps;

    public Pelanggan(int id, String nama, String maps) {
        this.id = id;
        this.nama = nama;
        this.maps = maps;
    }

    public int getId() { return id; }
    public String getMaps() { return maps; }

    @Override
    public String toString() {
        return nama;
    }
}