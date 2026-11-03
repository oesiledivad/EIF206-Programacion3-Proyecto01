package una.proyecto.bootstrap;

import una.proyecto.datos.Database;
import una.proyecto.security.PasswordHasher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public final class CargarDemostracion {
    public record Carga(List<String> usuarios, List<Integer> categorias, List<String> recursos, List<Integer> reservas) {
        public Carga {
            usuarios = List.copyOf(usuarios);
            categorias = List.copyOf(categorias);
            recursos = List.copyOf(recursos);
            reservas = List.copyOf(reservas);
        }
    }

    private static final class Registro {
        final List<String> usuarios = new ArrayList<>();
        final List<Integer> categorias = new ArrayList<>();
        final List<String> recursos = new ArrayList<>();
        final List<Integer> reservas = new ArrayList<>();

        Carga carga() { return new Carga(usuarios, categorias, recursos, reservas); }
    }

    private record Ejemplo(String usuario, String nombre, String telefono, String categoria,
                           String recurso, String descripcion, String actividad) { }

    private static final List<Ejemplo> EJEMPLOS = List.of(
            new Ejemplo("900000001", "Ana Mora", "80000001", "DEMO - Sala de reuniones",
                    "DEMO-SALA-001", "Sala de reuniones del primer piso", "DEMO - Reunión de coordinación"),
            new Ejemplo("900000002", "Luis Rojas", "80000002", "DEMO - Proyector",
                    "DEMO-PROYECTOR-001", "Proyector del área de capacitación", "DEMO - Capacitación técnica"),
            new Ejemplo("900000003", "Sofía Vargas", "80000003", "DEMO - Equipo portátil",
                    "DEMO-PORTATIL-001", "Equipo portátil para trabajo de oficina", "DEMO - Planificación de equipo"),
            new Ejemplo("900000004", "Carlos Soto", "80000004", "DEMO - Auditorio",
                    "DEMO-AUDITORIO-001", "Auditorio del edificio principal", "DEMO - Presentación de resultados"),
            new Ejemplo("900000005", "Elena Castro", "80000005", "DEMO - Laboratorio",
                    "DEMO-LABORATORIO-001", "Laboratorio de programación", "DEMO - Taller de programación")
    );

    private CargarDemostracion() { }

    public static void main(String[] args) {
        if (args.length == 2 && args[0].equals("limpiar")) {
            int conservados = limpiar(Path.of(args[1]));
            System.out.println("Limpieza de ejemplos de esta ejecución completada.");
            if (conservados > 0) System.out.println("Se conservaron " + conservados + " dependencias de datos creados manualmente.");
            return;
        }
        if (args.length == 0) cargar();
        else if (args.length == 2 && args[0].equals("cargar")) cargar(Path.of(args[1]));
        else throw new IllegalArgumentException("Use: cargar <registro> o limpiar <registro>.");
        System.out.println("Demostración preparada: 5 funcionarios, 5 categorías, 5 recursos y 5 reservas.");
        System.out.println("Usuarios: 900000001 a 900000005. La clave inicial de cada uno es su ID.");
        System.out.println("Las reservas nuevas comienzan mañana, una por día, de 09:00 a 10:00.");
        System.out.println("Los datos existentes se conservan, incluidas claves, fechas y cancelaciones.");
    }

    public static Carga cargar() { return cargar(null); }

    public static Carga cargar(Path archivo) {
        LocalDate primeraFecha = LocalDate.now().plusDays(1);
        Registro registro = new Registro();
        try (Connection connection = Database.instance().openConnection()) {
            connection.setAutoCommit(false);
            try {
                for (int i = 0; i < EJEMPLOS.size(); i++) {
                    Ejemplo ejemplo = EJEMPLOS.get(i);
                    crearFuncionario(connection, ejemplo, registro);
                    crearRecurso(connection, ejemplo, registro);
                    crearReserva(connection, ejemplo, primeraFecha.plusDays(i), registro);
                }
                // El registro queda disponible antes del commit para poder recuperar la limpieza.
                if (archivo != null) guardarRegistro(connection, archivo, registro.carga());
                connection.commit();
                return registro.carga();
            } catch (SQLException | IOException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException | IOException error) {
            throw new IllegalStateException("No se pudo cargar la demostración: " + error.getMessage(), error);
        }
    }

    private static void crearFuncionario(Connection connection, Ejemplo ejemplo, Registro registro) throws SQLException {
        String sql = "SELECT u.nombre, u.rol, f.id_usuario FROM usuarios u "
                + "LEFT JOIN funcionarios f ON f.id_usuario = u.id_usuario WHERE u.id_usuario = ? FOR UPDATE";
        try (PreparedStatement statement = preparar(connection, sql, ejemplo.usuario());
             ResultSet rows = statement.executeQuery()) {
            if (rows.next()) {
                if (!"FUNCIONARIO".equals(rows.getString("rol")) || rows.getString("id_usuario") == null) {
                    throw new SQLException("El ID " + ejemplo.usuario() + " ya está ocupado por otro perfil.");
                }
                if (!ejemplo.nombre().equals(rows.getString("nombre"))
                        && buscarId(connection, "SELECT id_reserva FROM reservas WHERE id_funcionario = ? AND actividad = ? LIMIT 1",
                        ejemplo.usuario(), ejemplo.actividad()) == 0) {
                    throw new SQLException("El ID " + ejemplo.usuario() + " ya existe y no pertenece a la demostración.");
                }
                return;
            }
        }
        ejecutar(connection, "INSERT INTO usuarios (id_usuario, nombre, clave_hash, rol) VALUES (?, ?, ?, 'FUNCIONARIO')",
                ejemplo.usuario(), ejemplo.nombre(), PasswordHasher.hash(ejemplo.usuario()));
        ejecutar(connection, "INSERT INTO funcionarios (id_usuario, telefono) VALUES (?, ?)",
                ejemplo.usuario(), ejemplo.telefono());
        registro.usuarios.add(ejemplo.usuario());
    }

    private static void crearRecurso(Connection connection, Ejemplo ejemplo, Registro registro) throws SQLException {
        // El recurso conserva el vínculo aunque se haya editado el nombre de su categoría.
        if (buscarId(connection, "SELECT id_categoria FROM recursos WHERE id_recurso = ? FOR UPDATE", ejemplo.recurso()) != 0) return;
        int categoria = buscarId(connection, "SELECT id_categoria FROM categorias WHERE descripcion = ? ORDER BY id_categoria LIMIT 1",
                ejemplo.categoria());
        if (categoria == 0) {
            categoria = insertar(connection, "INSERT INTO categorias (descripcion) VALUES (?)", ejemplo.categoria());
            registro.categorias.add(categoria);
        }
        ejecutar(connection, "INSERT INTO recursos (id_recurso, id_categoria, descripcion) VALUES (?, ?, ?)",
                ejemplo.recurso(), categoria, ejemplo.descripcion());
        registro.recursos.add(ejemplo.recurso());
    }

    private static void crearReserva(Connection connection, Ejemplo ejemplo, LocalDate fecha, Registro registro) throws SQLException {
        // Una reserva cancelada también identifica un ejemplo ya cargado.
        if (buscarId(connection, "SELECT id_reserva FROM reservas WHERE id_funcionario = ? AND actividad = ? LIMIT 1",
                ejemplo.usuario(), ejemplo.actividad()) != 0) return;
        String disponible = "SELECT c.id_categoria FROM recursos r JOIN categorias c ON c.id_categoria = r.id_categoria "
                + "JOIN usuarios u ON u.id_usuario = ? WHERE r.id_recurso = ? "
                + "AND r.activo = TRUE AND c.activo = TRUE AND u.activo = TRUE";
        if (buscarId(connection, disponible, ejemplo.usuario(), ejemplo.recurso()) == 0) {
            throw new SQLException("El funcionario, recurso o categoría del ejemplo " + ejemplo.recurso() + " está inactivo.");
        }
        LocalTime inicio = LocalTime.of(9, 0);
        LocalTime fin = LocalTime.of(10, 0);
        String ocupada = "SELECT r.id_reserva FROM reservas r JOIN reserva_recurso rr ON rr.id_reserva = r.id_reserva "
                + "WHERE rr.id_recurso = ? AND r.fecha = ? AND r.estado = 'ACTIVA' "
                + "AND r.hora_inicio < ? AND r.hora_fin > ? LIMIT 1";
        if (buscarId(connection, ocupada, ejemplo.recurso(), fecha, fin, inicio) != 0) {
            throw new SQLException("El recurso " + ejemplo.recurso() + " ya está reservado en el horario de demostración.");
        }
        int reserva = insertar(connection,
                "INSERT INTO reservas (id_funcionario, actividad, fecha, hora_inicio, hora_fin) VALUES (?, ?, ?, ?, ?)",
                ejemplo.usuario(), ejemplo.actividad(), fecha, inicio, fin);
        ejecutar(connection, "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)", reserva, ejemplo.recurso());
        registro.reservas.add(reserva);
    }

    public static int limpiar(Carga carga) {
        return limpiar(carga, null);
    }

    public static int limpiar(Path archivo) {
        Properties properties = new Properties();
        try (var input = Files.newInputStream(archivo)) {
            properties.load(input);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer el registro de demostración: " + archivo, error);
        }
        if (!"1".equals(properties.getProperty("version")) || properties.getProperty("destino") == null) {
            throw new IllegalArgumentException("Registro de demostración inválido.");
        }
        Carga carga = new Carga(leerLista(properties, "usuarios"), leerLista(properties, "categorias").stream().map(Integer::valueOf).toList(),
                leerLista(properties, "recursos"), leerLista(properties, "reservas").stream().map(Integer::valueOf).toList());
        int conservados = limpiar(carga, properties.getProperty("destino"));
        try {
            Files.delete(archivo);
        } catch (IOException error) {
            throw new IllegalStateException("La base se limpió, pero no se pudo eliminar el registro: " + archivo, error);
        }
        return conservados;
    }

    private static int limpiar(Carga carga, String destinoEsperado) {
        try (Connection connection = Database.instance().openConnection()) {
            if (destinoEsperado != null && !destinoEsperado.equals(destino(connection))) {
                throw new IllegalArgumentException("El registro pertenece a otra conexión de base de datos; no se eliminó nada.");
            }
            connection.setAutoCommit(false);
            try {
                for (int id : carga.reservas()) {
                    ejecutar(connection, "DELETE FROM reserva_recurso WHERE id_reserva = ?", id);
                    ejecutar(connection, "DELETE FROM reservas WHERE id_reserva = ?", id);
                }
                int conservados = 0;
                for (String id : carga.recursos()) {
                    ejecutar(connection, "DELETE FROM recursos WHERE id_recurso = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM reserva_recurso rr WHERE rr.id_recurso = recursos.id_recurso)", id);
                    conservados += buscarId(connection, "SELECT 1 FROM recursos WHERE id_recurso = ?", id);
                }
                for (int id : carga.categorias()) {
                    ejecutar(connection, "DELETE FROM categorias WHERE id_categoria = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM recursos r WHERE r.id_categoria = categorias.id_categoria)", id);
                    conservados += buscarId(connection, "SELECT 1 FROM categorias WHERE id_categoria = ?", id);
                }
                for (String id : carga.usuarios()) {
                    ejecutar(connection, "DELETE FROM funcionarios WHERE id_usuario = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM reservas r WHERE r.id_funcionario = funcionarios.id_usuario)", id);
                    ejecutar(connection, "DELETE FROM usuarios WHERE id_usuario = ? AND NOT EXISTS "
                            + "(SELECT 1 FROM funcionarios f WHERE f.id_usuario = usuarios.id_usuario)", id);
                    conservados += buscarId(connection, "SELECT 1 FROM usuarios WHERE id_usuario = ?", id);
                }
                connection.commit();
                return conservados;
            } catch (SQLException | RuntimeException error) {
                connection.rollback();
                throw error;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo limpiar la demostración: " + error.getMessage(), error);
        }
    }

    private static void guardarRegistro(Connection connection, Path archivo, Carga carga) throws SQLException, IOException {
        Properties properties = new Properties();
        properties.setProperty("version", "1");
        properties.setProperty("destino", destino(connection));
        properties.setProperty("usuarios", String.join(",", carga.usuarios()));
        properties.setProperty("categorias", carga.categorias().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
        properties.setProperty("recursos", String.join(",", carga.recursos()));
        properties.setProperty("reservas", carga.reservas().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(",")));
        try (var output = Files.newOutputStream(archivo, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            properties.store(output, "IDs creados por esta ejecucion; no contiene contrasenas");
        }
    }

    private static String destino(Connection connection) throws SQLException {
        String identidad = connection.getMetaData().getURL() + "\n" + connection.getMetaData().getUserName();
        return UUID.nameUUIDFromBytes(identidad.getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static List<String> leerLista(Properties properties, String nombre) {
        String value = properties.getProperty(nombre);
        if (value == null) throw new IllegalArgumentException("Falta " + nombre + " en el registro.");
        return value.isEmpty() ? List.of() : List.of(value.split(","));
    }

    private static PreparedStatement preparar(Connection connection, String sql, Object... valores) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            for (int i = 0; i < valores.length; i++) statement.setObject(i + 1, valores[i]);
            return statement;
        } catch (SQLException error) {
            statement.close();
            throw error;
        }
    }

    private static int buscarId(Connection connection, String sql, Object... valores) throws SQLException {
        try (PreparedStatement statement = preparar(connection, sql, valores);
             ResultSet rows = statement.executeQuery()) {
            return rows.next() ? rows.getInt(1) : 0;
        }
    }

    private static void ejecutar(Connection connection, String sql, Object... valores) throws SQLException {
        try (PreparedStatement statement = preparar(connection, sql, valores)) {
            statement.executeUpdate();
        }
    }

    private static int insertar(Connection connection, String sql, Object... valores) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) statement.setObject(i + 1, valores[i]);
            statement.executeUpdate();
            try (ResultSet rows = statement.getGeneratedKeys()) {
                if (!rows.next()) throw new SQLException("MySQL no devolvió el ID del ejemplo.");
                return rows.getInt(1);
            }
        }
    }
}
