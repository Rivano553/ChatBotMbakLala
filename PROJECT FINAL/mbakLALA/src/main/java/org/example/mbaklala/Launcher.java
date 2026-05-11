package org.example.mbaklala;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.mbaklala.database.Database;

public class Launcher extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        Database.initSchema();
        showHome();
    }

    // PATH SUDAH DIPERBAIKI MENJADI /org/example/mbaklala/...
    public static void showHome() { loadScene("/org/example/mbaklala/home.fxml", "Launderly - Home"); }
    public static void showBot() { loadScene("/org/example/mbaklala/bot.fxml", "Chatbot Launderly"); }
    public static void showLogin() { loadScene("/org/example/mbaklala/login.fxml", "Login Admin"); }
    public static void showPesanan() { loadScene("/org/example/mbaklala/pesanan.fxml", "Buat Pesanan Baru"); }
    public static void showAdmin() { loadScene("/org/example/mbaklala/dashbor.fxml", "Dashboard Admin"); }
    public static void showDaftarPesanan() { loadScene("/org/example/mbaklala/daftarpesanan.fxml", "Kelola Data Pesanan"); }
    public static void showInput() { loadScene("/org/example/mbaklala/input.fxml", "Input Berat & Tagihan"); }
    public static void showRiwayat() { loadScene("/org/example/mbaklala/riwayat.fxml", "Riwayat Transaksi"); }

    private static void loadScene(String fxmlPath, String title) {
        try {
            Scene scene = new Scene(new FXMLLoader(Launcher.class.getResource(fxmlPath)).load(), 1280, 720);
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            System.err.println("Gagal memuat FXML di path: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) { launch(); }
}