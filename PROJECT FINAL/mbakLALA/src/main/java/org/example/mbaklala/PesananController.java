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

    // =========================
    // 🔥 SIMPAN DATA
    // =========================
    @FXML
    private void handleSimpan(ActionEvent event) {

        String nama = txtNama.getText().trim();
        String telp = txtTelepon.getText().trim();
        String maps = txtLinkMap.getText().trim();

        // 🔥 ambil waktu sekarang (tanggal + jam)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // VALIDASI
        if (nama.isEmpty()) {
            showAlert("Error", "Nama wajib diisi!", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conn = Database.getConnection()) {

            if (conn == null) {
                showAlert("Error", "Koneksi database gagal!", Alert.AlertType.ERROR);
                return;
            }

            // =========================
            // 🔍 CEK PELANGGAN
            // =========================
            String cekSql = "SELECT id_pelanggan FROM pelanggan WHERE nama_pelanggan = ?";
            PreparedStatement psCek = conn.prepareStatement(cekSql);
            psCek.setString(1, nama);

            ResultSet rs = psCek.executeQuery();

            if (!rs.next()) {

                // =========================
                // 🆕 INSERT PELANGGAN BARU
                // =========================
                String sql = "INSERT INTO pelanggan (nama_pelanggan, no_telepon, link_maps, tgl_daftar) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setString(1, nama);
                ps.setString(2, telp);
                ps.setString(3, maps);

                // 🔥 simpan timestamp (tanggal + jam)
                ps.setTimestamp(4, Timestamp.valueOf(now));

                ps.executeUpdate();

                showAlert("Berhasil", "Data pelanggan berhasil disimpan!", Alert.AlertType.INFORMATION);

            } else {
                showAlert("Info", "Pelanggan sudah terdaftar.", Alert.AlertType.INFORMATION);
            }

            // 🔙 kembali ke chatbot
            Launcher.showBot();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // =========================
    // 🔙 KEMBALI
    // =========================
    @FXML
    private void handleBack(ActionEvent event) {
        Launcher.showBot();
    }

    // =========================
    // 🔔 ALERT HELPER
    // =========================
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}