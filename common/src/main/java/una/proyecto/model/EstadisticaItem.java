package una.proyecto.model;

public class EstadisticaItem {
    private final String nombre;
    private final int cantidad;

    public EstadisticaItem(String nombre, int cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
}