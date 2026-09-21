package Unit;

import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import una.proyecto.logic.ia.aiGenerator;
import una.proyecto.model.Categoria;
import una.proyecto.model.Reserva;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class IAgeneratorTest {

    private enum Fallo {
        SIN_API_KEY,
        FRASE_RECHAZADA_POR_IA,
        ERROR_API,
        JSON_INVALIDO,
        FECHA_HORA_INVALIDA,
        RED_O_TIMEOUT,
        OTRO
    }

    private List<Categoria> categorias;
    private HttpClient mockHttpClient;

    @BeforeEach
    void setUp() {
        categorias = new ArrayList<>();
        categorias.add(crearCategoria("Proyector"));
        categorias.add(crearCategoria("Sala de reuniones"));
        categorias.add(crearCategoria("Laptop"));

        mockHttpClient = Mockito.mock(HttpClient.class);
        aiGenerator.setHttpClient(mockHttpClient);
    }

    @AfterEach
    void tearDown() {
        aiGenerator.setHttpClient(null);
    }

    private Categoria crearCategoria(String descripcion) {
        Categoria c = new Categoria();
        c.setDescripcion(descripcion);
        return c;
    }

    private Fallo clasificar(Exception e) {
        if (e instanceof JSONException) return Fallo.JSON_INVALIDO;
        if (e instanceof DateTimeParseException) return Fallo.FECHA_HORA_INVALIDA;
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("API Key")) return Fallo.SIN_API_KEY;
        if (msg.startsWith("Error IA (400)")) return Fallo.FRASE_RECHAZADA_POR_IA;
        if (msg.startsWith("Error IA")) return Fallo.ERROR_API;
        return Fallo.OTRO;
    }

    @Test
    void reserva_con_frase_invalida_simulada() throws Exception {
        String jsonSimulado = "{\"choices\": [{\"message\": {\"content\": \"{\\\"actividad\\\": \\\"\\\", \\\"fecha\\\": \\\"" + LocalDate.now() + "\\\", \\\"horaInicio\\\": \\\"09:00\\\", \\\"horaFin\\\": \\\"10:00\\\", \\\"categorias\\\": []}\"}}]}";

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);

        doReturn(200).when(mockResponse).statusCode();
        doReturn(jsonSimulado).when(mockResponse).body();
        doReturn(mockResponse).when(mockHttpClient).send(any(), any());

        Reserva reserva = aiGenerator.extraeInformacion("asdfghjkl qwerty 12345 !!!", categorias);
        assertNotNull(reserva);
        assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
    }

    @Test
    void frase_vacia_simulada() throws Exception {
        String jsonSimulado = "{\"choices\": [{\"message\": {\"content\": \"{\\\"actividad\\\": \\\"\\\", \\\"fecha\\\": \\\"" + LocalDate.now() + "\\\", \\\"horaInicio\\\": \\\"09:00\\\", \\\"horaFin\\\": \\\"10:00\\\", \\\"categorias\\\": []}\"}}]}";

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);

        doReturn(200).when(mockResponse).statusCode();
        doReturn(jsonSimulado).when(mockResponse).body();
        doReturn(mockResponse).when(mockHttpClient).send(any(), any());

        Reserva reserva = aiGenerator.extraeInformacion("", categorias);
        assertNotNull(reserva);
        assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
    }

    @Test
    void sin_categorias_disponibles_simulado() throws Exception {
        String jsonSimulado = "{\"choices\": [{\"message\": {\"content\": \"{\\\"actividad\\\": \\\"Reunion\\\", \\\"fecha\\\": \\\"" + LocalDate.now() + "\\\", \\\"horaInicio\\\": \\\"09:00\\\", \\\"horaFin\\\": \\\"10:00\\\", \\\"categorias\\\": [\\\"Proyector\\\"]}\"}}]}";

        HttpResponse<String> mockResponse = Mockito.mock(HttpResponse.class);

        doReturn(200).when(mockResponse).statusCode();
        doReturn(jsonSimulado).when(mockResponse).body();
        doReturn(mockResponse).when(mockHttpClient).send(any(), any());

        Reserva reserva = aiGenerator.extraeInformacion("Necesito un proyector", new ArrayList<>());
        assertNotNull(reserva);
        assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
    }
}