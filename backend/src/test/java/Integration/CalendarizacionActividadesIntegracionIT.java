package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.Database;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.service.CalendarizacionService;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: ReservaService → ReservaLogic → ReservaDatos → MySQL (reservas + reserva_recurso).
 * Crea su propio funcionario, categoría y recurso con prefijo IT- y los borra al terminar.
 */
public class CalendarizacionActividadesIntegracionIT {

    private static final String FUNCIONARIO_PRUEBA = "IT-FUNC-CAL";
    private static final String PREFIJO_CATEGORIA = "IT-CAL";
    private static final String RECURSO_PRUEBA = "IT-CAL-001";
    private static final LocalDate FECHA = LocalDate.now().plusDays(7);

    private ReservaService service;
    private Categoria categoria;

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();

        ejecutar("insert into usuarios (id_usuario, nombre, clave_hash, rol) values (?, ?, ?, ?)",
                FUNCIONARIO_PRUEBA, "Funcionario de prueba", "sin-clave", "FUNCIONARIO");
        ejecutar("insert into funcionarios (id_usuario, telefono) values (?, ?)",
                FUNCIONARIO_PRUEBA, "00000000");

        categoria = new Categoria(null, PREFIJO_CATEGORIA + " Sala");
        new CategoriaDatos().crear(categoria);                       // MySQL le asigna el id
        new RecursoDatos().crear(new Recurso(RECURSO_PRUEBA, categoria.getId(), "Sala de prueba"));

        RecursoService recursoService = new RecursoService(new RecursoLogic(new RecursoDatos()));
        service = new ReservaService(new ReservaLogic(new ReservaDatos(), recursoService));
    }

    @AfterEach
    void tearDown() throws Exception {
        limpiarDatosDePrueba();
    }

    // Orden inverso a las llaves foráneas: hijos primero, padres después.
    private void limpiarDatosDePrueba() throws Exception {
        ejecutar("delete from reserva_recurso where id_reserva in "
                + "(select id_reserva from reservas where id_funcionario = ?)", FUNCIONARIO_PRUEBA);
        ejecutar("delete from reservas where id_funcionario = ?", FUNCIONARIO_PRUEBA);
        ejecutar("delete from recursos where id_recurso like ?", PREFIJO_CATEGORIA + "-%");
        ejecutar("delete from categorias where descripcion like ?", PREFIJO_CATEGORIA + "%");
        ejecutar("delete from funcionarios where id_usuario = ?", FUNCIONARIO_PRUEBA);
        ejecutar("delete from usuarios where id_usuario = ?", FUNCIONARIO_PRUEBA);
    }

    private void ejecutar(String sql, String... parametros) throws Exception {
        try (PreparedStatement stm = Database.instance().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stm.setString(i + 1, parametros[i]);
            }
            Database.instance().executeUpdate(stm);
        }
    }

    // Arma la reserva como lo hace la pantalla: la lógica asigna un recurso disponible por categoría.
    private Reserva reservar(String actividad, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        Reserva reserva = service.crearReserva(actividad, fecha, inicio, fin,
                FUNCIONARIO_PRUEBA, List.of(categoria), EstadoReserva.ACTIVA, null);
        service.save(reserva);
        return reserva;
    }

    private long contarReservasDePrueba() {
        return service.obtenerReservasPorFuncionario(FUNCIONARIO_PRUEBA).size();
    }

    @Test
    void crear_reserva_guarda_sus_recursos_y_categorias() {
        Reserva reserva = reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        Reserva leida = service.buscarPorId(reserva.getId());

        assertNotNull(leida);
        assertEquals(List.of(RECURSO_PRUEBA), leida.getRecursosAsignadosIds());
        assertEquals(List.of(categoria.getId()), leida.getCategoriasDeRecursosIds());
    }

    @Test
    void obtener_todas_incluye_los_recursos_de_cada_reserva() {
        reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        Reserva leida = service.obtenerReservasPorFuncionario(FUNCIONARIO_PRUEBA).get(0);

        assertEquals(List.of(RECURSO_PRUEBA), leida.getRecursosAsignadosIds());
    }

    @Test
    void crear_con_recurso_inexistente_hace_rollback() {
        Reserva reserva = new Reserva("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0),
                FUNCIONARIO_PRUEBA, new ArrayList<>(), EstadoReserva.ACTIVA, null);
        reserva.setRecursosAsignadosIds(List.of("IT-CAL-NO-EXISTE"));

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.save(reserva));

        assertTrue(error.getMessage().contains("no existe"), "Mensaje recibido: " + error.getMessage());
        assertEquals(0, contarReservasDePrueba()); // no quedó la reserva sin recursos
        assertNull(reserva.getId());
    }

    @Test
    void recurso_ocupado_no_se_asigna_dos_veces_en_el_mismo_horario() {
        reservar("Primera", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> reservar("Traslapada", FECHA, LocalTime.of(9, 30), LocalTime.of(10, 30)));

        assertTrue(error.getMessage().contains("No hay recursos disponibles"),
                "Mensaje recibido: " + error.getMessage());
        assertEquals(1, contarReservasDePrueba());
    }

    @Test
    void recurso_se_puede_reusar_en_un_horario_sin_traslape() {
        reservar("Primera", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));
        reservar("Segunda", FECHA, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(2, contarReservasDePrueba());
    }

    @Test
    void cancelar_reserva_libera_el_recurso() {
        Reserva primera = reservar("Primera", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        service.delete(primera.getId());
        Reserva nueva = reservar("Nueva", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertEquals(List.of(RECURSO_PRUEBA), service.buscarPorId(nueva.getId()).getRecursosAsignadosIds());
    }

    @Test
    void filtrar_por_fecha_y_categoria_encuentra_la_reserva() {
        Reserva reserva = reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        List<Reserva> filtradas = service.filtrarReservasService(FECHA, categoria.getId());

        assertTrue(filtradas.stream().anyMatch(r -> r.getId().equals(reserva.getId())));
    }

    @Test
    void filtrar_por_otra_fecha_no_encuentra_la_reserva() {
        reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertTrue(service.filtrarReservasService(FECHA.plusDays(1), categoria.getId()).isEmpty());
    }

    @Test
    void filtrar_por_categoria_inexistente_retorna_vacio() {
        reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertTrue(service.filtrarReservasService(FECHA, "CAT-999999").isEmpty());
    }

    @Test
    void repoblar_categorias_asigna_los_objetos_categoria() {
        reservar("Reunion IT", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));
        List<Reserva> reservas = service.obtenerReservasPorFuncionario(FUNCIONARIO_PRUEBA);

        service.repoblarCategoriasService(reservas, List.of(categoria));

        assertEquals(1, reservas.get(0).getCategoriasDeRecursos().size());
        assertEquals(categoria.getId(), reservas.get(0).getCategoriasDeRecursos().get(0).getId());
    }

    @Test
    void calendarizacion_semanal_incluye_solo_las_reservas_de_esa_semana() {
        Reserva estaSemana = reservar("Esta semana", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0));
        Reserva otraSemana = reservar("Otra semana", FECHA.plusWeeks(1), LocalTime.of(9, 0), LocalTime.of(10, 0));

        List<Reserva> semana = new CalendarizacionService().obtenerReservasDeLaSemana(FECHA);

        assertTrue(semana.stream().anyMatch(r -> r.getId().equals(estaSemana.getId())));
        assertTrue(semana.stream().noneMatch(r -> r.getId().equals(otraSemana.getId())));
    }
}