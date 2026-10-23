package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Recurso;
import una.proyecto.service.RecursoService;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;
public class RecursosIntegracionIT {

    private static final String XML_TEST = "target/test-it-recursos.xml";
    private RecursoService service;
    //----Creamos carpeta si no existe, limpiamos el xml para evitar aceptaciones o rechazos de test
    //    y creamos el service con su nuevo xml, esto con datos limpios. Esto se ejecuta antes de cada test.
    @BeforeEach
    void setUp() throws Exception {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        service = new RecursoService(new RecursoLogic(new RecursoDatos(XML_TEST)));
    }
    //---Limpiamos cada xml luego de ejecuatar un test---
    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }
    @Test

    void obtener_todos_recursos_devuelve_vacio_si_no_hay_en_el_xml(){
        assertNotNull(service.obtenerTodosRecursos());
        assertTrue(service.obtenerTodosRecursos().isEmpty());
    }
    @Test
    void obtener_todos_recursos_devuelve_recursos_extraidos_xml(){
        service.save(new Recurso("RE-100", "CAT-100", "Sala"));
        service.save(new Recurso("RE-101", "CAT-100", "Sala para 10"));
        Recurso r1 = service.recurPorId("RE-100");
        Recurso r2 = service.recurPorId("RE-101");
        assertNotNull(r1);
        assertNotNull(r2);
        assertEquals("RE-100", r1.getId());
        assertEquals("RE-101", r2.getId());
        assertEquals(2, service.obtenerTodosRecursos().size());
    }
    @Test
    void crear_recurso_valido(){
        service.save(new Recurso("RE-100", "CAT-100", "Sala"));
        Recurso r1 = service.recurPorId("RE-100");
        assertNotNull(r1);
        assertEquals("RE-100", r1.getId());
        assertEquals(1, service.obtenerTodosRecursos().size());
    }
    @Test
    void crear_recurso_existente(){
        service.save(new Recurso("RE-100", "CAT-100", "Sala"));
        Recurso r1 = service.recurPorId("RE-100");
        assertNotNull(r1);
        assertThrows(IllegalArgumentException.class, () -> service.save(r1));
    }
    @Test
    void actualizar_recurso_Inexistente(){
        Recurso r1 = new Recurso("RE-100", "CAT-100", "Sala");
        assertThrows(IllegalArgumentException.class, () ->service.update(r1));
    }
    @Test
    void eliminar_recurso_inexistente(){
        Recurso r1 = new Recurso("RE-100", "CAT-100", "Sala");
        assertThrows(IllegalArgumentException.class, () ->service.delete(r1.getId()));
    }
    @Test
    void verifica_si_elimina_correctamente_recurso_existente(){
        service.save(new Recurso("RE-100", "CAT-100", "Sala"));
        Recurso r1 = service.recurPorId("RE-100");
        assertNotNull(r1);
        assertEquals(1, service.obtenerTodosRecursos().size());
        service.delete(r1.getId());
        Recurso r2 = service.recurPorId("RE-100");
        assertNull(r2);
        assertEquals(0, service.obtenerTodosRecursos().size());
    }
    @Test
    void obtener_recursos_por_categoria_especifica() {
        Recurso r1 = new Recurso("RE-100", "CAT-101", "Sala1");
        Recurso r2 = new Recurso("RE-101", "CAT-101", "Sala2");
        Recurso r3 = new Recurso("RE-102", "CAT-101", "Sala3");
        service.save(r1);
        service.save(r2);
        service.save(r3);
        assertNotNull(r1);
        assertNotNull(r2);
        assertNotNull(r3);
        assertEquals(3, service.recursosPorCategoria("CAT-101").size());
    }
}
