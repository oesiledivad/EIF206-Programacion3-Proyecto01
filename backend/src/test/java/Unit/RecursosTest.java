package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecursosTest {

    @Mock
    private RecursoDatos datos;
    private RecursoLogic logica;

    @BeforeEach
    void setUp() {
        logica = new RecursoLogic(datos);
    }

    private Recurso recursoValido() {
        return new Recurso("", "", "");
    }

    // ---------- crear ----------

    @Test
    void crear_RetornaUnaExcepcion_IdExistente() {
        Recurso nuevo = recursoValido();
        nuevo.setId("RESCUR-000");
        Recurso existente = recursoValido();
        existente.setId("RESCUR-000");

        when(datos.leerPorId("RESCUR-000")).thenReturn(existente);

        assertThrows(IllegalArgumentException.class, () -> logica.crear(nuevo));
        verify(datos, never()).crear(any());
    }

    @Test
    void crear_RetornaExcepcion_IdNuloOVacio() {
        Recurso nuevo = recursoValido();
        nuevo.setId(null);
        assertThrows(IllegalArgumentException.class, () -> logica.crear(nuevo));

        Recurso nuevo2 = recursoValido();
        nuevo2.setId("");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(nuevo2));

        verify(datos, never()).crear(any());
    }

    @Test
    void crear_CreaRecursoValido_Exitoso() {
        Recurso nuevo = recursoValido();
        nuevo.setId("RESCUR-001");

        when(datos.leerPorId("RESCUR-001")).thenReturn(null);

        assertDoesNotThrow(() -> logica.crear(nuevo));
        verify(datos, times(1)).crear(nuevo);
    }

    // ---------- actualizar ----------



    @Test
    void actualizarRecurso_NoExiste_DevuelveExcepcion() {
        Recurso noExiste = recursoValido();
        noExiste.setId("RESCUR-999");

        when(datos.leerPorId("RESCUR-999")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> logica.actualizar(noExiste));
        verify(datos, never()).actualizar(any());
    }

    @Test
    void actualizarRecurso_ActualizaValido_Exitoso() {
        Recurso actualizado = recursoValido();
        actualizado.setId("RESCUR-002");
        when(datos.leerPorId("RESCUR-002")).thenReturn(actualizado);
        assertDoesNotThrow(() -> logica.actualizar(actualizado));
        verify(datos, times(1)).actualizar(actualizado);
    }

    // ---------- eliminar ----------

    @Test
    void eliminarExistenteDevuelveExcepccion() {
        when(datos.leerPorId("RESS-000")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("RESS-000"));
        verify(datos, never()).eliminar(any());
    }

    @Test
    void eliminar_EliminaRecursoExistente_Exitoso() {
        Recurso existente = recursoValido();
        existente.setId("RESS-001");

        when(datos.leerPorId("RESS-001")).thenReturn(existente);

        assertDoesNotThrow(() -> logica.eliminar("RESS-001"));
        verify(datos, times(1)).eliminar("RESS-001");
    }

    // ---------- existe ----------

    @Test
    void existe_DevuelveTrue_SiElRecursoExiste() {
        Recurso existente = recursoValido();
        existente.setId("RESS-002");

        when(datos.leerPorId("RESS-002")).thenReturn(existente);

        assertTrue(logica.existe("RESS-002"));
    }

    @Test
    void existe_DevuelveFalse_SiElRecursoNoExiste() {
        when(datos.leerPorId("RESS-003")).thenReturn(null);

        assertFalse(logica.existe("RESS-003"));
    }

    // ---------- obtenerTodos ----------

    @Test
    void obtenerTodos_DevuelveListaDeDatos() {
        Recurso r1 = recursoValido();
        r1.setId("R1");
        Recurso r2 = recursoValido();
        r2.setId("R2");

        when(datos.obtenerTodos()).thenReturn(Arrays.asList(r1, r2));

        List<Recurso> resultado = logica.obtenerTodos();

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(r1));
        assertTrue(resultado.contains(r2));
    }

    @Test
    void obtenerTodos_ListaVacia_DevuelveListaVacia() {
        when(datos.obtenerTodos()).thenReturn(Collections.emptyList());
        List<Recurso> resultado = logica.obtenerTodos();
        assertTrue(resultado.isEmpty());
    }
    // ---------- idRecursos ----------
    @Test
    void idRecursos_FiltraPorCategoria_DevuelveSoloLosIdsCorrectos() {
        Recurso r1 = recursoValido();
        r1.setId("R1");
        r1.setIdCategoria("CAT-1");
        Recurso r2 = recursoValido();
        r2.setId("R2");
        r2.setIdCategoria("CAT-2");
        when(datos.obtenerTodos()).thenReturn(Arrays.asList(r1, r2));
        List<String> ids = logica.idRecursos("CAT-1");
        assertEquals(1, ids.size());
        assertEquals("R1", ids.get(0));
    }
    // ---------- buscarPorId ----------
    @Test
    void buscarPorId_ExisteElRecurso_LoDevuelve() {
        Recurso r1 = recursoValido();
        r1.setId("R1");
        when(datos.obtenerTodos()).thenReturn(Collections.singletonList(r1));
        Recurso resultado = logica.buscarPorId("R1");
        assertNotNull(resultado);
        assertEquals("R1", resultado.getId());
    }

    @Test
    void buscarPorId_NoExiste_DevuelveNull() {
        when(datos.obtenerTodos()).thenReturn(Collections.emptyList());
        Recurso resultado = logica.buscarPorId("NO-EXISTE");
        assertNull(resultado);
    }

    // ---------- listaDeRecursosPorCategoria ----------

    @Test
    void listaDeRecursosPorCategoria_DevuelveSoloLosDeEsaCategoria() {
        Recurso r1 = recursoValido();
        r1.setId("R1");
        r1.setIdCategoria("CAT-1");
        Recurso r2 = recursoValido();
        r2.setId("R2");
        r2.setIdCategoria("CAT-2");
        when(datos.obtenerTodos()).thenReturn(Arrays.asList(r1, r2));
        List<Recurso> resultado = logica.listaDeRecursosPorCategoria("CAT-1");
        assertEquals(1, resultado.size());
        assertEquals("R1", resultado.get(0).getId());
    }

    // ---------- obtenerRecursosPorCategorias ----------

    @Test
    void obtenerRecursosPorCategorias_ListaNula_DevuelveNull() {
        assertNull(logica.obtenerRecursosPorCategorias(null));
    }

    @Test
    void obtenerRecursosPorCategorias_ListaVacia_DevuelveNull() {
        assertNull(logica.obtenerRecursosPorCategorias(Collections.emptyList()));
    }

    @Test
    void obtenerRecursosPorCategorias_SinCoincidencias_DevuelveNull() {
        Categoria cat = new Categoria();
        cat.setId("CAT-1");

        Recurso r1 = recursoValido();
        r1.setId("R1");
        r1.setIdCategoria("CAT-2");

        when(datos.obtenerTodos()).thenReturn(Collections.singletonList(r1));

        assertNull(logica.obtenerRecursosPorCategorias(Collections.singletonList(cat)));
    }

    @Test
    void obtenerRecursosPorCategorias_ConCoincidencias_DevuelveIdsUnidos() {
        Categoria cat = new Categoria();
        cat.setId("CAT-1");

        Recurso r1 = recursoValido();
        r1.setId("R1");
        r1.setIdCategoria("CAT-1");

        when(datos.obtenerTodos()).thenReturn(Collections.singletonList(r1));
        String resultado = logica.obtenerRecursosPorCategorias(Collections.singletonList(cat));

        assertEquals("R1", resultado);
    }
}