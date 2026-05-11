package org.example.mbaklala.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.mbaklala.model.Launcher;
import org.example.mbaklala.database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InputController {
    public static String selectedIdToInput = null;

    @FXML private ComboBox<String> cbPesanan;
    @FXML private Label lblNama, lblTelp, lblAlamat, lblGrandTotal;
    @FXML private VBox vboxLayanan;

    private static class LayananRow { int plId; double hargaDigunakan; TextField tfBerat; Label lblSubtotal; }
    private final List<LayananRow> rows = new ArrayList<>();

    @FXML
    public void initialize() {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_pesanan, nama_pelanggan FROM pesanan WHERE status IN ('Menunggu Jemput', 'Antre Cuci', 'Proses Cuci & Setrika')")) {
            while (rs.next()) cbPesanan.getItems().add(rs.getString("id_pesanan") + " - " + rs.getString("nama_pelanggan"));
        } catch (Exception e) { e.printStackTrace(); }

        if (selectedIdToInput != null) {
            for (String item : cbPesanan.getItems()) {
                if (item.startsWith(selectedIdToInput)) { cbPesanan.setValue(item); cbPesanan.setDisable(true); onPesananSelected(); break; }
            }
            selectedIdToInput = null;
        }
    }

    @FXML
    private void onPesananSelected() {
        if (cbPesanan.getValue() == null) return;
        String idPesanan = cbPesanan.getValue().split(" - ")[0].trim();
        rows.clear(); vboxLayanan.getChildren().clear();

        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement("SELECT * FROM pesanan WHERE id_pesanan = ?");
            ps1.setString(1, idPesanan);
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) { lblNama.setText("Nama: " + rs1.getString("nama_pelanggan")); }

            PreparedStatement ps2 = conn.prepareStatement("SELECT pl.id_detail, l.nama_layanan, pl.jenis_layanan, l.harga_reguler, l.harga_express, pl.berat, l.satuan FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?");
            ps2.setString(1, idPesanan);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                LayananRow row = new LayananRow();
                row.plId = rs2.getInt("id_detail");
                String jenis = rs2.getString("jenis_layanan"); if (jenis == null) jenis = "Reguler";
                row.hargaDigunakan = jenis.equalsIgnoreCase("Express") ? rs2.getDouble("harga_express") : rs2.getDouble("harga_reguler");
                row.tfBerat = new TextField(String.valueOf(rs2.getDouble("berat")));
                row.tfBerat.textProperty().addListener((obs, o, n) -> hitungTotal());
                row.lblSubtotal = new Label("Rp 0");
                HBox baris = new HBox(12);
                baris.getChildren().addAll(new Label("• " + rs2.getString("nama_layanan")), row.tfBerat, row.lblSubtotal);
                vboxLayanan.getChildren().add(baris);
                rows.add(row);
            }
            hitungTotal();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void hitungTotal() {
        double total = 0;
        for (LayananRow r : rows) {
            try { double sub = Double.parseDouble(r.tfBerat.getText()) * r.hargaDigunakan; r.lblSubtotal.setText("Rp " + sub); total += sub; } catch (Exception ignored) {}
        }
        lblGrandTotal.setText("Total Tagihan: Rp " + String.format("%,.0f", total));
    }

    @FXML
    private void handleSimpan() {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE pesanan_layanan SET berat = ?, total_bayar = ? WHERE id_detail = ?");
            for (LayananRow r : rows) {
                double qty = Double.parseDouble(r.tfBerat.getText());
                ps.setDouble(1, qty); ps.setDouble(2, qty * r.hargaDigunakan); ps.setInt(3, r.plId); ps.executeUpdate();
            }
            conn.createStatement().executeUpdate("UPDATE pesanan SET status = 'Proses Cuci & Setrika' WHERE id_pesanan = '" + cbPesanan.getValue().split(" - ")[0].trim() + "'");
            Launcher.showDaftarPesanan();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleKembali1(ActionEvent event) { Launcher.showDaftarPesanan(); }
}