package una.proyecto.pruebas;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

// Omite las pruebas de integración cuando la base MySQL de pruebas no responde
// (por ejemplo, Docker apagado), en lugar de reportarlas como fallidas.
// Failsafe la activa para todas las clases *IT mediante autodetección de JUnit.
// En GitHub Actions (CI=true) no omite nada: allí la base debe estar arriba.
// Las clases que no usan la base se marcan con @Tag("sin-mysql").
public class RequiereMySQL implements ExecutionCondition {

    public static final String TAG_SIN_MYSQL = "sin-mysql";
    private static final int ESPERA_MS = 1500;

    private static ConditionEvaluationResult resultado;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (context.getTags().contains(TAG_SIN_MYSQL)) {
            return ConditionEvaluationResult.enabled("La prueba no usa MySQL");
        }
        return evaluarUnaVez();
    }

    private static synchronized ConditionEvaluationResult evaluarUnaVez() {
        if (resultado == null) {
            resultado = evaluar(System.getenv("DB_URL"), "true".equalsIgnoreCase(System.getenv("CI")));
        }
        return resultado;
    }

    public static ConditionEvaluationResult evaluar(String jdbcUrl, boolean enCI) {
        if (enCI) {
            return ConditionEvaluationResult.enabled("En CI la base de pruebas es obligatoria");
        }
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")) {
            return ConditionEvaluationResult.enabled("Sin DB_URL: no se puede comprobar la base");
        }
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String host = uri.getHost() == null ? "localhost" : uri.getHost();
        int puerto = uri.getPort() == -1 ? 3306 : uri.getPort();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), ESPERA_MS);
            return ConditionEvaluationResult.enabled("MySQL responde en " + host + ":" + puerto);
        } catch (IOException e) {
            return ConditionEvaluationResult.disabled("MySQL de pruebas no responde en " + host + ":" + puerto
                    + ". Inicie Docker y ejecute: docker compose -f compose.test.yaml up -d --wait");
        }
    }
}
