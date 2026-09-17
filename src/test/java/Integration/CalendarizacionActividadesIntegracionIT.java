package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.*;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: ReservaService → ReservaLogic → ReservaDatos → XML.
 * Usa un archivo XML temporal en target/ para no tocar los datos originales del proyecto.
 * RecursoService se implementa sin recursos para aislar la capa de reservas.
 */
public class CalendarizacionActividadesIntegracionIT {

    private static final String XML_TEST = "target/test-it-reservas.xml";
    private ReservaService service;

    @BeforeEach
    void setUp() throws Exception {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        RecursoService sinRecursos = new RecursoService(null) {
            @Override public List<Recurso> obtenerTodosRecursos() { return new ArrayList<>(); }
        };
        service = new ReservaService(new ReservaLogic(new ReservaDatos(XML_TEST), sinRecursos));
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }

    private Reserva reservaValida(String id) {
        Funcionario f = new Funcionario("111111111", "FUNCIONARIO", "Juan Perez", "88887777");
        Reserva r = new Reserva("Reunion IT", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "111111111", new ArrayList<>(), EstadoReserva.ACTIVA, f);
        r.setId(id);
        r.setCategoriasDeRecursosIds(new ArrayList<>());
        r.setRecursosAsignadosIds(new ArrayList<>());
        return r;
    }

    // Integración CRUD: guardar una reserva y leerla desde el XML real
    @Test
    void crearYLeerReservaDesdeXML() {
        service.save(reservaValida("RES-IT-001"));
        Reserva leida = service.buscarPorId("RES-IT-001");
        assertNotNull(leida);
        assertEquals("RES-IT-001", leida.getId());
        assertEquals("Reunion IT", leida.getActividad());
    }

    // Integración CRUD: crear varias reservas y obtenerlas todas desde el XML
    @Test
    void crearVariasReservasYObtenerTodasDesdeXML() {
        service.save(reservaValida("RES-IT-001"));
        service.save(reservaValida("RES-IT-002"));
        assertEquals(2, service.obtenerTodasReservas().size());
    }

    // Integración CRUD: eliminar una reserva y verificar que ya no existe en el XML
    @Test
    void eliminarReservaYaNoExisteEnXML() {
        service.save(reservaValida("RES-IT-001"));
        service.delete("RES-IT-001");
        assertNull(service.buscarPorId("RES-IT-001"));
    }

    // Integración: filtrar reservas por fecha y categoría sobre datos persistidos en XML
    @Test
    void filtrarReservasPorFechaYCategoriaDesdeXML() {
        Reserva r = reservaValida("RES-IT-001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        service.save(r);
        assertFalse(service.filtrarReservasService(r.getFecha(), "CAT-000001").isEmpty());
    }

    // Integración: filtrar por categoría inexistente retorna vacío desde XML real
    @Test
    void filtrarReservasCategoriaInexistenteRetornaVacioDesdeXML() {
        Reserva r = reservaValida("RES-IT-001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        service.save(r);
        assertTrue(service.filtrarReservasService(r.getFecha(), "CAT-999999").isEmpty());
    }

    // Integración: obtener reservas por funcionario sobre datos persistidos en XML
    @Test
    void obtenerReservasPorFuncionarioDesdeXML() {
        service.save(reservaValida("RES-IT-001"));
        assertFalse(service.obtenerReservasPorFuncionario("111111111").isEmpty());
    }

    // Integración: repoblar categorías asigna objetos reales a reservas cargadas desde XML
    @Test
    void repoblarCategoriasAsignaCategoriasSobreReservasDeXML() {
        Reserva r = reservaValida("RES-IT-001");
        r.setCategoriasDeRecursosIds(List.of("CAT-000001"));
        r.setCategoriasDeRecursos(null);
        service.save(r);
        List<Reserva> reservas = service.obtenerTodasReservas();
        List<Categoria> catalogo = List.of(new Categoria("CAT-000001", "Sala"));
        service.repoblarCategoriasService(reservas, catalogo);
        assertNotNull(reservas.get(0).getCategoriasDeRecursos());
        assertEquals(1, reservas.get(0).getCategoriasDeRecursos().size());
    }

    // Integración: id duplicado no genera un segundo registro en el XML
    @Test
    void crearIdDuplicadoNoGuardaSegundoRegistroEnXML() {
        service.save(reservaValida("RES-IT-001"));
        assertThrows(IllegalArgumentException.class,
                () -> service.save(reservaValida("RES-IT-001")));
        assertEquals(1, service.obtenerTodasReservas().size());
    }
}
