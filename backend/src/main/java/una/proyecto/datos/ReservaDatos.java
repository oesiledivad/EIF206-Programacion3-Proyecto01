package una.proyecto.datos;

import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDatos {

    private static final String PREFIJO_RESERVA = "RES-";
    private static final String PREFIJO_CATEGORIA = "CAT-";
    private static final String COLUMNAS =
            "id_reserva, id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado";

    private final Database db;

    public ReservaDatos() {
        this.db = Database.instance();
    }

    // Guarda la reserva y sus recursos asignados en una sola transaccion.
    // Si algo falla, no queda ni la reserva ni sus recursos (rollback).
    public void crear(Reserva nuevoDto) {
        String sqlReserva = "insert into reservas (id_funcionario, actividad, fecha, hora_inicio, hora_fin) "
                + "values (?, ?, ?, ?, ?)";
        String sqlRecurso = "insert into reserva_recurso (id_reserva, id_recurso) values (?, ?)";

        try (Connection cnx = db.openConnection()) {
            cnx.setAutoCommit(false);
            try {
                int idReserva;
                try (PreparedStatement stm = cnx.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                    stm.setString(1, nuevoDto.getIdFuncionario());
                    stm.setString(2, nuevoDto.getActividad());
                    stm.setObject(3, nuevoDto.getFecha());
                    stm.setObject(4, nuevoDto.getHoraInicio());
                    stm.setObject(5, nuevoDto.getHoraFin());
                    stm.executeUpdate();
                    try (ResultSet llaves = stm.getGeneratedKeys()) {
                        if (!llaves.next()) {
                            throw new SQLException("MySQL no devolvio el id de la reserva creada");
                        }
                        idReserva = llaves.getInt(1);
                    }
                }

                List<String> recursos = nuevoDto.getRecursosAsignadosIds();
                if (recursos != null) {
                    try (PreparedStatement stm = cnx.prepareStatement(sqlRecurso)) {
                        for (String idRecurso : recursos) {
                            stm.setInt(1, idReserva);
                            stm.setString(2, idRecurso);
                            stm.executeUpdate();
                        }
                    }
                }

                cnx.commit();
                nuevoDto.setId(convertirID(idReserva));
            } catch (SQLException e) {
                cnx.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if (ErroresMySQL.esLlaveForanea(e)) {
                if (e.getMessage() != null && e.getMessage().contains("fk_asignacion_recurso")) {
                    throw new RuntimeException("Uno de los recursos asignados no existe", e);
                }
                throw new RuntimeException(
                        "El funcionario " + nuevoDto.getIdFuncionario() + " no existe", e);
            }
            if (ErroresMySQL.esRestriccionCheck(e)) {
                throw new RuntimeException(
                        "La hora de inicio debe ser anterior a la hora de fin", e);
            }
            throw new RuntimeException("Error al crear la reserva: " + e.getMessage(), e);
        }
    }

    // Devuelve la reserva activa con ese id (con sus recursos), o null si no existe o esta cancelada.
    public Reserva leerPorId(String id) {
        int num = obtenerIdReserva(id);
        String sql = "select " + COLUMNAS + " from reservas "
                + "where id_reserva = ? and estado = 'ACTIVA'";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, num);
            Reserva reserva = null;
            try (ResultSet rs = db.executeQuery(stm)) {
                if (rs.next()) {
                    reserva = aReserva(rs);
                }
            }
            if (reserva != null) {
                cargarAsignaciones(reserva);
            }
            return reserva;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la reserva " + id + ": " + e.getMessage(), e);
        }
    }

    // Devuelve todas las reservas activas (con sus recursos), ordenadas por fecha y hora.
    public List<Reserva> obtenerTodos() {
        String sql = "select " + COLUMNAS + " from reservas "
                + "where estado = 'ACTIVA' order by fecha, hora_inicio";
        List<Reserva> resultado = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            try (ResultSet rs = db.executeQuery(stm)) {
                while (rs.next()) {
                    resultado.add(aReserva(rs));
                }
            }
            for (Reserva reserva : resultado) {
                cargarAsignaciones(reserva);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener las reservas: " + e.getMessage(), e);
        }
        return resultado;
    }

    // Cancela la reserva: cambia su estado a CANCELADA y conserva la fila como historial.
    public void eliminar(String id) {
        int num = obtenerIdReserva(id);
        String sql = "update reservas set estado = 'CANCELADA' "
                + "where id_reserva = ? and estado = 'ACTIVA'";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, num);
            int filas = db.executeUpdate(stm);
            if (filas == 0) {
                throw new RuntimeException("No existe una reserva activa con el id " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cancelar la reserva " + id + ": " + e.getMessage(), e);
        }
    }

    // Convierte la fila actual del ResultSet en un objeto Reserva (sin recursos todavia).
    private Reserva aReserva(ResultSet rs) throws SQLException {
        Reserva reserva = new Reserva();
        reserva.setId(convertirID(rs.getInt("id_reserva")));
        reserva.setIdFuncionario(rs.getString("id_funcionario"));
        reserva.setActividad(rs.getString("actividad"));
        reserva.setFecha(rs.getObject("fecha", LocalDate.class));
        reserva.setHoraInicio(rs.getObject("hora_inicio", LocalTime.class));
        reserva.setHoraFin(rs.getObject("hora_fin", LocalTime.class));
        reserva.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        reserva.setRecursosAsignadosIds(new ArrayList<>());
        reserva.setCategoriasDeRecursosIds(new ArrayList<>());
        return reserva;
    }

    // Llena los recursos asignados y sus categorias leyendo reserva_recurso + recursos.
    private void cargarAsignaciones(Reserva reserva) throws SQLException {
        String sql = "select rr.id_recurso, r.id_categoria from reserva_recurso rr "
                + "join recursos r on r.id_recurso = rr.id_recurso "
                + "where rr.id_reserva = ? order by rr.id_recurso";
        List<String> recursos = new ArrayList<>();
        List<String> categorias = new ArrayList<>();
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, obtenerIdReserva(reserva.getId()));
            try (ResultSet rs = db.executeQuery(stm)) {
                while (rs.next()) {
                    recursos.add(rs.getString("id_recurso"));
                    String idCategoria = String.format("%s%06d", PREFIJO_CATEGORIA, rs.getInt("id_categoria"));
                    if (!categorias.contains(idCategoria)) {
                        categorias.add(idCategoria);
                    }
                }
            }
        }
        reserva.setRecursosAsignadosIds(recursos);
        reserva.setCategoriasDeRecursosIds(categorias);
    }

    // RES-000001 -> 1
    private int obtenerIdReserva(String id) {
        if (id == null || !id.startsWith(PREFIJO_RESERVA)) {
            throw new IllegalArgumentException("Id de reserva invalido: " + id);
        }
        try {
            return Integer.parseInt(id.substring(PREFIJO_RESERVA.length()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Id de reserva invalido: " + id, e);
        }
    }

    // 1 -> RES-000001
    private String convertirID(int n) {
        return String.format("%s%06d", PREFIJO_RESERVA, n);
    }
}