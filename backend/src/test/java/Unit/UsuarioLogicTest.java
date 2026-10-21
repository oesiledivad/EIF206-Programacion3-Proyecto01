package Unit;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioLogicTest {

    @Mock
    private UsuarioDatos usuarioDatos;

    private UsuarioLogic usuarioLogic;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usuarioLogic = new UsuarioLogic(usuarioDatos);
    }

    private Funcionario crearUsuario(String id, String password) {
        Funcionario u = new Funcionario(id, "FUNCIONARIO", "Nombre Test");
        u.setPassword(password);
        return u;
    }

    // ---------- cambiarContrasena ----------

    @Test
    void cambiarContrasena_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("123456789", "actual", "nuevaClave"));

        assertEquals("El usuario no existe.", ex.getMessage());
        verify(usuarioDatos, never()).saveUsers(any());
    }

    @Test
    void cambiarContrasena_contrasenaActualIncorrecta_lanzaExcepcion() {
        Funcionario u = crearUsuario("123456789", "claveReal");
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>(List.of(u)));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("123456789", "claveIncorrecta", "nuevaClave"));

        assertEquals("La contraseña actual es incorrecta.", ex.getMessage());
        verify(usuarioDatos, never()).saveUsers(any());
    }

    @Test
    void cambiarContrasena_nuevaContrasenaNull_lanzaExcepcion() {
        Funcionario u = crearUsuario("123456789", "claveActual");
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>(List.of(u)));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("123456789", "claveActual", null));

        assertEquals("La nueva contraseña debe tener al menos 6 caracteres.", ex.getMessage());
    }

    @Test
    void cambiarContrasena_nuevaContrasenaCorta_lanzaExcepcion() {
        Funcionario u = crearUsuario("123456789", "claveActual");
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>(List.of(u)));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("123456789", "claveActual", "123"));

        assertEquals("La nueva contraseña debe tener al menos 6 caracteres.", ex.getMessage());
    }

    @Test
    void cambiarContrasena_nuevaIgualALaActual_lanzaExcepcion() {
        Funcionario u = crearUsuario("123456789", "claveActual");
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>(List.of(u)));

        var ex = assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("123456789", "claveActual", "claveActual"));

        assertEquals("La nueva contraseña debe ser diferente a la actual.", ex.getMessage());
    }

    @Test
    void cambiarContrasena_casoExitoso_cambiaYGuarda() {
        Funcionario u = crearUsuario("123456789", "claveVieja");
        List<Usuario> lista = new ArrayList<>(List.of(u));
        when(usuarioDatos.loadUsers()).thenReturn(lista);

        usuarioLogic.cambiarContrasena("123456789", "claveVieja", "claveNueva");

        assertEquals("claveNueva", u.getPassword());
        verify(usuarioDatos).saveUsers(lista);
    }

    @Test
    void cambiarContrasena_idCaseInsensitive_encuentraUsuario() {
        Funcionario u = crearUsuario("ABC123", "claveVieja");
        when(usuarioDatos.loadUsers()).thenReturn(new ArrayList<>(List.of(u)));

        usuarioLogic.cambiarContrasena("abc123", "claveVieja", "claveNueva");

        assertEquals("claveNueva", u.getPassword());
    }

    // ---------- obtenerUsuario ----------

    @Test
    void obtenerUsuario_idNull_retornaNullSinConsultarDatos() {
        assertNull(usuarioLogic.obtenerUsuario(null));
        verifyNoInteractions(usuarioDatos);
    }

    @Test
    void obtenerUsuario_idVacio_retornaNullSinConsultarDatos() {
        assertNull(usuarioLogic.obtenerUsuario(""));
        verifyNoInteractions(usuarioDatos);
    }

    @Test
    void obtenerUsuario_existente_loRetorna() {
        Funcionario u = crearUsuario("123456789", "clave123");
        when(usuarioDatos.loadUsers()).thenReturn(List.of(u));

        Usuario result = usuarioLogic.obtenerUsuario("123456789");

        assertEquals(u, result);
    }

    @Test
    void obtenerUsuario_noExistente_retornaNull() {
        when(usuarioDatos.loadUsers()).thenReturn(List.of());

        assertNull(usuarioLogic.obtenerUsuario("999999999"));
    }

    @Test
    void obtenerUsuario_caseInsensitive_loEncuentra() {
        Funcionario u = crearUsuario("ABC123", "clave123");
        when(usuarioDatos.loadUsers()).thenReturn(List.of(u));

        assertEquals(u, usuarioLogic.obtenerUsuario("abc123"));
    }
}