module EIF.Programacion.Proyecto {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires java.xml;

    opens una.proyecto.app to javafx.fxml, javafx.graphics;
    opens una.proyecto.controller to javafx.fxml;
    opens una.proyecto.model to javafx.base;

    exports una.proyecto.app;
}