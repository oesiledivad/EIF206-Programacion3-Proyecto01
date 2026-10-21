package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.EstadisticasLogic;
import una.proyecto.model.*;
import una.proyecto.service.EstadisticasService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: EstadisticasService -> EstadisticasLogic -> ReservaDatos, CategoriaDatos → XML.
 */
public class EstadisticasIntegracionIT {

    private static final String XML_RESERVAS = "target/test-it-reservas-estadisticas.xml";
    private static final String XML_CATEGORIAS = "target/test-it-categorias-estadisticas.xml";

    private EstadisticasService service;
    private ReservaDatos reservaDatos;
    private CategoriaDatos categoriaDatos;
    private Locale localeOriginal;

    @BeforeEach
    void setUp() throws Exception {
        new File("target").mkdirs();
        new File(XML_RESERVAS).delete();
        new File(XML_CATEGORIAS).delete();

        localeOriginal = Locale.getDefault();
        Locale.setDefault(new Locale("es", "CR"));

        reservaDatos = new ReservaDatos(XML_RESERVAS);
        categoriaDatos = new CategoriaDatos(XML_CATEGORIAS);
        service = new EstadisticasService(new EstadisticasLogic(reservaDatos, categoriaDatos));
    }

    @AfterEach
    void tearDown() {
        new File(XML_RESERVAS).delete();
        new File(XML_CATEGORIAS).delete();
        Locale.setDefault(localeOriginal);
    }

    private Reserva reservaConCategoriaIds(String id, LocalDate fecha, List<String> categoriaIds) {
        Reserva r = new Reserva("Actividad Test", fecha, LocalTime.of(8, 0), LocalTime.of(10, 0),
                "111111111", null, EstadoReserva.ACTIVA, null);
        r.setId(id);
        r.setCategoriasDeRecursosIds(categoriaIds);
        return r;
    }

    @Test
    void estadisticasRecursos_conCategoriaPersistidaEnXML_cuentaCorrectamente() {
        categoriaDatos.crear(new Categoria("CAT1", "Salones"));

        reservaDatos.crear(reservaConCategoriaIds("R1",
                LocalDate.of(2026, 1, 15), List.of("CAT1")));
        reservaDatos.crear(reservaConCategoriaIds("R2",
                LocalDate.of(2026, 1, 20), List.of("CAT1")));

        List<EstadisticaItem> result = service.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
        assertEquals("Salones", result.get(0).getNombre());
        assertEquals(2, result.get(0).getCantidad());
    }

    @Test
    void estadisticasRecursos_categoriaNoExisteEnXML_usaEtiquetaGenerica() {
        reservaDatos.crear(reservaConCategoriaIds("R1",
                LocalDate.of(2026, 1, 15), List.of("CAT_INEXISTENTE")));

        List<EstadisticaItem> result = service.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals("Categoría (CAT_INEXISTENTE)", result.get(0).getNombre());
    }

    @Test
    void estadisticasRecursos_fueraDeRango_seExcluye() {
        categoriaDatos.crear(new Categoria("CAT1", "Salones"));
        reservaDatos.crear(reservaConCategoriaIds("R1",
                LocalDate.of(2025, 12, 1), List.of("CAT1")));

        List<EstadisticaItem> result = service.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(result.isEmpty());
    }

    @Test
    void estadisticasActividades_agrupaPorSemanaDesdeXML() {
        reservaDatos.crear(reservaConCategoriaIds("R1",
                LocalDate.of(2026, 1, 12), List.of()));
        reservaDatos.crear(reservaConCategoriaIds("R2",
                LocalDate.of(2026, 1, 14), List.of()));

        List<EstadisticaItem> result = service.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getCantidad());
    }

    @Test
    void estadisticas_listaVaciaEnXML_retornaVacio() {
        List<EstadisticaItem> result = service.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(result.isEmpty());
    }
}