module org.example.mbaklala {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;

    opens org.example.mbaklala to javafx.fxml;
    exports org.example.mbaklala;
    exports org.example.mbaklala.admin;
    opens org.example.mbaklala.admin to javafx.fxml;
}