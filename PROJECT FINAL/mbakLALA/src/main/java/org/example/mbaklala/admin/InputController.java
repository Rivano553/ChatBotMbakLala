package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mbaklala.Launcher;
import org.example.mbaklala.database.Database;
import org.example.mbaklala.model.Layanan;
import org.example.mbaklala.model.Pelanggan;

import java.sql.*;

public class InputController {

    @FXML private ComboBox<Pelanggan> cbPelanggan;
    @FXML private ComboBox<Layanan> cbLayanan;
    @FXML private TextField tfBerat;

    @FXML private Label lblHarga;
    @FXML private Label lblEstimasi;
    @FXML private Label lblTotal;
    @FXML private Label lblId;

    private String generatedId;

    @FXML
    public void initialize() {
        loadPelanggan();
        loadLayanan();
        generateId();

        // 🔥 update harga & estimasi saat pilih layanan
        cbLayanan.setOnAction(e -> updateLayanan());

        // 🔥 auto hitung saat ketik berat
        tfBerat.textProperty().addListener((obs, oldVal, newVal) -> hitungTotal());
    }

    // ================= LOAD DATA =================

    private void loadPelanggan() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM pelanggan")) {

            while (rs.next()) {
                cbPelanggan.getItems().add(
                        new Pelanggan(
                                rs.getInt("id_pelanggan"),
                                rs.getString("nama_pelanggan"),
                                rs.getString("link_maps")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadLayanan() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM layanan")) {

            while (rs.next()) {
                cbLayanan.getItems().add(
                        new Layanan(
                                rs.getInt("id_layanan"),
                                rs.getString("nama_layanan"),
                                rs.getDouble("harga_per_unit"),
                                rs.getString("estimasi")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= AUTO ID =================

    private void generateId() {
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id_pesanan FROM pesanan ORDER BY id_pesanan DESC LIMIT 1"
             )) {

            if (rs.next()) {
                String lastId = rs.getString("id_pesanan").replace("#", "");
                int next = Integer.parseInt(lastId) + 1;
                generatedId = "#" + next;
            } else {
                generatedId = "#2601";
            }

            lblId.setText("ID: " + generatedId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UPDATE UI =================

    private void updateLayanan() {
        Layanan l = cbLayanan.getValue();
        if (l != null) {
            lblHarga.setText("Harga: Rp " + l.getHarga());
            lblEstimasi.setText("Estimasi: " + l.getEstimasi());
            hitungTotal();
        }
    }

    private void hitungTotal() {
        try {
            Layanan l = cbLayanan.getValue();
            double berat = Double.parseDouble(tfBerat.getText());

            if (l != null) {
                double total = berat * l.getHarga();
                lblTotal.setText("Total: Rp " + String.format("%,.0f", total));
            }

        } catch (Exception ignored) {}
    }

    // ================= SIMPAN =================

    @FXML
    private void handleSimpan() {

        Pelanggan p = cbPelanggan.getValue();
        Layanan l = cbLayanan.getValue();

        if (p == null || l == null || tfBerat.getText().isEmpty()) {
            showAlert("Error", "Lengkapi data!");
            return;
        }

        try {
            double berat = Double.parseDouble(tfBerat.getText());
            double total = berat * l.getHarga();

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO pesanan " +
                                 "(id_pesanan, id_pelanggan, id_layanan, berat, total_bayar, status, waktu_pengerjaan, alamat_maps, id_admin) " +
                                 "VALUES (?, ?, ?, ?, ?, 'Antre', ?, ?, ?)"
                 )) {

                ps.setString(1, generatedId);
                ps.setInt(2, p.getId());
                ps.setInt(3, l.getId());
                ps.setDouble(4, berat);
                ps.setDouble(5, total);
                ps.setString(6, l.getEstimasi());
                ps.setString(7, p.getMaps());
                ps.setInt(8, 1); // sementara admin = 1

                ps.executeUpdate();

                showAlert("Sukses", "Pesanan berhasil!");
                generateId();
                tfBerat.clear();
                lblTotal.setText("Total: -");
                Launcher.showAdmin();

            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Gagal simpan!");
        }
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setContentText(m);
        a.show();
    }
    @FXML
    private void handleKembali1(ActionEvent event) {
        Launcher.showAdmin();
    }
}