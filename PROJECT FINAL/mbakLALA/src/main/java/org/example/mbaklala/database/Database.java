package org.example.mbaklala.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/launderly";
    private static final String USER = "root";
    private static final String PASS = "";

    // 🔥 KONEKSI UTAMA (TIDAK ADA throws)
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 🔥 CEK KONEKSI (BOOLEAN)
    public static boolean cekKoneksi() {
        try (Connection conn = getConnection()) {
            return conn != null;
        } catch (Exception e) {
            return false;
        }
    }
}