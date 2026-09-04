package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "categoria")
@XmlAccessorType(XmlAccessType.FIELD)
public class Categoria {
    private int id;
    private String descripcion;

    public Categoria() {}

    public Categoria(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }
    @Override
    public String toString() {
        return descripcion;
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}