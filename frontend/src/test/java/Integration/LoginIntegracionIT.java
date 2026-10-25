package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.presentation.logic.LoginLogic;
import una.proyecto.service.AuthService;
import una.proyecto.utils.SessionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.*;

class LoginIntegracionIT {
    private static final String ID = "992000001";
    private UsuarioDatos datos;
    private LoginLogic logic;

    @BeforeEach void setup() throws Exception {
        limpiar();
        datos = new UsuarioDatos();
        new FuncionarioLogic(datos).crear(new Funcionario(ID, "FUNCIONARIO", "Juan Perez", "88887777"));
        logic = new LoginLogic(new AuthService(datos));
    }

    @AfterEach void limpiar() throws Exception {
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
        new UsuarioLogic(datos).cambiarContrasena(ID, ID, " clave nueva ");
        assertTrue(logic.validateCredentials(ID, " clave nueva ").isSuccess());
    }

    @Test void formatoInvalidoNoIniciaSesion() {
        assertFalse(logic.validateCredentials("abc", ID).isSuccess());
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }
}
