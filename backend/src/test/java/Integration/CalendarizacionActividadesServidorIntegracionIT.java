package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.Database;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;
import una.proyecto.server.Server;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de Calendarización por sockets:
 * Cliente → Server → Worker → ReservaService / CalendarizacionService → MySQL.
 * Datos propios con prefijo IT-AGENDA (categorías y recursos) e IT-FUNC-AGD (funcionarios).
 */
class CalendarizacionServidorIntegracionIT {

    private static final String FUNCIONARIO = "IT-FUNC-AGD";
    private static final String OTRO = "IT-FUNC-AGD2";
    private static final String NOMBRE = "Ana Agenda";
    private static final String CLAVE = "clavePrueba";
    private static final String PREFIJO = "IT-AGENDA";
    private static final String RECURSO_SALA = PREFIJO + "-1";
    private static final String RECURSO_LAPTOP = PREFIJO + "-2";
    private static final LocalDate FECHA = LocalDate.now().plusDays(7);

    private static Server server;
    private static Thread serverThread;

    private Cliente cliente;
    private Categoria sala;
    private Categoria laptop;
    private Reserva reservaSala;
    private Reserva reservaLaptop;
    private Reserva reservaOtraSemana;

    @BeforeAll
    static void iniciarServidor() throws Exception {
        server = new Server(0);
        serverThread = new Thread(server::iniciar, "Servidor-Pruebas-Calendarizacion");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterAll
    static void cerrarServidor() throws Exception {
        server.close();
        serverThread.join(5000);
    }

    @BeforeEach
    void setUp() throws Exception {
        limpiar();

        UsuarioDatos usuarios = new UsuarioDatos();
        for (String[] datos : new String[][]{{FUNCIONARIO, NOMBRE}, {OTRO, "Otro Funcionario"}}) {
            Funcionario f = new Funcionario(datos[0], "FUNCIONARIO", datos[1], "00000000");
            f.setPassword(CLAVE);
            assertTrue(usuarios.addUser(f));
        }

        sala = new Categoria(null, PREFIJO + " Sala");
        laptop = new Categoria(null, PREFIJO + " Laptop");
        CategoriaDatos categorias = new CategoriaDatos();
        categorias.crear(sala);
        categorias.crear(laptop);

        RecursoDatos recursos = new RecursoDatos();
        recursos.crear(new Recurso(RECURSO_SALA, sala.getId(), "Sala de prueba"));
        recursos.crear(new Recurso(RECURSO_LAPTOP, laptop.getId(), "Laptop de prueba"));

        // Reservas creadas directo en el backend: aquí solo se prueba CONSULTARLAS por socket.
        ReservaService reservas = new ReservaService(new ReservaLogic(new ReservaDatos(),
                new RecursoService(new RecursoLogic(new RecursoDatos()))));
        reservaSala = reservar(reservas, "Reunion sala", FECHA, LocalTime.of(9, 0), LocalTime.of(10, 0), sala);
        reservaLaptop = reservar(reservas, "Clase laptop", FECHA, LocalTime.of(11, 0), LocalTime.of(12, 0), laptop);
        reservaOtraSemana = reservar(reservas, "Sala semana siguiente", FECHA.plusWeeks(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0), sala);

        cliente = new Cliente();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (cliente != null) cliente.close();
        limpiar();
    }

    // Hijos primero: reserva_recurso -> reservas -> recursos -> categorias -> funcionarios -> usuarios.
    private void limpiar() throws Exception {
        try (Connection cnx = Database.instance().openConnection()) {
            ejecutar(cnx, "DELETE FROM reserva_recurso WHERE id_reserva IN "
                    + "(SELECT id_reserva FROM reservas WHERE id_funcionario IN (?, ?))", FUNCIONARIO, OTRO);
            ejecutar(cnx, "DELETE FROM reservas WHERE id_funcionario IN (?, ?)", FUNCIONARIO, OTRO);
            ejecutar(cnx, "DELETE FROM recursos WHERE id_recurso LIKE ?", PREFIJO + "-%");
            ejecutar(cnx, "DELETE FROM categorias WHERE descripcion LIKE ?", PREFIJO + "%");
            ejecutar(cnx, "DELETE FROM funcionarios WHERE id_usuario IN (?, ?)", FUNCIONARIO, OTRO);
            ejecutar(cnx, "DELETE FROM usuarios WHERE id_usuario IN (?, ?)", FUNCIONARIO, OTRO);
        }
    }

    private void ejecutar(Connection cnx, String sql, String... parametros) throws Exception {
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) stm.setString(i + 1, parametros[i]);
            stm.executeUpdate();
        }
    }

    private Reserva reservar(ReservaService service, String actividad, LocalDate fecha,
                             LocalTime inicio, LocalTime fin, Categoria categoria) {
        Reserva r = service.crearReserva(actividad, fecha, inicio, fin, FUNCIONARIO,
                List.of(categoria), EstadoReserva.ACTIVA, null);
        service.save(r);
        return r;
    }

    @SuppressWarnings("unchecked")
    private List<Reserva> reservasDelDia(LocalDate fecha, String idCategoria) throws Exception {
        Mensaje m = cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_DIA, fecha, idCategoria);
        assertEquals(Protocol.STATUS_NO_ERROR, m.codigo(), String.valueOf(m.objeto()));
        return (List<Reserva>) m.objeto();
    }

    @SuppressWarnings("unchecked")
    private List<Reserva> reservasDeLaSemana(LocalDate fecha) throws Exception {
        Mensaje m = cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_SEMANA, fecha);
        assertEquals(Protocol.STATUS_NO_ERROR, m.codigo(), String.valueOf(m.objeto()));
        return (List<Reserva>) m.objeto();
    }

    private static boolean contiene(List<Reserva> reservas, Reserva buscada) {
        return reservas.stream().anyMatch(r -> r.getId().equals(buscada.getId()));
    }

    private static Reserva buscar(List<Reserva> reservas, Reserva buscada) {
        return reservas.stream().filter(r -> r.getId().equals(buscada.getId())).findFirst().orElseThrow();
    }

    // ---------- matriz por fecha y categoría ----------

    @Test
    @SuppressWarnings("unchecked")
    void recursos_por_categoria_devuelve_solo_los_de_esa_categoria() throws Exception {
        cliente.login(FUNCIONARIO, CLAVE);

        List<Recurso> recursos = (List<Recurso>) cliente
                .enviar(Protocol.CALENDARIZACION_RECURSOS_CATEGORIA, sala.getId()).objeto();

        assertEquals(List.of(RECURSO_SALA), recursos.stream().map(Recurso::getId).toList());
    }

    @Test
    void reservas_del_dia_traen_sus_recursos_y_su_encargado() throws Exception {
        cliente.login(FUNCIONARIO, CLAVE);

        Reserva leida = buscar(reservasDelDia(FECHA, sala.getId()), reservaSala);

        assertEquals(List.of(RECURSO_SALA), leida.getRecursosAsignadosIds());
        assertNotNull(leida.getFuncionarioReserva(), "El backend debe llenar el encargado");
        assertEquals(NOMBRE, leida.getFuncionarioReserva().getName());
        assertNull(leida.getFuncionarioReserva().getPassword(), "La clave no debe viajar por el socket");
    }

    @Test
    void reservas_del_dia_filtran_por_categoria_y_por_fecha() throws Exception {
        cliente.login(FUNCIONARIO, CLAVE);

        List<Reserva> deSala = reservasDelDia(FECHA, sala.getId());
        assertTrue(contiene(deSala, reservaSala));
        assertFalse(contiene(deSala, reservaLaptop), "La reserva de laptop es de otra categoría");

        assertFalse(contiene(reservasDelDia(FECHA.plusDays(1), sala.getId()), reservaSala),
                "Otro día no debe incluirla");
    }

    // ---------- vista semanal de actividades ----------

    @Test
    void semana_incluye_solo_reservas_de_esa_semana_con_encargado() throws Exception {
        cliente.login(FUNCIONARIO, CLAVE);

        List<Reserva> semana = reservasDeLaSemana(FECHA);

        assertTrue(contiene(semana, reservaSala));
        assertTrue(contiene(semana, reservaLaptop));
        assertFalse(contiene(semana, reservaOtraSemana));
        assertEquals(NOMBRE, buscar(semana, reservaLaptop).getFuncionarioReserva().getName());
    }

    @Test
    void otro_funcionario_tambien_ve_las_reservas_en_el_calendario() throws Exception {
        cliente.login(OTRO, CLAVE);

        assertTrue(contiene(reservasDeLaSemana(FECHA), reservaSala));
        assertTrue(contiene(reservasDelDia(FECHA, sala.getId()), reservaSala));
    }

    // ---------- sesión y errores ----------

    @Test
    void sin_login_da_error_y_la_conexion_sigue_sirviendo() throws Exception {
        // RESERVAS_DIA envía DOS parámetros: el Worker debe leer ambos antes de rechazar.
        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_DIA, FECHA, sala.getId()).codigo());
        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_SEMANA, FECHA).codigo());

        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(FUNCIONARIO, CLAVE).codigo());
        assertTrue(contiene(reservasDeLaSemana(FECHA), reservaSala));
    }

    @Test
    void parametros_nulos_responden_error_con_mensaje() throws Exception {
        cliente.login(FUNCIONARIO, CLAVE);

        Mensaje sinCategoria = cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_DIA, FECHA, null);
        Mensaje sinFecha = cliente.enviar(Protocol.CALENDARIZACION_RESERVAS_SEMANA, (Object) null);

        assertEquals(Protocol.STATUS_ERROR, sinCategoria.codigo());
        assertTrue(String.valueOf(sinCategoria.objeto()).contains("categoría"), "Mensaje: " + sinCategoria.objeto());
        assertEquals(Protocol.STATUS_ERROR, sinFecha.codigo());
        assertTrue(String.valueOf(sinFecha.objeto()).contains("fecha"), "Mensaje: " + sinFecha.objeto());
    }

    // ================= Cliente mínimo del protocolo =================

    private record Mensaje(int codigo, Object objeto) { }

    private static class Cliente implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;

        Cliente() throws Exception {
            socket = new Socket("localhost", server.getPuerto());
            socket.setSoTimeout(5000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            output.writeInt(Protocol.SYNC);
            output.flush();
            input.readObject(); // saludo: sid de la sesión
        }

        Mensaje login(String id, String clave) throws Exception {
            return enviar(Protocol.AUTH_LOGIN, id, clave);
        }

        Mensaje enviar(int operacion, Object... parametros) throws Exception {
            output.writeInt(operacion);
            for (Object p : parametros) output.writeObject(p);
            output.flush();
            output.reset();
            return new Mensaje(input.readInt(), input.readObject());
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}