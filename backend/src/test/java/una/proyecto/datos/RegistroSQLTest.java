package una.proyecto.datos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistroSQLTest {
    @TempDir Path temporal;

    @Test void registraLaEjecucionSinParametrosNiConsumirResultados() throws Exception {
        Path archivo = temporal.resolve("sql.log");
        Connection real = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet rows = mock(ResultSet.class);
        String sql = "SELECT nombre FROM usuarios WHERE id_usuario = ? AND clave_hash = ?";
        when(real.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(rows);
        when(statement.getConnection()).thenReturn(real);
        Connection connection = new RegistroSQL(archivo).envolver(real);
        PreparedStatement consulta = connection.prepareStatement(sql);
        consulta.setString(1, "identificacion-privada");
        consulta.setString(2, "hash-privado");

        assertSame(rows, consulta.executeQuery());

        assertSame(connection, consulta.getConnection());
        assertSame(connection, connection.unwrap(Connection.class));
        assertSame(consulta, consulta.unwrap(PreparedStatement.class));
        assertTrue(consulta.isWrapperFor(Statement.class));
        verify(statement).setString(1, "identificacion-privada");
        verify(statement).setString(2, "hash-privado");
        verifyNoInteractions(rows);
        String log = Files.readString(archivo);
        assertTrue(log.contains(sql));
        assertTrue(log.contains(" ms | OK | "));
        assertFalse(log.contains("identificacion-privada"));
        assertFalse(log.contains("hash-privado"));
        assertEquals(1, log.lines().count());
    }

    @Test void conservaLaExcepcionOriginalSinEscribirSuMensaje() throws Exception {
        Path archivo = temporal.resolve("sql.log");
        Connection real = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(real.prepareStatement("INSERT INTO usuarios (nombre) VALUES (?)")).thenReturn(statement);
        SQLException error = new SQLException("Duplicate entry 'dato-privado'", "23000", 1062);
        when(statement.executeUpdate()).thenThrow(error);
        var consulta = new RegistroSQL(archivo).envolver(real).prepareStatement("INSERT INTO usuarios (nombre) VALUES (?)");

        assertSame(error, assertThrows(SQLException.class, consulta::executeUpdate));

        String log = Files.readString(archivo);
        assertTrue(log.contains("ERROR SQLState=23000 codigo=1062"));
        assertFalse(log.contains("dato-privado"));
        assertFalse(log.contains("Duplicate entry"));
    }

    @Test void ocultaLiteralesComentariosYNumerosEnSentenciasDirectas() throws Exception {
        Path archivo = temporal.resolve("sql.log");
        Connection real = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(real.createStatement()).thenReturn(statement);
        var consulta = new RegistroSQL(archivo).envolver(real).createStatement();
        String sql = "SELECT `campo123`, 'secreto''doble', 'barra\\\'oculta', \"texto privado\", 12345 "
                + "/* comentario privado */ FROM tabla -- dato privado\n WHERE id = 0xFF # otro dato privado";

        consulta.execute(sql);
        consulta.execute("SELECT 'literal sin cierre privado");
        consulta.execute("SELECT 'termina en barra privada\\");

        String log = Files.readString(archivo);
        assertTrue(log.contains("SELECT `campo123`, ?, ?, ?, ? FROM tabla WHERE id = ?"));
        assertFalse(log.contains("secreto"));
        assertFalse(log.contains("oculta"));
        assertFalse(log.contains("privad"));
        assertFalse(log.contains("12345"));
        assertEquals(3, log.lines().count());
    }

    @Test void registraLotesYTransaccionesSinGuardarSusValores() throws Exception {
        Path archivo = temporal.resolve("sql.log");
        Connection real = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(real.prepareStatement("INSERT INTO tabla (valor) VALUES (?)")).thenReturn(statement);
        when(statement.executeBatch()).thenReturn(new int[]{1, 1});
        var connection = new RegistroSQL(archivo).envolver(real);
        var consulta = connection.prepareStatement("INSERT INTO tabla (valor) VALUES (?)");
        consulta.setString(1, "primer valor privado");
        consulta.addBatch();
        consulta.setString(1, "segundo valor privado");
        consulta.addBatch();

        assertArrayEquals(new int[]{1, 1}, consulta.executeBatch());
        connection.commit();
        connection.rollback();

        String log = Files.readString(archivo);
        assertTrue(log.contains("OK operaciones=2 | BATCH[2] INSERT INTO tabla (valor) VALUES (?)"));
        assertTrue(log.contains("| OK | COMMIT"));
        assertTrue(log.contains("| OK | ROLLBACK"));
        assertFalse(log.contains("valor privado"));
        verify(real).commit();
        verify(real).rollback();
    }

    @Test void unFalloDelArchivoNoImpideLaOperacionSQL() throws Exception {
        Path carpetaInvalida = temporal.resolve("archivo");
        Files.writeString(carpetaInvalida, "No es un directorio");
        Connection real = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(real.prepareStatement("UPDATE tabla SET valor = ?")).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(2);
        var consulta = new RegistroSQL(carpetaInvalida.resolve("sql.log")).envolver(real).prepareStatement("UPDATE tabla SET valor = ?");

        int primero = assertDoesNotThrow(() -> { return consulta.executeUpdate(); });
        int segundo = assertDoesNotThrow(() -> { return consulta.executeUpdate(); });
        assertEquals(2, primero);
        assertEquals(2, segundo);
    }

    @Test void consultasSimultaneasEscribenEntradasCompletas() throws Exception {
        Path archivo = temporal.resolve("sql.log");
        RegistroSQL registro = new RegistroSQL(archivo);
        var hilos = Executors.newFixedThreadPool(4);
        var tareas = new ArrayList<java.util.concurrent.Future<?>>();
        try {
            for (int i = 0; i < 24; i++) {
                tareas.add(hilos.submit(() -> {
                    Connection real = mock(Connection.class);
                    PreparedStatement statement = mock(PreparedStatement.class);
                    when(real.prepareStatement("UPDATE tabla SET valor = ?")).thenReturn(statement);
                    when(statement.executeUpdate()).thenReturn(1);
                    registro.envolver(real).prepareStatement("UPDATE tabla SET valor = ?").executeUpdate();
                    return null;
                }));
            }
            for (var tarea : tareas) tarea.get(5, TimeUnit.SECONDS);
        } finally {
            hilos.shutdownNow();
            assertTrue(hilos.awaitTermination(5, TimeUnit.SECONDS));
        }
        var lineas = Files.readAllLines(archivo);
        assertEquals(24, lineas.size());
        assertTrue(lineas.stream().allMatch(l -> l.endsWith("| OK filas=1 | UPDATE tabla SET valor = ?")));
    }
}
