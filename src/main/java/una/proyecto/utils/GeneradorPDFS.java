package una.proyecto.utils;


import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.vandeseer.easytable.RepeatedHeaderTableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GeneradorPDFS {

    public static<T> TablePDF desdeTableView(TableView<T> tableView){
        TablePDF tabla = new TablePDF();
        List<TableColumn<T, ?>> columnas = new ArrayList<>();
        for (TableColumn<T,?> colum : tableView.getColumns()){
            columnas.add(colum);
        }
        List<String> encabezados = new ArrayList();
        for(TableColumn<T, ?> colum: columnas){
            encabezados.add(colum.getText());
        }
        tabla.setEncabezados(encabezados);
         //extraer el contenido de las filas
        for(T item : tableView.getItems()){
            List<String> valoresFila= new ArrayList<>();
            for(TableColumn<T, ?> col: columnas) {
                Object valor = col.getCellData(item);
                valoresFila.add(valor != null ? valor.toString() : "");
            }
            tabla.agregarFila(valoresFila);

            }
        return tabla;
    }
    public static void generarPDF(TablePDF tabla, String path) throws  Exception{
        try(PDDocument documento= new PDDocument()){
            float anchoDisponible= PDRectangle.A4.getWidth()-100;
            float anchoColumna= anchoDisponible/ tabla.getEncabezados().size();
            Table.TableBuilder tableBuilder = Table.builder().fontSize(10).font(new PDType1Font(Standard14Fonts.FontName.HELVETICA));
            for(int i =0; i<tabla.getEncabezados().size(); i++){
                tableBuilder.addColumnsOfWidth(anchoColumna);

            }
            Row.RowBuilder headerRow= Row.builder().font(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
            for(String encabezado : tabla.getEncabezados()){
                headerRow.add(TextCell.builder().text(encabezado).backgroundColor(Color.LIGHT_GRAY).build());

            }
            tableBuilder.addRow(headerRow.build());

            //datos de la fila
            for (List<String> fila : tabla.getFilas()) {
                Row.RowBuilder row = Row.builder();
                for (String celda : fila) {
                    row.add(TextCell.builder().text(celda).build());
                }
                tableBuilder.addRow(row.build());
            }

            // Dibujar tabla en el documento
            RepeatedHeaderTableDrawer.builder()
                    .table(tableBuilder.build())
                    .startX(50)
                    .startY(750)
                    .endY(50)
                    .build()
                    .draw(() -> documento, () -> new PDPage(PDRectangle.A4), 50);
            File carpeta = new File("pdfs");
            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }
            File archivoSalida = new File(carpeta, path);
            documento.save(archivoSalida);
        }
    }
    }


