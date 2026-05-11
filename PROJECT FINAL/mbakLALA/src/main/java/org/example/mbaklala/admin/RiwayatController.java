package org.example.mbaklala.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mbaklala.model.Launcher;
import org.example.mbaklala.database.Database;
import java.sql.*;

public class RiwayatController {
    @FXML private ListView<Pesanan> listRiwayat;
    private final ObservableList<Pesanan> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try (Connection conn = Database.getConnection(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT id_pesanan, nama_pelanggan, status FROM pesanan WHERE status = 'Selesai'");
            while (rs.next()) data.add(new Pesanan(rs.getString("id_pesanan"), rs.getString("nama_pelanggan"), "", 0.0, rs.getString("status")));
            listRiwayat.setItems(data);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleKembali() { Launcher.showAdmin(); }
}