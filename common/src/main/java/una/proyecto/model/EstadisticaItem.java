package una.proyecto.model;

import java.io.Serializable;

public class EstadisticaItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String nombre;
    private final int cantidad;

    public EstadisticaItem(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
}