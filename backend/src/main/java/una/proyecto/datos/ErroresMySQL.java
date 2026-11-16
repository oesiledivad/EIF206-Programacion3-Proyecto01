package una.proyecto.datos;

import java.sql.SQLException;

// Interpreta los codigos de error de MySQL.
// Solo la usan las clases del paquete datos: el resto de la aplicacion
// nunca debe conocer estos numeros.
final class ErroresMySQL {

    private static final int DUPLICADO = 1062;
    private static final int LLAVE_FORANEA = 1452;
    private static final int LLAVE_FORANEA_SIN_DETALLE = 1216;
    private static final int RESTRICCION_CHECK = 3819;

    private ErroresMySQL() {
    }

    static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == DUPLICADO;
    }

    // MySQL usa 1452 o 1216 para la misma violacion, segun los permisos del usuario.
    static boolean esLlaveForanea(SQLException e) {
        return e.getErrorCode() == LLAVE_FORANEA
                || e.getErrorCode() == LLAVE_FORANEA_SIN_DETALLE;
    }

    // Una restriccion CHECK de la tabla no se cumplio (por ejemplo, el horario de la reserva).
    static boolean esRestriccionCheck(SQLException e) {
        return e.getErrorCode() == RESTRICCION_CHECK;
    }
}