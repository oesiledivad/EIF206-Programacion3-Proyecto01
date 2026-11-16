package Unit;

import org.junit.jupiter.api.Test;
import una.proyecto.pruebas.RequiereMySQL;

import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequiereMySQLTest {

    @Test
    void enCINuncaOmite() {
        assertFalse(RequiereMySQL.evaluar("jdbc:mysql://127.0.0.1:1/x", true).isDisabled());
    }

    @Test
    void sinUrlNoOmite() {
        assertFalse(RequiereMySQL.evaluar(null, false).isDisabled());
    }

    @Test
    void puertoAbiertoNoOmite() throws Exception {
        try (ServerSocket servidor = new ServerSocket(0)) {
            String url = "jdbc:mysql://127.0.0.1:" + servidor.getLocalPort() + "/sistema_reservas";
            assertFalse(RequiereMySQL.evaluar(url, false).isDisabled());
        }
    }

    @Test
    void puertoCerradoOmite() throws Exception {
        int puertoLibre;
        try (ServerSocket servidor = new ServerSocket(0)) {
            puertoLibre = servidor.getLocalPort();
        }
        String url = "jdbc:mysql://127.0.0.1:" + puertoLibre + "/sistema_reservas";
        assertTrue(RequiereMySQL.evaluar(url, false).isDisabled());
    }
}
