package org.example.mbaklala.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.mbaklala.Launcher;
import org.example.mbaklala.database.Database;
import java.sql.*;

public class AdminController {
    @FXML private Label lblJemput, lblAntre, lblProses, lblSiap, lblDiantar, lblSelesai, lblPendapatan, lblNama;

    @FXML
    public void initialize() {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {

            // status pesanan
            lblJemput.setText(getCount(st, "Menunggu Jemput"));

            lblAntre.setText(getCount(st, "Antre Cuci"));

            lblProses.setText(getCount(st, "Proses Cuci & Setrika"));

            lblSiap.setText(getCount(st, "Siap Antar"));

            lblDiantar.setText(getCount(st, "Sedang Diantar"));

            lblSelesai.setText(getCount(st, "Selesai"));

            // 7. Total Pendapatan
            ResultSet rs4 = st.executeQuery(
                    "SELECT SUM(pl.total_bayar) as total FROM pesanan_layanan pl " +
                            "JOIN pesanan p ON pl.id_pesanan = p.id_pesanan WHERE p.status = 'Selesai'"
            );
            if (rs4.next()) {
                double total = rs4.getDouble("total");
                lblPendapatan.setText("Rp " + String.format("%,.0f", total));
            }

            // Menampilkan aktivitas terbaru setelah order laundry
            ResultSet rs5 = st.executeQuery("SELECT nama_pelanggan, status FROM pesanan ORDER BY tgl_masuk DESC LIMIT 1");
            if (rs5.next()) {
                lblNama.setText("Pesanan terbaru dari " + rs5.getString("nama_pelanggan") + " (Status: " + rs5.getString("status") + ")");
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // Query jumlah pesanan berdasarkan status tertentu
    private String getCount(Statement st, String status) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) as total FROM pesanan WHERE status = '" + status + "'");
        return rs.next() ? rs.getString("total") : "0";
    }

    @FXML
    private void handleKelolaPesanan() {
        Launcher.showDaftarPesanan();
    }

    @FXML
    private void handleRiwayat() {
        Launcher.showRiwayat();
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        Launcher.showHome();
    }
}