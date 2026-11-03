package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.Database;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.model.Categoria;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Recurso;
import una.proyecto.service.RecursoService;

import java.sql.PreparedStatement;

import static org.junit.jupiter.api.Assertions.*;

public class RecursosIntegracionIT {

    // Todas las pruebas usan ids con este prefijo para no tocar otros datos.
    private static final String PREFIJO_PRUEBA = "IT-REC-";
    private String categoria1;
    private String categoria2;

    private RecursoService service;

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();
        Categoria primera = new Categoria(null, PREFIJO_PRUEBA + "Sala");
        Categoria segunda = new Categoria(null, PREFIJO_PRUEBA + "Laptop");
        CategoriaDatos categorias = new CategoriaDatos();
        categorias.crear(primera);
        categorias.crear(segunda);
        categoria1 = primera.getId();
        categoria2 = segunda.getId();
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
        try (PreparedStatement stm = Database.instance().prepareStatement("delete from categorias where descripcion like ?")) {
            stm.setString(1, PREFIJO_PRUEBA + "%");
            stm.executeUpdate();
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
        service.save(new Recurso("IT-REC-100", categoria1, "Sala de prueba"));

        Recurso guardado = service.recurPorId("IT-REC-100");
        assertNotNull(guardado);
        assertEquals("IT-REC-100", guardado.getId());
        assertEquals(categoria1, guardado.getIdCategoria());
        assertEquals("Sala de prueba", guardado.getDescripcion());
    }

    @Test
    void crear_recurso_existente_lanza_excepcion() {
        service.save(new Recurso("IT-REC-100", categoria1, "Sala de prueba"));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Recurso("IT-REC-100", categoria1, "Otra sala")));
    }

    @Test
    void crear_recurso_con_categoria_inexistente_lanza_excepcion() {
        RuntimeException error = assertThrows(RuntimeException.class,
                () -> service.save(new Recurso("IT-REC-100", "CAT-999999", "Sala de prueba")));

        assertTrue(error.getMessage().contains("no existe"),
                "Mensaje recibido: " + error.getMessage());
    }

    // ---------- leer ----------

    @Test
    void obtener_todos_incluye_los_recursos_creados() {
        service.save(new Recurso("IT-REC-100", categoria1, "Sala de prueba"));
        service.save(new Recurso("IT-REC-101", categoria2, "Laptop de prueba"));

        assertEquals(2, contarRecursosDePrueba());
    }

    @Test
    void leer_recurso_inexistente_devuelve_null() {
        assertNull(service.recurPorId("IT-REC-NO-EXISTE"));
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_recurso_existente_cambia_sus_datos() {
        service.save(new Recurso("IT-REC-100", categoria1, "Sala de prueba"));

        service.update(new Recurso("IT-REC-100", categoria2, "Descripcion nueva"));

        Recurso actualizado = service.recurPorId("IT-REC-100");
        assertEquals("Descripcion nueva", actualizado.getDescripcion());
        assertEquals(categoria2, actualizado.getIdCategoria());
    }

    @Test
    void actualizar_recurso_inexistente_lanza_excepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(new Recurso("IT-REC-NO-EXISTE", categoria1, "Sala")));
    }
}
