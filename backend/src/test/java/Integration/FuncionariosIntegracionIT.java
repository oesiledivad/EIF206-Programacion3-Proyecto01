package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.service.FuncionarioService;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración reales: FuncionarioService → FuncionarioLogic → UsuarioDatos → XML.
 * Usa un archivo XML temporal en target/ para no tocar los datos originales del proyecto.
 */
public class FuncionariosIntegracionIT {

    private static final String XML_TEST = "target/test-it-usuarios.xml";
    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        service = new FuncionarioService(new FuncionarioLogic(new UsuarioDatos(XML_TEST)));
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }

    private Funcionario funcionarioValido(String id) {
        return new Funcionario(id, "FUNCIONARIO", "Juan Perez", "88887777");
    }

    // Integración CRUD: guardar un funcionario y leerlo desde el XML real
    @Test
    void crearYLeerFuncionarioDesdeXML() {
        service.addEmployee(funcionarioValido("111111111"));
        Funcionario resultado = service.obetenerUsuarioPorId("111111111");
        assertNotNull(resultado);
        assertEquals("111111111", resultado.getId());
        assertEquals("Juan Perez", resultado.getName());
    }

    // Integración CRUD: actualizar nombre y teléfono y verificar cambio persistido en XML
    @Test
    void actualizarFuncionarioPersisteCambioEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        service.updateEmployee(new Funcionario("111111111", "FUNCIONARIO", "Maria Lopez", "77776666"));
        Funcionario leido = service.obetenerUsuarioPorId("111111111");
        assertEquals("Maria Lopez", leido.getName());
        assertEquals("77776666", leido.getPhone());
    }

    // Integración CRUD: actualizar conserva la clave original en el XML
    @Test
    void actualizarFuncionarioConservaClaveEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        String claveOriginal = service.obetenerUsuarioPorId("111111111").getPassword();
        service.updateEmployee(new Funcionario("111111111", "FUNCIONARIO", "Maria Lopez", "77776666"));
        assertEquals(claveOriginal, service.obetenerUsuarioPorId("111111111").getPassword());
    }

    // Integración CRUD: eliminar un funcionario y verificar que ya no existe en el XML
    @Test
    void eliminarFuncionarioYaNoExisteEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        service.deleteEmployee("111111111");
        assertNull(service.obetenerUsuarioPorId("111111111"));
    }

    // Integración CRUD: crear varios funcionarios y obtenerlos todos desde el XML
    @Test
    void crearVariosFuncionariosYObtenerTodosDesdeXML() {
        service.addEmployee(funcionarioValido("111111111"));
        service.addEmployee(new Funcionario("222222222", "FUNCIONARIO", "Ana Gomez", "66665555"));
        assertEquals(2, service.getAllEmployees().size());
    }

    // Integración: buscar por id parcial encuentra funcionario persistido en XML
    @Test
    void buscarPorIdParcialEncuentraFuncionarioEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        assertFalse(service.findById("111").isEmpty());
    }

    // Integración: buscar por nombre parcial encuentra funcionario persistido en XML
    @Test
    void buscarPorNombreParcialEncuentraFuncionarioEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        assertFalse(service.findByName("Juan").isEmpty());
    }

    // Integración: cédula duplicada no genera un segundo registro en el XML
    @Test
    void crearCedulaDuplicadaNoGuardaSegundoRegistroEnXML() {
        service.addEmployee(funcionarioValido("111111111"));
        assertThrows(IllegalArgumentException.class,
                () -> service.addEmployee(funcionarioValido("111111111")));
        assertEquals(1, service.getAllEmployees().size());
    }
}