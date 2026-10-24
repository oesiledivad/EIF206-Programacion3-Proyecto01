package una.proyecto.datos;
import una.proyecto.model.Recurso;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;


//=======================================
//Aca iran los imports de la base de datos.
//=======================================
import java.sql.ResultSet;

public class RecursoDatos {

    // Atributos, instancia de la conexion
     Database theInstance;
    private static final String PREFIJO_CATEGORIA = "CAT-";
    private static final int ERROR_DUPLICADO = 1062;
    private static final int ERROR_LLAVE_FORANEA = 1452;
    private static final int ERROR_LLAVE_FORANEA_SIN_DETALLE = 1216;


    public RecursoDatos()throws Exception{

        this.theInstance= Database.instance();
    }

    private boolean esErrorLlaveForanea(SQLException e) {
        return e.getErrorCode() == ERROR_LLAVE_FORANEA
                || e.getErrorCode() == ERROR_LLAVE_FORANEA_SIN_DETALLE;
    }
    public void crear(Recurso nuevoDto) {
        String sql = "insert into recursos (id_recurso, descripcion, id_categoria) values (?, ?, ?)";
        try (PreparedStatement stm = theInstance.prepareStatement(sql)) {
            stm.setString(1, nuevoDto.getId());
            stm.setString(2, nuevoDto.getDescripcion());
            stm.setInt(3, categoriaAId(nuevoDto.getIdCategoria()));
            theInstance.executeUpdate(stm);
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERROR_DUPLICADO) {
                throw new RuntimeException("Ya existe un recurso con el id " + nuevoDto.getId(), e);
            }
            if (esErrorLlaveForanea(e)) {
                throw new RuntimeException("La categoria " + nuevoDto.getIdCategoria() + " no existe", e);
            }
            throw new RuntimeException("Error de integridad al crear el recurso: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear el recurso: " + e.getMessage(), e);
        }
    }
    // Convierte el formato de la aplicacion (CAT-000001) al id de la tabla (1).
    private int categoriaAId(String idCategoria) {
        if (idCategoria == null || !idCategoria.startsWith(PREFIJO_CATEGORIA)) {
            throw new IllegalArgumentException("Id de categoria invalido: " + idCategoria);
        }
        try {
            return Integer.parseInt(idCategoria.substring(PREFIJO_CATEGORIA.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Id de categoria invalido: " + idCategoria, e);
        }
    }
    public Recurso leerPorId(String id) {
        String sql = "select id_recurso, descripcion, id_categoria from recursos "
                + "where id_recurso = ? and activo = true";
        try (PreparedStatement stm = theInstance.prepareStatement(sql)) {
            stm.setString(1, id);
            try (ResultSet rs = theInstance.executeQuery(stm)) {
                if (rs.next()) {
                    return aRecurso(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el recurso " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public void actualizar(Recurso dtoActualizado) {
        String sql = "update recursos set descripcion = ?, id_categoria = ? "
                + "where id_recurso = ? and activo = true";
        try (PreparedStatement stm = theInstance.prepareStatement(sql)) {
            stm.setString(1, dtoActualizado.getDescripcion());
            stm.setInt(2, categoriaAId(dtoActualizado.getIdCategoria()));
            stm.setString(3, dtoActualizado.getId());
            int filas = theInstance.executeUpdate(stm);
            if (filas == 0) {
                throw new RuntimeException("No existe un recurso activo con el id " + dtoActualizado.getId());
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getErrorCode() == ERROR_LLAVE_FORANEA) {
                throw new RuntimeException("La categoria " + dtoActualizado.getIdCategoria() + " no existe", e);
            }
            throw new RuntimeException("Error de integridad al actualizar el recurso: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el recurso: " + e.getMessage(), e);
        }
    }

    public void eliminar(String id) {
        String sql = "update recursos set activo = false where id_recurso = ? and activo = true";
        try (PreparedStatement stm = theInstance.prepareStatement(sql)) {
            stm.setString(1, id);
            int filas = theInstance.executeUpdate(stm);
            if (filas == 0) {
                throw new RuntimeException("No existe un recurso activo con el id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el recurso " + id + ": " + e.getMessage(), e);
        }
    }


    public List<Recurso> obtenerTodos() {
        String sql= "select id_recurso, descripcion, id_categoria from recursos " +"where activo = true order by id_recurso";
        List<Recurso> resultado = new ArrayList<>();
        try(PreparedStatement std= theInstance.prepareStatement(sql)){
            ResultSet resultSet= theInstance.executeQuery(std);
            while (resultSet.next()) {
                resultado.add(aRecurso(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener los recursos: " + e.getMessage(), e);
        }
        return resultado;
    }
    private Recurso aRecurso(ResultSet rs) throws SQLException {
        Recurso recurso = new Recurso();
        recurso.setId(rs.getString("id_recurso"));
        recurso.setDescripcion(rs.getString("descripcion"));
        recurso.setIdCategoria(idACategoria(rs.getInt("id_categoria")));
        return recurso;
    }
    private String idACategoria(int id) {
        return String.format("%s%06d", PREFIJO_CATEGORIA, id);
    }

}



