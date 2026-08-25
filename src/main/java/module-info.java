module EIF.Programacion.Proyecto {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.kordamp.ikonli.javafx;

    requires java.xml;
    requires jakarta.xml.bind;

    opens una.proyecto.app to javafx.fxml, javafx.graphics;
    opens una.proyecto.controller to javafx.fxml;
    opens una.proyecto.model to javafx.base, jakarta.xml.bind;

    exports una.proyecto.app;
}
