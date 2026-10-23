module una.proyecto.frontend {
    requires una.proyecto.common;
    // Temporal: llamadas locales mientras se implementa el transporte por sockets.
    requires una.proyecto.backend;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome;
    requires org.apache.pdfbox;
    requires easytable;
    requires java.desktop;
    opens una.proyecto.app to javafx.fxml, javafx.graphics;
    opens una.proyecto.controller to javafx.fxml;
    opens una.proyecto.utils;
    exports una.proyecto.app;
}
