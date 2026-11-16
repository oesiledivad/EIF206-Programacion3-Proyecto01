package una.proyecto.datos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class RegistroSQLIntegracionIT {
    @TempDir Path temporal;

    @Test void registraConsultasRealesYConservaLlavesResultadosYTransacciones() throws Exception {
        Path archivo = temporal.resolve("backend-sql.log");
        try (var connection = new RegistroSQL(archivo).envolver(Database.instance().openConnection());
             var statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TEMPORARY TABLE sql_log_it (id INT PRIMARY KEY AUTO_INCREMENT, valor VARCHAR(80) UNIQUE)");
            connection.setAutoCommit(false);
            try (var insertar = connection.prepareStatement("INSERT INTO sql_log_it (valor) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                insertar.setString(1, "dato-privado-integracion");
                assertEquals(1, insertar.executeUpdate());
                try (var llaves = insertar.getGeneratedKeys()) {
                    assertTrue(llaves.next());
                    assertTrue(llaves.getInt(1) > 0);
                }
                connection.commit();
                SQLException error = assertThrows(SQLException.class, insertar::executeUpdate);
                assertEquals(1062, error.getErrorCode());
                connection.rollback();
            }
            try (var consulta = connection.prepareStatement("SELECT COUNT(*) FROM sql_log_it WHERE valor = ?")) {
                consulta.setString(1, "dato-privado-integracion");
                try (var rows = consulta.executeQuery()) {
                    assertTrue(rows.next());
                    assertEquals(1, rows.getInt(1));
                }
            }
        }
        String log = Files.readString(archivo);
        assertTrue(log.contains("INSERT INTO sql_log_it (valor) VALUES (?)"));
        assertTrue(log.contains("ERROR SQLState=23000 codigo=1062"));
        assertTrue(log.contains("| OK | COMMIT"));
        assertTrue(log.contains("| OK | ROLLBACK"));
        assertTrue(log.contains("SELECT COUNT(*) FROM sql_log_it WHERE valor = ?"));
        assertFalse(log.contains("dato-privado-integracion"));
    }
}
