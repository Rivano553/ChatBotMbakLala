package id.ac.ukdw.rplbo.model;

public class Layanan {
    private int id;
    private String nama;
    private double hargaReguler;
    private double hargaExpress;
    private String satuan;

    public Layanan(int id, String nama, double hargaReguler, double hargaExpress, String satuan) {
        this.id = id;
        this.nama = nama;
        this.hargaReguler = hargaReguler;
        this.hargaExpress = hargaExpress;
        this.satuan = satuan;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public double getHargaReguler() { return hargaReguler; }
    public double getHargaExpress() { return hargaExpress; }
    public String getSatuan() { return satuan; }

    @Override
    public String toString() { return nama; }
}