package Unit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.security.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioLogicTest {
    private static String oldHash;
    private UsuarioDatos datos;
    private UsuarioLogic logic;

    @BeforeAll static void hash() { oldHash = PasswordHasher.hash("claveActual"); }
    @BeforeEach void setup() {
        datos = mock(UsuarioDatos.class);
        logic = new UsuarioLogic(datos);
    }

    private void usuarioActivo() {
        when(datos.findUserById("123456789")).thenReturn(
                new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "88888888"));
        when(datos.findPasswordHash("123456789")).thenReturn(oldHash);
    }

    @Test void cambiaSoloLaCredencialEsperadaYGuardaHashNuevo() {
        usuarioActivo();
        when(datos.changePassword(eq("123456789"), eq(oldHash), anyString())).thenReturn(true);
        logic.cambiarContrasena("123456789", "claveActual", "claveNueva");
        var captor = ArgumentCaptor.forClass(String.class);
        verify(datos).changePassword(eq("123456789"), eq(oldHash), captor.capture());
        assertTrue(PasswordHasher.verify("claveNueva", captor.getValue()));
        assertNotEquals(oldHash, captor.getValue());
        verify(datos, never()).updateUser(any());
    }

    @Test void usuarioInexistenteNoCambiaClave() {
        assertThrows(IllegalArgumentException.class,
                () -> logic.cambiarContrasena("nadie", "claveActual", "claveNueva"));
        verify(datos, never()).changePassword(any(), any(), any());
    }

    @Test void usuarioInactivoNoCambiaClave() {
        var user = new Funcionario("123456789", "FUNCIONARIO", "Juan", "88888888");
        user.setActive(false);
        when(datos.findUserById("123456789")).thenReturn(user);
        assertThrows(IllegalArgumentException.class,
                () -> logic.cambiarContrasena("123456789", "claveActual", "claveNueva"));
        verify(datos, never()).findPasswordHash(any());
    }

    @Test void claveActualIncorrectaNoEscribe() {
        usuarioActivo();
        assertThrows(IllegalArgumentException.class,
                () -> logic.cambiarContrasena("123456789", "incorrecta", "claveNueva"));
        verify(datos, never()).changePassword(any(), any(), any());
    }

    @Test void rechazaClaveNuevaCortaNulaOIgual() {
        usuarioActivo();
        for (String invalid : new String[]{null, "123", "claveActual"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> logic.cambiarContrasena("123456789", "claveActual", invalid));
        }
        verify(datos, never()).changePassword(any(), any(), any());
    }

    @Test void cambioConcurrenteNoSeSobrescribe() {
        usuarioActivo();
        when(datos.changePassword(any(), any(), any())).thenReturn(false);
        assertThrows(IllegalArgumentException.class,
                () -> logic.cambiarContrasena("123456789", "claveActual", "claveNueva"));
    }

    @Test void obtenerUsuarioConIdVacioNoConsultaDatos() {
        assertNull(logic.obtenerUsuario(null));
        assertNull(logic.obtenerUsuario(" "));
        verifyNoInteractions(datos);
    }

    @Test void obtenerUsuarioConsultaPorId() {
        var user = new Funcionario("123456789", "FUNCIONARIO", "Juan", "88888888");
        when(datos.findUserById("123456789")).thenReturn(user);
        assertSame(user, logic.obtenerUsuario("123456789"));
        assertNull(logic.obtenerUsuario("999999999"));
    }
}
