module EIF.Programacion.Proyecto {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires org.kordamp.ikonli.javafx;
    requires jakarta.xml.bind;
    requires java.net.http;
    requires java.xml;
    requires org.apache.pdfbox;
    requires easytable;
    requires java.desktop;
    //requires jdk.jsonbject;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    opens una.proyecto.app to javafx.fxml, javafx.graphics;
    opens una.proyecto.controller to javafx.fxml;
    opens una.proyecto.model to javafx.base, jakarta.xml.bind;
    opens una.proyecto.utils;

    exports una.proyecto.app;
    exports una.proyecto.utils to org.glassfish.jaxb.core, org.glassfish.jaxb.runtime;
    exports una.proyecto.datos;
    exports una.proyecto.logic;
    exports una.proyecto.model;
    exports una.proyecto.service;
    opens una.proyecto.datos.wrapper to jakarta.xml.bind, javafx.base;
}