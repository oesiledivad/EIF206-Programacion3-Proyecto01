package Unit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorPDFSTest {
    @ParameterizedTest
    @ValueSource(strings = {"regular", "bold", "italic", "bolditalic"})
    void fuentesDelClasspathSeIncrustanYPermitenTextoUnicode(String tipo) throws Exception {
        try (PDDocument documento = new PDDocument()) {
            var fuente = GeneradorPDFS.obtenerFuente(documento, tipo);

            assertInstanceOf(PDType0Font.class, fuente);
            assertTrue(fuente.isEmbedded());
            assertDoesNotThrow(() -> fuente.encode("Reunión técnica → José Muñoz"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void tablaComienzaDebajoDeLosDatosDelEncabezado(boolean conRango) throws Exception {
        TablePDF tabla = new TablePDF(List.of("Encabezado"));
        tabla.agregarFila(List.of("Reunión → José Muñoz"));
        ReportePDF reporte = conRango
                ? new ReportePDF("Reporte", LocalDate.now(), LocalDate.now(), tabla)
                : new ReportePDF("Reporte", tabla);
        String nombre = "prueba-pdf-" + UUID.randomUUID() + ".pdf";
        Path archivo = Path.of("pdfs", nombre);
        try {
            GeneradorPDFS.generar(reporte, nombre);
            try (PDDocument documento = Loader.loadPDF(archivo.toFile())) {
                PosicionesEncabezado texto = new PosicionesEncabezado();
                assertTrue(texto.getText(documento).contains("Reunión → José Muñoz"));
                assertTrue(texto.ultimaLinea > 0);
                assertTrue(texto.tabla > texto.ultimaLinea + 15,
                        "La tabla debe dejar espacio bajo la fecha de generación y el rango");
            }
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    private static class PosicionesEncabezado extends PDFTextStripper {
        private float ultimaLinea;
        private float tabla;

        PosicionesEncabezado() throws IOException { }

        @Override
        protected void writeString(String texto, List<TextPosition> posiciones) throws IOException {
            if (texto.startsWith("Generado:") || texto.startsWith("Desde:")) {
                ultimaLinea = posiciones.get(0).getYDirAdj();
            }
            if (texto.equals("Encabezado")) tabla = posiciones.get(0).getYDirAdj();
            super.writeString(texto, posiciones);
        }
    }
}
