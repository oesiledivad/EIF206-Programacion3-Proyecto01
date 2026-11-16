package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Administrador;
import una.proyecto.presentation.logic.LoginLogic;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.server.Server;
import una.proyecto.utils.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.*;

class LoginIntegracionIT {
    private static final String ID = "992000001";
    private UsuarioDatos datos;
    private LoginLogic logic;
    private ServiceProxy proxy;
    private static Server server;
    private static Thread serverThread;

    @BeforeAll static void iniciarServidor() throws Exception {
        server = new Server(0);
        serverThread = new Thread(server::iniciar, "Login-Servidor-Pruebas");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterAll static void detenerServidor() throws Exception {
        server.close();
        serverThread.join(5000);
        assertFalse(serverThread.isAlive());
    }

    @BeforeEach void setup() throws Exception {
        limpiar();
        datos = new UsuarioDatos();
        new FuncionarioLogic(datos).crear(new Funcionario(ID, "FUNCIONARIO", "Juan Perez", "88887777"));
        proxy = new ServiceProxy("127.0.0.1", server.getPuerto());
        logic = new LoginLogic(proxy);
    }

    @AfterEach void limpiar() throws Exception {
        if (proxy != null) proxy.close();
        SessionManager.getInstance().logout();
        try (Connection connection = Database.instance().openConnection()) {
            for (String table : new String[]{"funcionarios", "usuarios"}) {
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM " + table + " WHERE id_usuario = ?")) {
                    statement.setString(1, ID);
                    statement.executeUpdate();
                }
            }
        }
    }

    @Test void loginConClaveInicialCargaSesionSinCredenciales() {
        var result = logic.validateCredentials(ID, ID);
        assertTrue(result.isSuccess());
        assertEquals("Juan Perez", result.getUsuario().getName());
        assertNull(result.getUsuario().getPassword());
        assertEquals(ID, SessionManager.getInstance().getId());
    }

    @Test void claveIncorrectaNoIniciaSesion() {
        assertFalse(logic.validateCredentials(ID, "incorrecta").isSuccess());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test void bajaLogicaImpideLogin() {
        datos.deleteUser(ID);
        assertFalse(logic.validateCredentials(ID, ID).isSuccess());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test void claveConEspaciosSeVerificaSinRecortarla() {
        assertTrue(logic.validateCredentials(ID, ID).isSuccess());
        proxy.changePassword(ID, " clave nueva ");
        proxy.logout();
        assertTrue(logic.validateCredentials(ID, " clave nueva ").isSuccess());
    }

    @Test void cargaDashboardDesdeLaSesionRemota() {
        assertTrue(logic.validateCredentials(ID, ID).isSuccess());
        var panel = proxy.dashboard();
        assertNotNull(panel.reservas());
        assertTrue(panel.reservas().stream().allMatch(r -> ID.equals(r.getIdFuncionario())));
        assertTrue(panel.totalRecursos() >= 0);
        assertEquals(0, panel.totalFuncionarios());
    }

    @Test void logoutRevocaLaSesionYPermiteVolverAIngresar() {
        assertTrue(logic.validateCredentials(ID, ID).isSuccess());
        proxy.logout();
        assertThrows(ServiceProxy.RemoteException.class, proxy::dashboard);
        assertTrue(logic.validateCredentials(ID, ID).isSuccess());
    }

    @Test void informaServidorNoDisponibleSinAutenticarLocalmente() throws Exception {
        int port;
        try (java.net.ServerSocket reservation = new java.net.ServerSocket(0)) { port = reservation.getLocalPort(); }
        try (ServiceProxy unavailable = new ServiceProxy("127.0.0.1", port)) {
            var result = new LoginLogic(unavailable).validateCredentials(ID, ID);
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("backend"));
            assertFalse(SessionManager.getInstance().isLoggedIn());
        }
    }

    @Test void administradorEntraYCargaElPanelRemoto() throws Exception {
        limpiar();
        Administrador admin = new Administrador(ID, "ADMIN", "Administrador de prueba");
        admin.setPassword(ID);
        assertTrue(datos.addUser(admin));
        assertTrue(logic.validateCredentials(ID, ID).isSuccess());
        assertTrue(SessionManager.getInstance().isAdmin());
        assertNotNull(proxy.dashboard().reservas());
    }

    @Test void formatoInvalidoNoIniciaSesion() {
        assertFalse(logic.validateCredentials("abc", ID).isSuccess());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }
}
