package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="recurso")
@XmlAccessorType(XmlAccessType.FIELD)
public class Recurso {
    private String id;
    private int numeroDeActivo;
    private String idCategoria;
    private String descripcion;
    public Recurso(){}


    public Recurso(String id, String idCategoria, String descripcion) {
        this.id = id;
        this.numeroDeActivo = 0;
        this.idCategoria = idCategoria;
        this.descripcion = descripcion;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public int getNumeroDeActivo() {
        return numeroDeActivo;
    }
    public void setNumeroDeActivo(int numeroDeActivo) {
        this.numeroDeActivo = numeroDeActivo;
    }
    public String getIdCategoria() {
        return idCategoria;
    }
    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

}
