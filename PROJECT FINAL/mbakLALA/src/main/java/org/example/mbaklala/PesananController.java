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
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PesananController {
    @FXML private TextField txtNama, txtTelepon, txtCatatan;
    @FXML private TextArea txtAlamat;
    @FXML private VBox vboxDaftarLayanan;
    @FXML private ComboBox<String> cbPembayaran;

    private ObservableList<String> daftarBarang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbPembayaran.setItems(FXCollections.observableArrayList("Cash", "QRIS"));
        cbPembayaran.setValue("Cash");
        loadLayanan();
        tambahBarisInput();
    }

    private void loadLayanan() {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT nama_layanan FROM layanan")) {
            while (rs.next()) daftarBarang.add(rs.getString("nama_layanan"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tambahBarisInput() {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbB = new ComboBox<>(daftarBarang);
        ComboBox<String> cbT = new ComboBox<>(FXCollections.observableArrayList("Reguler", "Express"));
        Button btnAdd = new Button("+"); btnAdd.setOnAction(e -> tambahBarisInput());
        row.getChildren().addAll(cbB, cbT, btnAdd);
        vboxDaftarLayanan.getChildren().add(row);
    }

    @FXML
    private void handleKirim(ActionEvent event) {
        String id = "LND-" + new SimpleDateFormat("ddMMyy").format(new Date()) + "-" + ((int)(Math.random()*9000)+1000);
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO pesanan (id_pesanan, nama_pelanggan, no_telepon, alamat_jemput, deskripsi, metode_pembayaran) VALUES (?,?,?,?,?,?)");
            ps.setString(1, id); ps.setString(2, txtNama.getText()); ps.setString(3, txtTelepon.getText());
            ps.setString(4, txtAlamat.getText()); ps.setString(5, txtCatatan.getText()); ps.setString(6, cbPembayaran.getValue());
            ps.executeUpdate();

            BotController.pendingConfirmation = "Pesanan Berhasil!\nID: " + id + "\nMetode: " + cbPembayaran.getValue();
            Launcher.showBot();
        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void handleBatal() { Launcher.showBot(); }
}