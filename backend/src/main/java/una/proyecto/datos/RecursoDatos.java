package una.proyecto.datos;

import java.time.LocalDate;
import java.time.LocalTime;
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
    //Aca se encuentran errores de la base de datos
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
    // Si el id pertenece a un recurso dado de baja, lo reactiva con los datos nuevos
    // (el número de activo identifica al mismo equipo físico). Si no, lo inserta.
    public void crear(Recurso nuevoDto) {
        String sqlReactivar = "update recursos set descripcion = ?, id_categoria = ?, activo = true "
                + "where id_recurso = ? and activo = false";
        String sqlInsertar = "insert into recursos (id_recurso, descripcion, id_categoria) values (?, ?, ?)";
        try {
            int idCategoria = categoriaAId(nuevoDto.getIdCategoria());

            try (PreparedStatement stm = theInstance.prepareStatement(sqlReactivar)) {
                stm.setString(1, nuevoDto.getDescripcion());
                stm.setInt(2, idCategoria);
                stm.setString(3, nuevoDto.getId());
                if (theInstance.executeUpdate(stm) == 1) {
                    return; // estaba dado de baja: quedó reactivado
                }
            }

            try (PreparedStatement stm = theInstance.prepareStatement(sqlInsertar)) {
                stm.setString(1, nuevoDto.getId());
                stm.setString(2, nuevoDto.getDescripcion());
                stm.setInt(3, idCategoria);
                theInstance.executeUpdate(stm);
            }
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

    // Baja lógica del recurso. En la misma transacción cancela sus reservas FUTURAS:
    // un recurso dado de baja no puede seguir comprometido. Las pasadas quedan como historial.
    public void eliminar(String id) {
        String sqlBaja = "update recursos set activo = false where id_recurso = ? and activo = true";
        String sqlCancelar = "update reservas r join reserva_recurso rr on rr.id_reserva = r.id_reserva "
                + "set r.estado = 'CANCELADA' "
                + "where rr.id_recurso = ? and r.estado = 'ACTIVA' "
                + "and (r.fecha > ? or (r.fecha = ? and r.hora_inicio > ?))";

        try (Connection cnx = theInstance.openConnection()) {
            cnx.setAutoCommit(false);
            try {
                int filas;
                try (PreparedStatement stm = cnx.prepareStatement(sqlBaja)) {
                    stm.setString(1, id);
                    filas = stm.executeUpdate();
                }
                if (filas == 0) {
                    cnx.rollback();
                    throw new RuntimeException("No existe un recurso activo con el id " + id);
                }

                LocalDate hoy = LocalDate.now();
                try (PreparedStatement stm = cnx.prepareStatement(sqlCancelar)) {
                    stm.setString(1, id);
                    stm.setObject(2, hoy);
                    stm.setObject(3, hoy);
                    stm.setObject(4, LocalTime.now());
                    stm.executeUpdate();
                }
                cnx.commit();
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
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



