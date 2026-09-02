package una.proyecto.utils;

import java.util.ArrayList;
import java.util.List;

public class TablePDF {
    private List<String> encabezados= new ArrayList<>();
    private List<List<String>> filas =new ArrayList<>();
    public  TablePDF(){}
    public List<String> getEncabezados(){
        return encabezados;
    }
    public List<List<String>> getFilas(){
        return filas;
    }
    public void setEncabezados(List<String> encabezados){this.encabezados=encabezados;}
    public void setFilas(List<List<String>> filas){this.filas = filas;}
    public void agregarFila(List<String> fila) {
        this.filas.add(fila);
    }


}
