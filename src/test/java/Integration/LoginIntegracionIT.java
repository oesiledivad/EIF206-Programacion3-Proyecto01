package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.LoginLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.service.AuthService;
import una.proyecto.utils.SessionManager;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: LoginLogic -> AuthService -> UsuarioDatos -> XML.
 */
public class LoginIntegracionIT {

    private static final String XML_TEST = "target/test-it-login.xml";
    private LoginLogic loginLogic;
    private UsuarioDatos usuarioDatos;

    @BeforeEach
    void setUp() {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        usuarioDatos = new UsuarioDatos(XML_TEST);
        loginLogic = new LoginLogic(new AuthService(usuarioDatos));
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
        SessionManager.getInstance().logout();
    }

    @Test
    void loginConAdminPorDefecto_exitoso() {
        var result = loginLogic.validateCredentials("admin", "admin");

        assertTrue(result.isSuccess());
        assertEquals("admin", result.getUsuario().getId());
        assertTrue(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void loginConFuncionarioCreadoEnXML_exitoso() {
        Funcionario f = new Funcionario("111111111", "FUNCIONARIO", "Juan Perez", "88887777");
        f.setPassword("clave123");
        usuarioDatos.addUser(f);

        var result = loginLogic.validateCredentials("111111111", "clave123");

        assertTrue(result.isSuccess());
        assertEquals("Juan Perez", result.getUsuario().getName());
    }

    @Test
    void loginConPasswordIncorrecta_falla() {
        Funcionario f = new Funcionario("111111111", "FUNCIONARIO", "Juan Perez", "88887777");
        f.setPassword("clave123");
        usuarioDatos.addUser(f);

        var result = loginLogic.validateCredentials("111111111", "claveMala");

        assertFalse(result.isSuccess());
        assertEquals("Credenciales incorrectas", result.getMessage());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void loginConUsuarioInexistente_falla() {
        var result = loginLogic.validateCredentials("999999999", "clave123");

        assertFalse(result.isSuccess());
        assertEquals("Credenciales incorrectas", result.getMessage());
    }

    @Test
    void loginConFormatoIdInvalido_noConsultaXML() {
        var result = loginLogic.validateCredentials("abc", "clave123");

        assertFalse(result.isSuccess());
        assertEquals("Formato de ID inválido", result.getMessage());
    }
}