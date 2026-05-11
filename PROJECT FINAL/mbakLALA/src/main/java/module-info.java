module org.example.mbaklala {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens org.example.mbaklala.model to javafx.fxml;
    exports org.example.mbaklala.model;

    opens org.example.mbaklala.admin to javafx.fxml;
    exports org.example.mbaklala.admin;

    exports org.example.mbaklala.database;
    exports org.example.mbaklala.bot;
}