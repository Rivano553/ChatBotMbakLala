package org.example.mbaklala.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.mbaklala.Launcher;
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
                if (item.startsWith(selectedIdToInput)) {
                    cbPesanan.setValue(item); cbPesanan.setDisable(true);
                    onPesananSelected(); break;
                }
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
            if (rs1.next()) {
                lblNama.setText("Nama Pelanggan: " + rs1.getString("nama_pelanggan"));
                lblTelp.setText("Nomor Kontak: " + rs1.getString("no_telepon"));
                lblAlamat.setText("Alamat Jemput: " + rs1.getString("alamat_jemput"));
            }

            PreparedStatement ps2 = conn.prepareStatement("SELECT pl.id_detail, l.nama_layanan, pl.jenis_layanan, l.harga_reguler, l.harga_express, pl.berat, l.satuan FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?");
            ps2.setString(1, idPesanan);
            ResultSet rs2 = ps2.executeQuery();

            while (rs2.next()) {
                LayananRow row = new LayananRow();
                row.plId = rs2.getInt("id_detail");

                String jenis = rs2.getString("jenis_layanan");
                if (jenis == null) jenis = "Reguler"; // Pencegah Error Null

                String satuan = rs2.getString("satuan");
                if (satuan == null) satuan = "qty"; // Pencegah Error Null

                row.hargaDigunakan = jenis.equalsIgnoreCase("Express") ? rs2.getDouble("harga_express") : rs2.getDouble("harga_reguler");

                row.tfBerat = new TextField(String.valueOf(rs2.getDouble("berat")));
                row.tfBerat.setPrefWidth(60);
                row.tfBerat.textProperty().addListener((obs, o, n) -> hitungTotal());

                row.lblSubtotal = new Label("Rp 0");
                row.lblSubtotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e4581;");

                HBox baris = new HBox(12);
                baris.setAlignment(Pos.CENTER_LEFT);

                Label lblLayanan = new Label("• " + rs2.getString("nama_layanan") + " [" + jenis + "]");
                lblLayanan.setPrefWidth(220);
                lblLayanan.setStyle("-fx-font-weight: bold;");

                Label lblSatuan = new Label(satuan);
                lblSatuan.setPrefWidth(50);

                baris.getChildren().addAll(lblLayanan, new Label("Qty:"), row.tfBerat, lblSatuan, new Label("(@ Rp " + String.format("%,.0f", row.hargaDigunakan) + ")"), new Region(), row.lblSubtotal);
                HBox.setHgrow(baris.getChildren().get(5), Priority.ALWAYS);
                vboxLayanan.getChildren().add(baris);
                rows.add(row);
            }
            hitungTotal();
        } catch (Exception e) {
            e.printStackTrace();
            // ALARM ERROR AGAR KITA TAHU JIKA ADA YANG SALAH
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal memuat data layanan!\n" + e.getMessage());
            alert.show();
        }
    }

    private void hitungTotal() {
        double total = 0;
        for (LayananRow r : rows) {
            try {
                double qty = Double.parseDouble(r.tfBerat.getText());
                double sub = qty * r.hargaDigunakan;
                r.lblSubtotal.setText("Rp " + String.format("%,.0f", sub));
                total += sub;
            } catch (Exception ignored) { r.lblSubtotal.setText("Rp 0"); }
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

            String idPesanan = cbPesanan.getValue().split(" - ")[0].trim();
            conn.createStatement().executeUpdate("UPDATE pesanan SET status = 'Proses Cuci & Setrika' WHERE id_pesanan = '" + idPesanan + "'");

            DaftarPesananController.idToSelect = idPesanan;
            Launcher.showDaftarPesanan();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Gagal menyimpan!\n" + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleKembali1(ActionEvent event) {
        if (cbPesanan.getValue() != null) {
            DaftarPesananController.idToSelect = cbPesanan.getValue().split(" - ")[0].trim();
        }
        Launcher.showDaftarPesanan();
    }
}