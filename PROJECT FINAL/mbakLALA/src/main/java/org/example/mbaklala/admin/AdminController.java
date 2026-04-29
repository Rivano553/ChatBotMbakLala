package org.example.mbaklala.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.mbaklala.Launcher;
import org.example.mbaklala.database.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminController {

    // CARD
    @FXML private Label lblPesananBaru;
    @FXML private Label lblPelanggan;
    @FXML private Label lblSelesai;

    // AKTIVITAS
    @FXML private Label lblNama;
    @FXML private Label lblAktivitas;
    @FXML private Label lblWaktu;

    @FXML
    public void initialize() {
        loadDashboard();
        loadAktivitasBaru();
    }

    // ================= DASHBOARD =================
    private void loadDashboard() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            // Pesanan baru
            ResultSet rs1 = st.executeQuery(
                    "SELECT COUNT(*) as total FROM pesanan WHERE status IN ('Proses','Antre')"
            );
            if (rs1.next()) {
                lblPesananBaru.setText(rs1.getString("total"));
            }

            // Pelanggan unik
            ResultSet rs2 = st.executeQuery(
                    "SELECT COUNT(DISTINCT id_pelanggan) as total FROM pesanan"
            );
            if (rs2.next()) {
                lblPelanggan.setText(rs2.getString("total"));
            }

            // Selesai
            ResultSet rs3 = st.executeQuery(
                    "SELECT COUNT(*) as total FROM pesanan WHERE status='Selesai'"
            );
            if (rs3.next()) {
                lblSelesai.setText(rs3.getString("total"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AKTIVITAS =================
    private void loadAktivitasBaru() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement()) {

            ResultSet rs = st.executeQuery(
                    "SELECT * FROM pelanggan ORDER BY tgl_daftar DESC LIMIT 1"
            );

            if (rs.next()) {

                String nama = rs.getString("nama_pelanggan");

                lblNama.setText(nama);
                lblAktivitas.setText("Pelanggan baru");

                java.sql.Timestamp waktuDB = rs.getTimestamp("tgl_daftar");
                long selisih = System.currentTimeMillis() - waktuDB.getTime();

                long menit = selisih / (1000 * 60);

                if (menit < 1) {
                    lblWaktu.setText("Baru saja");
                } else if (menit < 60) {
                    lblWaktu.setText(menit + " menit lalu");
                } else {
                    long jam = menit / 60;
                    lblWaktu.setText(jam + " jam lalu");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handlePesananClick(){
        Launcher.showDaftarPesanan();
    }
    @FXML
    private void handlePengaturanClick(){
        Launcher.showInput();
    }



    public void handlekembali3(ActionEvent actionEvent) {
        Launcher.showHome();
    }
}