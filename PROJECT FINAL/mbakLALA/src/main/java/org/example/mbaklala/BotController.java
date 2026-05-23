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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotController {
    @FXML private VBox chatBox;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField inputField;

    private ChatbotEngine bot = new ChatbotEngine();
    public static String pendingConfirmation = null;
    private static final List<Node> savedHistory = new ArrayList<>();

    // Auto-scroll ke bawah saat ada pesan baru
    @FXML
    public void initialize() {
        chatBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        });

        if (!savedHistory.isEmpty()) {
            chatBox.getChildren().addAll(savedHistory);
        } else {
            addMessage("Halo! Selamat datang di Launderly.\nKetik pertanyaan atau masukkan ID/Nama untuk cek status.", false);
        }

        if (pendingConfirmation != null) {
            addMessage(pendingConfirmation, false);
            pendingConfirmation = null;
        }
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String input = (event.getSource() instanceof Button btn && btn.getUserData() != null) ? btn.getUserData().toString() : inputField.getText().trim();
        if (input.isEmpty()) return;
        inputField.clear();
        addMessage(input, true);
        processInput(input);
    }

    private void processInput(String input) {
        String lowerInput = input.toLowerCase();

        if (input.startsWith("#") || input.toUpperCase().startsWith("LND")) {
            boolean found = cariStatusCucian("id", input);
            if (!found) {
                addMessage("Maaf, pesanan dengan ID '" + input + "' tidak ditemukan. Cek kembali nomor resinya ya.", false);
            }
            return;
        }

        if (hitungEstimasiDinamis(lowerInput)) {
            return;
        }

        if (cekPertanyaanUmum(lowerInput)) {
            return;
        }

        String intent = IntentService.detectIntent(input);

        if (intent.equals("paketan")) {
            addMessage(bot.process("paketan"), false);
            return;
        }

        if (cariHargaSpesifik(lowerInput)) {
            return;
        }

        if (intent.equals("fallback")) {
            boolean namaDitemukan = cariStatusCucian("nama", input);
            if (!namaDitemukan) {
                addMessage(bot.process("fallback"), false);
            }
        } else {
            addMessage(bot.process(intent), false);
            if (intent.equals("harga") || intent.equals("layanan")) {
                tampilkanHargaLayanan();
            }
        }
    }

    private boolean cariStatusCucian(String tipe, String nilai) {
        String cleanNilai = nilai.replace("-", "").replace("#", "").trim().toUpperCase();

        String sqlData = (tipe.equals("id"))
                ? "SELECT id_pesanan, nama_pelanggan, no_telepon, alamat_jemput, status FROM pesanan WHERE REPLACE(id_pesanan, '-', '') = ?"
                : "SELECT id_pesanan, nama_pelanggan, no_telepon, alamat_jemput, status FROM pesanan WHERE LOWER(nama_pelanggan) = LOWER(?) ORDER BY tgl_masuk DESC LIMIT 1";

        try (Connection conn = Database.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlData)) {
            ps.setString(1, tipe.equals("id") ? cleanNilai : nilai);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String idCucian = rs.getString("id_pesanan");
                String namaUser = rs.getString("nama_pelanggan");
                String noTelp = rs.getString("no_telepon");
                String alamat = rs.getString("alamat_jemput");
                String statusCucian = rs.getString("status");

                StringBuilder rincian = new StringBuilder();
                double total = 0;

                String sqlLayanan = "SELECT l.nama_layanan, pl.jenis_layanan, pl.berat, l.satuan, pl.total_bayar FROM pesanan_layanan pl JOIN layanan l ON pl.id_layanan = l.id_layanan WHERE pl.id_pesanan = ?";

                try (PreparedStatement psL = conn.prepareStatement(sqlLayanan)) {
                    psL.setString(1, idCucian);
                    ResultSet rsL = psL.executeQuery();
                    while (rsL.next()) {
                        rincian.append("- ").append(rsL.getString("nama_layanan"))
                                .append(" [").append(rsL.getString("jenis_layanan")).append("]");
                        if (rsL.getDouble("berat") > 0) {
                            rincian.append(" (").append(rsL.getDouble("berat")).append(" ").append(rsL.getString("satuan")).append(")");
                        }
                        rincian.append("\n");
                        total += rsL.getDouble("total_bayar");
                    }
                }

                String info = "Data Cucian Ditemukan!\n\n" +
                        "ID Cucian: " + idCucian + "\n" +
                        "Nama: " + namaUser + "\n" +
                        "No. HP: " + noTelp + "\n" +
                        "Alamat Jemput:\n" + alamat + "\n\n" +
                        "Status: " + statusCucian + "\n\n" +
                        "Rincian:\n" + rincian.toString() + "\n" +
                        "Total Tagihan: Rp " + String.format("%,.0f", total);

                addMessage(info, false);
                return true;
            }
            return false;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private boolean cariHargaSpesifik(String input) {
        String lowerInput = input.toLowerCase();
        String[] keywords = {"pakaian", "handuk", "seragam", "bayi", "gordyn", "tirai", "boneka", "jas", "blazer", "sepatu", "tas", "ransel", "selimut", "bed cover", "sprei"};

        List<String> matchedKeywords = new ArrayList<>();
        for (String kw : keywords) {
            if (lowerInput.contains(kw)) { matchedKeywords.add(kw); }
        }
        if (matchedKeywords.isEmpty()) return false;

        StringBuilder response = new StringBuilder("Berikut informasi harga yang kamu tanyakan:\n\n");
        boolean found = false;

        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama_layanan, harga_reguler, harga_express, satuan FROM layanan")) {

            while (rs.next()) {
                String namaLayanan = rs.getString("nama_layanan").toLowerCase();
                boolean match = false;
                for (String kw : matchedKeywords) {
                    if (namaLayanan.contains(kw)) { match = true; break; }
                }
                if (match) {
                    response.append("• ").append(rs.getString("nama_layanan")).append("\n")
                            .append("  - Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_reguler"))).append("/").append(rs.getString("satuan")).append("\n")
                            .append("  - Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_express"))).append("/").append(rs.getString("satuan")).append("\n\n");
                    found = true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (found) { addMessage(response.toString().trim(), false); return true; }
        return false;
    }

    private void addMessage(String text, boolean isUser) {
        VBox bubble = new VBox(2);
        bubble.setStyle("-fx-padding: 12 18; -fx-background-radius: 15; -fx-background-color: " + (isUser ? "#30a8d4" : "#FFFFFF") + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        bubble.setMaxWidth(400);

        String[] lines = text.split("\n", -1);
        for (String line : lines) {
            if (line.isEmpty()) {
                Label space = new Label(" ");
                space.setStyle("-fx-font-size: 6px;");
                bubble.getChildren().add(space);
                continue;
            }

            int httpIndex = line.indexOf("http");
            if (httpIndex >= 0) {
                HBox linkBox = new HBox();
                linkBox.setAlignment(Pos.CENTER_LEFT);

                if (httpIndex > 0) {
                    Label lblPre = new Label(line.substring(0, httpIndex));
                    lblPre.setStyle("-fx-text-fill: " + (isUser ? "white" : "#2C3E50") + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
                    linkBox.getChildren().add(lblPre);
                }

                String url = line.substring(httpIndex).trim();
                Hyperlink link = new Hyperlink(url);
                link.setWrapText(true);
                link.setMaxWidth(360);
                link.setStyle("-fx-text-fill: " + (isUser ? "#E0F7FA" : "#2980B9") + "; -fx-font-weight: bold; -fx-underline: true; -fx-padding: 0; -fx-border-color: transparent; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");

                link.setOnAction(e -> {
                    new Thread(() -> {
                        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
                    }).start();
                });

                linkBox.getChildren().add(link);
                bubble.getChildren().add(linkBox);

            } else {
                if (line.length() <= 45 && !line.startsWith("- ")) {
                    TextField selectableText = new TextField(line);
                    selectableText.setEditable(false);
                    selectableText.setStyle("-fx-background-color: transparent; -fx-focus-color: transparent; -fx-faint-focus-color: transparent; -fx-padding: 0; -fx-text-fill: " + (isUser ? "white" : "#2C3E50") + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
                    selectableText.setPrefWidth(370);
                    bubble.getChildren().add(selectableText);
                } else {
                    Label lbl = new Label(line);
                    lbl.setWrapText(true);
                    lbl.setMaxWidth(370);
                    lbl.setStyle("-fx-text-fill: " + (isUser ? "white" : "#2C3E50") + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-line-spacing: 2px;");
                    bubble.getChildren().add(lbl);
                }
            }
        }

        HBox box = new HBox(bubble);
        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatBox.getChildren().add(box);
        savedHistory.add(box);
    }

    private void tampilkanHargaLayanan() {
        StringBuilder res = new StringBuilder("Daftar Layanan Launderly:\n");
        try (Connection conn = Database.getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT kategori, nama_layanan, harga_reguler, harga_express, satuan FROM layanan ORDER BY kategori DESC")) {
            String curKategori = "";
            while (rs.next()) {
                String kat = rs.getString("kategori");
                if (!kat.equals(curKategori)) {
                    if (!curKategori.isEmpty()) { res.append("\n"); }
                    res.append(kat).append("\n");
                    curKategori = kat;
                }
                String unit = rs.getString("satuan");
                res.append("• ").append(rs.getString("nama_layanan")).append("\n")
                        .append("  - Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_reguler"))).append("/").append(unit).append("\n")
                        .append("  - Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", rs.getDouble("harga_express"))).append("/").append(unit).append("\n");
            }
            addMessage(res.toString().trim(), false);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean hitungEstimasiDinamis(String input) {
        // PERBAIKAN: Preprocessing teks menjadi angka ("satu" jadi "1", "sepasang" jadi "1 pasang")
        String processedInput = input.toLowerCase()
                .replaceAll("\\bsatu\\b", "1")
                .replaceAll("\\bdua\\b", "2")
                .replaceAll("\\btiga\\b", "3")
                .replaceAll("\\bempat\\b", "4")
                .replaceAll("\\blima\\b", "5")
                .replaceAll("\\benam\\b", "6")
                .replaceAll("\\btujuh\\b", "7")
                .replaceAll("\\bdelapan\\b", "8")
                .replaceAll("\\bsembilan\\b", "9")
                .replaceAll("\\bsepuluh\\b", "10")
                .replaceAll("\\bsetengah\\b", "0.5")
                .replaceAll("\\bsepasang\\b", "1 pasang")
                .replaceAll("\\bsebuah\\b", "1 buah")
                .replaceAll("\\bseset\\b", "1 set")
                .replaceAll("\\bsekilo\\b", "1 kg");

        String[] chunks = processedInput.split("(?i)\\+|\\bdan\\b|\\batau\\b|\\bserta\\b|,|&|\\bterus\\b|\\blalu\\b");

        boolean foundAny = false;
        double grandTotalReguler = 0;
        double grandTotalExpress = 0;
        StringBuilder rincianHitungan = new StringBuilder();

        Pattern pattern = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*(kg|kilo|pasang|pcs|set|buah)", Pattern.CASE_INSENSITIVE);

        try (Connection conn = Database.getConnection()) {
            for (String chunk : chunks) {
                Matcher matcher = pattern.matcher(chunk);
                if (matcher.find()) {
                    String numStr = matcher.group(1).replace(",", ".");
                    double jumlah = 0;
                    try {
                        jumlah = Double.parseDouble(numStr);
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    String lowerChunk = chunk.toLowerCase();
                    String keywordLayanan = "pakaian harian";

                    if (lowerChunk.contains("sepatu")) keywordLayanan = "sepatu";
                    else if (lowerChunk.contains("selimut double") || lowerChunk.contains("queen")) keywordLayanan = "selimut double";
                    else if (lowerChunk.contains("selimut")) keywordLayanan = "selimut single";
                    else if (lowerChunk.contains("bed cover")) keywordLayanan = "bed cover";
                    else if (lowerChunk.contains("sprei")) keywordLayanan = "sprei";
                    else if (lowerChunk.contains("jas") || lowerChunk.contains("blazer")) keywordLayanan = "jas";
                    else if (lowerChunk.contains("tas") || lowerChunk.contains("ransel")) keywordLayanan = "tas";
                    else if (lowerChunk.contains("boneka")) keywordLayanan = "boneka";
                    else if (lowerChunk.contains("handuk")) keywordLayanan = "handuk";
                    else if (lowerChunk.contains("gordyn") || lowerChunk.contains("tirai")) keywordLayanan = "gordyn";
                    else if (lowerChunk.contains("bayi") || lowerChunk.contains("anak")) keywordLayanan = "bayi";
                    else if (lowerChunk.contains("seragam")) keywordLayanan = "seragam";
                    else if (lowerChunk.contains("pakaian") || lowerChunk.contains("baju") || lowerChunk.contains("kaos") || lowerChunk.contains("celana") || lowerChunk.contains("kemeja")) keywordLayanan = "pakaian harian";

                    String sql = "SELECT nama_layanan, harga_reguler, harga_express, satuan FROM layanan WHERE LOWER(nama_layanan) LIKE ? LIMIT 1";

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, "%" + keywordLayanan + "%");
                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {
                            String namaLayanan = rs.getString("nama_layanan");
                            double hargaReguler = rs.getDouble("harga_reguler");
                            double hargaExpress = rs.getDouble("harga_express");
                            String satuanDb = rs.getString("satuan");

                            double totalReguler = jumlah * hargaReguler;
                            double totalExpress = jumlah * hargaExpress;

                            grandTotalReguler += totalReguler;
                            grandTotalExpress += totalExpress;

                            String formatJumlah = (jumlah % 1 == 0) ? String.format("%.0f", jumlah) : String.valueOf(jumlah).replace(".", ",");

                            rincianHitungan.append("• ").append(formatJumlah).append(" ").append(satuanDb).append(" (").append(namaLayanan).append(")\n")
                                    .append("  - Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", totalReguler)).append("\n")
                                    .append("  - Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", totalExpress)).append("\n\n");

                            foundAny = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (foundAny) {
            StringBuilder response = new StringBuilder("Berikut estimasi biaya untuk pesanan cucianmu:\n\n");
            response.append(rincianHitungan.toString());

            int itemHitung = rincianHitungan.toString().split("•").length - 1;
            if (itemHitung > 1) {
                response.append("Total Keseluruhan:\n")
                        .append("- Reguler (Est. 2-3 Hari): Rp ").append(String.format("%,.0f", grandTotalReguler)).append("\n")
                        .append("- Express (Est. 1 Hari): Rp ").append(String.format("%,.0f", grandTotalExpress)).append("\n\n");
            }

            response.append("Siap untuk dijemput kurir Launderly? Silakan klik tombol Order Laundry ya!");
            addMessage(response.toString().trim(), false);
            return true;
        }

        return false;
    }

    private boolean cekPertanyaanUmum(String input) {
        // PERBAIKAN: Jika input sudah diproses sebagai angka teks (satu, dua), skip bagian ini
        String processedInput = input.replaceAll("\\bsatu\\b|\\bdua\\b|\\btiga\\b|\\bempat\\b|\\blima\\b|\\benam\\b|\\btujuh\\b|\\bdelapan\\b|\\bsembilan\\b|\\bsepuluh\\b|\\bsepasang\\b|\\bsebuah\\b|\\bseset\\b|\\bsekilo\\b", "1");

        if (!processedInput.matches(".*\\d+.*")) {

            if (input.matches(".*\\b(selain|apa lagi|yang lain|lainnya)\\b.*")) {
                String balasan = "Banyak banget kak! Sesuai dengan layanan Launderly, kami juga ahlinya mencuci:\n\n" +
                        "• Kebutuhan Rumah (Gordyn/Tirai, Handuk)\n" +
                        "• Perlengkapan Tidur (Selimut, Bed Cover, Sprei)\n" +
                        "• Barang Spesial (Sepatu, Tas/Ransel, Boneka)\n" +
                        "• Pakaian Khusus (Jas/Blazer, Seragam)\n\n" +
                        "Ketik 'harga' untuk melihat daftar tarif lengkapnya, atau langsung sebutkan barang yang mau dicuci ya!";
                addMessage(balasan, false);
                return true;
            }

            if (input.matches(".*\\b(boneka)\\b.*")) {
                addMessage("Launderly juga jago nyuci boneka lho biar wangi dan bebas debu!\n\nUntuk cuci boneka hitungannya Kiloan ya kak. Kira-kira mau cuci berapa kg boneka? (Contoh: '2 kg boneka')", false);
                return true;
            }

            if (input.matches(".*\\b(tas|ransel)\\b.*")) {
                addMessage("Mau cuci tas atau ransel? Kami melayani cuci tas bersih tuntas dengan hitungan satuan (pcs).\n\nAda berapa pcs tas yang mau dicuci kak? (Contoh balas: '1 pcs tas')", false);
                return true;
            }

            if (input.matches(".*\\b(gordyn|tirai|handuk)\\b.*")) {
                addMessage("Untuk perlengkapan rumah seperti gordyn, tirai, atau handuk, kami melayaninya dengan hitungan Kiloan kak.\n\nBerapa perkiraan beratnya? (Contoh balas: '3 kg gordyn' atau '2 kg handuk')", false);
                return true;
            }

            if (input.matches(".*\\b(seragam|bayi|anak)\\b.*")) {
                addMessage("Untuk seragam atau pakaian bayi/anak, Launderly menyediakan treatment pencucian khusus agar bersih maksimal dan aman (Hitungan Kiloan).\n\nBerapa kg nih kak yang mau dicuci? (Contoh: '2 kg seragam' atau '1 kg pakaian bayi')", false);
                return true;
            }

            if (input.matches(".*\\b(selimut|bed cover|sprei)\\b.*")) {
                addMessage("Untuk perlengkapan tidur, kami menyediakan layanan cuci:\n" +
                        "- Selimut Single (Satuan pcs)\n" +
                        "- Selimut Double/Queen (Satuan pcs)\n" +
                        "- Bed Cover (Satuan pcs)\n" +
                        "- Sprei & Sarung Bantal (Satuan set)\n\n" +
                        "Mau cuci yang mana nih kak? Sebutkan jumlahnya ya, contoh: '1 pcs bed cover'.", false);
                return true;
            }

            if (input.matches(".*\\b(sepatu)\\b.*")) {
                addMessage("Wah, mau cuci sepatu ya? Launderly melayani cuci sepatu bersih tuntas dengan hitungan per pasang.\n\nBerapa pasang sepatu yang mau dicuci kak? Contoh balas: '2 pasang sepatu'.", false);
                return true;
            }

            if (input.matches(".*\\b(baju|pakaian|kaos|kemeja|jas|blazer)\\b.*")) {
                String balasan = "Untuk pakaian, Launderly punya beberapa pilihan kategori cucian nih kak:\n" +
                        "1. Pakaian Harian (Kiloan)\n" +
                        "2. Jas atau Blazer (Satuan pcs)\n\n" +
                        "Kakak mau cuci pakaian jenis apa? Boleh sebutkan estimasi jumlahnya, contoh: '2 kg pakaian harian' atau '1 pcs jas'.";
                addMessage(balasan, false);
                return true;
            }

            if (input.matches(".*\\b(laundry|cuci|nyuci)\\b.*")) {
                addMessage("Pasti kak! Launderly siap membuat cucianmu bersih dan wangi.\n\n" +
                        "Barang apa yang mau kakak cuci hari ini? (Misal: baju, sepatu, tas, boneka, atau selimut)\n" +
                        "Kakak juga bisa langsung sebutkan jumlahnya lho, contoh: '2 kg boneka'.", false);
                return true;
            }
        }
        return false;
    }

    @FXML private void handleBuatPesanan() {
        Launcher.showPesanan();
    }
    @FXML private void handleKembali() {
        savedHistory.clear();
        Launcher.showHome();
    }
}