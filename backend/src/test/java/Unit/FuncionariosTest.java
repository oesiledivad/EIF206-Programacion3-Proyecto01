package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FuncionariosTest {

    @Mock
    private UsuarioDatos datos;

    private FuncionarioLogic logica;

    private Funcionario funcionarioValido(String id) {
        return new Funcionario(id, "FUNCIONARIO", "Juan Perez", "88887777");
    }

    @BeforeEach
    void setUp() {
        logica = new FuncionarioLogic(datos);
    }

    // Probar que buscarPorId("") retorne una lista vacia
    @Test
    void buscarPorIdVacioDebeRetornarListaVacia() {
        assertTrue(logica.buscarPorId("").isEmpty());
    }

    // Probar que buscarPorId(null) retorne una lista vacia
    @Test
    void buscarPorIdNullDebeRetornarListaVacia() {
        assertTrue(logica.buscarPorId(null).isEmpty());
    }

    // Probar que buscarPorNombre("") retorne una lista vacia
    @Test
    void buscarPorNombreVacio() {
        assertTrue(logica.buscarPorNombre("").isEmpty());
    }

    // Probar que buscarPorNombre(null) retorne una lista vacia
    @Test
    void buscarPorNombreNull() {
        assertTrue(logica.buscarPorNombre(null).isEmpty());
    }

    // buscarPorId con termino parcial — debe encontrar coincidencias
    @Test
    void buscarPorIdParcialEncuentraCoincidencias() {
        when(datos.loadUsers()).thenReturn(List.of(funcionarioValido("123456789")));
        assertFalse(logica.buscarPorId("123").isEmpty());
    }

    // buscarPorId con termino que no existe — debe retornar lista vacia
    @Test
    void buscarPorIdSinCoincidenciasRetornaVacio() {
        when(datos.loadUsers()).thenReturn(List.of(funcionarioValido("123456789")));
        assertTrue(logica.buscarPorId("999").isEmpty());
    }

    // buscarPorNombre con termino parcial — debe encontrar coincidencias
    @Test
    void buscarPorNombreParcialEncuentraCoincidencias() {
        when(datos.loadUsers()).thenReturn(List.of(funcionarioValido("123456789")));
        assertFalse(logica.buscarPorNombre("Juan").isEmpty());
    }

    // buscarPorNombre con termino que no existe — debe retornar lista vacia
    @Test
    void buscarPorNombreSinCoincidenciasRetornaVacio() {
        when(datos.loadUsers()).thenReturn(List.of(funcionarioValido("123456789")));
        assertTrue(logica.buscarPorNombre("Carlos").isEmpty());
    }

    // Probar crear() con una cedula vacia — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaVaciaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar crear() con una cedula de menos de 9 digitos — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaMenosDeNueveDigitosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("12345", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar crear() con una cedula de mas de 9 digitos — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaMasDeNueveDigitosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("1234567890", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar crear() con una cedula que contenga letras — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaConLetrasLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("12345678A", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar crear() con cedula de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("         ", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar crear() con nombre vacio — debe lanzar IllegalArgumentException
    @Test
    void crearNombreVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "", "88887777")));
    }

    // Probar crear() con nombre que contenga numeros — debe lanzar IllegalArgumentException
    @Test
    void crearNombreConNumerosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "Juan123", "88887777")));
    }

    // Probar crear() con nombre de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearNombreSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "   ", "88887777")));
    }

    // Probar crear() con telefono vacio — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoVacioLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "")));
    }

    // Probar crear() con telefono que contenga letras — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoConLetrasLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "8888ABCD")));
    }

    // Probar crear() con telefono de solo espacios — debe lanzar IllegalArgumentException
    @Test
    void crearTelefonoSoloEspaciosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.crear(new Funcionario("123456789", "FUNCIONARIO", "Juan Perez", "   ")));
    }

    // Probar crear() correctamente — verificar que se cree el funcionario
    @Test
    void crearFuncionarioCorrectamente() {
        when(datos.addUser(any())).thenReturn(true);
        assertDoesNotThrow(() -> logica.crear(funcionarioValido("123456789")));
        verify(datos).addUser(any());
    }

    // Verificar que crear() establezca el rol como "FUNCIONARIO"
    @Test
    void crearEstableceRolFuncionario() {
        when(datos.addUser(any())).thenReturn(true);
        Funcionario f = funcionarioValido("123456789");
        logica.crear(f);
        assertEquals("FUNCIONARIO", f.getRole());
    }

    // Verificar que crear() establezca la contrasena igual a la cedula
    @Test
    void crearEstableceContrasenaIgualACedula() {
        when(datos.addUser(any())).thenReturn(true);
        Funcionario f = funcionarioValido("123456789");
        logica.crear(f);
        assertEquals("123456789", f.getPassword());
    }

    // Probar crear() con una cedula que ya existe — debe lanzar IllegalArgumentException
    @Test
    void crearCedulaDuplicadaLanzaExcepcion() {
        when(datos.addUser(any())).thenReturn(false);
        assertThrows(IllegalArgumentException.class, () -> logica.crear(funcionarioValido("123456789")));
    }

    // Probar actualizar() con un funcionario que no existe — debe lanzar IllegalArgumentException
    @Test
    void actualizarFuncionarioInexistenteLanzaExcepcion() {
        when(datos.findUserById("999999999")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.actualizar(funcionarioValido("999999999")));
    }

    // Probar actualizar() con datos invalidos — debe lanzar IllegalArgumentException
    @Test
    void actualizarDatosInvalidosLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Funcionario("123456789", "FUNCIONARIO", "", "88887777")));
    }

    // Probar actualizar() con cedula invalida — debe lanzar IllegalArgumentException
    @Test
    void actualizarCedulaInvalidaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
                () -> logica.actualizar(new Funcionario("12345", "FUNCIONARIO", "Juan Perez", "88887777")));
    }

    // Probar actualizar() correctamente
    @Test
    void actualizarFuncionarioCorrectamente() {
        Funcionario existente = funcionarioValido("123456789");
        existente.setPassword("123456789");
        when(datos.findUserById("123456789")).thenReturn(existente);
        when(datos.updateUser(any())).thenReturn(true);
        Funcionario actualizado = new Funcionario("123456789", "FUNCIONARIO", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        verify(datos).updateUser(actualizado);
    }

    // Verificar que actualizar() conserve la contrasena existente
    @Test
    void actualizarConservaContrasena() {
        Funcionario existente = funcionarioValido("123456789");
        existente.setPassword("clave123");
        when(datos.findUserById("123456789")).thenReturn(existente);
        when(datos.updateUser(any())).thenReturn(true);
        Funcionario actualizado = new Funcionario("123456789", "FUNCIONARIO", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        assertEquals("clave123", actualizado.getPassword());
    }

    // Verificar que actualizar() conserve el rol "FUNCIONARIO"
    @Test
    void actualizarConservaRolFuncionario() {
        Funcionario existente = funcionarioValido("123456789");
        existente.setPassword("123456789");
        when(datos.findUserById("123456789")).thenReturn(existente);
        when(datos.updateUser(any())).thenReturn(true);
        Funcionario actualizado = new Funcionario("123456789", "ADMIN", "Maria Lopez", "77776666");
        logica.actualizar(actualizado);
        assertEquals("FUNCIONARIO", actualizado.getRole());
    }

    // Probar eliminar() con un funcionario que no existe — debe lanzar IllegalArgumentException
    @Test
    void eliminarFuncionarioInexistenteLanzaExcepcion() {
        when(datos.findUserById("999999999")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("999999999"));
    }

    // Probar eliminar() con id null — debe lanzar IllegalArgumentException
    @Test
    void eliminarIdNullLanzaExcepcion() {
        when(datos.findUserById(null)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar(null));
    }

    // Probar eliminar() con id vacio — debe lanzar IllegalArgumentException
    @Test
    void eliminarIdVacioLanzaExcepcion() {
        when(datos.findUserById("")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar(""));
    }

    // Probar eliminar() correctamente
    @Test
    void eliminarFuncionarioCorrectamente() {
        when(datos.findUserById("123456789")).thenReturn(funcionarioValido("123456789"));
        when(datos.deleteUser("123456789")).thenReturn(true);
        assertDoesNotThrow(() -> logica.eliminar("123456789"));
        verify(datos).deleteUser("123456789");
    }

    // Probar porId() cuando el funcionario existe
    @Test
    void porIdFuncionarioExistente() {
        when(datos.loadUsers()).thenReturn(List.of(funcionarioValido("123456789")));
        assertNotNull(logica.porId("123456789"));
    }

    // Probar porId() cuando el funcionario no existe — debe retornar null
    @Test
    void porIdFuncionarioInexistenteRetornaNull() {
        when(datos.loadUsers()).thenReturn(new ArrayList<>());
        assertNull(logica.porId("000000000"));
    }

    // Probar porId() con null — debe retornar null sin lanzar excepcion
    @Test
    void porIdNullRetornaNull() {
        assertNull(logica.porId(null));
    }

    // Probar obtenerTodos() — verificar que retorne unicamente funcionarios
    @Test
    void obtenerTodosRetornaSoloFuncionarios() {
        Administrador admin = new Administrador("admin", "ADMIN", "Admin");
        List<Usuario> usuarios = List.of(funcionarioValido("123456789"), admin);
        when(datos.loadUsers()).thenReturn(usuarios);
        List<Funcionario> resultado = logica.obtenerTodos();
        assertTrue(resultado.stream().allMatch(u -> u instanceof Funcionario));
        assertEquals(1, resultado.size());
    }

    // Probar obtenerTodos() cuando no hay usuarios — debe retornar lista vacia
    @Test
    void obtenerTodosSinUsuariosRetornaVacio() {
        when(datos.loadUsers()).thenReturn(new ArrayList<>());
        assertTrue(logica.obtenerTodos().isEmpty());
    }

    // Probar obtenerTodos() cuando solo hay admins — debe retornar lista vacia
    @Test
    void obtenerTodosSoloAdminsRetornaVacio() {
        when(datos.loadUsers()).thenReturn(List.of(new Administrador("admin", "ADMIN", "Admin")));
        assertTrue(logica.obtenerTodos().isEmpty());
    }
}
