package una.proyecto.utils;

import javafx.scene.Node;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa la información general de un reporte PDF.
 */
public class ReportePDF {

    private String titulo;
    private LocalDate desde;
    private LocalDate hasta;
    private LocalDateTime fechaGeneracion;
    private TablePDF tabla;
    private Node grafico;

    /**
     * Constructor para reportes sin gráfico.
     */
    public ReportePDF(
            String titulo,
            TablePDF tabla
    ) {
        this.titulo = titulo;
        this.tabla = tabla;
        this.fechaGeneracion = LocalDateTime.now();
    }

    /**
     * Constructor para reportes con rango de fechas.
     */
    public ReportePDF(
            String titulo,
            LocalDate desde,
            LocalDate hasta,
            TablePDF tabla
    ) {
        this.titulo = titulo;
        this.desde = desde;
        this.hasta = hasta;
        this.tabla = tabla;
        this.fechaGeneracion = LocalDateTime.now();
    }

    /**
     * Constructor para reportes con gráfico.
     */
    public ReportePDF(
            String titulo,
            LocalDate desde,
            LocalDate hasta,
            TablePDF tabla,
            Node grafico
    ) {
        this.titulo = titulo;
        this.desde = desde;
        this.hasta = hasta;
        this.tabla = tabla;
        this.grafico = grafico;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(
            LocalDateTime fechaGeneracion
    ) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public TablePDF getTabla() {
        return tabla;
    }

    public void setTabla(TablePDF tabla) {
        this.tabla = tabla;
    }

    public Node getGrafico() {
        return grafico;
    }

    public void setGrafico(Node grafico) {
        this.grafico = grafico;
    }

    /**
     * Indica si el reporte tiene un rango de fechas.
     */
    public boolean tieneRangoFechas() {
        return desde != null || hasta != null;
    }

    /**
     * Indica si el reporte tiene un gráfico.
     */
    public boolean tieneGrafico() {
        return grafico != null;
    }
}
