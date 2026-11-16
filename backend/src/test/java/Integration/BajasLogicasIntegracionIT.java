package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.Database;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integración de datos: reglas de las bajas lógicas contra MySQL.
 * - Una categoría con recursos activos no se puede eliminar.
 * - La baja de un recurso o de un funcionario cancela sus reservas futuras y conserva las pasadas.
 * - Crear otra vez un recurso dado de baja lo reactiva.
 * - El historial muestra el nombre del encargado aunque esté dado de baja.
 * Datos propios: categorías "IT-BAJA...", recursos "IT-BAJA-..." y funcionario IT-FUNC-BAJA.
 */
class BajasLogicasIntegracionIT {

    private static final String FUNCIONARIO = "IT-FUNC-BAJA";
    private static final String NOMBRE = "Marta Mata";
    private static final String PREFIJO = "IT-BAJA";
    private static final String RECURSO = PREFIJO + "-1";

    private Categoria categoria;
    private RecursoService recursoService;
    private CategoriaService categoriaService;
    private UsuarioDatos usuarios;
    private ReservaDatos reservas;

    @BeforeEach
    void setUp() throws Exception {
        limpiar();
        usuarios = new UsuarioDatos();
        Funcionario f = new Funcionario(FUNCIONARIO, "FUNCIONARIO", NOMBRE, "00000000");
        f.setPassword("clavePrueba");
        assertTrue(usuarios.addUser(f));

        categoria = new Categoria(null, PREFIJO + " Sala");
        new CategoriaDatos().crear(categoria);
        new RecursoDatos().crear(new Recurso(RECURSO, categoria.getId(), "Sala de prueba"));

        recursoService = new RecursoService(new RecursoLogic(new RecursoDatos()));
        categoriaService = new CategoriaService(new CategoriaLogic(new CategoriaDatos(), recursoService));
        reservas = new ReservaDatos();
    }

    @AfterEach
    void tearDown() throws Exception {
        limpiar();
    }

    // Hijos primero: reserva_recurso -> reservas -> recursos -> categorias -> funcionarios -> usuarios.
    private void limpiar() throws Exception {
        try (Connection cnx = Database.instance().openConnection()) {
            ejecutar(cnx, "DELETE FROM reserva_recurso WHERE id_reserva IN "
                    + "(SELECT id_reserva FROM reservas WHERE id_funcionario = ?)", FUNCIONARIO);
            ejecutar(cnx, "DELETE FROM reservas WHERE id_funcionario = ?", FUNCIONARIO);
            ejecutar(cnx, "DELETE FROM recursos WHERE id_recurso LIKE ?", PREFIJO + "-%");
            ejecutar(cnx, "DELETE FROM categorias WHERE descripcion LIKE ?", PREFIJO + "%");
            ejecutar(cnx, "DELETE FROM funcionarios WHERE id_usuario = ?", FUNCIONARIO);
            ejecutar(cnx, "DELETE FROM usuarios WHERE id_usuario = ?", FUNCIONARIO);
        }
    }

    private void ejecutar(Connection cnx, String sql, String parametro) throws Exception {
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            stm.setString(1, parametro);
            stm.executeUpdate();
        }
    }

    // Se guarda directo con el DAO para poder crear también reservas en el pasado.
    private Reserva reservar(String actividad, LocalDate fecha) {
        Reserva r = new Reserva(actividad, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0),
                FUNCIONARIO, null, EstadoReserva.ACTIVA, null);
        r.setRecursosAsignadosIds(List.of(RECURSO));
        reservas.crear(r);
        return r;
    }

    private String estadoEnBaseDeDatos(Reserva reserva) throws Exception {
        int numero = Integer.parseInt(reserva.getId().substring("RES-".length()));
        try (Connection cnx = Database.instance().openConnection();
             PreparedStatement stm = cnx.prepareStatement("SELECT estado FROM reservas WHERE id_reserva = ?")) {
            stm.setInt(1, numero);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // ---------- categorías ----------

    @Test
    void categoria_con_recursos_activos_no_se_puede_eliminar() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> categoriaService.delete(categoria.getId()));

        assertTrue(error.getMessage().contains("recurso"), "Mensaje: " + error.getMessage());
        assertTrue(categoriaService.obtenerTodas().stream().anyMatch(c -> c.getId().equals(categoria.getId())));
    }

    @Test
    void categoria_se_puede_eliminar_despues_de_eliminar_sus_recursos() {
        recursoService.delete(RECURSO);

        categoriaService.delete(categoria.getId());

        assertTrue(categoriaService.obtenerTodas().stream().noneMatch(c -> c.getId().equals(categoria.getId())));
    }

    // ---------- recursos ----------

    @Test
    void baja_de_recurso_cancela_sus_reservas_futuras_y_conserva_las_pasadas() throws Exception {
        Reserva pasada = reservar("Pasada", LocalDate.now().minusDays(3));
        Reserva futura = reservar("Futura", LocalDate.now().plusDays(3));

        recursoService.delete(RECURSO);

        assertEquals("CANCELADA", estadoEnBaseDeDatos(futura));
        assertEquals("ACTIVA", estadoEnBaseDeDatos(pasada));
    }

    @Test
    void crear_de_nuevo_un_recurso_dado_de_baja_lo_reactiva() {
        recursoService.delete(RECURSO);

        recursoService.save(new Recurso(RECURSO, categoria.getId(), "Sala renovada"));

        Recurso reactivado = recursoService.recurPorId(RECURSO);
        assertNotNull(reactivado);
        assertEquals("Sala renovada", reactivado.getDescripcion());
    }

    @Test
    void crear_un_recurso_activo_repetido_sigue_fallando() {
        assertThrows(IllegalArgumentException.class,
                () -> recursoService.save(new Recurso(RECURSO, categoria.getId(), "Otra sala")));
    }

    // ---------- funcionarios ----------

    @Test
    void baja_de_funcionario_cancela_sus_reservas_futuras_y_conserva_las_pasadas() throws Exception {
        Reserva pasada = reservar("Pasada", LocalDate.now().minusDays(3));
        Reserva futura = reservar("Futura", LocalDate.now().plusDays(3));

        assertTrue(usuarios.deleteUser(FUNCIONARIO));

        assertEquals("CANCELADA", estadoEnBaseDeDatos(futura));
        assertEquals("ACTIVA", estadoEnBaseDeDatos(pasada));
    }

    @Test
    void historial_muestra_el_nombre_del_encargado_dado_de_baja() {
        Reserva pasada = reservar("Pasada", LocalDate.now().minusDays(3));
        usuarios.deleteUser(FUNCIONARIO);

        Reserva leida = reservas.leerPorId(pasada.getId());

        assertNotNull(leida.getFuncionarioReserva());
        assertEquals(NOMBRE, leida.getFuncionarioReserva().getName());
        assertEquals("FINALIZADA", leida.getEstadoActual());
    }
}
