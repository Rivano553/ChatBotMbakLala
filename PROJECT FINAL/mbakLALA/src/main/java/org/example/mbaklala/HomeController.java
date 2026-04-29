package org.example.mbaklala;

import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void openChatbot() {
        Launcher.showBot();
    }

    @FXML
    private void openAdmin() {
        Launcher.showLogin();
    }
}