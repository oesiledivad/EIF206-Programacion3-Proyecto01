package Unit;

import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.EstadisticasLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadisticaItem;
import una.proyecto.model.Reserva;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstadisticasLogicTest {

    @Mock
    private ReservaDatos reservaDatos;

    @Mock
    private CategoriaDatos categoriaDatos;

    private EstadisticasLogic estadisticasLogic;
    private Locale localeOriginal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        estadisticasLogic = new EstadisticasLogic(reservaDatos, categoriaDatos);

        localeOriginal = Locale.getDefault();
    }

    @AfterEach
    void tearDown() {
        Locale.setDefault(localeOriginal);
    }

    private Reserva mockReserva(LocalDate fecha) {
        Reserva r = mock(Reserva.class);
        when(r.getFecha()).thenReturn(fecha);
        return r;
    }

    // ---------- obtenerEstadisticasRecursos ----------

    @Test
    void recursos_reservaConFechaNull_seIgnora() {
        Reserva r = mockReserva(null);
        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(result.isEmpty());
        verifyNoInteractions(categoriaDatos);
    }

    @Test
    void recursos_fechaFueraDeRango_seIgnora() {
        Reserva antes = mockReserva(LocalDate.of(2025, 12, 31));
        Reserva despues = mockReserva(LocalDate.of(2026, 2, 1));
        when(reservaDatos.obtenerTodos()).thenReturn(List.of(antes, despues));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(result.isEmpty());
    }

    @Test
    void recursos_fechaEnLimitesInclusive_seCuenta() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);

        Categoria cat = mock(Categoria.class);
        when(cat.getDescripcion()).thenReturn("Salones");

        Reserva enDesde = mockReserva(desde);
        when(enDesde.getCategoriasDeRecursos()).thenReturn(List.of(cat));

        Reserva enHasta = mockReserva(hasta);
        when(enHasta.getCategoriasDeRecursos()).thenReturn(List.of(cat));

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(enDesde, enHasta));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(desde, hasta);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getCantidad());
    }

    @Test
    void recursos_categoriasEnMemoria_cuentaPorDescripcion_sinConsultarCategoriaDatos() {
        Categoria cat1 = mock(Categoria.class);
        when(cat1.getDescripcion()).thenReturn("Salones");
        Categoria cat2 = mock(Categoria.class);
        when(cat2.getDescripcion()).thenReturn("Equipos");

        Reserva r = mockReserva(LocalDate.of(2026, 1, 15));
        when(r.getCategoriasDeRecursos()).thenReturn(List.of(cat1, cat2));

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(2, result.size());
        verifyNoInteractions(categoriaDatos);
    }

    @Test
    void recursos_fallbackPorIds_consultaCategoriaDatos() {
        Reserva r = mockReserva(LocalDate.of(2026, 1, 15));
        when(r.getCategoriasDeRecursos()).thenReturn(null);
        when(r.getCategoriasDeRecursosIds()).thenReturn(List.of("CAT1"));

        Categoria cat = mock(Categoria.class);
        when(cat.getDescripcion()).thenReturn("Salones");
        when(categoriaDatos.leerPorId("CAT1")).thenReturn(cat);

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
        assertEquals("Salones", result.get(0).getNombre());
    }

    @Test
    void recursos_fallbackPorIds_categoriaNoEncontrada_usaEtiquetaGenerica() {
        Reserva r = mockReserva(LocalDate.of(2026, 1, 15));
        when(r.getCategoriasDeRecursos()).thenReturn(null);
        when(r.getCategoriasDeRecursosIds()).thenReturn(List.of("CAT_INEXISTENTE"));

        when(categoriaDatos.leerPorId("CAT_INEXISTENTE")).thenReturn(null);

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(1, result.size());
        assertEquals("Categoría (CAT_INEXISTENTE)", result.get(0).getNombre());
    }

    @Test
    void recursos_sinCategoriasEnMemoriaNiIds_seIgnoraSilenciosamente() {
        Reserva r = mockReserva(LocalDate.of(2026, 1, 15));
        when(r.getCategoriasDeRecursos()).thenReturn(null);
        when(r.getCategoriasDeRecursosIds()).thenReturn(null);

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        // Documenta el comportamiento actual: la reserva no se cuenta en ningún lado
        assertTrue(result.isEmpty());
    }

    @Test
    void recursos_listaVacia_retornaListaVacia() {
        when(reservaDatos.obtenerTodos()).thenReturn(List.of());

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasRecursos(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertTrue(result.isEmpty());
    }

    // ---------- obtenerEstadisticasActividades ----------

    @Test
    void actividades_fechaNull_seIgnora() {
        Reserva r = mockReserva(null);
        when(reservaDatos.obtenerTodos()).thenReturn(List.of(r));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(item -> item.getCantidad() == 0));
    }

    @Test
    void actividades_fueraDeRango_seIgnora() {
        Reserva antes = mockReserva(LocalDate.of(2025, 12, 31));
        when(reservaDatos.obtenerTodos()).thenReturn(List.of(antes));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(5, result.size());
        assertTrue(result.stream().allMatch(item -> item.getCantidad() == 0));
    }

    @Test
    void actividades_mismaSemana_seAgrupanJuntas() {
        Reserva lunes = mockReserva(LocalDate.of(2026, 1, 12));
        Reserva miercoles = mockReserva(LocalDate.of(2026, 1, 14));
        Reserva domingo = mockReserva(LocalDate.of(2026, 1, 18));

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(lunes, miercoles, domingo));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(5, result.size());
        assertEquals(3, result.get(2).getCantidad());
        assertEquals("2026-01-12", result.get(2).getNombre());
        assertEquals(3, result.stream().mapToInt(item -> item.getCantidad()).sum());
    }

    @Test
    void actividades_semanasDistintas_seCuentanSeparadas() {
        Reserva semana1 = mockReserva(LocalDate.of(2026, 1, 12));
        Reserva semana2 = mockReserva(LocalDate.of(2026, 1, 19));

        when(reservaDatos.obtenerTodos()).thenReturn(List.of(semana1, semana2));

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(5, result.size());
        assertEquals(1, result.get(2).getCantidad());
        assertEquals(1, result.get(3).getCantidad());
    }

    @Test
    void actividades_listaVacia_incluyeTodasLasSemanasConCero() {
        when(reservaDatos.obtenerTodos()).thenReturn(List.of());

        List<EstadisticaItem> result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertEquals(List.of("2025-12-29", "2026-01-05", "2026-01-12", "2026-01-19", "2026-01-26"),
                result.stream().map(item -> item.getNombre()).toList());
        assertTrue(result.stream().allMatch(item -> item.getCantidad() == 0));
    }

    @Test
    void actividades_ordenCronologicoAunqueReservasVenganDesordenadas() {
        doReturn(List.of(mockReserva(LocalDate.of(2026, 1, 26)), mockReserva(LocalDate.of(2026, 1, 5))))
                .when(reservaDatos).obtenerTodos();
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 26));
        assertEquals(List.of("2026-01-05", "2026-01-12", "2026-01-19", "2026-01-26"),
                result.stream().map(item -> item.getNombre()).toList());
        assertEquals(List.of(1, 0, 0, 1), result.stream().map(item -> item.getCantidad()).toList());
    }

    @Test
    void actividades_cuentaLimitesInclusivosSinAmpliarPeriodoALaSemanaCompleta() {
        doReturn(List.of(
                mockReserva(LocalDate.of(2026, 1, 6)), mockReserva(LocalDate.of(2026, 1, 7)),
                mockReserva(LocalDate.of(2026, 1, 9)), mockReserva(LocalDate.of(2026, 1, 10))))
                .when(reservaDatos).obtenerTodos();
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 7), LocalDate.of(2026, 1, 9));
        assertEquals(1, result.size());
        assertEquals("2026-01-05", result.get(0).getNombre());
        assertEquals(2, result.get(0).getCantidad());
    }

    @Test
    void actividades_domingoYLunesPertenecenASemanasDiferentes() {
        doReturn(List.of(mockReserva(LocalDate.of(2026, 1, 18)), mockReserva(LocalDate.of(2026, 1, 19))))
                .when(reservaDatos).obtenerTodos();
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 18), LocalDate.of(2026, 1, 19));
        assertEquals(List.of("2026-01-12", "2026-01-19"), result.stream().map(item -> item.getNombre()).toList());
        assertEquals(List.of(1, 1), result.stream().map(item -> item.getCantidad()).toList());
    }

    @Test
    void actividades_periodoDeUnDiaDevuelveSuSemana() {
        doReturn(List.of(mockReserva(LocalDate.of(2026, 1, 14)))).when(reservaDatos).obtenerTodos();
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 14), LocalDate.of(2026, 1, 14));
        assertEquals(1, result.size());
        assertEquals("2026-01-12", result.get(0).getNombre());
        assertEquals(1, result.get(0).getCantidad());
    }

    @Test
    void actividades_cruceDeAnioIncluyeSemanasSinDatos() {
        when(reservaDatos.obtenerTodos()).thenReturn(List.of());
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2025, 12, 31), LocalDate.of(2026, 1, 6));
        assertEquals(List.of("2025-12-29", "2026-01-05"), result.stream().map(item -> item.getNombre()).toList());
        assertTrue(result.stream().allMatch(item -> item.getCantidad() == 0));
    }

    @Test
    void actividades_inicioLunesNoDependeDelLocale() {
        Locale.setDefault(Locale.US);
        doReturn(List.of(mockReserva(LocalDate.of(2026, 1, 18)))).when(reservaDatos).obtenerTodos();
        var result = estadisticasLogic.obtenerEstadisticasActividades(
                LocalDate.of(2026, 1, 18), LocalDate.of(2026, 1, 18));
        assertEquals("2026-01-12", result.get(0).getNombre());
        assertEquals(1, result.get(0).getCantidad());
    }
}
