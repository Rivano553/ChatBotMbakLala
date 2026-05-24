package org.example.mbaklala;

import org.example.mbaklala.database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PesananController {

    @FXML private TextField txtNama;
    @FXML private TextField txtTelepon;
    @FXML private TextArea txtAlamat;
    @FXML private VBox vboxDaftarLayanan;
    @FXML private ComboBox<String> cbTipePesanan; // Ditambahkan untuk Tipe Global
    @FXML private TextField txtCatatan;
    @FXML private ComboBox<String> cbPembayaran;

    private ObservableList<String> daftarBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadLayananDariDatabase();
        tambahBarisInput();

        cbPembayaran.setItems(FXCollections.observableArrayList("Cash", "QRIS"));
        cbPembayaran.setValue("Cash");

        // Inisialisasi Tipe Pesanan
        cbTipePesanan.setItems(FXCollections.observableArrayList("Reguler", "Express"));
    }

    private void loadLayananDariDatabase() {
        daftarBarang.clear();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT nama_layanan FROM layanan")) {

            while (rs.next()) {
                daftarBarang.add(rs.getString("nama_layanan"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tambahBarisInput() {
        HBox barisBaru = new HBox(10);
        barisBaru.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> cbBarang = new ComboBox<>(daftarBarang);
        cbBarang.setPromptText("Pilih Barang...");
        cbBarang.setPrefWidth(250); // Lebar ditambah karena dropdown tipe dihapus dari baris ini
        cbBarang.setStyle("-fx-background-color: #F8F9FA; -fx-border-color: #BDC3C7; -fx-border-radius: 6; -fx-font-family: 'Segoe UI';");

        Button btnTambah = new Button("Tambah");
        btnTambah.setStyle("-fx-background-color: transparent; -fx-border-color: #30a8d4; -fx-text-fill: #30a8d4; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        btnTambah.setOnAction(e -> tambahBarisInput());

        Button btnHapus = new Button("Hapus");
        btnHapus.setStyle("-fx-background-color: transparent; -fx-border-color: #E74C3C; -fx-text-fill: #E74C3C; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-family: 'Segoe UI';");
        btnHapus.setOnAction(e -> {
            if (vboxDaftarLayanan.getChildren().size() > 1) {
                vboxDaftarLayanan.getChildren().remove(barisBaru);
            }
        });

        barisBaru.getChildren().addAll(cbBarang, btnTambah, btnHapus);
        vboxDaftarLayanan.getChildren().add(barisBaru);
    }

    @FXML
    private void handleBatal(ActionEvent event) {
        Launcher.showBot();
    }

    @FXML
    private void handleKirim(ActionEvent event) {
        String nama = txtNama.getText().trim();
        String telp = txtTelepon.getText().trim();
        String alamat = txtAlamat.getText().trim();
        String catatan = txtCatatan.getText().trim();
        String metodeBayar = cbPembayaran.getValue();
        String tipePesanan = cbTipePesanan.getValue(); // Ambil tipe global

        // Validasi form termasuk Tipe Pesanan
        if (nama.isEmpty() || telp.isEmpty() || tipePesanan == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Nama Lengkap, Nomor Telepon, dan Tipe Pesanan wajib diisi!");
            alert.showAndWait();
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sqlCek = "SELECT status FROM pesanan WHERE LOWER(nama_pelanggan) = LOWER(?) AND status != 'Selesai' LIMIT 1";
            try (PreparedStatement psCek = conn.prepareStatement(sqlCek)) {
                psCek.setString(1, nama);
                ResultSet rsCek = psCek.executeQuery();
                if (rsCek.next()) {
                    String statusAktif = rsCek.getString("status");
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Pesanan Masih Aktif");
                    alert.setHeaderText("Pelanggan Sudah Terdaftar!");
                    alert.setContentText("Mohon maaf, pelanggan atas nama '" + nama + "' masih memiliki pesanan yang belum selesai (Status: " + statusAktif + ").\n\nMohon tunggu hingga pesanan sebelumnya berstatus 'Selesai'.");
                    alert.showAndWait();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String timeStamp = new SimpleDateFormat("ddMMyy").format(new Date());
        int randomNum = (int) (Math.random() * 9000) + 1000;
        String idPesanan = "LND-" + timeStamp + "-" + randomNum;

        StringBuilder rincian = new StringBuilder();
        int jumlahLayanan = 0;

        try (Connection conn = Database.getConnection()) {
            String sqlPesanan = "INSERT INTO pesanan (id_pesanan, nama_pelanggan, no_telepon, alamat_jemput, deskripsi, metode_pembayaran) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps1 = conn.prepareStatement(sqlPesanan)) {
                ps1.setString(1, idPesanan);
                ps1.setString(2, nama);
                ps1.setString(3, telp);
                ps1.setString(4, alamat);
                ps1.setString(5, catatan);
                ps1.setString(6, metodeBayar);
                ps1.executeUpdate();
            }

            // Memasukkan rincian layanan menggunakan tipePesanan global
            String sqlLayanan = "INSERT INTO pesanan_layanan (id_pesanan, id_layanan, jenis_layanan) VALUES (?, (SELECT id_layanan FROM layanan WHERE nama_layanan = ?), ?)";
            try (PreparedStatement ps2 = conn.prepareStatement(sqlLayanan)) {
                for (Node node : vboxDaftarLayanan.getChildren()) {
                    if (node instanceof HBox row) {
                        @SuppressWarnings("unchecked")
                        ComboBox<String> cbB = (ComboBox<String>) row.getChildren().get(0);

                        if (cbB.getValue() != null) {
                            rincian.append("- ").append(cbB.getValue()).append("\n");
                            jumlahLayanan++;

                            ps2.setString(1, idPesanan);
                            ps2.setString(2, cbB.getValue());
                            ps2.setString(3, tipePesanan); // Masukkan tipe Reguler/Express ke setiap item
                            ps2.executeUpdate();
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Database");
            alert.setContentText("Gagal menyimpan pesanan ke database.");
            alert.showAndWait();
            return;
        }

        if (jumlahLayanan == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Peringatan");
            alert.setHeaderText(null);
            alert.setContentText("Mohon pilih minimal 1 layanan laundry!");
            alert.showAndWait();
            return;
        }

        String konfirmasiBot = "Pesanan Berhasil Dibuat!\n\n" +
                "ID Pesanan: " + idPesanan + "\n" +
                "Nama: " + nama + "\n" +
                "No. Telp: " + telp + "\n" +
                "Alamat Jemput:\n" + alamat + "\n\n" +
                "Tipe Pengerjaan: " + tipePesanan + "\n" +
                "Rincian Layanan:\n" + rincian.toString() + "\n" +
                "Metode Pembayaran: " + metodeBayar + "\n" +
                (!catatan.isEmpty() ? "Catatan: " + catatan + "\n\n" : "") +
                "Mohon ditunggu ya, kurir Launderly akan segera menuju ke lokasimu untuk menjemput cucian. Terima kasih!";

        BotController.pendingConfirmation = konfirmasiBot;

        Launcher.showBot();
    }
}