package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Usuario;
import una.proyecto.service.AuthService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UsuarioDatos usuarioDatos;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(usuarioDatos);
    }

    @Test
    void usuarioExiste_credencialesCorrectas_retornaUsuario() {
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn("123456789");
        when(u.getPassword()).thenReturn("clave123");
        when(usuarioDatos.loadUsers()).thenReturn(List.of(u));

        Usuario result = authService.authenticate("123456789", "clave123");

        assertEquals(u, result);
    }

    @Test
    void idCaseInsensitive_encuentraUsuario() {
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn("ADMIN");
        when(u.getPassword()).thenReturn("admin");
        when(usuarioDatos.loadUsers()).thenReturn(List.of(u));

        Usuario result = authService.authenticate("admin", "admin");

        assertEquals(u, result);
    }

    @Test
    void passwordIncorrecta_retornaNull() {
        Usuario u = mock(Usuario.class);
        when(u.getId()).thenReturn("123456789");
        when(u.getPassword()).thenReturn("clave123");
        when(usuarioDatos.loadUsers()).thenReturn(List.of(u));

        assertNull(authService.authenticate("123456789", "otraClave"));
    }

    @Test
    void usuarioNoExiste_retornaNull() {
        when(usuarioDatos.loadUsers()).thenReturn(List.of());

        assertNull(authService.authenticate("999999999", "clave123"));
    }

    @Test
    void idONullNull_retornaNullSinConsultarDatos() {
        assertNull(authService.authenticate(null, "clave123"));
        assertNull(authService.authenticate("123456789", null));
        verifyNoInteractions(usuarioDatos);
    }
}