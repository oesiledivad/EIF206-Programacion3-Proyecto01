package Unit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Administrador;
import una.proyecto.security.PasswordHasher;
import una.proyecto.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private static String hash;
    private UsuarioDatos datos;
    private AuthService service;

    @BeforeAll static void hash() { hash = PasswordHasher.hash("clave123"); }
    @BeforeEach void setup() {
        datos = mock(UsuarioDatos.class);
        service = new AuthService(datos);
    }

    @Test void credencialesCorrectasDevuelvenPerfilSinClave() {
        Administrador user = new Administrador("admin", "ADMIN", "Administrador");
        when(datos.findUserById("ADMIN")).thenReturn(user);
        when(datos.findPasswordHash("ADMIN")).thenReturn(hash);
        var result = service.authenticate("ADMIN", "clave123");
        assertEquals("admin", result.getId());
        assertNull(result.getPassword());
    }

    @Test void claveIncorrectaNoAutentica() {
        when(datos.findUserById("admin")).thenReturn(new Administrador("admin", "ADMIN", "Admin"));
        when(datos.findPasswordHash("admin")).thenReturn(hash);
        assertNull(service.authenticate("admin", "incorrecta"));
    }

    @Test void usuarioInactivoNoAutentica() {
        var user = new Administrador("admin", "ADMIN", "Admin");
        user.setActive(false);
        when(datos.findUserById("admin")).thenReturn(user);
        assertNull(service.authenticate("admin", "clave123"));
        verify(datos, never()).findPasswordHash(any());
    }

    @Test void usuarioInexistenteNoAutentica() {
        assertNull(service.authenticate("nadie", "clave123"));
    }

    @Test void camposVaciosNoConsultanDatos() {
        assertNull(service.authenticate(null, "clave123"));
        assertNull(service.authenticate("admin", null));
        assertNull(service.authenticate(" ", "clave123"));
        assertNull(service.authenticate("admin", ""));
        verifyNoInteractions(datos);
    }
}
