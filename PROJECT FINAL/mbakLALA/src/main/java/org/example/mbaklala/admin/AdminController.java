package org.example.mbaklala.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.mbaklala.model.Launcher;
import org.example.mbaklala.database.Database;
import java.sql.*;

public class AdminController {
    @FXML private Label lblJemput, lblAntre, lblProses, lblSiap, lblDiantar, lblSelesai, lblPendapatan, lblNama;

    @FXML
    public void initialize() {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
            lblJemput.setText(getCount(st, "Menunggu Jemput"));
            lblAntre.setText(getCount(st, "Antre Cuci"));
            lblProses.setText(getCount(st, "Proses Cuci & Setrika"));
            lblSiap.setText(getCount(st, "Siap Antar"));
            lblDiantar.setText(getCount(st, "Sedang Diantar"));
            lblSelesai.setText(getCount(st, "Selesai"));

            ResultSet rs4 = st.executeQuery("SELECT SUM(pl.total_bayar) as total FROM pesanan_layanan pl JOIN pesanan p ON pl.id_pesanan = p.id_pesanan WHERE p.status = 'Selesai'");
            if (rs4.next()) lblPendapatan.setText("Rp " + String.format("%,.0f", rs4.getDouble("total")));

            ResultSet rs5 = st.executeQuery("SELECT nama_pelanggan, status FROM pesanan ORDER BY tgl_masuk DESC LIMIT 1");
            if (rs5.next()) lblNama.setText("Pesanan terbaru dari " + rs5.getString("nama_pelanggan") + " (Status: " + rs5.getString("status") + ")");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getCount(Statement st, String status) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) as total FROM pesanan WHERE status = '" + status + "'");
        return rs.next() ? rs.getString("total") : "0";
    }

    @FXML private void handleKelolaPesanan() { Launcher.showDaftarPesanan(); }
    @FXML private void handleRiwayat() { Launcher.showRiwayat(); }
    @FXML public void handleLogout(ActionEvent e) { Launcher.showHome(); }
}