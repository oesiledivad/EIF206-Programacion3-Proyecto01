package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.service.RecursoService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriasTest {

    private CategoriaLogic logica;
    private List<Categoria> store;

    @BeforeEach
    void setUp() throws Exception {
        store = new ArrayList<>();
        CategoriaDatos datos = new CategoriaDatos("") {
            @Override public List<Categoria> cargarTodo() { return new ArrayList<>(store); }
            @Override public List<Categoria> obtenerTodos() { return new ArrayList<>(store); }
            @Override public void crear(Categoria c) { store.add(c); }
            @Override public Categoria leerPorId(String id) {
                return store.stream().filter(c -> id.equals(c.getId())).findFirst().orElse(null);
            }
            @Override public void actualizar(Categoria c) {
                for (int i = 0; i < store.size(); i++) {
                    if (store.get(i).getId().equals(c.getId())) { store.set(i, c); return; }
                }
            }
            @Override public void eliminar(String id) { store.removeIf(c -> id.equals(c.getId())); }
            @Override public void guardarTodo(List<Categoria> lista) {}
        };

        RecursoService recursoServiceMock = new RecursoService(null) {
            @Override public List<Recurso> obtenerTodosRecursos() { return new ArrayList<>(); }
        };

        logica = new CategoriaLogic(datos, recursoServiceMock);
    }

    private Categoria categoriaValida(String descripcion) {
        return new Categoria(null, descripcion);
    }

    // ── obtenerTodos ──────────────────────────────────────────────────────────

    @Test
    void obtenerTodosSinCategoriasRetornaVacio() {
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    @Test
    void obtenerTodosRetornaTodasLasCategorias() {
        logica.crear(categoriaValida("Sala"));
        logica.crear(categoriaValida("Computadora"));
        assertEquals(2, logica.obtenerTodos().size());
    }

    // ── buscarPorDescripcion ──────────────────────────────────────────────────

    @Test
    void buscarPorDescripcionNullRetornaTodos() {
        logica.crear(categoriaValida("Sala"));
        assertEquals(1, logica.buscarPorDescripcion(null).size());
    }

    @Test
    void buscarPorDescripcionVacioRetornaTodos() {
        logica.crear(categoriaValida("Sala"));
        assertEquals(1, logica.buscarPorDescripcion("").size());
    }

    @Test
    void buscarPorDescripcionParcialEncuentraCoincidencias() {
        logica.crear(categoriaValida("Sala de reuniones"));
        assertFalse(logica.buscarPorDescripcion("Sala").isEmpty());
    }

    @Test
    void buscarPorDescripcionSinCoincidenciasRetornaVacio() {
        logica.crear(categoriaValida("Sala"));
        assertTrue(logica.buscarPorDescripcion("Proyector").isEmpty());
    }

    @Test
    void buscarPorDescripcionEsInsensibleAMayusculas() {
        logica.crear(categoriaValida("Sala"));
        assertFalse(logica.buscarPorDescripcion("sala").isEmpty());
    }

    // ── buscarPorId ───────────────────────────────────────────────────────────

    @Test
    void buscarPorIdNullRetornaNull() {
        assertNull(logica.buscarPorId(null));
    }

    @Test
    void buscarPorIdVacioRetornaNull() {
        assertNull(logica.buscarPorId(""));
    }

    @Test
    void buscarPorIdExistenteRetornaCategoria() {
        logica.crear(categoriaValida("Sala"));
        String id = store.get(0).getId();
        assertNotNull(logica.buscarPorId(id));
    }

    @Test
    void buscarPorIdInexistenteRetornaNull() {
        assertNull(logica.buscarPorId("CAT-999999"));
    }

    // ── crear ─────────────────────────────────────────────────────────────────

    @Test
    void crearDescripcionVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("")));
    }

    @Test
    void crearDescripcionNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(new Categoria(null, null)));
    }

    @Test
    void crearDescripcionSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("   ")));
    }

    @Test
    void crearDescripcionDuplicadaLanzaExcepcion() {
        logica.crear(categoriaValida("Sala"));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("Sala")));
    }

    @Test
    void crearDescripcionDuplicadaInsensibleAMayusculasLanzaExcepcion() {
        logica.crear(categoriaValida("Sala"));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("SALA")));
    }

    @Test
    void crearAsignaIdAutogenerado() {
        logica.crear(categoriaValida("Sala"));
        assertNotNull(store.get(0).getId());
        assertTrue(store.get(0).getId().startsWith("CAT-"));
    }

    @Test
    void crearPrimerIdEsCAT000001() {
        logica.crear(categoriaValida("Sala"));
        assertEquals("CAT-000001", store.get(0).getId());
    }

    @Test
    void crearSegundoIdEsCAT000002() {
        logica.crear(categoriaValida("Sala"));
        logica.crear(categoriaValida("Computadora"));
        assertEquals("CAT-000002", store.get(1).getId());
    }

    @Test
    void crearCategoriaCorrectamente() {
        logica.crear(categoriaValida("Proyector"));
        assertEquals(1, logica.obtenerTodos().size());
    }

    // ── actualizar ────────────────────────────────────────────────────────────

    @Test
    void actualizarIdNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria(null, "Sala")));
    }

    @Test
    void actualizarIdVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria("", "Sala")));
    }

    @Test
    void actualizarDescripcionVaciaLanzaExcepcion() {
        logica.crear(categoriaValida("Sala"));
        String id = store.get(0).getId();
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria(id, "")));
    }

    @Test
    void actualizarCategoriaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria("CAT-999999", "Sala")));
    }

    @Test
    void actualizarCategoriaCorrectamente() {
        logica.crear(categoriaValida("Sala"));
        String id = store.get(0).getId();
        logica.actualizar(new Categoria(id, "Sala de reuniones"));
        assertEquals("Sala de reuniones", logica.buscarPorId(id).getDescripcion());
    }

    // ── eliminar ──────────────────────────────────────────────────────────────

    @Test
    void eliminarCategoriaInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("CAT-999999"));
    }

    @Test
    void eliminarCategoriaCorrectamente() {
        logica.crear(categoriaValida("Sala"));
        String id = store.get(0).getId();
        logica.eliminar(id);
        assertNull(logica.buscarPorId(id));
    }

    @Test
    void eliminarReduceElTotal() {
        logica.crear(categoriaValida("Sala"));
        logica.crear(categoriaValida("Computadora"));
        String id = store.get(0).getId();
        logica.eliminar(id);
        assertEquals(1, logica.obtenerTodos().size());
    }

    // ── categoriasSinRecursos / categoriaConRecursos ──────────────────────────

    @Test
    void categoriasSinRecursosConListaVaciaRetornaVacio() {
        assertTrue(logica.categoriasSinRecursos(new ArrayList<>()).isEmpty());
    }

    @Test
    void categoriasSinRecursosConNullRetornaVacio() {
        assertTrue(logica.categoriasSinRecursos(null).isEmpty());
    }

    @Test
    void categoriasSinRecursosRetornaCategoriaSinRecurso() {
        logica.crear(categoriaValida("Sala"));
        // el mock de RecursoService devuelve lista vacía → todas sin recursos
        List<Categoria> resultado = logica.categoriasSinRecursos(logica.obtenerTodos());
        assertEquals(1, resultado.size());
    }

    @Test
    void categoriaConRecursosRetornaVacioSiNingunaTieneRecurso() {
        logica.crear(categoriaValida("Sala"));
        List<Categoria> resultado = logica.categoriaConRecursos(logica.obtenerTodos());
        assertTrue(resultado.isEmpty());
    }
}
