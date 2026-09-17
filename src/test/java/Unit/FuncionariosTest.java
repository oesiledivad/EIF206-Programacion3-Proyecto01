package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FuncionariosTest {

    private FuncionarioLogic logica;
    private List<Usuario> store;

    @BeforeEach
    void setUp() {
        store = new ArrayList<>();
        UsuarioDatos datos = new UsuarioDatos() {
            @Override public List<Usuario> loadUsers() { return new ArrayList<>(store); }
            @Override public boolean addUser(Usuario u) {
                if (findUserById(u.getId()) != null) return false;
                store.add(u); return true;
            }
            @Override public boolean updateUser(Usuario u) {
                for (int i = 0; i < store.size(); i++) {
                    if (store.get(i).getId().equalsIgnoreCase(u.getId())) { store.set(i, u); return true; }
                }
                return false;
            }
            @Override public boolean deleteUser(String id) { return store.removeIf(u -> u.getId().equalsIgnoreCase(id)); }
            @Override public Usuario findUserById(String id) {
                return store.stream().filter(u -> u.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
            }
        };
        logica = new FuncionarioLogic(datos);
    }

    private Funcionario funcionarioValido(String id) {
        return new Funcionario(id, "FUNCIONARIO", "Juan Perez", "88887777");
    }

    // Probar que buscarPorId("") retorne una lista vacía
    @Test
    void buscarPorIdVacioDebeRetornarListaVacia() {
        assertTrue(logica.buscarPorId("").isEmpty());
    }

    // Probar que buscarPorId(null) retorne una lista vacía
    @Test
    void buscarPorIdNullDebeRetornarListaVacia() {
        assertTrue(logica.buscarPorId(null).isEmpty());
    }

    // Probar que buscarPorNombre("") retorne una lista vacía
    @Test
    void buscarPorNombreVacio() {
        assertTrue(logica.buscarPorNombre("").isEmpty());
    }

    // Probar que buscarPorNombre(null) retorne una lista vacía
    @Test
    void buscarPorNombreNull() {
        assertTrue(logica.buscarPorNombre(null).isEmpty());
    }

    // Probar crear() con una cédula vacía — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaVaciaLanzaExcepcion() {
        Funcionario f = new Funcionario("", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con una cédula de menos de 9 dígitos — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaMenosDeNueveDigitosLanzaExcepcion() {
        Funcionario f = new Funcionario("12345", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con una cédula de más de 9 dígitos — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaMasDeNueveDigitosLanzaExcepcion() {
        Funcionario f = new Funcionario("1234567890", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con una cédula que contenga letras — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaConLetrasLanzaExcepcion() {
        Funcionario f = new Funcionario("12345678A", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con nombre vacío — debe lanzar IllegalArgumentException
    @Test
    void crearNombreVacioLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con nombre que contenga números — debe lanzar IllegalArgumentException
    @Test
    void crearNombreConNumerosLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan123", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con teléfono vacío — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoVacioLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() con teléfono que contenga letras — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoConLetrasLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "8888ABCD");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // Probar crear() correctamente — verificar que se cree el funcionario
    @Test
    void crearFuncionarioCorrectamente() {
        logica.crear(funcionarioValido("123456789"));
        assertNotNull(logica.porId("123456789"));
    }

    // Verificar que crear() establezca el rol como "FUNCIONARIO"
    @Test
    void crearEstableceRolFuncionario() {
        Funcionario f = funcionarioValido("123456789");
        logica.crear(f);
        assertEquals("FUNCIONARIO", logica.porId("123456789").getRole());
    }

    // Verificar que crear() establezca la contraseña igual a la cédula
    @Test
    void crearEstableceContrasenaIgualACedula() {
        Funcionario f = funcionarioValido("123456789");
        logica.crear(f);
        assertEquals("123456789", logica.porId("123456789").getPassword());
    }

    // Probar crear() con una cédula que ya existe — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaDuplicadaLanzaExcepcion() {
        logica.crear(funcionarioValido("123456789"));
        assertThrows(IllegalArgumentException.class, () -> logica.crear(funcionarioValido("123456789")));
    }

    // Probar actualizar() con un funcionario que no existe — debe lanzar IllegalArgumentException
    @Test
    void actualizarFuncionarioInexistenteLanzaExcepcion() {
        Funcionario f = funcionarioValido("999999999");
        assertThrows(IllegalArgumentException.class, () -> logica.actualizar(f));
    }

    // Probar actualizar() con datos inválidos — debe lanzar IllegalArgumentException
    @Test
    void actualizarDatosInvalidosLanzaExcepcion() {
        logica.crear(funcionarioValido("123456789"));
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.actualizar(f));
    }

    // Probar actualizar() correctamente
    @Test
    void actualizarFuncionarioCorrectamente() {
        logica.crear(funcionarioValido("123456789"));
        Funcionario actualizado = new Funcionario("123456789", "FUNCIONARIO", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        assertEquals("Maria Lopez", logica.porId("123456789").getName());
    }

    // Verificar que actualizar() conserve la contraseña existente
    @Test
    void actualizarConservaContrasena() {
        Funcionario original = funcionarioValido("123456789");
        logica.crear(original);
        String claveOriginal = logica.porId("123456789").getPassword();
        Funcionario actualizado = new Funcionario("123456789", "FUNCIONARIO", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        assertEquals(claveOriginal, logica.porId("123456789").getPassword());
    }

    // Verificar que actualizar() conserve el rol "FUNCIONARIO"
    @Test
    void actualizarConservaRolFuncionario() {
        logica.crear(funcionarioValido("123456789"));
        Funcionario actualizado = new Funcionario("123456789", "ADMIN", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        assertEquals("FUNCIONARIO", logica.porId("123456789").getRole());
    }

    // Probar eliminar() con un funcionario que no existe — debe lanzar IllegalArgumentException
    @Test
    void eliminarFuncionarioInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("999999999"));
    }

    // Probar eliminar() correctamente
    @Test
    void eliminarFuncionarioCorrectamente() {
        logica.crear(funcionarioValido("123456789"));
        logica.eliminar("123456789");
        assertNull(logica.porId("123456789"));
    }

    // Probar porId() cuando el funcionario existe
    @Test
    void porIdFuncionarioExistente() {
        logica.crear(funcionarioValido("123456789"));
        assertNotNull(logica.porId("123456789"));
    }

    // Probar porId() cuando el funcionario no existe — debe retornar null
    @Test
    void porIdFuncionarioInexistenteRetornaNull() {
        assertNull(logica.porId("000000000"));
    }

    // Probar porId() con null — debe retornar null sin lanzar excepción
    @Test
    void porIdNullRetornaNull() {
        assertNull(logica.porId(null));
    }

    // buscarPorId con término parcial — debe encontrar coincidencias
    @Test
    void buscarPorIdParcialEncuentraCoincidencias() {
        logica.crear(funcionarioValido("123456789"));
        assertFalse(logica.buscarPorId("123").isEmpty());
    }

    // buscarPorId con término que no existe — debe retornar lista vacía
    @Test
    void buscarPorIdSinCoincidenciasRetornaVacio() {
        logica.crear(funcionarioValido("123456789"));
        assertTrue(logica.buscarPorId("999").isEmpty());
    }

    // buscarPorNombre con término parcial — debe encontrar coincidencias
    @Test
    void buscarPorNombreParcialEncuentraCoincidencias() {
        logica.crear(funcionarioValido("123456789"));
        assertFalse(logica.buscarPorNombre("Juan").isEmpty());
    }

    // buscarPorNombre con término que no existe — debe retornar lista vacía
    @Test
    void buscarPorNombreSinCoincidenciasRetornaVacio() {
        logica.crear(funcionarioValido("123456789"));
        assertTrue(logica.buscarPorNombre("Carlos").isEmpty());
    }

    // crear() con cédula de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaSoloEspaciosLanzaExcepcion() {
        Funcionario f = new Funcionario("         ", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // crear() con nombre de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearNombreSoloEspaciosLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "   ", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // crear() con teléfono de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoSoloEspaciosLanzaExcepcion() {
        Funcionario f = new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "   ");
        assertThrows(IllegalArgumentException.class, () -> logica.crear(f));
    }

    // actualizar() con cédula inválida (menos de 9 dígitos) — debe lanzar IllegalArgumentException
    @Test
    void actualizarCedulaInvalidaLanzaExcepcion() {
        Funcionario f = new Funcionario("12345", "FUNCIONARIO", "Juan Perez", "88887777");
        assertThrows(IllegalArgumentException.class, () -> logica.actualizar(f));
    }

    // eliminar() con id null — debe lanzar IllegalArgumentException
    @Test
    void eliminarIdNullLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar(null));
    }

    // eliminar() con id vacío — debe lanzar IllegalArgumentException
    @Test
    void eliminarIdVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar(""));
    }

    // obtenerTodos() cuando no hay usuarios — debe retornar lista vacía
    @Test
    void obtenerTodosSinUsuariosRetornaVacio() {
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // obtenerTodos() cuando solo hay admins — debe retornar lista vacía
    @Test
    void obtenerTodosSoloAdminsRetornaVacio() {
        Administrador admin = new Administrador("admin", "ADMIN", "Admin");
        admin.setPassword("admin");
        store.add(admin);
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // Probar obtenerTodos() — verificar que retorne únicamente funcionarios
    @Test
    void obtenerTodosRetornaSoloFuncionarios() {
        logica.crear(funcionarioValido("123456789"));
        Administrador admin = new Administrador("admin", "ADMIN", "Admin");
        admin.setPassword("admin");
        store.add(admin);
        List<Funcionario> resultado = logica.obtenerTodos();
        assertTrue(resultado.stream().allMatch(u -> u instanceof Funcionario));
        assertEquals(1, resultado.size());
    }
}