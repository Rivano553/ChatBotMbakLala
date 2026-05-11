package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import id.ac.ukdw.rplbo.Launcher;
import id.ac.ukdw.rplbo.database.Database;
import id.ac.ukdw.rplbo.model.Pesanan;
import java.awt.Desktop;
import java.net.URI;
import java.sql.*;
import java.util.Optional;

public class DaftarPesananController {
    public static String idToSelect = null;

    @FXML private ListView<Pesanan> listPesanan;
    @FXML private Label lblId, lblNama, lblTelp, lblAlamat, lblStatus, lblTotal;
    @FXML private Label lblMetodeBayar, lblStatusBayar;
    @FXML private ComboBox<String> comboStatus;
    @FXML private Button btnUpdate, btnHapus, btnInputBerat, btnLunas;
    @FXML private VBox vboxLayananDetail;

    private final ObservableList<Pesanan> data = FXCollections.observableArrayList();
    private String selectedId = null;
    private double currentTotalTagihan = 0;
    private String currentMetodeBayar = "";
    private String currentStatusBayar = "";

    @FXML
    public void initialize() {
        setupComboStatus();
        loadList();

        listPesanan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                selectedId = selected.getId();
                loadDetail(selectedId);
                setActionButtonsDisabled(false);
            } else {
                clearDetailUI();
            }
        });

        if (idToSelect != null) {
            data.stream()
                    .filter(p -> p.getId().equals(idToSelect))
                    .findFirst()
                    .ifPresent(p -> listPesanan.getSelectionModel().select(p));
            idToSelect = null;
        }
    }

    private void setupComboStatus() {
        comboStatus.setItems(FXCollections.observableArrayList(
                "Menunggu Jemput", "Sedang Dijemput", "Antre Cuci",
                "Proses Cuci & Setrika", "Siap Antar", "Sedang Diantar", "Selesai"
        ));

        Callback<ListView<String>, ListCell<String>> cellFactory = param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #30a8d4; -fx-font-family: 'Segoe UI';");
                }
            }
        };
        comboStatus.setCellFactory(cellFactory);
        comboStatus.setButtonCell(cellFactory.call(null));
    }

    private void loadList() {
        data.clear();
        String query = "SELECT id_pesanan, nama_pelanggan, status FROM pesanan WHERE status != 'Selesai' ORDER BY tgl_masuk DESC";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                data.add(new Pesanan(rs.getString("id_pesanan"), rs.getString("nama_pelanggan"), "", 0.0, rs.getString("status")));
            }
            listPesanan.setItems(data);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadDetail(String id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM pesanan WHERE id_pesanan = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                lblId.setText(rs.getString("id_pesanan"));
                lblNama.setText(rs.getString("nama_pelanggan"));
                lblTelp.setText(rs.getString("no_telepon"));

                String alamat = rs.getString("alamat_jemput");
                lblAlamat.setText(alamat);
                setupAlamatLink(alamat);

                currentMetodeBayar = rs.getString("metode_pembayaran");
                currentStatusBayar = rs.getString("status_bayar");
                lblMetodeBayar.setText(currentMetodeBayar != null ? currentMetodeBayar : "-");
                lblStatusBayar.setText(currentStatusBayar != null ? currentStatusBayar : "-");

                if ("Lunas".equals(currentStatusBayar)) {
                    lblStatusBayar.setStyle("-fx-font-weight: bold; -fx-text-fill: #27AE60;");
                } else {
                    lblStatusBayar.setStyle("-fx-font-weight: bold; -fx-text-fill: #E74C3C;");
                }

                lblStatus.setText(rs.getString("status"));
                comboStatus.setValue(rs.getString("status"));
                loadLayanan(id);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void setupAlamatLink(String alamat) {
        if (alamat != null && alamat.contains("http")) {
            lblAlamat.setStyle("-fx-text-fill: #30a8d4; -fx-underline: true; -fx-cursor: hand; -fx-font-weight: bold;");
            lblAlamat.setOnMouseClicked(e -> {
                try { Desktop.getDesktop().browse(new URI(alamat.substring(alamat.indexOf("http")))); } catch (Exception ignored) {}
            });
        } else {
            lblAlamat.setStyle("-fx-text-fill: #1e4581; -fx-underline: false;");
            lblAlamat.setOnMouseClicked(null);
        }
    }

    private void loadLayanan(String id) throws SQLException {
        vboxLayananDetail.getChildren().clear();
        double total = 0;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT l.nama_layanan, pl.berat, pl.total_bayar FROM pesanan_layanan pl " +
                             "JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?")) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                double b = rs.getDouble("berat");
                String txt = "• " + rs.getString("nama_layanan") + (b > 0 ? " (" + b + " kg/satuan)" : " (Belum ditimbang)");
                Label lblLayanan = new Label(txt);
                lblLayanan.setStyle("-fx-text-fill: #1e4581;");
                vboxLayananDetail.getChildren().add(lblLayanan);
                total += rs.getDouble("total_bayar");
            }
            currentTotalTagihan = total;
            lblTotal.setText("Rp " + String.format("%,.0f", total));
        }
    }

    @FXML
    private void handleTandaiLunas() {
        if (selectedId == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tandai pesanan ini sebagai Lunas?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE pesanan SET status_bayar = 'Lunas' WHERE id_pesanan = ?")) {
                ps.setString(1, selectedId);
                ps.executeUpdate();
                loadDetail(selectedId);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleUpdateStatus() {
        if (selectedId == null) return;
        String statusBaru = comboStatus.getValue();

        String idYangDiproses = selectedId;

        // Validasi Berat
        if (currentTotalTagihan == 0 && isStatusMemerlukanBerat(statusBaru)) {
            tampilkanPeringatan("Pakaian Belum Ditimbang!", "Status '" + statusBaru + "' memerlukan input kuantitas/berat terlebih dahulu.");
            comboStatus.setValue(lblStatus.getText());
            return;
        }

        // Validasi Pelunasan
        if (statusBaru.equals("Selesai") && !"Lunas".equals(currentStatusBayar)) {
            tampilkanPeringatan("Pesanan Belum Lunas!", "Pesanan harus Lunas sebelum diubah ke status 'Selesai'.");
            comboStatus.setValue(lblStatus.getText());
            return;
        }

        // Pengingat Kurir HANYA MUNCUL JIKA METODE QRIS DAN BELUM LUNAS
        if (statusBaru.equals("Sedang Diantar") && "QRIS".equals(currentMetodeBayar) && !"Lunas".equals(currentStatusBayar)) {
            tampilkanInfo("Ingatkan Kurir — Cek Pembayaran QRIS!",
                    "Pelanggan memilih pembayaran QRIS namun statusnya masih 'Belum Lunas'.\n\n" +
                            "Mohon pastikan pembayaran sudah masuk atau ingatkan kurir untuk meminta scan QRIS saat tiba di lokasi.\n\n" +
                            "Total tagihan: Rp " + String.format("%,.0f", currentTotalTagihan));
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE pesanan SET status = ? WHERE id_pesanan = ?")) {
            ps.setString(1, statusBaru);
            ps.setString(2, idYangDiproses); // Gunakan ID yang diamankan
            ps.executeUpdate();

            if (statusBaru.equals("Selesai")) {
                loadList();
                clearDetailUI();
            } else {
                loadList();

                for (Pesanan p : data) {
                    if (p.getId().equals(idYangDiproses)) {
                        listPesanan.getSelectionModel().select(p);
                        break;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean isStatusMemerlukanBerat(String status) {
        return status.equals("Proses Cuci & Setrika") || status.equals("Siap Antar") ||
                status.equals("Sedang Diantar") || status.equals("Selesai");
    }

    @FXML
    private void handleHapus() {
        if (selectedId == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Hapus pesanan ini secara permanen?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Konfirmasi Hapus");
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM pesanan WHERE id_pesanan = ?")) {
                ps.setString(1, selectedId);
                ps.executeUpdate();
                loadList();
                clearDetailUI();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void clearDetailUI() {
        selectedId = null;
        currentTotalTagihan = 0;
        lblId.setText("-"); lblNama.setText("-"); lblTelp.setText("-");
        lblAlamat.setText("-"); lblStatus.setText("-"); lblTotal.setText("Rp -");
        lblMetodeBayar.setText("-"); lblStatusBayar.setText("-");
        vboxLayananDetail.getChildren().clear();
        setActionButtonsDisabled(true);
    }

    private void setActionButtonsDisabled(boolean disabled) {
        btnUpdate.setDisable(disabled);
        btnHapus.setDisable(disabled);
        btnInputBerat.setDisable(disabled);
        if(btnLunas != null) btnLunas.setDisable(disabled);
    }

    @FXML
    private void handleInputBerat() {
        if (selectedId == null) return;
        String statusSaatIni = lblStatus.getText();

        if (statusSaatIni.equals("Menunggu Jemput") || statusSaatIni.equals("Sedang Dijemput")) {
            tampilkanPeringatan("Cucian Belum Tiba", "Input berat hanya bisa dilakukan jika cucian sudah tiba di toko.");
            return;
        }

        InputController.selectedIdToInput = selectedId;
        Launcher.showInput();
    }

    private void tampilkanPeringatan(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void tampilkanInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(header);

        Label labelPesan = new Label(content);
        labelPesan.setWrapText(true);
        labelPesan.setPrefWidth(350);
        alert.getDialogPane().setContent(labelPesan);

        alert.showAndWait();
    }

    @FXML
    private void handleKembali() {
        Launcher.showAdmin();
    }
}