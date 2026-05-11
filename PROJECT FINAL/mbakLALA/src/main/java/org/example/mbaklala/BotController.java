package org.example.mbaklala;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.mbaklala.bot.ChatbotEngine;
import org.example.mbaklala.bot.IntentService;
import org.example.mbaklala.database.Database;

import java.awt.Desktop;
import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BotController {
    @FXML private VBox chatBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;

    private ChatbotEngine bot = new ChatbotEngine();
    public static String pendingConfirmation = null;
    private static final List<Node> savedHistory = new ArrayList<>();

    @FXML
    public void initialize() {
        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> scrollPane.setVvalue(1.0)));
        if (!savedHistory.isEmpty()) { chatBox.getChildren().addAll(savedHistory); }
        else { addMessage("Halo! Selamat datang di Launderly.\nKetik pertanyaan atau masukkan ID/Nama untuk cek status.", false); }

        if (pendingConfirmation != null) { addMessage(pendingConfirmation, false); pendingConfirmation = null; }
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String input = (event.getSource() instanceof Button btn && btn.getUserData() != null) ? btn.getUserData().toString() : inputField.getText().trim();
        if (input.isEmpty()) return;
        inputField.clear(); addMessage(input, true); processInput(input);
    }

    private void processInput(String input) {
        String lowerInput = input.toLowerCase();
        if (input.startsWith("#") || input.toUpperCase().startsWith("LND")) {
            if (!cariStatusCucian("id", input)) addMessage("Maaf, ID '" + input + "' tidak ditemukan.", false);
            return;
        }
        String intent = IntentService.detectIntent(input);
        if (intent.equals("paketan")) { addMessage(bot.process("paketan"), false); return; }
        if (cariHargaSpesifik(lowerInput)) return;

        if (intent.equals("fallback")) {
            if (!cariStatusCucian("nama", input)) addMessage(bot.process("fallback"), false);
        } else {
            addMessage(bot.process(intent), false);
            if (intent.equals("harga") || intent.equals("layanan")) tampilkanHargaLayanan();
        }
    }

    private boolean cariStatusCucian(String tipe, String nilai) {
        String sql = (tipe.equals("id")) ? "SELECT * FROM pesanan WHERE REPLACE(id_pesanan, '-', '') = ?" : "SELECT * FROM pesanan WHERE LOWER(nama_pelanggan) = LOWER(?) ORDER BY tgl_masuk DESC LIMIT 1";
        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipe.equals("id") ? nilai.replace("-", "").replace("#", "").toUpperCase() : nilai);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String id = rs.getString("id_pesanan");
                StringBuilder rincian = new StringBuilder();
                double total = 0;
                try (PreparedStatement psL = conn.prepareStatement("SELECT l.nama_layanan, pl.jenis_layanan, pl.berat, l.satuan, pl.total_bayar FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?")) {
                    psL.setString(1, id);
                    ResultSet rsL = psL.executeQuery();
                    while (rsL.next()) {
                        rincian.append("- ").append(rsL.getString("nama_layanan")).append(" [").append(rsL.getString("jenis_layanan")).append("]");
                        if (rsL.getDouble("berat") > 0) rincian.append(" (").append(rsL.getDouble("berat")).append(" ").append(rsL.getString("satuan")).append(")");
                        rincian.append("\n");
                        total += rsL.getDouble("total_bayar");
                    }
                }
                addMessage("Data Ditemukan!\nID: " + id + "\nStatus: " + rs.getString("status") + "\nTotal: Rp " + String.format("%,.0f", total), false);
                return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    private boolean cariHargaSpesifik(String input) {
        StringBuilder response = new StringBuilder("Harga Layanan:\n\n");
        boolean found = false;
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM layanan")) {
            while (rs.next()) {
                if (input.contains(rs.getString("nama_layanan").toLowerCase())) {
                    response.append("• ").append(rs.getString("nama_layanan")).append("\n")
                            .append("  - Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_reguler"))).append("/").append(rs.getString("satuan")).append("\n")
                            .append("  - Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_express"))).append("/").append(rs.getString("satuan")).append("\n\n");
                    found = true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (found) addMessage(response.toString().trim(), false);
        return found;
    }

    private void tampilkanHargaLayanan() {
        StringBuilder res = new StringBuilder("Daftar Layanan:\n");
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM layanan ORDER BY kategori DESC")) {
            while (rs.next()) {
                res.append("• ").append(rs.getString("nama_layanan")).append("\n")
                        .append("  - Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_reguler"))).append("\n")
                        .append("  - Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_express"))).append("\n");
            }
            addMessage(res.toString().trim(), false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addMessage(String text, boolean isUser) {
        VBox bubble = new VBox(new Label(text));
        bubble.setStyle("-fx-padding: 10; -fx-background-radius: 10; -fx-background-color: " + (isUser ? "#30a8d4" : "white"));
        HBox box = new HBox(bubble); box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatBox.getChildren().add(box); savedHistory.add(box);
    }

    @FXML private void handleBuatPesanan() { Launcher.showPesanan(); }
    @FXML private void handleKembali() { savedHistory.clear(); Launcher.showHome(); }
}