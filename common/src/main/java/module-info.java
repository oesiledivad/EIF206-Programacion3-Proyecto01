module una.proyecto.common {
    requires transitive jakarta.xml.bind;
    exports una.proyecto.model;
    exports una.proyecto.serialization;
    opens una.proyecto.model;
}
