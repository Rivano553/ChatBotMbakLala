package org.example.mbaklala.database;

import java.sql.*;
import java.security.MessageDigest;

public class Database {
    private static final String URL = "jdbc:sqlite:launderly.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void initSchema() {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {

            // TABEL ADMIN
            st.execute("CREATE TABLE IF NOT EXISTS admin (id_admin INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, nama_lengkap TEXT, role TEXT)");

            String hashedAdmin123 = hashPassword("admin123");

            st.execute("INSERT OR IGNORE INTO admin (username, password, nama_lengkap, role) VALUES ('admin', '" + hashedAdmin123 + "', 'Dita Pranata', 'Super Admin')");
            st.execute("INSERT OR IGNORE INTO admin (username, password, nama_lengkap, role) VALUES ('rivano', '" + hashedAdmin123 + "', 'Rivano Putra', 'Admin')");
            st.execute("INSERT OR IGNORE INTO admin (username, password, nama_lengkap, role) VALUES ('intan', '" + hashedAdmin123 + "', 'Intan Aryani', 'Admin')");
            st.execute("INSERT OR IGNORE INTO admin (username, password, nama_lengkap, role) VALUES ('samuel', '" + hashedAdmin123 + "', 'Samuel Varabu', 'Admin')");

            // TABEL LAYANAN
            st.execute("CREATE TABLE IF NOT EXISTS layanan (id_layanan INTEGER PRIMARY KEY AUTOINCREMENT, kategori TEXT, nama_layanan TEXT, harga_reguler REAL, harga_express REAL, satuan TEXT)");

            ResultSet rs = st.executeQuery("SELECT COUNT(*) AS total FROM layanan");
            if (rs.next() && rs.getInt("total") == 0) {
                st.execute("INSERT INTO layanan (kategori, nama_layanan, harga_reguler, harga_express, satuan) VALUES " +
                        "('Kiloan', 'Pakaian Harian', 5000, 8000, 'kg')," +
                        "('Kiloan', 'Handuk', 6000, 10000, 'kg')," +
                        "('Kiloan', 'Seragam', 6000, 10000, 'kg')," +
                        "('Kiloan', 'Pakaian Bayi atau Anak', 7000, 12000, 'kg')," +
                        "('Kiloan', 'Gordyn atau Tirai', 8000, 14000, 'kg')," +
                        "('Kiloan', 'Boneka', 8000, 14000, 'kg')," +
                        "('Satuan', 'Jas atau Blazer', 25000, 40000, 'pcs')," +
                        "('Satuan', 'Sepatu', 20000, 35000, 'pasang')," +
                        "('Satuan', 'Tas atau Ransel', 20000, 35000, 'pcs')," +
                        "('Satuan', 'Selimut Single', 20000, 35000, 'pcs')," +
                        "('Satuan', 'Selimut Double/Queen', 30000, 50000, 'pcs')," +
                        "('Satuan', 'Bed Cover', 35000, 55000, 'pcs')," +
                        "('Satuan', 'Sprei & Sarung Bantal', 25000, 40000, 'set')");
            }

            // TABEL PESANAN
            st.execute("CREATE TABLE IF NOT EXISTS pesanan (" +
                    "id_pesanan TEXT PRIMARY KEY, " +
                    "nama_pelanggan TEXT, " +
                    "no_telepon TEXT, " +
                    "alamat_jemput TEXT, " +
                    "deskripsi TEXT, " +
                    "metode_pembayaran TEXT DEFAULT 'Cash', " +
                    "status_bayar TEXT DEFAULT 'Belum Dibayar', " +
                    "status TEXT DEFAULT 'Menunggu Jemput', " +
                    "tgl_masuk TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // TABEL PESANAN LAYANAN
            st.execute("CREATE TABLE IF NOT EXISTS pesanan_layanan (" +
                    "id_detail INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "id_pesanan TEXT, " +
                    "id_layanan INTEGER, " +
                    "jenis_layanan TEXT, " +
                    "berat REAL DEFAULT 0, " +
                    "total_bayar REAL DEFAULT 0, " +
                    "FOREIGN KEY (id_pesanan) REFERENCES pesanan(id_pesanan) ON DELETE CASCADE)");

            try {
                st.execute("ALTER TABLE pesanan ADD COLUMN metode_pembayaran TEXT DEFAULT 'Cash'");
            } catch (Exception ignored) {}

            try {
                st.execute("ALTER TABLE pesanan ADD COLUMN status_bayar TEXT DEFAULT 'Belum Dibayar'");
            } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}