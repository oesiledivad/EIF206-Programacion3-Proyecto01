package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Funcionario;
import una.proyecto.service.FuncionarioService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import una.proyecto.datos.Database;

import static org.junit.jupiter.api.Assertions.*;

public class FuncionariosIntegracionIT {

    private FuncionarioService service;

    @BeforeEach
    void setUp() throws Exception {
        limpiar();
        service = new FuncionarioService(new FuncionarioLogic(new UsuarioDatos()));
    }

    @AfterEach
    void limpiar() throws Exception {
        try (Connection connection = Database.instance().openConnection()) {
            for (String table : new String[]{"funcionarios", "usuarios"}) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + table + " WHERE id_usuario IN ('991000011', '991000012')")) {
                    statement.executeUpdate();
                }
            }
        }
    }

    private Funcionario funcionarioValido(String id) {
        return new Funcionario(id, "FUNCIONARIO", "Juan Perez", "88887777");
    }

    // Integración CRUD: guardar un funcionario y leerlo desde el MySQL real
    @Test
    void crearYLeerFuncionarioDesdeMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        Funcionario resultado = service.obetenerUsuarioPorId("991000011");
        assertNotNull(resultado);
        assertEquals("991000011", resultado.getId());
        assertEquals("Juan Perez", resultado.getName());
    }

    // Integración CRUD: actualizar nombre y teléfono y verificar cambio persistido en MySQL
    @Test
    void actualizarFuncionarioPersisteCambioEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        service.updateEmployee(new Funcionario("991000011", "FUNCIONARIO", "Maria Lopez", "77776666"));
        Funcionario leido = service.obetenerUsuarioPorId("991000011");
        assertEquals("Maria Lopez", leido.getName());
        assertEquals("77776666", leido.getPhone());
    }

    // Integración CRUD: actualizar conserva la clave original en el MySQL
    @Test
    void actualizarFuncionarioConservaClaveEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        String claveOriginal = new UsuarioDatos().findPasswordHash("991000011");
        service.updateEmployee(new Funcionario("991000011", "FUNCIONARIO", "Maria Lopez", "77776666"));
        assertEquals(claveOriginal, new UsuarioDatos().findPasswordHash("991000011"));
    }

    // Integración CRUD: eliminar un funcionario y verificar que ya no existe en el MySQL
    @Test
    void eliminarFuncionarioYaNoExisteEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        service.deleteEmployee("991000011");
        assertNull(service.obetenerUsuarioPorId("991000011"));
    }

    // Integración CRUD: crear varios funcionarios y obtenerlos todos desde el MySQL
    @Test
    void crearVariosFuncionariosYObtenerTodosDesdeMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        service.addEmployee(new Funcionario("991000012", "FUNCIONARIO", "Ana Gomez", "66665555"));
        assertEquals(2, service.getAllEmployees().size());
    }

    // Integración: buscar por id parcial encuentra funcionario persistido en MySQL
    @Test
    void buscarPorIdParcialEncuentraFuncionarioEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        assertFalse(service.findById("991000011").isEmpty());
    }

    // Integración: buscar por nombre parcial encuentra funcionario persistido en MySQL
    @Test
    void buscarPorNombreParcialEncuentraFuncionarioEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        assertFalse(service.findByName("Juan").isEmpty());
    }

    // Integración: cédula duplicada no genera un segundo registro en el MySQL
    @Test
    void crearCedulaDuplicadaNoGuardaSegundoRegistroEnMySQL() {
        service.addEmployee(funcionarioValido("991000011"));
        assertThrows(IllegalArgumentException.class,
                () -> service.addEmployee(funcionarioValido("991000011")));
        assertEquals(1, service.getAllEmployees().size());
    }
}