package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.mbaklala.Launcher;
import org.example.mbaklala.database.Database;
import org.example.mbaklala.model.Pesanan;
import java.sql.*;

public class RiwayatController {
    @FXML private ListView<Pesanan> listRiwayat;
    @FXML private Label lblId, lblNama, lblTelp, lblAlamat, lblTotal;

    // TAMBAHAN: Deklarasi label untuk pembayaran
    @FXML private Label lblMetodeBayar, lblStatusBayar;

    @FXML private VBox vboxLayananDetail;

    private final ObservableList<Pesanan> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadListRiwayat();

        listRiwayat.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                loadDetailRiwayat(selected.getId());
            }
        });
    }

    private void loadListRiwayat() {
        data.clear();
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
            // HANYA MENGAMBIL PESANAN DENGAN STATUS 'Selesai'
            ResultSet rs = st.executeQuery("SELECT p.id_pesanan, p.nama_pelanggan, p.status FROM pesanan p WHERE p.status = 'Selesai' ORDER BY p.tgl_masuk DESC");
            while (rs.next()) {
                data.add(new Pesanan(rs.getString("id_pesanan"), rs.getString("nama_pelanggan"), "", 0.0, rs.getString("status")));
            }
            listRiwayat.setItems(data);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDetailRiwayat(String id) {
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM pesanan WHERE id_pesanan = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblId.setText(rs.getString("id_pesanan"));
                lblNama.setText(rs.getString("nama_pelanggan"));
                lblTelp.setText(rs.getString("no_telepon"));
                lblAlamat.setText(rs.getString("alamat_jemput"));

                // TAMBAHAN: Mengambil data pembayaran
                String metode = rs.getString("metode_pembayaran");
                String statusBayar = rs.getString("status_bayar");

                lblMetodeBayar.setText(metode != null ? metode : "-");

                // Set teks dan warna untuk Status Bayar
                if ("Lunas".equals(statusBayar)) {
                    lblStatusBayar.setText("Lunas");
                    lblStatusBayar.setStyle("-fx-font-weight: bold; -fx-text-fill: #2ECC71; -fx-font-family: 'Segoe UI';"); // Hijau
                } else {
                    lblStatusBayar.setText("Belum Lunas");
                    lblStatusBayar.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C; -fx-font-family: 'Segoe UI';"); // Merah
                }

                loadLayananRiwayat(id);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadLayananRiwayat(String id) throws SQLException {
        vboxLayananDetail.getChildren().clear();
        double total = 0;
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT l.nama_layanan, pl.berat, pl.total_bayar FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double b = rs.getDouble("berat");
                String txt = "• " + rs.getString("nama_layanan") + (b > 0 ? " (" + b + ")" : "");

                Label lblLayanan = new Label(txt);
                lblLayanan.setStyle("-fx-text-fill: #1e4581; -fx-font-family: 'Segoe UI';");

                vboxLayananDetail.getChildren().add(lblLayanan);
                total += rs.getDouble("total_bayar");
            }
            lblTotal.setText("Rp " + String.format("%,.0f", total));
        }
    }

    @FXML private void handleKembali() {
        Launcher.showAdmin();
    }
}