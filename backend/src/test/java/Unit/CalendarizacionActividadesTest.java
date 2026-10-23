package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.*;
import una.proyecto.service.RecursoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CalendarizacionActividadesTest {

    @Mock
    private ReservaDatos datos;

    @Mock
    private RecursoService recursoService;

    private ReservaLogic logica;

    @BeforeEach
    void setUp() {
        logica = new ReservaLogic(datos, recursoService);
    }

    private Reserva reservaValida(String id) {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "88887777");
        Reserva r = new Reserva("Reunion", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "123456789", new ArrayList<>(), EstadoReserva.ACTIVA, f);
        r.setId(id);
        r.setCategoriasDeRecursosIds(new ArrayList<>());
        r.setRecursosAsignadosIds(new ArrayList<>());
        return r;
    }

    // Probar obtenerTodos() cuando no hay reservas — debe retornar lista vacia
    @Test
    void obtenerTodosSinReservasRetornaVacio() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // Probar obtenerTodos() — debe retornar todas las reservas
    @Test
    void obtenerTodosRetornaTodasLasReservas() {
        when(datos.obtenerTodos()).thenReturn(List.of(
                reservaValida("RES-000001"), reservaValida("RES-000002")));
        assertEquals(2, logica.obtenerTodos().size());
    }

    // Probar leerPorId() cuando la reserva existe
    @Test
    void leerPorIdExistenteRetornaReserva() {
        when(datos.leerPorId("RES-000001")).thenReturn(reservaValida("RES-000001"));
        assertNotNull(logica.leerPorId("RES-000001"));
    }

    // Probar leerPorId() cuando la reserva no existe — debe retornar null
    @Test
    void leerPorIdInexistenteRetornaNull() {
        when(datos.leerPorId("RES-999999")).thenReturn(null);
        assertNull(logica.leerPorId("RES-999999"));
    }

    // Probar crear() con reserva nula — debe lanzar IllegalArgumentException
    @Test
    void crearReservaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(null));
    }

    // Probar crear() correctamente — verificar que se llame a datos.crear()
    @Test
    void crearReservaCorrectamente() {
        Reserva r = reservaValida("RES-000001");
        when(datos.leerPorId("RES-000001")).thenReturn(null);
        logica.crear(r);
        verify(datos).crear(r);
    }

    // Probar crear() con id duplicado — debe lanzar IllegalArgumentException
    @Test
    void crearReservaIdDuplicadoLanzaExcepcion() {
        when(datos.leerPorId("RES-000001")).thenReturn(reservaValida("RES-000001"));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(reservaValida("RES-000001")));
    }

    // Probar crear() sin id — debe autogenerar un id con formato RES-
    @Test
    void crearReservaSinIdAutogeneraId() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        Reserva r = reservaValida(null);
        logica.crear(r);
        assertNotNull(r.getId());
        assertTrue(r.getId().startsWith("RES-"));
    }

    // Verificar que el primer id autogenerado sea RES-000001
    @Test
    void crearPrimerIdAutogeneradoEsRES000001() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        Reserva r = reservaValida(null);
        logica.crear(r);
        assertEquals("RES-000001", r.getId());
    }

    // Verificar que el segundo id autogenerado sea RES-000002
    @Test
    void crearSegundoIdAutogeneradoEsRES000002() {
        Reserva primera = reservaValida("RES-000001");
        when(datos.obtenerTodos())
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(primera));
        Reserva r1 = reservaValida(null);
        logica.crear(r1);
        Reserva r2 = reservaValida(null);
        logica.crear(r2);
        assertEquals("RES-000002", r2.getId());
    }

    // Probar eliminar() con una reserva que no existe — debe lanzar IllegalArgumentException
    @Test
    void eliminarReservaInexistenteLanzaExcepcion() {
        when(datos.leerPorId("RES-999999")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("RES-999999"));
    }

    // Probar eliminar() correctamente
    @Test
    void eliminarReservaCorrectamente() {
        when(datos.leerPorId("RES-000001")).thenReturn(reservaValida("RES-000001"));
        logica.eliminar("RES-000001");
        verify(datos).eliminar("RES-000001");
    }

    // Verificar que eliminar() invoque datos.eliminar() con el id correcto
    @Test
    void eliminarInvocaDatosEliminarConIdCorrecto() {
        when(datos.leerPorId("RES-000001")).thenReturn(reservaValida("RES-000001"));
        logica.eliminar("RES-000001");
        verify(datos, times(1)).eliminar("RES-000001");
    }

    // Probar verificarHoras() con hora inicio igual a hora fin — debe lanzar IllegalArgumentException
    @Test
    void verificarHorasInicioIgualFinLanzaExcepcion() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> logica.verificarHoras(manana, LocalTime.of(9, 0), LocalTime.of(9, 0)));
    }

    // Probar verificarHoras() con hora inicio despues de hora fin — debe lanzar IllegalArgumentException
    @Test
    void verificarHorasInicioDespuesDeFinLanzaExcepcion() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> logica.verificarHoras(manana, LocalTime.of(11, 0), LocalTime.of(9, 0)));
    }

    // Probar verificarHoras() con fecha pasada — debe lanzar IllegalArgumentException
    @Test
    void verificarHorasFechaPasadaLanzaExcepcion() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> logica.verificarHoras(ayer, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    // Probar verificarHoras() con fecha futura valida — no debe lanzar excepcion
    @Test
    void verificarHorasFechaFuturaValidaNoLanzaExcepcion() {
        LocalDate manana = LocalDate.now().plusDays(1);
        assertDoesNotThrow(() -> logica.verificarHoras(manana, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    // Probar obtenerReservasPorFuncionario() con id null — debe retornar lista vacia
    @Test
    void obtenerReservasPorFuncionarioNullRetornaVacio() {
        assertTrue(logica.obtenerReservasPorFuncionario(null).isEmpty());
    }

    // Probar obtenerReservasPorFuncionario() con id vacio — debe retornar lista vacia
    @Test
    void obtenerReservasPorFuncionarioVacioRetornaVacio() {
        assertTrue(logica.obtenerReservasPorFuncionario("").isEmpty());
    }

    // Probar obtenerReservasPorFuncionario() con funcionario que tiene reservas
    @Test
    void obtenerReservasPorFuncionarioConReservas() {
        when(datos.obtenerTodos()).thenReturn(List.of(reservaValida("RES-000001")));
        assertFalse(logica.obtenerReservasPorFuncionario("123456789").isEmpty());
    }

    // Probar obtenerReservasPorFuncionario() con funcionario sin reservas — debe retornar lista vacia
    @Test
    void obtenerReservasPorFuncionarioSinReservasRetornaVacio() {
        when(datos.obtenerTodos()).thenReturn(List.of(reservaValida("RES-000001")));
        assertTrue(logica.obtenerReservasPorFuncionario("999999999").isEmpty());
    }

    // Probar crearReserva() sin categorias — debe lanzar IllegalArgumentException
    @Test
    void crearReservaSinCategoriasLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class,
                () -> logica.crearReserva("Reunion", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0),
                        "123456789", new ArrayList<>(), EstadoReserva.ACTIVA, f));
    }

    // Probar crearReserva() con lista de categorias null — debe lanzar IllegalArgumentException
    @Test
    void crearReservaCategoriasNullLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class,
                () -> logica.crearReserva("Reunion", LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0),
                        "123456789", null, EstadoReserva.ACTIVA, f));
    }

    // Probar actualizarCategoriasDeReserva() con null — debe lanzar IllegalArgumentException
    @Test
    void actualizarCategoriasDeReservaConNullLanzaExcepcion() {
        Reserva r = reservaValida("RES-000001");
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizarCategoriasDeReserva(r, null));
    }

    // Probar actualizarCategoriasDeReserva() correctamente — debe agregar las categorias
    @Test
    void actualizarCategoriasDeReservaAgregaCategorias() {
        Reserva r = reservaValida("RES-000001");
        logica.actualizarCategoriasDeReserva(r, List.of(new Categoria("CAT-000001", "Sala")));
        assertEquals(1, r.getCategoriasDeRecursos().size());
    }

    // Probar borrarCategoriasDeReserva() con null — debe lanzar IllegalArgumentException
    @Test
    void borrarCategoriasDeReservaConNullLanzaExcepcion() {
        Reserva r = reservaValida("RES-000001");
        assertThrows(IllegalArgumentException.class,
                () -> logica.borrarCategoriasDeReserva(r, null));
    }

    // Probar borrarCategoriasDeReserva() correctamente — debe eliminar las categorias indicadas
    @Test
    void borrarCategoriasDeReservaEliminaCategorias() {
        Reserva r = reservaValida("RES-000001");
        Categoria cat = new Categoria("CAT-000001", "Sala");
        logica.actualizarCategoriasDeReserva(r, new ArrayList<>(List.of(cat)));
        logica.borrarCategoriasDeReserva(r, List.of(cat));
        assertTrue(r.getCategoriasDeRecursos().isEmpty());
    }

    // Probar filtrarReserva() con fecha que no tiene reservas — debe retornar lista vacia
    @Test
    void filtrarReservaFechaSinReservasRetornaVacio() {
        when(datos.obtenerTodos()).thenReturn(List.of(reservaValida("RES-000001")));
        assertTrue(logica.filtrarReserva(LocalDate.now().plusDays(99), "CAT-000001").isEmpty());
    }

    // Probar filtrarReserva() con categoria que no coincide — debe retornar lista vacia
    @Test
    void filtrarReservaCategoriaSinCoincidenciaRetornaVacio() {
        Reserva r = reservaValida("RES-000001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        when(datos.obtenerTodos()).thenReturn(List.of(r));
        assertTrue(logica.filtrarReserva(r.getFecha(), "CAT-999999").isEmpty());
    }

    // Probar filtrarReserva() correctamente — debe retornar la reserva que coincide
    @Test
    void filtrarReservaEncuentraCoincidencia() {
        Reserva r = reservaValida("RES-000001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        when(datos.obtenerTodos()).thenReturn(List.of(r));
        assertFalse(logica.filtrarReserva(r.getFecha(), "CAT-000001").isEmpty());
    }
}
