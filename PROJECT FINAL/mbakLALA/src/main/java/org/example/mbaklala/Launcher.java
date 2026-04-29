package org.example.mbaklala;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Launcher extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        showHome();
    }

    // --- HALAMAN HOME ---
    public static void showHome() {
        loadScene("/org/example/mbaklala/home.fxml", "Launderly - Home");
    }

    // --- HALAMAN CHATBOT ---
    public static void showBot() {
        loadScene("/org/example/mbaklala/bot.fxml", "Chatbot Mbak Lala");
    }

    // --- HALAMAN LOGIN ---
    public static void showLogin() {
        loadScene("/org/example/mbaklala/login.fxml", "Login Admin");
    }

    // --- HALAMAN PESANAN (BARU) ---
    public static void showPesanan() {
        loadScene("/org/example/mbaklala/pesanan.fxml", "Buat Pesanan Baru");
    }
    public static void showAdmin() {
        loadScene("/org/example/mbaklala/dashbor.fxml", "Admin");
    }
    public static void showDaftarPesanan() {
        loadScene("/org/example/mbaklala/daftarpesanan.fxml", "Daftar Pesanan");
    }
    public static void showInput() {
        loadScene("/org/example/mbaklala/input.fxml", "Input");
    }

    /**
     * Helper method agar tidak nulis kode yang sama berulang kali
     * dan memastikan otomatis Maximized
     */
    private static void loadScene(String fxmlPath, String title) {
        try {
            URL fxmlLocation = Launcher.class.getResource(fxmlPath);
            if (fxmlLocation == null) {
                System.out.println("❌ ERROR: File " + fxmlPath + " tidak ditemukan!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load(),1280, 720);

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setResizable(false);// Memastikan selalu Fullscreen (Maximized)
            stage.show();
        } catch (Exception e) {
            System.out.println("❌ Gagal memuat halaman: " + title);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
