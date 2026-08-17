module EIF.Programacion.Proyecto {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens una.proyecto to javafx.fxml, javafx.graphics;
    exports una.proyecto;
}