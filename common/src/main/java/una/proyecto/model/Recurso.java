package una.proyecto.model;

import java.io.Serializable;

public class Recurso implements Serializable {
    private static final long serialVersionUID = 1L;
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
