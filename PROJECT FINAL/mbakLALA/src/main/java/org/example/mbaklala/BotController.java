package org.example.mbaklala;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.mbaklala.database.Database;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

public class BotController {

    @FXML private VBox chatBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;

    private ChatbotEngine bot;
    private List<PatternData> patterns;
    private Map<String, List<String>> responses;

    @FXML
    public void initialize() {

        // 🔥 auto scroll ke bawah
        chatBox.heightProperty().addListener((obs, oldVal, newVal) ->
                scrollPane.setVvalue(1.0));

        // 🔥 cek DB
        Database.cekKoneksi();

        // 🔥 init chatbot
        bot = new ChatbotEngine();
        patterns = ChatbotRepository.loadPatterns();
        responses = ChatbotRepository.loadResponses();

        // 🔥 opening message
        addMessage("Halo! Selamat datang di Mbak Lala Laundry 😊", false);
        addMessage("Mau cek harga, status, atau layanan?", false);
    }

    // =========================
    // 🔹 HANDLE INPUT
    // =========================
    @FXML
    private void handleSend() {

        String input = inputField.getText().trim();
        if (input.isEmpty()) return;

        addMessage(input, true);
        inputField.clear();

        String inputLower = input.toLowerCase();

        // 🔥 1. DETEKSI ID PESANAN (#123)
        Matcher matcher = Pattern.compile("#\\d+").matcher(input);
        if (matcher.find()) {
            cariStatusPesanan(matcher.group());
            return;
        }

        if (inputLower.contains("layanan") && !inputLower.contains("harga")) {

            addMessage("📋 Layanan kami tersedia:\n" +
                    "- Pakaian\n" +
                    "- Boneka\n" +
                    "- Sepatu\n" +
                    "- Paket Cuci setrika\n" +
                    "- Spary\n" +
                    "- Selimut\n" +
                    "- Setrika\n" +
                    "- Dll", false);
            return;
        }

        // 🔥 2. DETEKSI INTENT
        String intent = IntentService.detectIntent(inputLower, patterns);

        System.out.println("INPUT: " + input);
        System.out.println("INTENT: " + intent);

        // 🔥 3. HANDLE BERDASARKAN INTENT (NO DOUBLE MESSAGE)
        switch (intent) {

            case "harga":
                addMessage(bot.process("harga", responses), false);
                handlePriceList(input);
                break;

            case "layanan":
                addMessage(bot.process("layanan", responses), false);

                // kalau user sekalian tanya harga
                if (inputLower.contains("harga")) {
                    handlePriceList(input);
                }
                break;

            case "status":
                addMessage("Masukkan ID pesanan kamu ya (contoh: #2601)", false);
                break;

            case "lokasi":
                addMessage(bot.process("lokasi", responses), false);
                break;

            case "opening":
                addMessage(bot.process("opening", responses), false);
                break;

            default:
                addMessage(bot.process("fallback", responses), false);
        }
    }

    // =========================
    // 🔹 PRICE LIST + ESTIMASI
    // =========================
    private void handlePriceList(String input) {

        StringBuilder response = new StringBuilder("📋 Hasil Pencarian:\n\n");

        String inputLower = input.toLowerCase();

        // 🔥 tentukan keyword layanan
        List<String> layananList = List.of(
                "boneka", "pakaian", "jaket", "sepatu",
                "karpet", "selimut", "bedcover", "tas"
        );

        String keyword = "";

        for (String l : layananList) {
            if (inputLower.contains(l)) {
                keyword = l;
                break;
            }
        }

        String query;

        if (!keyword.isEmpty()) {
            query = "SELECT nama_layanan, harga_per_unit, satuan, estimasi " +
                    "FROM layanan WHERE LOWER(nama_layanan) LIKE ?";
        } else {
            // fallback → semua data
            query = "SELECT nama_layanan, harga_per_unit, satuan, estimasi FROM layanan";
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (!keyword.isEmpty()) {
                stmt.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = stmt.executeQuery();

            int i = 1;
            boolean found = false;

            while (rs.next()) {
                found = true;

                response.append(i++).append(". ")
                        .append(rs.getString("nama_layanan"))
                        .append("\n   Rp")
                        .append(String.format("%,.0f", rs.getDouble("harga_per_unit")))
                        .append("/")
                        .append(rs.getString("satuan"))
                        .append("\n   ⏱ Estimasi: ")
                        .append(rs.getString("estimasi"))
                        .append("\n\n");
            }

            if (!found) {
                addMessage("❌ Layanan tidak ditemukan 😢", false);
            } else {
                addMessage(response.toString(), false);
            }

        } catch (Exception e) {
            addMessage("❌ Gagal mengambil data harga", false);
            e.printStackTrace();
        }
    }
    // =========================
    // 🔹 CEK STATUS PESANAN
    // =========================
    private void cariStatusPesanan(String id) {

        String sql = "SELECT p.id_pesanan, pl.nama_pelanggan, p.status, p.total_bayar, p.berat " +
                "FROM pesanan p JOIN pelanggan pl ON p.id_pelanggan = pl.id_pelanggan " +
                "WHERE p.id_pesanan = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                String info = "📦 Pesanan Ditemukan!\n\n" +
                        "Nama: " + rs.getString("nama_pelanggan") +
                        "\nID: " + rs.getString("id_pesanan") +
                        "\nStatus: " + rs.getString("status") +
                        "\nTotal: Rp" + String.format("%,.0f", rs.getDouble("total_bayar")) +
                        "\nBerat: " + rs.getDouble("berat") + " kg";

                addMessage(info, false);

            } else {
                addMessage("❌ ID " + id + " tidak ditemukan.", false);
            }

        } catch (Exception e) {
            addMessage("⚠️ Koneksi database bermasalah.", false);
            e.printStackTrace();
        }
    }

    // =========================
    // 🔹 UI CHAT BUBBLE (FIX)
    // =========================
    private void addMessage(String text, boolean isUser) {

        Label label = new Label(text);
        label.setWrapText(true);

        // 🔥 PENTING: clear dulu biar ga bug style
        label.getStyleClass().clear();

        label.getStyleClass().add("bubble");

        if (isUser) {
            label.getStyleClass().add("user-bubble");
        } else {
            label.getStyleClass().add("lala-bubble");
        }

        HBox box = new HBox(label);
        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        chatBox.getChildren().add(box);
    }

    // =========================
    // 🔹 NAVIGASI
    // =========================
    @FXML
    private void handleBuatPesanan(ActionEvent e) {
        Launcher.showPesanan();
    }

    @FXML
    private void handleKembali(ActionEvent e) {
        Launcher.showHome();
    }
}