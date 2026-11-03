module una.proyecto.backend {
    requires transitive una.proyecto.common;
    requires java.net.http;
    requires org.json;
    requires io.github.cdimascio.dotenv.java;
    exports una.proyecto.service;
    exports una.proyecto.logic;
    exports una.proyecto.logic.ia;
    exports una.proyecto.bootstrap;
    exports una.proyecto.datos;
    requires java.sql;
}
