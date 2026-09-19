package Unit;

import una.proyecto.logic.LoginLogic;
import una.proyecto.model.Usuario;
import una.proyecto.service.AuthService;
import una.proyecto.utils.SessionManager;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginLogicTest {

    @Mock
    private AuthService authService;

    private LoginLogic loginLogic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginLogic = new LoginLogic(authService);
        SessionManager.getInstance().logout(); // limpiar estado singleton entre tests
    }

    @Test
    void userIdVacio_devuelveError() {
        var result = loginLogic.validateCredentials("", "123456");
        assertFalse(result.isSuccess());
        assertEquals("El ID de usuario es requerido", result.getMessage());
        verifyNoInteractions(authService);
    }

    @Test
    void passwordVacio_devuelveError() {
        var result = loginLogic.validateCredentials("123456789", "");
        assertFalse(result.isSuccess());
        assertEquals("La contraseña es requerida", result.getMessage());
    }

    @Test
    void formatoIdInvalido_devuelveError() {
        var result = loginLogic.validateCredentials("12345", "123456"); // no son 9 dígitos ni "admin"
        assertFalse(result.isSuccess());
        assertEquals("Formato de ID inválido", result.getMessage());
    }

    @Test
    void passwordMenorA6Caracteres_devuelveError() {
        var result = loginLogic.validateCredentials("123456789", "123");
        assertFalse(result.isSuccess());
        assertEquals("La contraseña debe de ser mayor a 6 carácteres", result.getMessage());
    }

    @Test
    void credencialesIncorrectas_authServiceRetornaNull_devuelveError() {
        when(authService.authenticate("123456789", "123456")).thenReturn(null);

        var result = loginLogic.validateCredentials("123456789", "123456");

        assertFalse(result.isSuccess());
        assertEquals("Credenciales incorrectas", result.getMessage());
    }

    @Test
    void loginExitoso_funcionario_iniciaSesion() {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn("123456789");
        when(usuario.getName()).thenReturn("Juan Perez");
        when(usuario.getRole()).thenReturn("FUNCIONARIO");

        when(authService.authenticate("123456789", "123456")).thenReturn(usuario);

        var result = loginLogic.validateCredentials("123456789", "123456");

        assertTrue(result.isSuccess());
        assertEquals(usuario, result.getUsuario());
        assertEquals("Login exitoso", result.getMessage());
        assertTrue(SessionManager.getInstance().isLoggedIn()); // ajustar al método real
    }

    @Test
    void loginExitoso_admin_credencialesEspeciales() {
        Usuario admin = mock(Usuario.class);
        when(admin.getId()).thenReturn("admin");
        when(admin.getName()).thenReturn("Administrador");
        when(admin.getRole()).thenReturn("ADMIN");

        when(authService.authenticate("admin", "admin")).thenReturn(admin);

        var result = loginLogic.validateCredentials("admin", "admin");

        assertTrue(result.isSuccess());
    }

    @Test
    void trim_seAplicaAntesDeAutenticar() {
        Usuario usuario = mock(Usuario.class);
        when(authService.authenticate("123456789", "123456")).thenReturn(usuario);

        loginLogic.validateCredentials("  123456789  ", "  123456  ");

        verify(authService).authenticate("123456789", "123456");
    }
}