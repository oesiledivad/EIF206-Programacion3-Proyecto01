package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import una.proyecto.bootstrap.CargarDemostracion;
import una.proyecto.datos.Database;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.security.PasswordHasher;
import una.proyecto.service.AuthService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DemostracionIntegracionIT {
    private static final String IDS = "('900000001','900000002','900000003','900000004','900000005')";
    private static final String IDS_LIMPIEZA = IDS.replace(")", ",'IT-DEMO-MANUAL')");
    @TempDir Path temporal;

    @BeforeEach
    @AfterEach
    void limpiar() throws Exception {
        try (Connection connection = Database.instance().openConnection()) {
            for (String sql : List.of(
                    "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario IN " + IDS_LIMPIEZA + ")",
                    "DELETE FROM reservas WHERE id_funcionario IN " + IDS_LIMPIEZA,
                    "DELETE FROM recursos WHERE id_recurso LIKE 'DEMO-%' OR id_recurso = 'IT-DEMO-MANUAL'",
                    "DELETE FROM categorias WHERE descripcion LIKE 'DEMO - %' OR descripcion = 'Categoría editada por el usuario'",
                    "DELETE FROM funcionarios WHERE id_usuario IN " + IDS_LIMPIEZA,
                    "DELETE FROM usuarios WHERE id_usuario IN " + IDS_LIMPIEZA)) {
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.executeUpdate();
                }
            }
        }
    }

    @Test void cargaCincoDeCadaEntidadConReservasFuturasYLoginFuncional() throws Exception {
        CargarDemostracion.cargar();

        comprobarCantidades(5);
        UsuarioDatos usuarios = new UsuarioDatos();
        AuthService auth = new AuthService();
        for (int i = 1; i <= 5; i++) {
            String id = "90000000" + i;
            String hash = usuarios.findPasswordHash(id);
            assertNotEquals(id, hash);
            assertTrue(PasswordHasher.verify(id, hash));
            assertNotNull(auth.authenticate(id, id));
            var reserva = new ReservaDatos().obtenerTodos().stream()
                    .filter(r -> id.equals(r.getIdFuncionario())).findFirst().orElseThrow();
            assertTrue(reserva.getFecha().isAfter(LocalDate.now()));
            assertEquals(1, reserva.getRecursosAsignadosIds().size());
            assertEquals(1, reserva.getCategoriasDeRecursosIds().size());
            assertNotNull(reserva.getFuncionarioReserva().getName());
        }
        assertEquals(5, contar("SELECT COUNT(DISTINCT fecha) FROM reservas WHERE id_funcionario IN " + IDS));
    }

    @Test void repetirNoDuplicaNiRestableceClavesPerfilesCategoriasOReservas() throws Exception {
        CargarDemostracion.cargar();
        String hash = PasswordHasher.hash("claveEditada");
        ejecutar("UPDATE usuarios SET nombre = 'Nombre editado', clave_hash = ?, activo = FALSE WHERE id_usuario = '900000001'", hash);
        ejecutar("UPDATE categorias SET descripcion = 'Categoría editada por el usuario' WHERE id_categoria = "
                + "(SELECT id_categoria FROM recursos WHERE id_recurso = 'DEMO-SALA-001')");
        ejecutar("UPDATE reservas SET estado = 'CANCELADA', fecha = ? WHERE id_funcionario = '900000001'", LocalDate.now().plusDays(60));
        int reserva = contar("SELECT id_reserva FROM reservas WHERE id_funcionario = '900000001'");

        CargarDemostracion.cargar();

        comprobarCantidades(5);
        assertEquals(reserva, contar("SELECT id_reserva FROM reservas WHERE id_funcionario = '900000001'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM reservas WHERE id_funcionario = '900000001' AND estado = 'CANCELADA' AND fecha = ?",
                LocalDate.now().plusDays(60)));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario = '900000001' AND nombre = 'Nombre editado' AND clave_hash = ? AND activo = FALSE", hash));
        assertEquals(1, contar("SELECT COUNT(*) FROM categorias WHERE descripcion = 'Categoría editada por el usuario'"));
        assertNull(new AuthService().authenticate("900000001", "claveEditada"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "FUNCIONARIO"})
    void idOcupadoRevierteTodaLaCargaSinModificarAlUsuarioExistente(String rol) throws Exception {
        ejecutar("INSERT INTO usuarios (id_usuario, nombre, clave_hash, rol) VALUES ('900000005', 'Usuario existente', 'hash-conservado', ?)", rol);
        if (rol.equals("FUNCIONARIO")) {
            ejecutar("INSERT INTO funcionarios (id_usuario, telefono) VALUES ('900000005', '88888888')");
        }

        IllegalStateException error = assertThrows(IllegalStateException.class, CargarDemostracion::cargar);

        assertTrue(error.getMessage().contains("900000005"));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario IN " + IDS));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario = '900000005' AND nombre = 'Usuario existente' AND clave_hash = 'hash-conservado'"));
        assertEquals(0, contar("SELECT COUNT(*) FROM recursos WHERE id_recurso LIKE 'DEMO-%'"));
        assertEquals(0, contar("SELECT COUNT(*) FROM categorias WHERE descripcion LIKE 'DEMO - %'"));
        assertEquals(0, contar("SELECT COUNT(*) FROM reservas WHERE id_funcionario IN " + IDS));
    }

    @Test void noInsertaUnaReservaQueSeSolapeConUnaExistente() throws Exception {
        CargarDemostracion.cargar();
        ejecutar("UPDATE reservas SET actividad = 'Reserva del usuario' WHERE id_funcionario = '900000005'");

        IllegalStateException error = assertThrows(IllegalStateException.class, CargarDemostracion::cargar);

        assertTrue(error.getMessage().contains("ya está reservado"), error.getMessage());
        comprobarCantidades(5);
        assertEquals(1, contar("SELECT COUNT(*) FROM reservas WHERE actividad = 'Reserva del usuario' AND id_funcionario = '900000005'"));
    }

    @Test void limpiezaEliminaSoloLoRegistradoInclusoSiSeCanceloUnaReserva() throws Exception {
        var carga = CargarDemostracion.cargar();
        ejecutar("UPDATE reservas SET estado = 'CANCELADA' WHERE id_funcionario = '900000001'");

        assertEquals(0, CargarDemostracion.limpiar(carga));

        comprobarCantidades(0);
        assertEquals(0, contar("SELECT COUNT(*) FROM categorias WHERE descripcion LIKE 'DEMO - %'"));
        assertEquals(0, CargarDemostracion.limpiar(carga), "Repetir la limpieza debe ser seguro");
    }

    @Test void segundaCargaNoSeApropiaDeLosEjemplosQueYaExistian() throws Exception {
        var primera = CargarDemostracion.cargar();
        var segunda = CargarDemostracion.cargar();

        assertTrue(segunda.usuarios().isEmpty());
        assertTrue(segunda.categorias().isEmpty());
        assertTrue(segunda.recursos().isEmpty());
        assertTrue(segunda.reservas().isEmpty());
        assertEquals(0, CargarDemostracion.limpiar(segunda));
        comprobarCantidades(5);
        CargarDemostracion.limpiar(primera);
        comprobarCantidades(0);
    }

    @Test void limpiezaConservaDatosManualesYSusDependenciasDeDemostracion() throws Exception {
        var carga = CargarDemostracion.cargar();
        int categoria = contar("SELECT id_categoria FROM recursos WHERE id_recurso = 'DEMO-SALA-001'");
        ejecutar("INSERT INTO usuarios (id_usuario, nombre, clave_hash, rol) VALUES ('IT-DEMO-MANUAL', 'Cuenta manual', 'hash-conservado', 'FUNCIONARIO')");
        ejecutar("INSERT INTO funcionarios (id_usuario, telefono) VALUES ('IT-DEMO-MANUAL', '88888888')");
        ejecutar("INSERT INTO recursos (id_recurso, id_categoria, descripcion) VALUES ('IT-DEMO-MANUAL', ?, 'Recurso manual')", categoria);
        ejecutar("INSERT INTO reservas (id_funcionario, actividad, fecha, hora_inicio, hora_fin) VALUES ('900000001', 'Reserva manual', ?, '15:00', '16:00')",
                LocalDate.now().plusDays(20));
        int reserva = contar("SELECT id_reserva FROM reservas WHERE actividad = 'Reserva manual' AND id_funcionario = '900000001'");
        ejecutar("INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, 'DEMO-SALA-001'), (?, 'IT-DEMO-MANUAL')", reserva, reserva);

        assertEquals(3, CargarDemostracion.limpiar(carga));

        assertEquals(1, contar("SELECT COUNT(*) FROM reservas WHERE id_reserva = ? AND actividad = 'Reserva manual'", reserva));
        assertEquals(2, contar("SELECT COUNT(*) FROM reserva_recurso WHERE id_reserva = ?", reserva));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario = 'IT-DEMO-MANUAL'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM funcionarios WHERE id_usuario = 'IT-DEMO-MANUAL'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM recursos WHERE id_recurso = 'IT-DEMO-MANUAL'"));
        assertEquals(1, contar("SELECT COUNT(*) FROM categorias WHERE id_categoria = ?", categoria));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario IN " + IDS));
        assertEquals(0, contar("SELECT COUNT(*) FROM reservas WHERE actividad LIKE 'DEMO - %' AND id_funcionario IN " + IDS));
    }

    @Test void registroPersistidoPermiteLimpiarYRechazaUnaConexionDiferente() throws Exception {
        Path archivo = temporal.resolve("demo.properties");
        var carga = CargarDemostracion.cargar(archivo);
        assertEquals(5, carga.reservas().size());
        Properties properties = new Properties();
        try (var input = Files.newInputStream(archivo)) { properties.load(input); }
        String destino = properties.getProperty("destino");
        properties.setProperty("destino", "otra-conexion");
        try (var output = Files.newOutputStream(archivo)) { properties.store(output, null); }

        assertThrows(IllegalArgumentException.class, () -> CargarDemostracion.limpiar(archivo));
        comprobarCantidades(5);
        assertTrue(Files.exists(archivo));

        properties.setProperty("destino", destino);
        try (var output = Files.newOutputStream(archivo)) { properties.store(output, null); }
        assertEquals(0, CargarDemostracion.limpiar(archivo));
        assertFalse(Files.exists(archivo));
        comprobarCantidades(0);
    }

    @Test void falloAlRegistrarRevierteLaCargaYNoSobrescribeOtroRegistro() throws Exception {
        Path archivo = temporal.resolve("existente.properties");
        Files.writeString(archivo, "registro existente");

        assertThrows(IllegalStateException.class, () -> CargarDemostracion.cargar(archivo));

        comprobarCantidades(0);
        assertEquals("registro existente", Files.readString(archivo));
    }

    private void comprobarCantidades(int cantidad) throws Exception {
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM usuarios WHERE id_usuario IN " + IDS));
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM funcionarios WHERE id_usuario IN " + IDS));
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM categorias WHERE id_categoria IN (SELECT id_categoria FROM recursos WHERE id_recurso LIKE 'DEMO-%')"));
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM recursos WHERE id_recurso LIKE 'DEMO-%'"));
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM reservas WHERE id_funcionario IN " + IDS));
        assertEquals(cantidad, contar("SELECT COUNT(*) FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario IN " + IDS + ")"));
    }

    private int contar(String sql, Object... valores) throws Exception {
        try (Connection connection = Database.instance().openConnection();
             PreparedStatement statement = preparar(connection, sql, valores);
             var rows = statement.executeQuery()) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private void ejecutar(String sql, Object... valores) throws Exception {
        try (Connection connection = Database.instance().openConnection();
             PreparedStatement statement = preparar(connection, sql, valores)) {
            statement.executeUpdate();
        }
    }

    private PreparedStatement preparar(Connection connection, String sql, Object... valores) throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < valores.length; i++) statement.setObject(i + 1, valores[i]);
        return statement;
    }
}
