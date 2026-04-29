package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.example.mbaklala.Launcher;
import org.example.mbaklala.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DaftarPesananController {

    @FXML private ListView<Pesanan> listPesanan;

    @FXML private Label lblId;
    @FXML private Label lblNama;
    @FXML private Label lblTelp;
    @FXML private Label lblLayanan;
    @FXML private Label lblWaktu;
    @FXML private Label lblAlamat;
    @FXML private Label lblBerat; // 🔥 tambahan

    private ObservableList<Pesanan> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadList();

        listPesanan.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            if (selected != null) {
                loadDetail(selected.getId());
            }
        });
    }

    // ================= LIST =================
    private void loadList() {
        data.clear();

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.id_pesanan, pl.nama_pelanggan, l.nama_layanan, p.berat " +
                             "FROM pesanan p " +
                             "JOIN pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                             "JOIN layanan l ON p.id_layanan = l.id_layanan"
             )) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String id = rs.getString("id_pesanan");

                data.add(new Pesanan(
                        id,
                        rs.getString("nama_pelanggan"),
                        rs.getString("nama_layanan"),
                        rs.getDouble("berat")
                ));
            }

            listPesanan.setItems(data);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= DETAIL =================
    private void loadDetail(String id) {
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT p.id_pesanan, pl.nama_pelanggan, pl.no_telepon, " +
                             "l.nama_layanan, p.waktu_pengerjaan, pl.link_maps, p.berat " +
                             "FROM pesanan p " +
                             "JOIN pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                             "JOIN layanan l ON p.id_layanan = l.id_layanan " +
                             "WHERE p.id_pesanan = ?"
             )) {

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                lblId.setText("Nomor ID Pesanan : " + rs.getString("id_pesanan"));
                lblNama.setText("Nama : " + rs.getString("nama_pelanggan"));
                lblTelp.setText("Nomor Telepon : " + rs.getString("no_telepon"));
                lblLayanan.setText("Layanan : " + rs.getString("nama_layanan"));
                lblWaktu.setText("Waktu : " + rs.getString("waktu_pengerjaan"));
                lblAlamat.setText("Alamat : " + rs.getString("link_maps"));
                lblBerat.setText("Berat : " + rs.getDouble("berat") + " kg"); // 🔥 tambahan
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleKembali(ActionEvent event) {
        Launcher.showAdmin();
    }
}