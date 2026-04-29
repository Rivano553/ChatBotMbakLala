package org.example.mbaklala;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mbaklala.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // VALIDASI INPUT
        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Isi username & password!");
            return;
        }

        // CEK KONEKSI DATABASE
        if (!Database.cekKoneksi()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Database tidak terhubung!");
            return;
        }

        try (Connection conn = Database.getConnection()) {

            if (conn == null) {
                messageLabel.setText("Koneksi database gagal!");
                return;
            }

            String sql = "SELECT * FROM admin WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String dbPassword = rs.getString("password_hash");

                // 🔥 DEBUG (boleh dihapus nanti)
                System.out.println("Input: " + password);
                System.out.println("DB: " + dbPassword);

                if (password.equals(dbPassword)) {

                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Login berhasil!");

                    Launcher.showAdmin();

                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Password salah!");
                }

            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Username tidak ditemukan!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Terjadi error!");
        }
    }
}