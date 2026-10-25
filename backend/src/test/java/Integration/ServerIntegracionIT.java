package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.model.Usuario;
import una.proyecto.protocolo.Protocol;
import una.proyecto.server.Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServerIntegracionIT {
    private static final String ID = "IT-FUNC-SRV";
    private static final String OTRO = "IT-FUNC-OTRO";
    private static final String ADMIN = "IT-ADMIN-SRV";
    private static final String CLAVE = "clavePrueba";
    private static Server server;
    private static Thread serverThread;
    private Cliente cliente;

    @BeforeAll static void iniciarServidor() throws Exception {
        server = new Server(0);
        serverThread = new Thread(server::iniciar, "Servidor-Pruebas");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterAll static void cerrarServidor() throws Exception {
        server.close();
        serverThread.join(5000);
        assertFalse(serverThread.isAlive());
    }

    @BeforeEach void setup() throws Exception {
        limpiar();
        UsuarioDatos datos = new UsuarioDatos();
        for (String id : new String[]{ID, OTRO}) {
            Funcionario user = new Funcionario(id, "FUNCIONARIO", "Funcionario de prueba", "00000000");
            user.setPassword(CLAVE);
            assertTrue(datos.addUser(user));
        }
        Administrador admin = new Administrador(ADMIN, "ADMIN", "Administrador de prueba");
        admin.setPassword(CLAVE);
        assertTrue(datos.addUser(admin));
        cliente = new Cliente();
    }

    @AfterEach void terminar() throws Exception {
        if (cliente != null) cliente.close();
        limpiar();
    }

    private void limpiar() throws Exception {
        try (Connection connection = Database.instance().openConnection()) {
            String[] statements = {
                "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario IN (?, ?, ?))",
                "DELETE FROM reservas WHERE id_funcionario IN (?, ?, ?)",
                "DELETE FROM funcionarios WHERE id_usuario IN (?, ?, ?)",
                "DELETE FROM usuarios WHERE id_usuario IN (?, ?, ?)"
            };
            for (String sql : statements) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, ID);
                    statement.setString(2, OTRO);
                    statement.setString(3, ADMIN);
                    statement.executeUpdate();
                }
            }
        }
    }

    @Test void loginRetornaUsuarioSinCredenciales() throws Exception {
        Usuario user = (Usuario) cliente.login(ID, CLAVE).objeto();
        assertEquals(ID, user.getId());
        assertNull(user.getPassword());
        assertTrue(user.isActive());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.RESERVA_LISTAR).codigo());
    }

    @Test void loginIncorrectoNoDaAccesoYPermiteReintentar() throws Exception {
        assertEquals(Protocol.STATUS_ERROR, cliente.login(ID, "incorrecta").codigo());
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_LISTAR).codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(ID, CLAVE).codigo());
    }

    @Test void sinLoginConsumeLaPeticionYPermiteAutenticarseDespues() throws Exception {
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_CREAR, reserva(ID)).codigo());
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.AUTH_CAMBIAR_CLAVE, CLAVE, "nuevaClave").codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(ID, CLAVE).codigo());
    }

    @Test void logoutRevocaSoloLaSesionDeEsaConexion() throws Exception {
        cliente.login(ID, CLAVE);
        try (Cliente otro = new Cliente()) {
            otro.login(OTRO, CLAVE);
            assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.AUTH_LOGOUT).codigo());
            assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_LISTAR).codigo());
            assertEquals(Protocol.STATUS_NO_ERROR, otro.enviar(Protocol.RESERVA_LISTAR).codigo());
        }
    }

    @Test void cambiaLaClaveDelUsuarioDeLaSesion() throws Exception {
        cliente.login(ID, CLAVE);
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.AUTH_CAMBIAR_CLAVE, "incorrecta", "nuevaClave").codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.AUTH_CAMBIAR_CLAVE, CLAVE, "nuevaClave").codigo());
        cliente.enviar(Protocol.AUTH_LOGOUT);
        assertEquals(Protocol.STATUS_ERROR, cliente.login(ID, CLAVE).codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(ID, "nuevaClave").codigo());
    }

    @Test void bajaLogicaRevocaUnaSesionYaAbierta() throws Exception {
        cliente.login(ID, CLAVE);
        new UsuarioDatos().deleteUser(ID);
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_LISTAR).codigo());
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.AUTH_CAMBIAR_CLAVE, CLAVE, "nuevaClave").codigo());
        assertEquals(Protocol.STATUS_ERROR, cliente.login(ID, CLAVE).codigo());
    }

    @Test void loginFallidoTambienRevocaLaSesionAnterior() throws Exception {
        cliente.login(ID, CLAVE);
        cliente.login(OTRO, "incorrecta");
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_LISTAR).codigo());
    }

    @Test void creaBuscaYListaSoloLasReservasPropias() throws Exception {
        cliente.login(ID, CLAVE);
        Mensaje response = cliente.enviar(Protocol.RESERVA_CREAR, reserva(ID));
        assertEquals(Protocol.STATUS_NO_ERROR, response.codigo(), String.valueOf(response.objeto()));
        Reserva created = (Reserva) response.objeto();
        assertNotNull(created.getId());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.RESERVA_BUSCAR, created.getId()).codigo());
        try (Cliente otro = new Cliente()) {
            otro.login(OTRO, CLAVE);
            assertTrue(((List<?>) otro.enviar(Protocol.RESERVA_LISTAR).objeto()).isEmpty());
            assertEquals(Protocol.STATUS_ERROR, otro.enviar(Protocol.RESERVA_BUSCAR, created.getId()).codigo());
            assertEquals(Protocol.STATUS_ERROR, otro.enviar(Protocol.RESERVA_CANCELAR, created.getId()).codigo());
        }
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.RESERVA_CANCELAR, created.getId()).codigo());
    }

    @Test void noPermiteSuplantarFuncionarioNiCrearComoAdministrador() throws Exception {
        cliente.login(ID, CLAVE);
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_CREAR, reserva(OTRO)).codigo());
        cliente.login(ADMIN, CLAVE);
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_CREAR, reserva(ADMIN)).codigo());
    }

    @Test void noPermiteCancelarReservasPasadas() throws Exception {
        cliente.login(ID, CLAVE);
        Reserva created = (Reserva) cliente.enviar(Protocol.RESERVA_CREAR, reserva(ID)).objeto();
        try (Connection connection = Database.instance().openConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE reservas SET fecha = ? WHERE id_funcionario = ?")) {
            statement.setObject(1, LocalDate.now().minusDays(1));
            statement.setString(2, ID);
            statement.executeUpdate();
        }
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.RESERVA_CANCELAR, created.getId()).codigo());
    }

    @Test void operacionDesconocidaNoBloqueaLaConexion() throws Exception {
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(12345).codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(ID, CLAVE).codigo());
    }

    @Test void notificaSoloAOtrosUsuariosAutenticadosYCierraAlertasAlSalir() throws Exception {
        cliente.login(ID, CLAVE);
        try (Cliente otro = new Cliente(); Cliente mismaCuenta = new Cliente()) {
            otro.login(OTRO, CLAVE);
            mismaCuenta.login(ID, CLAVE);
            try (CanalAsync destino = new CanalAsync(otro.sid); CanalAsync propio = new CanalAsync(mismaCuenta.sid)) {
                cliente.enviar(Protocol.RESERVA_CREAR, reserva(ID));
                assertEquals(Protocol.NOTIFICACION_RESERVA_CREADA, destino.input.readInt());
                assertInstanceOf(Reserva.class, destino.input.readObject());
                assertThrows(SocketTimeoutException.class, propio.input::readInt);
                otro.enviar(Protocol.AUTH_LOGOUT);
                assertThrows(IOException.class, destino.input::readInt);
            }
        }
    }

    @Test void canalAsyncRechazaUnaConexionSinLogin() throws Exception {
        assertThrows(IOException.class, () -> {
            try (CanalAsync ignored = new CanalAsync(cliente.sid)) { fail("Debió rechazar el canal"); }
        });
    }

    private Reserva reserva(String id) {
        Reserva r = new Reserva();
        r.setIdFuncionario(id);
        r.setActividad("Prueba de servidor");
        r.setFecha(LocalDate.now().plusDays(7));
        r.setHoraInicio(LocalTime.of(9, 0));
        r.setHoraFin(LocalTime.of(10, 0));
        return r;
    }

    private record Mensaje(int codigo, Object objeto) { }

    private static class Cliente implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;
        final String sid;
        Cliente() throws Exception {
            socket = new Socket("localhost", server.getPuerto());
            socket.setSoTimeout(5000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            output.writeInt(Protocol.SYNC);
            output.flush();
            sid = (String) input.readObject();
        }
        Mensaje login(String id, String password) throws Exception {
            return enviar(Protocol.AUTH_LOGIN, id, password);
        }
        Mensaje enviar(int operation, Object... params) throws Exception {
            output.writeInt(operation);
            for (Object param : params) output.writeObject(param);
            output.flush();
            output.reset();
            return new Mensaje(input.readInt(), input.readObject());
        }
        public void close() throws IOException { socket.close(); }
    }

    private static class CanalAsync implements AutoCloseable {
        final Socket socket;
        final ObjectInputStream input;
        CanalAsync(String sid) throws IOException {
            socket = new Socket("localhost", server.getPuerto());
            socket.setSoTimeout(1500);
            try {
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.flush();
                input = new ObjectInputStream(socket.getInputStream());
                output.writeInt(Protocol.ASYNC);
                output.writeObject(sid);
                output.flush();
                if (input.readInt() != Protocol.STATUS_NO_ERROR) throw new IOException("Canal rechazado");
            } catch (IOException e) {
                socket.close();
                throw e;
            }
        }
        public void close() throws IOException { socket.close(); }
    }
}
