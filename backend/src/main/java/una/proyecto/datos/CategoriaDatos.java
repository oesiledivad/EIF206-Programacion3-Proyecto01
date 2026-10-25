package una.proyecto.datos;

import una.proyecto.model.Categoria;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDatos {

    private static final String PREFIJO_CATEGORIA = "CAT-";

    private final Database db;

    public CategoriaDatos() {
        this.db = Database.instance();
    }

    // Guarda la categoria y le asigna el id que genero MySQL (formato CAT-000001).
    public void crear(Categoria nueva) {
        String sql = "insert into categorias (descripcion) values (?)";
        try (PreparedStatement stm = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, nueva.getDescripcion());
            db.executeUpdate(stm);
            try (ResultSet llaves = stm.getGeneratedKeys()) {
                if (!llaves.next()) {
                    throw new SQLException("MySQL no devolvio el id de la categoria creada");
                }
                nueva.setId(convertirId(llaves.getInt(1)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear la categoria: " + e.getMessage(), e);
        }
    }

    // Devuelve la categoria activa con ese id, o null si no existe.
    public Categoria leerPorId(String id) {
        if (!esIdValido(id)) {
            return null; // un id con otro formato no puede existir en la tabla
        }
        String sql = "select id_categoria, descripcion from categorias "
                + "where id_categoria = ? and activo = true";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, obtenerIdCategoria(id));
            try (ResultSet rs = db.executeQuery(stm)) {
                if (rs.next()) {
                    return aCategoria(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la categoria " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public void actualizar(Categoria actualizada) {
        String sql = "update categorias set descripcion = ? "
                + "where id_categoria = ? and activo = true";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, actualizada.getDescripcion());
            stm.setInt(2, obtenerIdCategoria(actualizada.getId()));
            int filas = db.executeUpdate(stm);
            if (filas == 0) {
                throw new RuntimeException("No existe una categoria activa con el id " + actualizada.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la categoria: " + e.getMessage(), e);
        }
    }

    // Baja logica: la fila se conserva porque recursos y reservas historicas la referencian.
    public void eliminar(String id) {
        String sql = "update categorias set activo = false "
                + "where id_categoria = ? and activo = true";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, obtenerIdCategoria(id));
            int filas = db.executeUpdate(stm);
            if (filas == 0) {
                throw new RuntimeException("No existe una categoria activa con el id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la categoria " + id + ": " + e.getMessage(), e);
        }
    }

    // Devuelve todas las categorias activas, ordenadas por id.
    public List<Categoria> obtenerTodos() {
        String sql = "select id_categoria, descripcion from categorias "
                + "where activo = true order by id_categoria";
        List<Categoria> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql);
             ResultSet rs = db.executeQuery(stm)) {
            while (rs.next()) {
                resultado.add(aCategoria(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener las categorias: " + e.getMessage(), e);
        }
        return resultado;
    }

    // Convierte la fila actual del ResultSet en un objeto Categoria (el DTO).
    private Categoria aCategoria(ResultSet rs) throws SQLException {
        return new Categoria(convertirId(rs.getInt("id_categoria")), rs.getString("descripcion"));
    }

    private boolean esIdValido(String id) {
        return id != null && id.matches(PREFIJO_CATEGORIA + "\\d+");
    }

    // CAT-000001 -> 1
    private int obtenerIdCategoria(String id) {
        if (!esIdValido(id)) {
            throw new IllegalArgumentException("Id de categoria invalido: " + id);
        }
        return Integer.parseInt(id.substring(PREFIJO_CATEGORIA.length()));
    }

    // 1 -> CAT-000001
    private String convertirId(int numero) {
        return String.format("%s%06d", PREFIJO_CATEGORIA, numero);
    }
}