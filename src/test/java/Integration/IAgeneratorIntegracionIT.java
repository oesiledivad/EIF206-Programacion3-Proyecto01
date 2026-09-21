package Integration;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.logic.ia.aiGenerator;
import una.proyecto.model.Categoria;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IAgeneratorIntegracionIT {

    private List<Categoria> categorias;

    @BeforeAll
    static void verificarApiKey() {
        String apiKey = Dotenv.configure().ignoreIfMissing().load().get("GROQ_API_KEY");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(),
                "GROQ_API_KEY no configurada, se omiten los tests de integración");
    }

    @BeforeEach
    void setUp() {
        aiGenerator.setHttpClient(null);

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

    private List<String> descripciones(Reserva reserva) {
        List<String> lista = new ArrayList<>();
        for (Categoria c : reserva.getCategoriasDeRecursos()) {
            lista.add(c.getDescripcion());
        }
        return lista;
    }

    @Test
    void frase_valida_extrae_todos_los_datos() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion(
                "Necesito un proyector mañana de 9:00 a 10:00 para una reunión de equipo",
                categorias);

        assertNotNull(reserva);
        assertFalse(reserva.getActividad().isBlank(), "La actividad no debe venir vacía");
        assertEquals(LocalDate.now().plusDays(1), reserva.getFecha());
        assertEquals(LocalTime.of(9, 0), reserva.getHoraInicio());
        assertEquals(LocalTime.of(10, 0), reserva.getHoraFin());
        assertTrue(descripciones(reserva).contains("Proyector"));
    }

    @Test
    void frase_con_varias_categorias() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion(
                "Reservar la sala de reuniones y un proyector mañana de 14:00 a 16:00",
                categorias);

        List<String> desc = descripciones(reserva);
        assertTrue(desc.contains("Sala de reuniones"));
        assertTrue(desc.contains("Proyector"));
    }

    @Test
    void hora_de_un_solo_digito_se_normaliza() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion(
                "Necesito una laptop mañana de 8 a 9 de la mañana para una capacitación",
                categorias);

        assertEquals(LocalTime.of(8, 0), reserva.getHoraInicio());
        assertEquals(LocalTime.of(9, 0), reserva.getHoraFin());
    }

    @Test
    void categorias_devueltas_solo_pertenecen_a_la_lista_disponible() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion(
                "Necesito un helicóptero y un proyector mañana de 9:00 a 10:00",
                categorias);

        List<String> permitidas = List.of("Proyector", "Sala de reuniones", "Laptop");
        for (String d : descripciones(reserva)) {
            assertTrue(permitidas.contains(d), "Categoría fuera de la lista: " + d);
        }
    }

    @Test
    void frase_invalida_no_produce_categorias() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion("asdfghjkl qwerty 12345 !!!", categorias);
        assertNotNull(reserva);
        assertTrue(reserva.getCategoriasDeRecursos().isEmpty(),
                "Una frase inválida no debería producir categorías");
    }

    @Test
    void sin_categorias_disponibles_no_asigna_ninguna() throws Exception {
        Reserva reserva = aiGenerator.extraeInformacion(
                "Necesito un proyector mañana de 9:00 a 10:00", new ArrayList<>());

        assertTrue(reserva.getCategoriasDeRecursos().isEmpty());
    }
}