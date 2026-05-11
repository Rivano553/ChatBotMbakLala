package id.ac.ukdw.rplbo;

import id.ac.ukdw.rplbo.database.Database;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    private void handleLogin() {
        String inputUsername = usernameField.getText().trim();
        String inputPassword = passwordField.getText().trim();

        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
            messageLabel.setText("Username atau Password wajib diisi!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM admin WHERE username = ? AND password = ?")) {

            String hashedInputPassword = Database.hashPassword(inputPassword);

            ps.setString(1, inputUsername);
            ps.setString(2, hashedInputPassword); // Gunakan hashedInputPassword, bukan input biasa!

            if (ps.executeQuery().next()) {
                Launcher.showAdmin();
            } else {
                messageLabel.setText("Username atau Password salah!");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void handleBack() { Launcher.showHome(); }
}