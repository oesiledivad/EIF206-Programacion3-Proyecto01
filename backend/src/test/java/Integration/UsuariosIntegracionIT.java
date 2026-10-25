package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.security.PasswordHasher;
import una.proyecto.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

class UsuariosIntegracionIT {
    private static final String ID = "991000001";
    private static final String ADMIN = "IT-ADMIN-AUTH";
    private UsuarioDatos datos;
    private AuthService auth;
    private UsuarioLogic logic;

    @BeforeEach void setup() throws Exception {
        datos = new UsuarioDatos();
        auth = new AuthService(datos);
        logic = new UsuarioLogic(datos);
        limpiar();
    }

    @AfterEach void limpiar() throws Exception {
        try (Connection connection = Database.instance().openConnection()) {
            for (String table : new String[]{"funcionarios", "usuarios"}) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE id_usuario IN (?, ?)")) {
                    statement.setString(1, ID);
                    statement.setString(2, ADMIN);
                    statement.executeUpdate();
                }
            }
        }
    }

    private void crearFuncionario() {
        new FuncionarioLogic(datos).crear(new Funcionario(ID, "FUNCIONARIO", "Juan Perez", "88888888"));
    }

    @Test void creaAmbasFilasConClaveInicialHasheadaYPerfilSinCredenciales() {
        crearFuncionario();
        var user = datos.findUserById(ID);
        assertInstanceOf(Funcionario.class, user);
        assertEquals("Juan Perez", user.getName());
        assertNull(user.getPassword());
        assertTrue(PasswordHasher.verify(ID, datos.findPasswordHash(ID)));
        assertNotNull(auth.authenticate(ID, ID));
        Funcionario duplicate = new Funcionario(ID, "FUNCIONARIO", "Otro", "88888888");
        duplicate.setPassword("otraClave");
        assertFalse(datos.addUser(duplicate));
    }

    @Test void falloDelPerfilRevierteLaFilaUsuario() {
        Funcionario invalid = new Funcionario(ID, "FUNCIONARIO", "Juan Perez", null);
        invalid.setPassword(ID);
        assertThrows(IllegalStateException.class, () -> datos.addUser(invalid));
        assertNull(datos.findUserById(ID));
    }

    @Test void cambioDeClavePersisteYLaAnteriorDejaDeFuncionar() {
        crearFuncionario();
        logic.cambiarContrasena(ID, ID, "nuevaClave");
        assertNull(auth.authenticate(ID, ID));
        assertNotNull(auth.authenticate(ID, "nuevaClave"));
        assertTrue(PasswordHasher.verify("nuevaClave", new UsuarioDatos().findPasswordHash(ID)));
    }

    @Test void noCambiaClaveSiLaActualEsIncorrecta() {
        crearFuncionario();
        String before = datos.findPasswordHash(ID);
        assertThrows(IllegalArgumentException.class, () -> logic.cambiarContrasena(ID, "incorrecta", "nuevaClave"));
        assertEquals(before, datos.findPasswordHash(ID));
    }

    @Test void editarNombreYTelefonoNoReemplazaLaClave() {
        crearFuncionario();
        String before = datos.findPasswordHash(ID);
        Funcionario user = new Funcionario(ID, "FUNCIONARIO", "Ana Perez", "77777777");
        user.setPassword("noUsarEstaClave");
        assertTrue(datos.updateUser(user));
        assertEquals(before, datos.findPasswordHash(ID));
        assertEquals("77777777", ((Funcionario) datos.findUserById(ID)).getPhone());
    }

    @Test void falloAlEditarPerfilRevierteCambioDeNombre() {
        crearFuncionario();
        assertThrows(IllegalStateException.class, () -> datos.updateUser(
                new Funcionario(ID, "FUNCIONARIO", "Nombre cambiado", null)));
        assertEquals("Juan Perez", datos.findUserById(ID).getName());
    }

    @Test void bajaLogicaBloqueaLoginCambioYReactivacionPorEdicion() {
        crearFuncionario();
        assertTrue(datos.deleteUser(ID));
        assertNull(auth.authenticate(ID, ID));
        assertNull(datos.findUserById(ID));
        assertTrue(datos.loadUsers().stream().noneMatch(user -> ID.equals(user.getId())));
        assertThrows(IllegalArgumentException.class, () -> logic.cambiarContrasena(ID, ID, "nuevaClave"));
        assertFalse(datos.updateUser(new Funcionario(ID, "FUNCIONARIO", "Juan", "88888888")));
        try (Connection connection = Database.instance().openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT u.activo, f.telefono FROM usuarios u JOIN funcionarios f USING (id_usuario) WHERE id_usuario = ?")) {
            statement.setString(1, ID);
            try (var row = statement.executeQuery()) {
                assertTrue(row.next());
                assertFalse(row.getBoolean(1));
                assertEquals("88888888", row.getString(2));
            }
        } catch (Exception e) { fail(e); }
    }

    @Test void actualizacionDeClaveObsoletaNoSobrescribeLaNueva() {
        crearFuncionario();
        String before = datos.findPasswordHash(ID);
        logic.cambiarContrasena(ID, ID, "nuevaClave");
        assertFalse(datos.changePassword(ID, before, PasswordHasher.hash("otraClave")));
        assertNotNull(auth.authenticate(ID, "nuevaClave"));
    }

    @Test void administradorNoTienePerfilFuncionarioYSeBuscaSinDistinguirMayusculas() {
        Administrador admin = new Administrador(ADMIN, "ADMIN", "Administrador de prueba");
        admin.setPassword("claveAdmin");
        assertTrue(datos.addUser(admin));
        var result = auth.authenticate(ADMIN.toLowerCase(), "claveAdmin");
        assertInstanceOf(Administrador.class, result);
        assertNull(result.getPassword());
    }
}
