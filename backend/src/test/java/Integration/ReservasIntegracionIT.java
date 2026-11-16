package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.Database;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;
import una.proyecto.service.ReservaService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReservasIntegracionIT {

    // Funcionario que crea la prueba; la llave foranea de reservas lo exige.
    private static final String FUNCIONARIO_PRUEBA = "IT-FUNC-1";
    private static final LocalDate FECHA = LocalDate.now().plusDays(7);

    private ReservaService service;

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();
        ejecutar("insert into usuarios (id_usuario, nombre, clave_hash, rol) values (?, ?, ?, ?)",
                FUNCIONARIO_PRUEBA, "Funcionario de prueba", "sin-clave", "FUNCIONARIO");
        ejecutar("insert into funcionarios (id_usuario, telefono) values (?, ?)",
                FUNCIONARIO_PRUEBA, "00000000");
        service = new ReservaService(new ReservaLogic(new ReservaDatos()));
    }

    @AfterEach
    void tearDown() throws Exception {
        limpiarDatosDePrueba();
    }

    // Borra en orden inverso a las llaves foraneas: reservas, funcionario, usuario.
    private void limpiarDatosDePrueba() throws Exception {
        ejecutar("delete from reserva_recurso where id_reserva in "
                + "(select id_reserva from reservas where id_funcionario = ?)", FUNCIONARIO_PRUEBA);
        ejecutar("delete from reservas where id_funcionario = ?", FUNCIONARIO_PRUEBA);
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

    private Reserva nuevaReserva(String actividad, LocalTime inicio, LocalTime fin) {
        Reserva reserva = new Reserva();
        reserva.setIdFuncionario(FUNCIONARIO_PRUEBA);
        reserva.setActividad(actividad);
        reserva.setFecha(FECHA);
        reserva.setHoraInicio(inicio);
        reserva.setHoraFin(fin);
        return reserva;
    }

    private long contarReservasDePrueba() {
        return service.obtenerTodasReservas().stream()
                .filter(r -> FUNCIONARIO_PRUEBA.equals(r.getIdFuncionario()))
                .count();
    }

    // ---------- crear ----------

    @Test
    void crear_reserva_le_asigna_el_id_generado_por_mysql() {
        Reserva reserva = nuevaReserva("Reunion", LocalTime.of(9, 0), LocalTime.of(10, 0));

        service.save(reserva);

        assertNotNull(reserva.getId());
        assertTrue(reserva.getId().startsWith("RES-"), "Id recibido: " + reserva.getId());
    }

    @Test
    void crear_y_leer_reserva_conserva_todos_sus_datos() {
        Reserva reserva = nuevaReserva("Reunion", LocalTime.of(9, 0), LocalTime.of(10, 30));
        service.save(reserva);

        Reserva leida = service.buscarPorId(reserva.getId());

        assertNotNull(leida);
        assertEquals(reserva.getId(), leida.getId());
        assertEquals(FUNCIONARIO_PRUEBA, leida.getIdFuncionario());
        assertEquals("Reunion", leida.getActividad());
        assertEquals(FECHA, leida.getFecha());
        assertEquals(LocalTime.of(9, 0), leida.getHoraInicio());
        assertEquals(LocalTime.of(10, 30), leida.getHoraFin());
        assertEquals(EstadoReserva.ACTIVA, leida.getEstado());
    }

    @Test
    void dos_reservas_seguidas_reciben_ids_distintos() {
        Reserva primera = nuevaReserva("Primera", LocalTime.of(9, 0), LocalTime.of(10, 0));
        Reserva segunda = nuevaReserva("Segunda", LocalTime.of(11, 0), LocalTime.of(12, 0));

        service.save(primera);
        service.save(segunda);

        assertNotEquals(primera.getId(), segunda.getId());
    }

    @Test
    void crear_reserva_con_funcionario_inexistente_lanza_excepcion() {
        Reserva reserva = nuevaReserva("Reunion", LocalTime.of(9, 0), LocalTime.of(10, 0));
        reserva.setIdFuncionario("IT-NO-EXISTE");

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.save(reserva));

        assertTrue(error.getMessage().contains("no existe"), "Mensaje recibido: " + error.getMessage());
    }

    @Test
    void crear_reserva_con_hora_fin_antes_de_inicio_lanza_excepcion() {
        Reserva reserva = nuevaReserva("Reunion", LocalTime.of(10, 0), LocalTime.of(9, 0));

        RuntimeException error = assertThrows(RuntimeException.class, () -> service.save(reserva));

        assertTrue(error.getMessage().contains("hora de inicio"), "Mensaje recibido: " + error.getMessage());
    }

    // ---------- leer ----------

    @Test
    void obtener_todas_incluye_las_reservas_creadas() {
        service.save(nuevaReserva("Primera", LocalTime.of(9, 0), LocalTime.of(10, 0)));
        service.save(nuevaReserva("Segunda", LocalTime.of(11, 0), LocalTime.of(12, 0)));

        assertEquals(2, contarReservasDePrueba());
    }

    // ---------- cancelar ----------

    @Test
    void cancelar_reserva_la_oculta_pero_conserva_la_fila() throws Exception {
        Reserva reserva = nuevaReserva("Reunion", LocalTime.of(9, 0), LocalTime.of(10, 0));
        service.save(reserva);

        service.delete(reserva.getId());

        assertNull(service.buscarPorId(reserva.getId()));
        assertEquals(0, contarReservasDePrueba());
        assertEquals("CANCELADA", estadoEnBaseDeDatos(reserva.getId()));
    }

    @Test
    void cancelar_reserva_inexistente_lanza_excepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.delete("RES-999999"));
    }

    // Lee el estado directo de la tabla, sin pasar por el DAO (que oculta las canceladas).
    private String estadoEnBaseDeDatos(String idReserva) throws Exception {
        int numero = Integer.parseInt(idReserva.substring("RES-".length()));
        try (PreparedStatement stm = Database.instance()
                .prepareStatement("select estado from reservas where id_reserva = ?")) {
            stm.setInt(1, numero);
            try (ResultSet rs = Database.instance().executeQuery(stm)) {
                return rs.next() ? rs.getString("estado") : null;
            }
        }
    }
}