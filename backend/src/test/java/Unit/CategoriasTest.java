package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.service.RecursoService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoriasTest {

    @Mock
    private CategoriaDatos datos;

    @Mock
    private RecursoService recursoService;

    private CategoriaLogic logica;

    @BeforeEach
    void setUp() {
        logica = new CategoriaLogic(datos, recursoService);
    }

    private Categoria categoriaValida(String descripcion) {
        return new Categoria(null, descripcion);
    }

    // ── obtenerTodos ──────────────────────────────────────────────────────────

    // Probar obtenerTodos() cuando no hay categorias — debe retornar lista vacia
    @Test
    void obtenerTodosSinCategoriasRetornaVacio() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // Probar obtenerTodos() — debe retornar todas las categorias
    @Test
    void obtenerTodosRetornaTodasLasCategorias() {
        when(datos.obtenerTodos()).thenReturn(List.of(
                new Categoria("CAT-000001", "Sala"),
                new Categoria("CAT-000002", "Computadora")));
        assertEquals(2, logica.obtenerTodos().size());
    }

    // ── buscarPorDescripcion ──────────────────────────────────────────────────

    // Probar buscarPorDescripcion(null) — debe retornar todos
    @Test
    void buscarPorDescripcionNullRetornaTodos() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertEquals(1, logica.buscarPorDescripcion(null).size());
    }

    // Probar buscarPorDescripcion("") — debe retornar todos
    @Test
    void buscarPorDescripcionVacioRetornaTodos() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertEquals(1, logica.buscarPorDescripcion("").size());
    }

    // Probar buscarPorDescripcion() con termino parcial — debe encontrar coincidencias
    @Test
    void buscarPorDescripcionParcialEncuentraCoincidencias() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala de reuniones")));
        assertFalse(logica.buscarPorDescripcion("Sala").isEmpty());
    }

    // Probar buscarPorDescripcion() sin coincidencias — debe retornar lista vacia
    @Test
    void buscarPorDescripcionSinCoincidenciasRetornaVacio() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertTrue(logica.buscarPorDescripcion("Proyector").isEmpty());
    }

    // Probar buscarPorDescripcion() es insensible a mayusculas
    @Test
    void buscarPorDescripcionEsInsensibleAMayusculas() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertFalse(logica.buscarPorDescripcion("sala").isEmpty());
    }

    // ── buscarPorId ───────────────────────────────────────────────────────────

    // Probar buscarPorId(null) — debe retornar null
    @Test
    void buscarPorIdNullRetornaNull() {
        assertNull(logica.buscarPorId(null));
    }

    // Probar buscarPorId("") — debe retornar null
    @Test
    void buscarPorIdVacioRetornaNull() {
        assertNull(logica.buscarPorId(""));
    }

    // Probar buscarPorId() cuando la categoria existe
    @Test
    void buscarPorIdExistenteRetornaCategoria() {
        when(datos.leerPorId("CAT-000001")).thenReturn(new Categoria("CAT-000001", "Sala"));
        assertNotNull(logica.buscarPorId("CAT-000001"));
    }

    // Probar buscarPorId() cuando la categoria no existe — debe retornar null
    @Test
    void buscarPorIdInexistenteRetornaNull() {
        when(datos.leerPorId("CAT-999999")).thenReturn(null);
        assertNull(logica.buscarPorId("CAT-999999"));
    }

    // ── crear ─────────────────────────────────────────────────────────────────

    // Probar crear() con descripcion vacia — debe lanzar IllegalArgumentException
    @Test
    void crearDescripcionVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("")));
    }

    // Probar crear() con descripcion null — debe lanzar IllegalArgumentException
    @Test
    void crearDescripcionNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(new Categoria(null, null)));
    }

    // Probar crear() con descripcion de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearDescripcionSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("   ")));
    }

    // Probar crear() con descripcion duplicada — debe lanzar IllegalArgumentException
    @Test
    void crearDescripcionDuplicadaLanzaExcepcion() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("Sala")));
    }

    // Probar crear() con descripcion duplicada insensible a mayusculas — debe lanzar IllegalArgumentException
    @Test
    void crearDescripcionDuplicadaInsensibleAMayusculasLanzaExcepcion() {
        when(datos.obtenerTodos()).thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(categoriaValida("SALA")));
    }

    // Probar crear() correctamente — verificar que se llame a datos.crear()
    @Test
    void crearCategoriaCorrectamente() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        logica.crear(categoriaValida("Proyector"));
        verify(datos).crear(any());
    }

    // Verificar que crear() asigne un id con formato CAT-
    @Test
    void crearAsignaIdConFormatoCAT() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        Categoria c = categoriaValida("Sala");
        logica.crear(c);
        assertNotNull(c.getId());
        assertTrue(c.getId().startsWith("CAT-"));
    }

    // Verificar que el primer id autogenerado sea CAT-000001
    @Test
    void crearPrimerIdEsCAT000001() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        Categoria c = categoriaValida("Sala");
        logica.crear(c);
        assertEquals("CAT-000001", c.getId());
    }

    // Verificar que el segundo id autogenerado sea CAT-000002
    @Test
    void crearSegundoIdEsCAT000002() {
        when(datos.obtenerTodos())
                .thenReturn(new ArrayList<>())
                .thenReturn(List.of(new Categoria("CAT-000001", "Sala")));
        Categoria c1 = categoriaValida("Sala");
        logica.crear(c1);
        Categoria c2 = categoriaValida("Computadora");
        logica.crear(c2);
        assertEquals("CAT-000002", c2.getId());
    }

    // ── actualizar ────────────────────────────────────────────────────────────

    // Probar actualizar() con id null — debe lanzar IllegalArgumentException
    @Test
    void actualizarIdNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria(null, "Sala")));
    }

    // Probar actualizar() con id vacio — debe lanzar IllegalArgumentException
    @Test
    void actualizarIdVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria("", "Sala")));
    }

    // Probar actualizar() con descripcion vacia — debe lanzar IllegalArgumentException
    @Test
    void actualizarDescripcionVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria("CAT-000001", "")));
    }

    // Probar actualizar() con categoria inexistente — debe lanzar IllegalArgumentException
    @Test
    void actualizarCategoriaInexistenteLanzaExcepcion() {
        when(datos.leerPorId("CAT-999999")).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Categoria("CAT-999999", "Sala")));
    }

    // Probar actualizar() correctamente
    @Test
    void actualizarCategoriaCorrectamente() {
        when(datos.leerPorId("CAT-000001")).thenReturn(new Categoria("CAT-000001", "Sala"));
        logica.actualizar(new Categoria("CAT-000001", "Sala de reuniones"));
        verify(datos).actualizar(any());
    }

    // ── eliminar ──────────────────────────────────────────────────────────────

    // Probar eliminar() con categoria inexistente — debe lanzar IllegalArgumentException
    @Test
    void eliminarCategoriaInexistenteLanzaExcepcion() {
        when(datos.leerPorId("CAT-999999")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("CAT-999999"));
    }

    // Probar eliminar() correctamente
    @Test
    void eliminarCategoriaCorrectamente() {
        when(datos.leerPorId("CAT-000001")).thenReturn(new Categoria("CAT-000001", "Sala"));
        logica.eliminar("CAT-000001");
        verify(datos).eliminar("CAT-000001");
    }

    // ── categoriasSinRecursos / categoriaConRecursos ──────────────────────────

    // Probar categoriasSinRecursos() con lista vacia — debe retornar lista vacia
    @Test
    void categoriasSinRecursosConListaVaciaRetornaVacio() {
        assertTrue(logica.categoriasSinRecursos(new ArrayList<>()).isEmpty());
    }

    // Probar categoriasSinRecursos() con null — debe retornar lista vacia
    @Test
    void categoriasSinRecursosConNullRetornaVacio() {
        assertTrue(logica.categoriasSinRecursos(null).isEmpty());
    }

    // Probar categoriasSinRecursos() — debe retornar categorias sin recurso asignado
    @Test
    void categoriasSinRecursosRetornaCategoriaSinRecurso() {
        when(recursoService.obtenerTodosRecursos()).thenReturn(new ArrayList<>());
        List<Categoria> cats = List.of(new Categoria("CAT-000001", "Sala"));
        assertEquals(1, logica.categoriasSinRecursos(cats).size());
    }

    // Probar categoriaConRecursos() — debe retornar vacio si ninguna tiene recurso
    @Test
    void categoriaConRecursosRetornaVacioSiNingunaTieneRecurso() {
        when(recursoService.obtenerTodosRecursos()).thenReturn(new ArrayList<>());
        List<Categoria> cats = List.of(new Categoria("CAT-000001", "Sala"));
        assertTrue(logica.categoriaConRecursos(cats).isEmpty());
    }

    // Probar categoriaConRecursos() — debe retornar categoria que tiene recurso asignado
    @Test
    void categoriaConRecursosRetornaCategoriaConRecurso() {
        Recurso r = new Recurso("REC-001", "CAT-000001", "Sala A");
        when(recursoService.obtenerTodosRecursos()).thenReturn(List.of(r));
        List<Categoria> cats = List.of(new Categoria("CAT-000001", "Sala"));
        assertEquals(1, logica.categoriaConRecursos(cats).size());
    }
}
