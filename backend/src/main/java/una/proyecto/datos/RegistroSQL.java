package una.proyecto.datos;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

final class RegistroSQL {
    private static final Object ESCRITURA = new Object();
    private static final Pattern LITERALES = Pattern.compile(
            "`(?:``|[^`])*`|/\\*[\\s\\S]*?(?:\\*/|$)|--(?=\\s|$)[^\\r\\n]*|#[^\\r\\n]*"
                    + "|'(?:''|\\\\[\\s\\S]?|[^'\\\\])*(?:'|$)"
                    + "|\"(?:\"\"|\\\\[\\s\\S]?|[^\"\\\\])*(?:\"|$)"
                    + "|(?<![\\p{L}\\p{N}_$])(?:0[xX][0-9a-fA-F]+|[0-9]+(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?)(?![\\p{L}\\p{N}_$])");
    private final Path archivo;
    private final AtomicBoolean avisoEmitido = new AtomicBoolean();

    RegistroSQL(Path archivo) { this.archivo = archivo.toAbsolutePath(); }

    Connection envolver(Connection connection) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (proxy, method, args) -> {
            String nombre = method.getName();
            if (esIdentidad(method)) return identidad(proxy, method, args);
            if (esWrapperPropio(proxy, method, args)) return nombre.equals("unwrap") ? proxy : true;
            if (nombre.equals("commit") || nombre.equals("rollback")) {
                return ejecutar(connection, method, args, nombre.toUpperCase(Locale.ROOT));
            }
            if (nombre.equals("prepareStatement") || nombre.equals("prepareCall")) {
                String sql = ocultarLiterales((String) args[0]);
                long inicio = System.nanoTime();
                try {
                    Object statement = invocar(connection, method, args);
                    Class<?> tipo = nombre.equals("prepareCall") ? CallableStatement.class : PreparedStatement.class;
                    return envolverStatement(statement, tipo, sql, (Connection) proxy);
                } catch (SQLException error) {
                    registrar(sql, System.nanoTime() - inicio, null, error);
                    throw error;
                }
            }
            Object resultado = invocar(connection, method, args);
            if (nombre.equals("createStatement")) return envolverStatement(resultado, Statement.class, null, (Connection) proxy);
            return resultado;
        });
    }

    private Object envolverStatement(Object statement, Class<?> tipo, String preparado, Connection connection) {
        List<String> lote = new ArrayList<>();
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{tipo}, (proxy, method, args) -> {
            String nombre = method.getName();
            if (esIdentidad(method)) return identidad(proxy, method, args);
            if (esWrapperPropio(proxy, method, args)) return nombre.equals("unwrap") ? proxy : true;
            if (nombre.equals("getConnection")) {
                invocar(statement, method, args);
                return connection;
            }
            if (nombre.equals("addBatch")) {
                Object resultado = invocar(statement, method, args);
                lote.add(preparado != null ? preparado : ocultarLiterales((String) args[0]));
                return resultado;
            }
            if (nombre.equals("clearBatch")) {
                Object resultado = invocar(statement, method, args);
                lote.clear();
                return resultado;
            }
            if (nombre.equals("executeBatch") || nombre.equals("executeLargeBatch")) {
                String sql = "BATCH[" + lote.size() + "] " + String.join("; ", lote.stream().distinct().toList());
                try { return ejecutar(statement, method, args, sql); }
                finally { lote.clear(); }
            }
            if (nombre.equals("executeQuery") || nombre.equals("executeUpdate") || nombre.equals("executeLargeUpdate") || nombre.equals("execute")) {
                String sql = args != null && args.length > 0 && args[0] instanceof String texto
                        ? ocultarLiterales(texto) : preparado;
                return ejecutar(statement, method, args, sql);
            }
            return invocar(statement, method, args);
        });
    }

    private Object ejecutar(Object destino, Method method, Object[] args, String sql) throws Throwable {
        long inicio = System.nanoTime();
        try {
            Object resultado = invocar(destino, method, args);
            registrar(sql, System.nanoTime() - inicio, resultado, null);
            return resultado;
        } catch (SQLException error) {
            registrar(sql, System.nanoTime() - inicio, null, error);
            throw error;
        }
    }

    private void registrar(String sql, long nanos, Object resultado, SQLException error) {
        String estado = "OK";
        if (error != null) {
            String sqlState = error.getSQLState();
            estado = "ERROR SQLState=" + (sqlState != null && sqlState.matches("[A-Z0-9]{5}") ? sqlState : "?")
                    + " codigo=" + error.getErrorCode();
        } else if (resultado instanceof Number) estado += " filas=" + resultado;
        else if (resultado instanceof int[] lote) estado += " operaciones=" + lote.length;
        else if (resultado instanceof long[] lote) estado += " operaciones=" + lote.length;
        String linea = Instant.now() + " | " + String.format(Locale.ROOT, "%.3f ms", nanos / 1_000_000.0)
                + " | " + estado + " | " + sql + System.lineSeparator();
        synchronized (ESCRITURA) {
            try {
                Files.createDirectories(archivo.getParent());
                // Abrir y cerrar por entrada evita retener el archivo durante un clean de Maven.
                Files.writeString(archivo, linea, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException | SecurityException fallo) {
                if (avisoEmitido.compareAndSet(false, true)) {
                    System.err.println("No se pudo escribir el log SQL en " + archivo + ". Las operaciones de base de datos continuarán.");
                }
            }
        }
    }

    private static String ocultarLiterales(String sql) {
        if (sql == null) return "[SQL no disponible]";
        var matcher = LITERALES.matcher(sql);
        StringBuilder salida = new StringBuilder();
        while (matcher.find()) {
            String token = matcher.group();
            String reemplazo = token.startsWith("`") ? token
                    : token.startsWith("/*") || token.startsWith("--") || token.startsWith("#") ? " " : "?";
            matcher.appendReplacement(salida, java.util.regex.Matcher.quoteReplacement(reemplazo));
        }
        matcher.appendTail(salida);
        return salida.toString().replaceAll("\\s+", " ").trim();
    }

    private static boolean esWrapperPropio(Object proxy, Method method, Object[] args) {
        return (method.getName().equals("unwrap") || method.getName().equals("isWrapperFor"))
                && args != null && args[0] instanceof Class<?> tipo && tipo.isInstance(proxy);
    }

    private static boolean esIdentidad(Method method) { return method.getDeclaringClass() == Object.class; }

    private static Object identidad(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "toString" -> "JDBC con registro SQL";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new IllegalStateException("Método Object no soportado: " + method.getName());
        };
    }

    private static Object invocar(Object destino, Method method, Object[] args) throws Throwable {
        try { return method.invoke(destino, args); }
        catch (InvocationTargetException error) { throw error.getCause(); }
    }
}
