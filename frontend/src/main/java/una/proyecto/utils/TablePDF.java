package una.proyecto.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la información de una tabla que será utilizada
 * para generar un reporte PDF.
 */
public class TablePDF {

    private List<String> encabezados;
    private List<List<String>> filas;

    public TablePDF() {
        this.encabezados = new ArrayList<>();
        this.filas = new ArrayList<>();
    }

    public TablePDF(List<String> encabezados) {
        this();

        if (encabezados != null) {
            this.encabezados.addAll(encabezados);
        }
    }

    /**
     * Obtiene los encabezados de la tabla.
     */
    public List<String> getEncabezados() {
        return encabezados;
    }

    /**
     * Establece los encabezados de la tabla.
     */
    public void setEncabezados(List<String> encabezados) {
        this.encabezados.clear();

        if (encabezados != null) {
            this.encabezados.addAll(encabezados);
        }
    }

    /**
     * Obtiene todas las filas.
     */
    public List<List<String>> getFilas() {
        return filas;
    }

    /**
     * Establece las filas de la tabla.
     */
    public void setFilas(List<List<String>> filas) {
        this.filas.clear();

        if (filas != null) {
            for (List<String> fila : filas) {
                agregarFila(fila);
            }
        }
    }

    /**
     * Agrega una fila a la tabla.
     */
    public void agregarFila(List<String> fila) {

        if (fila == null) {
            return;
        }

        List<String> nuevaFila = new ArrayList<>();

        for (String valor : fila) {
            nuevaFila.add(valor != null ? valor : "");
        }

        filas.add(nuevaFila);
    }

    /**
     * Elimina todas las filas.
     */
    public void limpiarFilas() {
        filas.clear();
    }

    /**
     * Elimina todos los datos de la tabla.
     */
    public void limpiar() {
        encabezados.clear();
        filas.clear();
    }

    /**
     * Indica si la tabla tiene encabezados.
     */
    public boolean tieneEncabezados() {
        return !encabezados.isEmpty();
    }

    /**
     * Indica si la tabla tiene filas.
     */
    public boolean tieneFilas() {
        return !filas.isEmpty();
    }

    /**
     * Cantidad de columnas.
     */
    public int cantidadColumnas() {
        return encabezados.size();
    }

    /**
     * Cantidad de filas.
     */
    public int cantidadFilas() {
        return filas.size();
    }

    /**
     * Verifica que una fila tenga la misma cantidad
     * de columnas que la tabla.
     */
    public boolean filaValida(List<String> fila) {

        return fila != null && fila.size() == cantidadColumnas();
    }
}
