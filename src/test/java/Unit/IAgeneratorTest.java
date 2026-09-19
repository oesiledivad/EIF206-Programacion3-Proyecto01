package Unit;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.logic.ia.aiGenerator;
import una.proyecto.model.Categoria;
import una.proyecto.model.Reserva;
// import una.proyecto....IAgenerator;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IAgeneratorTest {

    private enum Fallo {
        SIN_API_KEY,          // falta GROQ_API_KEY en el .env
        ERROR_API,            // Groq respondió con status != 200
        JSON_INVALIDO,        // la IA devolvió algo que no es JSON válido
        FECHA_HORA_INVALIDA,  // fecha u hora con formato incorrecto
        RED_O_TIMEOUT,        // sin conexión, timeout o hilo interrumpido
        OTRO
    }

    private List<Categoria> categorias;

    @BeforeEach
    void setUp() {
        categorias = new ArrayList<>();
        categorias.add(crearCategoria("Proyector"));
        categorias.add(crearCategoria("Sala de reuniones"));
        categorias.add(crearCategoria("Laptop"));
    }

    private Categoria crearCategoria(String descripcion) {
        Categoria c = new Categoria();
        c.setDescripcion(descripcion);
        return c;
    }

    /** Atrapa cada tipo de excepción que puede lanzar extraeInformacion. */
    private Fallo clasificar(Exception e) {
        if (e instanceof JSONException) {
            return Fallo.JSON_INVALIDO;
        }
        if (e instanceof DateTimeParseException) {
            return Fallo.FECHA_HORA_INVALIDA;
        }
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return Fallo.RED_O_TIMEOUT;
        }
        if (e instanceof IOException) { // incluye HttpTimeoutException y ConnectException
            return Fallo.RED_O_TIMEOUT;
        }
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("API Key")) {
            return Fallo.SIN_API_KEY;
        }
        if (msg.startsWith("Error IA")) {
            return Fallo.ERROR_API;
        }
        return Fallo.OTRO;
    }

    @Test
    void reserva_con_frase_invalida() {
        String fraseInvalida = "asdfghjkl qwerty 12345 !!!";

        try {
            Reserva reserva = aiGenerator.extraeInformacion(fraseInvalida, categorias);

            // Caso feliz: la IA respondió, pero no encontró categorías
            assertNotNull(reserva);
            assertTrue(reserva.getCategoriasDeRecursos().isEmpty(),
                    "Una frase inválida no debería producir categorías");

        } catch (Exception e) {
            Fallo fallo = clasificar(e);
            System.out.println("Excepción atrapada: " + fallo + " -> " + e.getMessage());

            switch (fallo) {

                case JSON_INVALIDO:
                case FECHA_HORA_INVALIDA:
                    break;

                // Estas son problemas de entorno o del método: el test debe fallar
                case SIN_API_KEY:
                    fail("Falta GROQ_API_KEY en el .env: " + e.getMessage());
                    break;
                case ERROR_API:
                    fail("Groq devolvió error: " + e.getMessage());
                    break;
                case RED_O_TIMEOUT:
                    fail("Problema de red o timeout: " + e.getMessage());
                    break;
                default:
                    fail("Excepción inesperada: " + e);
            }
        }
    }

    @Test
    void frase_vacia_atrapa_excepciones_del_metodo() {
        try {
            Reserva reserva = aiGenerator.extraeInformacion("", categorias);
            assertNotNull(reserva);
            assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
        } catch (Exception e) {
            Fallo fallo = clasificar(e);
            assertTrue(fallo == Fallo.JSON_INVALIDO || fallo == Fallo.FECHA_HORA_INVALIDA,
                    "Excepción no esperada (" + fallo + "): " + e.getMessage());
        }
    }

    @Test
    void sin_categorias_disponibles_no_asigna_ninguna() {
        try {
            Reserva reserva = aiGenerator.extraeInformacion(
                    "Necesito un proyector mañana de 9:00 a 10:00", new ArrayList<>());
            assertNotNull(reserva);
            assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
        } catch (Exception e) {
            Fallo fallo = clasificar(e);
            assertTrue(fallo == Fallo.JSON_INVALIDO || fallo == Fallo.FECHA_HORA_INVALIDA,
                    "Excepción no esperada (" + fallo + "): " + e.getMessage());
        }
    }
}