package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.Database;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Recurso;
import una.proyecto.service.RecursoService;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class RecursosIntegracionIT {

    // Todas las pruebas usan ids con este prefijo para no tocar otros datos.
    private static final String PREFIJO_PRUEBA = "IT-";
    // Categorias que crea seed.sql (ids 1 y 2 en la tabla).
    private static final String CATEGORIA_1 = "CAT-000001";
    private static final String CATEGORIA_2 = "CAT-000002";

    private RecursoService service;

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();
        service = new RecursoService(new RecursoLogic(new RecursoDatos()));
    }

    @AfterEach
    void tearDown() throws Exception {
        limpiarDatosDePrueba();
    }

    private void limpiarDatosDePrueba() throws Exception {
        String sql = "delete from recursos where id_recurso like ?";
        try (PreparedStatement stm = Database.instance().prepareStatement(sql)) {
            stm.setString(1, PREFIJO_PRUEBA + "%");
            Database.instance().executeUpdate(stm);
        }
    }

    private long contarRecursosDePrueba() {
        return service.obtenerTodosRecursos().stream()
                .filter(r -> r.getId().startsWith(PREFIJO_PRUEBA))
                .count();
    }

    // ---------- crear ----------

    @Test
    void crear_recurso_valido_se_guarda_en_mysql() {
        service.save(new Recurso("IT-100", CATEGORIA_1, "Sala de prueba"));

        Recurso guardado = service.recurPorId("IT-100");
        assertNotNull(guardado);
        assertEquals("IT-100", guardado.getId());
        assertEquals(CATEGORIA_1, guardado.getIdCategoria());
        assertEquals("Sala de prueba", guardado.getDescripcion());
    }

    @Test
    void crear_recurso_existente_lanza_excepcion() {
        service.save(new Recurso("IT-100", CATEGORIA_1, "Sala de prueba"));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Recurso("IT-100", CATEGORIA_1, "Otra sala")));
    }

    @Test
    void crear_recurso_con_categoria_inexistente_lanza_excepcion() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.save(new Recurso("IT-100", "CAT-999999", "Sala de prueba")));

        assertTrue(error.getMessage().contains("no existe"),
                "Mensaje recibido: " + error.getMessage());
    }

    // ---------- leer ----------

    @Test
    void obtener_todos_incluye_los_recursos_creados() {
        service.save(new Recurso("IT-100", CATEGORIA_1, "Sala de prueba"));
        service.save(new Recurso("IT-101", CATEGORIA_2, "Laptop de prueba"));

        assertEquals(2, contarRecursosDePrueba());
    }

    @Test
    void leer_recurso_inexistente_devuelve_null() {
        assertNull(service.recurPorId("IT-NO-EXISTE"));
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_recurso_existente_cambia_sus_datos() {
        service.save(new Recurso("IT-100", CATEGORIA_1, "Sala de prueba"));

        service.update(new Recurso("IT-100", CATEGORIA_2, "Descripcion nueva"));

        Recurso actualizado = service.recurPorId("IT-100");
        assertEquals("Descripcion nueva", actualizado.getDescripcion());
        assertEquals(CATEGORIA_2, actualizado.getIdCategoria());
    }

    @Test
    void actualizar_recurso_inexistente_lanza_excepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(new Recurso("IT-NO-EXISTE", CATEGORIA_1, "Sala")));
    }
}