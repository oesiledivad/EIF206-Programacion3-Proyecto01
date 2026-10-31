package una.proyecto.datos;

import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;
import una.proyecto.security.PasswordHasher;

import java.time.LocalDate;
import java.time.LocalTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDatos {
    private static final String SELECT_USUARIO =
            "SELECT u.id_usuario, u.nombre, u.rol, u.activo, f.telefono "
            + "FROM usuarios u LEFT JOIN funcionarios f ON u.id_usuario = f.id_usuario ";
    private final Database db;

    public UsuarioDatos() {
        this.db = Database.instance();
    }

    public List<Usuario> loadUsers() {
        try (Connection connection = db.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_USUARIO + "WHERE u.activo = TRUE ORDER BY u.id_usuario");
             ResultSet rows = statement.executeQuery()) {
            List<Usuario> users = new ArrayList<>();
            while (rows.next()) users.add(toUsuario(rows));
            return users;
        } catch (SQLException e) {
            throw failure("consultar usuarios", e);
        }
    }

    public Usuario findUserById(String id) {
        if (id == null || id.isBlank()) return null;
        try (Connection connection = db.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     SELECT_USUARIO + "WHERE u.id_usuario = ? AND u.activo = TRUE")) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? toUsuario(rows) : null;
            }
        } catch (SQLException e) {
            throw failure("buscar el usuario", e);
        }
    }

    public String findPasswordHash(String id) {
        if (id == null || id.isBlank()) return null;
        try (Connection connection = db.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT clave_hash FROM usuarios WHERE id_usuario = ? AND activo = TRUE")) {
            statement.setString(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? rows.getString(1) : null;
            }
        } catch (SQLException e) {
            throw failure("consultar las credenciales", e);
        }
    }

    public boolean addUser(Usuario user) {
        if (user == null || user.getId() == null || user.getId().isBlank()) return false;
        validateProfile(user);
        String hash = PasswordHasher.hash(user.getPassword());
        try (Connection connection = db.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO usuarios (id_usuario, nombre, clave_hash, rol, activo) VALUES (?, ?, ?, ?, TRUE)")) {
                    statement.setString(1, user.getId());
                    statement.setString(2, user.getName());
                    statement.setString(3, hash);
                    statement.setString(4, user.getRole());
                    statement.executeUpdate();
                }
                if (user instanceof Funcionario funcionario) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT INTO funcionarios (id_usuario, telefono) VALUES (?, ?)")) {
                        statement.setString(1, user.getId());
                        statement.setString(2, funcionario.getPhone());
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                if (e.getErrorCode() == 1062) return false;
                throw e;
            }
        } catch (SQLException e) {
            throw failure("crear el usuario", e);
        }
    }

    public boolean updateUser(Usuario user) {
        if (user == null || user.getId() == null || user.getId().isBlank()) return false;
        validateProfile(user);
        // Las credenciales solo se actualizan mediante changePassword.
        try (Connection connection = db.openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE usuarios SET nombre = ? WHERE id_usuario = ? AND rol = ? AND activo = TRUE")) {
                    statement.setString(1, user.getName());
                    statement.setString(2, user.getId());
                    statement.setString(3, user.getRole());
                    if (statement.executeUpdate() == 0) {
                        connection.rollback();
                        return false;
                    }
                }
                if (user instanceof Funcionario funcionario) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE funcionarios SET telefono = ? WHERE id_usuario = ?")) {
                        statement.setString(1, funcionario.getPhone());
                        statement.setString(2, user.getId());
                        if (statement.executeUpdate() == 0) {
                            connection.rollback();
                            throw new SQLException("El funcionario no tiene un perfil asociado");
                        }
                    }
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw failure("actualizar el usuario", e);
        }
    }

    // Baja lógica del usuario. En la misma transacción cancela sus reservas FUTURAS
    // (las pasadas quedan como historial). Un usuario inactivo ya no puede autenticarse.
    public boolean deleteUser(String id) {
        if (id == null || id.isBlank()) return false;
        try (Connection connection = db.openConnection()) {
            connection.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE usuarios SET activo = FALSE WHERE id_usuario = ? AND activo = TRUE")) {
                    statement.setString(1, id);
                    filas = statement.executeUpdate();
                }
                if (filas != 1) {
                    connection.rollback();
                    return false;
                }

                LocalDate hoy = LocalDate.now();
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE reservas SET estado = 'CANCELADA' "
                                + "WHERE id_funcionario = ? AND estado = 'ACTIVA' "
                                + "AND (fecha > ? OR (fecha = ? AND hora_inicio > ?))")) {
                    statement.setString(1, id);
                    statement.setObject(2, hoy);
                    statement.setObject(3, hoy);
                    statement.setObject(4, LocalTime.now());
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw failure("desactivar el usuario", e);
        }
    }

    public boolean changePassword(String id, String expectedHash, String newHash) {
        try (Connection connection = db.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE usuarios SET clave_hash = ? WHERE id_usuario = ? AND clave_hash = ? AND activo = TRUE")) {
            statement.setString(1, newHash);
            statement.setString(2, id);
            statement.setString(3, expectedHash);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw failure("cambiar la contraseña", e);
        }
    }

    private Usuario toUsuario(ResultSet row) throws SQLException {
        String id = row.getString("id_usuario");
        String nombre = row.getString("nombre");
        Usuario user;
        if ("FUNCIONARIO".equals(row.getString("rol"))) {
            String telefono = row.getString("telefono");
            if (telefono == null) throw new SQLException("El funcionario no tiene un perfil asociado");
            user = new Funcionario(id, "FUNCIONARIO", nombre, telefono);
        } else {
            user = new Administrador(id, "ADMIN", nombre);
        }
        user.setActive(row.getBoolean("activo"));
        return user;
    }

    private void validateProfile(Usuario user) {
        boolean funcionario = user instanceof Funcionario && "FUNCIONARIO".equals(user.getRole());
        boolean admin = user instanceof Administrador && "ADMIN".equals(user.getRole());
        if (!funcionario && !admin) throw new IllegalArgumentException("El rol no corresponde al perfil del usuario.");
    }

    private IllegalStateException failure(String operation, SQLException cause) {
        return new IllegalStateException("No se pudo " + operation + ".", cause);
    }
}
