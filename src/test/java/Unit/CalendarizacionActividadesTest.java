package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.*;
import una.proyecto.service.RecursoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CalendarizacionActividadesTest {

    private ReservaLogic logica;
    private List<Reserva> store;

    @BeforeEach
    void setUp() throws Exception {
        store = new ArrayList<>();
        ReservaDatos datos = new ReservaDatos("") {
            @Override public List<Reserva> cargarTodo() { return new ArrayList<>(store); }
            @Override public List<Reserva> obtenerTodos() { return new ArrayList<>(store); }
            @Override public void crear(Reserva r) { store.add(r); }
            @Override public Reserva leerPorId(String id) {
                return store.stream().filter(r -> id.equals(r.getId())).findFirst().orElse(null);
            }
            @Override public void eliminar(String id) { store.removeIf(r -> id.equals(r.getId())); }
            @Override public void guardarReservas(List<Reserva> lista) {}
        };

        RecursoService recursoServiceMock = new RecursoService(null) {
            @Override public List<Recurso> obtenerTodosRecursos() { return new ArrayList<>(); }
        };

        logica = new ReservaLogic(datos, recursoServiceMock);
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
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // Probar obtenerTodos() — debe retornar todas las reservas creadas
    @Test
    void obtenerTodosRetornaTodasLasReservas() {
        logica.crear(reservaValida("RES-000001"));
        logica.crear(reservaValida("RES-000002"));
        assertEquals(2, logica.obtenerTodos().size());
    }

    // Probar leerPorId() cuando la reserva existe
    @Test
    void leerPorIdExistenteRetornaReserva() {
        logica.crear(reservaValida("RES-000001"));
        assertNotNull(logica.leerPorId("RES-000001"));
    }

    // Probar leerPorId() cuando la reserva no existe — debe retornar null
    @Test
    void leerPorIdInexistenteRetornaNull() {
        assertNull(logica.leerPorId("RES-999999"));
    }

    // Probar crear() con reserva nula — debe lanzar IllegalArgumentException
    @Test
    void crearReservaNulaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(null));
    }

    // Probar crear() correctamente — verificar que se guarde la reserva
    @Test
    void crearReservaCorrectamente() {
        logica.crear(reservaValida("RES-000001"));
        assertNotNull(logica.leerPorId("RES-000001"));
    }

    // Probar crear() con id duplicado — debe lanzar IllegalArgumentException
    @Test
    void crearReservaIdDuplicadoLanzaExcepcion() {
        logica.crear(reservaValida("RES-000001"));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(reservaValida("RES-000001")));
    }

    // Probar crear() sin id — debe autogenerar un id con formato RES-
    @Test
    void crearReservaSinIdAutogeneraId() {
        logica.crear(reservaValida(null));
        assertNotNull(store.get(0).getId());
        assertTrue(store.get(0).getId().startsWith("RES-"));
    }

    // Verificar que el primer id autogenerado sea RES-000001
    @Test
    void crearPrimerIdAutogeneradoEsRES000001() {
        logica.crear(reservaValida(null));
        assertEquals("RES-000001", store.get(0).getId());
    }

    // Verificar que el segundo id autogenerado sea RES-000002
    @Test
    void crearSegundoIdAutogeneradoEsRES000002() {
        logica.crear(reservaValida(null));
        logica.crear(reservaValida(null));
        assertEquals("RES-000002", store.get(1).getId());
    }

    // Probar eliminar() con una reserva que no existe — debe lanzar IllegalArgumentException
    @Test
    void eliminarReservaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("RES-999999"));
    }

    // Probar eliminar() correctamente
    @Test
    void eliminarReservaCorrectamente() {
        logica.crear(reservaValida("RES-000001"));
        logica.eliminar("RES-000001");
        assertNull(logica.leerPorId("RES-000001"));
    }

    // Verificar que eliminar() reduzca el total de reservas
    @Test
    void eliminarReduceElTotal() {
        logica.crear(reservaValida("RES-000001"));
        logica.crear(reservaValida("RES-000002"));
        logica.eliminar("RES-000001");
        assertEquals(1, logica.obtenerTodos().size());
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
        logica.crear(reservaValida("RES-000001"));
        assertFalse(logica.obtenerReservasPorFuncionario("123456789").isEmpty());
    }

    // Probar obtenerReservasPorFuncionario() con funcionario sin reservas — debe retornar lista vacia
    @Test
    void obtenerReservasPorFuncionarioSinReservasRetornaVacio() {
        logica.crear(reservaValida("RES-000001"));
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
        logica.crear(reservaValida("RES-000001"));
        assertTrue(logica.filtrarReserva(LocalDate.now().plusDays(99), "CAT-000001").isEmpty());
    }

    // Probar filtrarReserva() con categoria que no coincide — debe retornar lista vacia
    @Test
    void filtrarReservaCategoriaSinCoincidenciaRetornaVacio() {
        Reserva r = reservaValida("RES-000001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        logica.crear(r);
        assertTrue(logica.filtrarReserva(r.getFecha(), "CAT-999999").isEmpty());
    }

    // Probar filtrarReserva() correctamente — debe retornar la reserva que coincide
    @Test
    void filtrarReservaEncuentraCoincidencia() {
        Reserva r = reservaValida("RES-000001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        logica.crear(r);
        assertFalse(logica.filtrarReserva(r.getFecha(), "CAT-000001").isEmpty());
    }
}
