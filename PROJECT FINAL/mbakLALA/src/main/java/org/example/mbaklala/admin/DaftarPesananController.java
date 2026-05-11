package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.mbaklala.model.Launcher;
import org.example.mbaklala.database.Database;
import java.sql.*;

public class DaftarPesananController {
    public static String idToSelect = null;
    @FXML private ListView<Pesanan> listPesanan;
    @FXML private Label lblId, lblNama, lblStatus, lblTotal, lblMetodeBayar, lblStatusBayar;
    @FXML private ComboBox<String> comboStatus;
    @FXML private VBox vboxLayananDetail;
    @FXML private Button btnUpdate, btnHapus, btnInputBerat;

    private final ObservableList<Pesanan> data = FXCollections.observableArrayList();
    private String selectedId = null;
    private double currentTotal = 0;

    @FXML
    public void initialize() {
        comboStatus.setItems(FXCollections.observableArrayList("Menunggu Jemput", "Sedang Dijemput", "Antre Cuci", "Proses Cuci & Setrika", "Siap Antar", "Sedang Diantar", "Selesai"));
        loadList();
        listPesanan.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> { if (val != null) { selectedId = val.getId(); loadDetail(selectedId); } });
    }

    private void loadList() {
        data.clear();
        try (Connection conn = Database.getConnection(); ResultSet rs = conn.createStatement().executeQuery("SELECT id_pesanan, nama_pelanggan, status FROM pesanan WHERE status != 'Selesai'")) {
            while (rs.next()) data.add(new Pesanan(rs.getString("id_pesanan"), rs.getString("nama_pelanggan"), "", 0.0, rs.getString("status")));
            listPesanan.setItems(data);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDetail(String id) {
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM pesanan WHERE id_pesanan = ?")) {
            ps.setString(1, id); ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblId.setText(rs.getString("id_pesanan")); lblNama.setText(rs.getString("nama_pelanggan"));
                lblMetodeBayar.setText(rs.getString("metode_pembayaran"));
                lblStatusBayar.setText(rs.getString("status_bayar"));
                lblStatus.setText(rs.getString("status"));
                comboStatus.setValue(rs.getString("status"));
                loadLayanan(id);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadLayanan(String id) throws SQLException {
        vboxLayananDetail.getChildren().clear(); currentTotal = 0;
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT l.nama_layanan, pl.total_bayar FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?")) {
            ps.setString(1, id); ResultSet rs = ps.executeQuery();
            while (rs.next()) { currentTotal += rs.getDouble("total_bayar"); vboxLayananDetail.getChildren().add(new Label("• " + rs.getString("nama_layanan"))); }
            lblTotal.setText("Rp " + String.format("%,.0f", currentTotal));
        }
    }

    @FXML
    private void handleUpdateStatus() {
        String idYangDiproses = selectedId;
        String status = comboStatus.getValue();
        if (status.equals("Selesai") && !lblStatusBayar.getText().equals("Lunas")) {
            new Alert(Alert.AlertType.WARNING, "Sila tanda Lunas dahulu!").showAndWait(); return;
        }
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE pesanan SET status = ? WHERE id_pesanan = ?")) {
            ps.setString(1, status); ps.setString(2, idYangDiproses); ps.executeUpdate();
            loadList();
            for (Pesanan p : data) { if (p.getId().equals(idYangDiproses)) { listPesanan.getSelectionModel().select(p); break; } }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleTandaiLunas() {
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE pesanan SET status_bayar = 'Lunas' WHERE id_pesanan = ?")) {
            ps.setString(1, selectedId); ps.executeUpdate(); loadDetail(selectedId);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleInputBerat() { InputController.selectedIdToInput = selectedId; Launcher.showInput(); }
    @FXML private void handleHapus() { /* Kod hapus anda */ }
    @FXML private void handleKembali() { Launcher.showAdmin(); }
}