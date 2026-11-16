package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.Database;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: CategoriaService → CategoriaLogic → CategoriaDatos → MySQL.
 * Solo crea y borra categorías cuya descripción empieza con "IT-", para no tocar otros datos.
 */
public class CategoriasIntegracionIT {

    private static final String PREFIJO_PRUEBA = "IT-";

    private CategoriaService service;

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();
        // RecursoService vacío: estas pruebas no usan recursos.
        RecursoService sinRecursos = new RecursoService(null) {
            @Override public List<Recurso> obtenerTodosRecursos() {
                return new ArrayList<>();
            }
        };
        service = new CategoriaService(new CategoriaLogic(new CategoriaDatos(), sinRecursos));
    }

    @AfterEach
    void tearDown() throws Exception {
        limpiarDatosDePrueba();
    }

    // Borrado físico (no lógico) de las filas de prueba, para dejar la tabla como estaba.
    private void limpiarDatosDePrueba() throws Exception {
        String sql = "delete from categorias where descripcion like ?";
        try (PreparedStatement stm = Database.instance().prepareStatement(sql)) {
            stm.setString(1, PREFIJO_PRUEBA + "%");
            Database.instance().executeUpdate(stm);
        }
    }

    private long contarCategoriasDePrueba() {
        return service.obtenerTodas().stream()
                .filter(c -> c.getDescripcion().startsWith(PREFIJO_PRUEBA))
                .count();
    }

    // Lee "activo" directo de la tabla, sin pasar por el DAO (que oculta las inactivas).
    private Boolean activoEnBaseDeDatos(String idCategoria) throws Exception {
        int numero = Integer.parseInt(idCategoria.substring("CAT-".length()));
        try (PreparedStatement stm = Database.instance()
                .prepareStatement("select activo from categorias where id_categoria = ?")) {
            stm.setInt(1, numero);
            try (ResultSet rs = Database.instance().executeQuery(stm)) {
                return rs.next() ? rs.getBoolean("activo") : null;
            }
        }
    }

    @Test
    void crear_categoria_le_asigna_el_id_generado_por_mysql() {
        Categoria c = new Categoria(null, "IT-Sala");

        service.save(c);

        assertNotNull(c.getId());
        assertTrue(c.getId().matches("CAT-\\d{6}"), "Id recibido: " + c.getId());
    }

    @Test
    void crear_y_leer_categoria_conserva_sus_datos() {
        Categoria c = new Categoria(null, "IT-Sala de prueba");
        service.save(c);

        List<Categoria> encontradas = service.buscarPorDescripcion("IT-Sala de prueba");

        assertEquals(1, encontradas.size());
        assertEquals(c.getId(), encontradas.get(0).getId());
        assertEquals("IT-Sala de prueba", encontradas.get(0).getDescripcion());
    }

    @Test
    void dos_categorias_seguidas_reciben_ids_crecientes() {
        Categoria c1 = new Categoria(null, "IT-Sala");
        Categoria c2 = new Categoria(null, "IT-Laptop");

        service.save(c1);
        service.save(c2);

        int n1 = Integer.parseInt(c1.getId().substring(4));
        int n2 = Integer.parseInt(c2.getId().substring(4));
        assertTrue(n2 > n1, "Ids recibidos: " + c1.getId() + ", " + c2.getId());
    }

    @Test
    void crear_descripcion_duplicada_no_guarda_segundo_registro() {
        service.save(new Categoria(null, "IT-Sala"));

        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Categoria(null, "it-sala")));
        assertEquals(1, contarCategoriasDePrueba());
    }


    @Test
    void obtener_todas_incluye_las_categorias_creadas() {
        service.save(new Categoria(null, "IT-Sala"));
        service.save(new Categoria(null, "IT-Laptop"));

        assertEquals(2, contarCategoriasDePrueba());
    }

    @Test
    void buscar_por_descripcion_parcial_e_insensible_a_mayusculas() {
        service.save(new Categoria(null, "IT-Proyector HD"));

        assertFalse(service.buscarPorDescripcion("it-proyector").isEmpty());
    }


    @Test
    void actualizar_categoria_persiste_el_cambio() {
        Categoria c = new Categoria(null, "IT-Sala");
        service.save(c);

        service.update(new Categoria(c.getId(), "IT-Sala de reuniones"));

        List<Categoria> encontradas = service.buscarPorDescripcion("IT-Sala de reuniones");
        assertEquals(1, encontradas.size());
        assertEquals(c.getId(), encontradas.get(0).getId());
    }

    @Test
    void actualizar_categoria_inexistente_lanza_excepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> service.update(new Categoria("CAT-999999", "IT-No existe")));
    }


    @Test
    void eliminar_categoria_la_oculta_pero_conserva_la_fila() throws Exception {
        Categoria c = new Categoria(null, "IT-Sala");
        service.save(c);

        service.delete(c.getId());

        assertEquals(0, contarCategoriasDePrueba());
        assertEquals(Boolean.FALSE, activoEnBaseDeDatos(c.getId()));
    }

    @Test
    void eliminar_categoria_inexistente_lanza_excepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.delete("CAT-999999"));
    }
}