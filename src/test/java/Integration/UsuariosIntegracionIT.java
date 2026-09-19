package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Funcionario;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: UsuarioLogic → UsuarioDatos → XML.
 */
public class UsuariosIntegracionIT {

    private static final String XML_TEST = "target/test-it-usuarios-logic.xml";
    private UsuarioLogic usuarioLogic;
    private UsuarioDatos usuarioDatos;

    @BeforeEach
    void setUp() {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        usuarioDatos = new UsuarioDatos(XML_TEST);
        usuarioLogic = new UsuarioLogic(usuarioDatos);
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }

    @Test
    void obtenerUsuario_admiPorDefecto_existeEnXML() {
        var usuario = usuarioLogic.obtenerUsuario("admin");

        assertNotNull(usuario);
        assertEquals("admin", usuario.getId());
    }

    @Test
    void obtenerUsuario_inexistente_retornaNull() {
        assertNull(usuarioLogic.obtenerUsuario("999999999"));
    }

    @Test
    void cambiarContrasena_persisteCambioEnXML() {
        Funcionario f = new Funcionario("111111111", "FUNCIONARIO", "Juan Perez", "88887777");
        f.setPassword("claveVieja");
        usuarioDatos.addUser(f);

        usuarioLogic.cambiarContrasena("111111111", "claveVieja", "claveNueva");

        // Releer desde XML con una instancia nueva de UsuarioDatos para confirmar persistencia real
        UsuarioDatos datosVerificacion = new UsuarioDatos(XML_TEST);
        var actualizado = datosVerificacion.findUserById("111111111");

        assertEquals("claveNueva", actualizado.getPassword());
    }

    @Test
    void cambiarContrasena_actualIncorrecta_noModificaXML() {
        Funcionario f = new Funcionario("111111111", "FUNCIONARIO", "Juan Perez", "88887777");
        f.setPassword("claveVieja");
        usuarioDatos.addUser(f);

        assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("111111111", "claveMala", "claveNueva"));

        UsuarioDatos datosVerificacion = new UsuarioDatos(XML_TEST);
        assertEquals("claveVieja", datosVerificacion.findUserById("111111111").getPassword());
    }

    @Test
    void cambiarContrasena_usuarioInexistente_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> usuarioLogic.cambiarContrasena("999999999", "cualquiera", "claveNueva"));
    }
}