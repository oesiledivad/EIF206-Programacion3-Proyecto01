package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: CategoriaService → CategoriaLogic → CategoriaDatos → XML.
 * Usa un archivo XML temporal en target/ para no tocar los datos originales del proyecto.
 * RecursoService se pasa como null ya que los tests de esta clase no ejercen esa dependencia.
 */
public class CategoriasIntegracionIT {

    private static final String XML_TEST = "target/test-it-categorias.xml";
    private CategoriaService service;

    @BeforeEach
    void setUp() throws Exception {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        RecursoService sinRecursos = new RecursoService(null) {
            @Override public List<una.proyecto.model.Recurso> obtenerTodosRecursos() {
                return new java.util.ArrayList<>();
            }
        };
        service = new CategoriaService(new CategoriaLogic(new CategoriaDatos(XML_TEST), sinRecursos));
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }

    // Integración CRUD: guardar una categoría y leerla desde el XML real
    @Test
    void crearYLeerCategoriaDesdeXML() {
        Categoria c = new Categoria(null, "Sala de prueba");
        service.save(c);
        Categoria leida = service.buscarPorDescripcion("Sala de prueba").get(0);
        assertNotNull(leida);
        assertEquals("Sala de prueba", leida.getDescripcion());
    }

    // Integración CRUD: crear varias categorías y obtenerlas todas desde el XML
    @Test
    void crearVariasCategoriasYObtenerTodasDesdeXML() {
        service.save(new Categoria(null, "Sala IT"));
        service.save(new Categoria(null, "Laptop IT"));
        assertEquals(2, service.obtenerTodas().size());
    }

    // Integración CRUD: los ids se generan de forma secuencial y persisten en el XML
    @Test
    void crearCategoriasIdsSecuencialesPersistidosEnXML() {
        Categoria c1 = new Categoria(null, "Sala IT");
        Categoria c2 = new Categoria(null, "Laptop IT");
        service.save(c1);
        service.save(c2);
        assertEquals("CAT-000001", c1.getId());
        assertEquals("CAT-000002", c2.getId());
    }

    // Integración CRUD: actualizar descripción y verificar cambio persistido en XML
    @Test
    void actualizarCategoriaPersisteCambioEnXML() {
        Categoria c = new Categoria(null, "Sala IT");
        service.save(c);
        service.update(new Categoria(c.getId(), "Sala de reuniones IT"));
        assertEquals("Sala de reuniones IT",
                service.buscarPorDescripcion("Sala de reuniones IT").get(0).getDescripcion());
    }

    // Integración CRUD: eliminar una categoría y verificar que ya no existe en el XML
    @Test
    void eliminarCategoriaYaNoExisteEnXML() {
        Categoria c = new Categoria(null, "Sala IT");
        service.save(c);
        service.delete(c.getId());
        assertTrue(service.buscarPorDescripcion("Sala IT").isEmpty());
    }

    // Integración: buscar por descripción parcial encuentra categoría persistida en XML
    @Test
    void buscarPorDescripcionParcialEncuentraCategoriaEnXML() {
        service.save(new Categoria(null, "Sala de reuniones IT"));
        assertFalse(service.buscarPorDescripcion("Sala").isEmpty());
    }

    // Integración: buscar es insensible a mayúsculas sobre datos persistidos en XML
    @Test
    void buscarPorDescripcionInsensibleAMayusculasEnXML() {
        service.save(new Categoria(null, "Proyector IT"));
        assertFalse(service.buscarPorDescripcion("proyector it").isEmpty());
    }

    // Integración: descripción duplicada no genera un segundo registro en el XML
    @Test
    void crearDescripcionDuplicadaNoGuardaSegundoRegistroEnXML() {
        service.save(new Categoria(null, "Sala IT"));
        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Categoria(null, "Sala IT")));
        assertEquals(1, service.obtenerTodas().size());
    }
}