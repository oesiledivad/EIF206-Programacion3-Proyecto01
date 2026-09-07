package una.proyecto.utils;

import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.vandeseer.easytable.RepeatedHeaderTableDrawer;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GeneradorPDFS {
    // Rutas relativas dentro de src/main/resources
    private static final String RUTA_REGULAR = "src/main/resources/fonts/JetBrainsMono.ttf";
    private static final String RUTA_BOLD = "src/main/resources/fonts/JetBrainsMono-Bold.ttf";
    private static final String RUTA_ITALIC = "src/main/resources/fonts/JetBrainsMono-Italic.ttf";
    private static final String RUTA_BOLD_ITALIC = "src/main/resources/fonts/JetBrainsMono-BoldItalic.ttf";

    private static final float MARGEN = 50;
    private static final float ANCHO_PAGINA = PDRectangle.A4.getWidth();
    private static final float ALTO_PAGINA = PDRectangle.A4.getHeight();
    private static final float ANCHO_DISPONIBLE = ANCHO_PAGINA - (MARGEN * 2);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String NOMBRE_SISTEMA = "UNA - SISTEMA DE RESERVAS";

    /**
     * Carga de forma segura la fuente JetBrains Mono o recurre a Helvetica si hay algún error.
     */
    public static PDFont obtenerFuente(PDDocument documento, String tipo) {
        String ruta = switch (tipo.toLowerCase()) {
            case "bold" -> RUTA_BOLD;
            case "italic" -> RUTA_ITALIC;
            case "bolditalic" -> RUTA_BOLD_ITALIC;
            default -> RUTA_REGULAR;
        };

        try (InputStream fontStream = GeneradorPDFS.class.getResourceAsStream(ruta)) {
            if (fontStream != null) {
                return PDType0Font.load(documento, fontStream);
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar la fuente " + ruta + ", usando fuente por defecto. Error: " + e.getMessage());
        }

        // Respaldo (Fallback) a fuentes estándar si falla la carga
        Standard14Fonts.FontName fallback = tipo.toLowerCase().contains("bold") ? Standard14Fonts.FontName.HELVETICA_BOLD : Standard14Fonts.FontName.HELVETICA;
        return new PDType1Font(fallback);
    }

    /**
     * Convierte un TableView de JavaFX en un TablePDF.
     */
    public static <T> TablePDF desdeTableView(
            TableView<T> tableView
    ) {

        if (tableView == null) {
            throw new IllegalArgumentException("El TableView no puede ser null.");
        }

        TablePDF tabla = new TablePDF();

        List<TableColumn<T, ?>> columnas = new ArrayList<>(tableView.getColumns());

        // Encabezados
        List<String> encabezados = new ArrayList<>();

        for (TableColumn<T, ?> columna : columnas) {
            encabezados.add(textoSeguro(columna.getText()));
        }

        tabla.setEncabezados(encabezados);

        // Filas
        for (T item : tableView.getItems()) {

            List<String> fila = new ArrayList<>();

            for (TableColumn<T, ?> columna : columnas) {

                Object valor = columna.getCellData(item);

                fila.add(valor != null ? valor.toString() : "");
            }

            tabla.agregarFila(fila);
        }

        return tabla;
    }

    /**
     * Genera un PDF a partir de un ReportePDF.
     */
    public static void generar(
            ReportePDF reporte,
            String nombreArchivo
    ) throws Exception {

        validarReporte(reporte);
        validarNombreArchivo(nombreArchivo);

        try (PDDocument documento = new PDDocument()) {

            PDPage pagina = new PDPage(PDRectangle.A4);

            documento.addPage(pagina);

            // ENCABEZADO Y DATOS DEL REPORTE

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {

                dibujarEncabezado(documento, contenido, reporte);
            }

            // GRÁFICO

            float posicionTabla;

            if (reporte.tieneGrafico()) {

                posicionTabla = dibujarGrafico(documento, pagina, reporte.getGrafico(), reporte);

            } else {

                posicionTabla = obtenerPosicionTablaSinGrafico(reporte);
            }

            // TABLA
            Table tabla =
                    construirTabla(
                            documento,
                            reporte.getTabla()
                    );

            RepeatedHeaderTableDrawer.builder()
                    .table(tabla)
                    .startX(MARGEN)
                    .startY(posicionTabla)
                    .endY(MARGEN + 25)
                    .build()
                    .draw(
                            () -> documento,
                            () -> new PDPage(PDRectangle.A4),
                            MARGEN
                    );

            // PIE DE PÁGINA

            agregarNumeracionPaginas(documento);

            // GUARDAR

            guardarDocumento(documento, nombreArchivo);
        }
    }

    // ENCABEZADO

    private static void dibujarEncabezado(
            PDDocument documento,
            PDPageContentStream contenido,
            ReportePDF reporte
    ) throws Exception {

        float y = ALTO_PAGINA - MARGEN;

        // Nombre del sistema
        contenido.beginText();

        contenido.setFont(obtenerFuente(documento, "bold"), 14);

        contenido.newLineAtOffset(MARGEN, y);

        contenido.showText(NOMBRE_SISTEMA);

        contenido.endText();


        // Línea separadora
        contenido.setLineWidth(1);

        contenido.setStrokingColor(Color.DARK_GRAY);

        contenido.moveTo(MARGEN, y - 8);

        contenido.lineTo(ANCHO_PAGINA - MARGEN, y - 8);

        contenido.stroke();


        // Título
        contenido.beginText();

        contenido.setFont(obtenerFuente(documento, "bold"), 18);

        contenido.newLineAtOffset(MARGEN, y - 35);

        contenido.showText(textoSeguro(reporte.getTitulo()));

        contenido.endText();


        // Fecha de generación
        LocalDateTime fechaGeneracion = reporte.getFechaGeneracion();

        if (fechaGeneracion != null) {

            contenido.beginText();

            contenido.setFont(obtenerFuente(documento, "regular"), 9);

            contenido.newLineAtOffset(MARGEN, y - 52);

            contenido.showText("Generado: " + FORMATO_FECHA_HORA.format(fechaGeneracion));

            contenido.endText();
        }


        // Rango de fechas
        if (reporte.tieneRangoFechas()) {

            String rango = construirRangoFechas(reporte);
            System.out.println(rango);

            contenido.beginText();

            contenido.setFont(obtenerFuente(documento, "regular"),
                    10);

            contenido.newLineAtOffset(MARGEN, y - 70);

            contenido.showText(rango);

            contenido.endText();
        }
    }


    // RANGO DE FECHAS

    private static String construirRangoFechas(
            ReportePDF reporte
    ) {

        LocalDate desde = reporte.getDesde();

        LocalDate hasta = reporte.getHasta();

        if (desde != null && hasta != null) {

            return "Desde: " + FORMATO_FECHA.format(desde) + "    Hasta: " + FORMATO_FECHA.format(hasta);
        }

        if (desde != null) {

            return "Desde: " + FORMATO_FECHA.format(desde);
        }

        return "Hasta: " + FORMATO_FECHA.format(hasta);
    }

    // GRÁFICO

    // Modifica el método dibujarGrafico para que considere el rango de fechas
    private static float dibujarGrafico(
            PDDocument documento,
            PDPage pagina,
            Node grafico,
            ReportePDF reporte  // Añade este parámetro
    ) throws Exception {

        WritableImage imagenFX = grafico.snapshot(null, null);

        BufferedImage imagen = convertirFXaBuffered(imagenFX);

        if (imagen.getWidth() <= 0 || imagen.getHeight() <= 0) {

            throw new IllegalArgumentException("El gráfico no tiene dimensiones válidas.");
        }

        PDImageXObject imagenPDF = LosslessFactory.createFromImage(documento, imagen);

        float anchoMaximo = ANCHO_DISPONIBLE;

        float factor = anchoMaximo / imagen.getWidth();

        float ancho = imagen.getWidth() * factor;

        float alto = imagen.getHeight() * factor;

        float posicionX = MARGEN;

        // Calcula la posición Y considerando si hay rango de fechas
        float posicionY;
        if (reporte.tieneRangoFechas()) {
            // Si hay rango de fechas, el gráfico comienza más abajo
            posicionY = ALTO_PAGINA - 130 - alto;  // Ajusta este valor según sea necesario
        } else {
            posicionY = ALTO_PAGINA - 100 - alto;
        }

        try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina, PDPageContentStream.AppendMode.APPEND, true)) {

            contenido.drawImage(
                    imagenPDF,
                    posicionX,
                    posicionY,
                    ancho,
                    alto
            );
        }

        return posicionY - 20;
    }


    // POSICIÓN DE TABLA

    private static float obtenerPosicionTablaSinGrafico(
            ReportePDF reporte
    ) {

        if (reporte.tieneRangoFechas()) {
            return ALTO_PAGINA - 115;
        }

        return ALTO_PAGINA - 95;
    }


    // CONSTRUCCIÓN DE TABLA

    private static Table construirTabla(
            PDDocument documento,
            TablePDF tablaPDF
    ) {

        validarTabla(tablaPDF);

        int cantidadColumnas = tablaPDF.cantidadColumnas();

        float anchoColumna = ANCHO_DISPONIBLE / cantidadColumnas;

        Table.TableBuilder tableBuilder = Table.builder().fontSize(9).font(obtenerFuente(documento, "regular"));

        // Columnas
        for (int i = 0; i < cantidadColumnas; i++) {

            tableBuilder.addColumnsOfWidth(anchoColumna);
        }

        // ENCABEZADO DE TABLA

        Row.RowBuilder encabezado = Row.builder().font(obtenerFuente(documento, "bold"));

        for (String nombre : tablaPDF.getEncabezados()) {

            encabezado.add(TextCell.builder().text(textoSeguro(nombre)).backgroundColor(new Color(220, 220, 220)).build());
        }

        tableBuilder.addRow(encabezado.build());


        // FILAS

        for (List<String> fila : tablaPDF.getFilas()) {

            Row.RowBuilder filaBuilder = Row.builder();

            for (String valor : fila) {

                filaBuilder.add(TextCell.builder().text(textoSeguro(valor)).build());
            }

            tableBuilder.addRow(filaBuilder.build());
        }

        return tableBuilder.build();
    }


    // NUMERACIÓN

    private static void agregarNumeracionPaginas(
            PDDocument documento
    ) throws Exception {

        int cantidadPaginas =
                documento.getNumberOfPages();

        for (int i = 0; i < cantidadPaginas; i++) {

            PDPage pagina = documento.getPage(i);

            PDFont fuenteRegular = obtenerFuente(documento, "regular");

            try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina, PDPageContentStream.AppendMode.APPEND, true)) {

                String texto = "Página " + (i + 1) + " de " + cantidadPaginas;

                contenido.beginText();

                contenido.setFont(fuenteRegular, 8);

                float anchoTexto = fuenteRegular.getStringWidth(texto) / 1000 * 8;

                float posicionX = (ANCHO_PAGINA - anchoTexto) / 2;

                contenido.newLineAtOffset(posicionX, 25);

                contenido.showText(texto);

                contenido.endText();
            }
        }
    }

    // CONVERSIÓN DE IMAGEN

    private static BufferedImage convertirFXaBuffered(
            WritableImage imagenFX
    ) {

        int width = (int) imagenFX.getWidth();

        int height = (int) imagenFX.getHeight();

        BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PixelReader reader = imagenFX.getPixelReader();

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                imagen.setRGB(x, y, reader.getArgb(x, y));
            }
        }

        return imagen;
    }

    // GUARDAR

    private static void guardarDocumento(
            PDDocument documento,
            String nombreArchivo
    ) throws Exception {

        File carpeta = new File("pdfs");

        if (!carpeta.exists() && !carpeta.mkdirs()) {

            throw new Exception("No se pudo crear la carpeta 'pdfs'.");
        }

        File archivo = new File(carpeta, nombreArchivo);

        documento.save(archivo);
    }

    // VALIDACIONES

    private static void validarReporte(
            ReportePDF reporte
    ) {

        if (reporte == null) {
            throw new IllegalArgumentException("El reporte no puede ser null.");
        }

        if (reporte.getTitulo() == null || reporte.getTitulo().isBlank()) {

            throw new IllegalArgumentException("El reporte debe tener un título.");
        }

        if (reporte.getTabla() == null) {
            throw new IllegalArgumentException("El reporte debe tener una tabla.");
        }

        if (reporte.tieneRangoFechas()) {

            if (reporte.getDesde() != null && reporte.getHasta() != null && reporte.getDesde().isAfter(reporte.getHasta())) {
                throw new IllegalArgumentException("La fecha Desde no puede ser posterior a Hasta.");
            }
        }
    }


    private static void validarTabla(
            TablePDF tabla
    ) {

        if (tabla == null) {
            throw new IllegalArgumentException("La tabla no puede ser null.");
        }

        if (!tabla.tieneEncabezados()) {
            throw new IllegalArgumentException("La tabla debe tener encabezados.");
        }

        for (List<String> fila : tabla.getFilas()) {

            if (!tabla.filaValida(fila)) {
                throw new IllegalArgumentException("Una de las filas no coincide " + "con la cantidad de columnas.");
            }
        }
    }


    private static void validarNombreArchivo(
            String nombreArchivo
    ) {

        if (nombreArchivo == null || nombreArchivo.isBlank()) {

            throw new IllegalArgumentException("El nombre del archivo no puede estar vacío.");
        }

        if (!nombreArchivo.toLowerCase().endsWith(".pdf")) {

            throw new IllegalArgumentException("El archivo debe tener extensión .pdf.");
        }
    }


    // UTILIDADES

    private static String textoSeguro(String texto) {
        return texto != null ? texto : "";
    }
}