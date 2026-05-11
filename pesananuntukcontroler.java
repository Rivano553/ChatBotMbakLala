package org.example.mbaklala;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.mbaklala.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PesananController {

    @FXML private TextField txtNama;
    @FXML private TextField txtTelepon;
    @FXML private TextField txtLinkMap;

    @FXML
    private void handleSimpan(ActionEvent event) {
        String nama = txtNama.getText().trim();
        String telp = txtTelepon.getText().trim();
        String maps = txtLinkMap.getText().trim();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 1. Validasi Input Kosong
        if (nama.isEmpty() || telp.isEmpty()) {
            showAlert("Error", "Nama dan Nomor Telepon wajib diisi!", Alert.AlertType.WARNING);
            return;
        }

        // 2. Validasi Format Telepon (Hanya Angka)
        if (!telp.matches("\\d+")) {
            showAlert("Error", "Nomor telepon harus berupa angka!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = Database.getConnection()) {
            if (conn == null) {
                showAlert("Error", "Koneksi database gagal!", Alert.AlertType.ERROR);
                return;
            }

            // 3. Cek apakah Nama ATAU Nomor Telepon sudah ada
            String cekSql = "SELECT id_pelanggan FROM pelanggan WHERE LOWER(nama_pelanggan) = LOWER(?) OR no_telepon = ?";
            
            try (PreparedStatement psCek = conn.prepareStatement(cekSql)) {
                psCek.setString(1, nama);
                psCek.setString(2, telp);
                
                try (ResultSet rs = psCek.executeQuery()) {
                    if (rs.next()) {
                        showAlert("Info", "Pelanggan dengan nama atau nomor tersebut sudah terdaftar.", Alert.AlertType.INFORMATION);
                    } else {
                        // 4. Proses Simpan Data Baru
                        String sql = "INSERT INTO pelanggan (nama_pelanggan, no_telepon, link_maps, tgl_daftar) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setString(1, nama);
                            ps.setString(2, telp);
                            ps.setString(3, maps);
                            ps.setTimestamp(4, Timestamp.valueOf(now));
                            
                            ps.executeUpdate();
                            showAlert("Berhasil", "Data pelanggan berhasil disimpan!", Alert.AlertType.INFORMATION);
                            
                            // 5. Bersihkan form setelah sukses
                            bersihkanForm();
                        }
                    }
                }
            }
            
            // Kembali ke tampilan bot
            Launcher.showBot();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Gagal menyimpan data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        Launcher.showBot();
    }

    private void bersihkanForm() {
        txtNama.clear();
        txtTelepon.clear();
        txtLinkMap.clear();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
