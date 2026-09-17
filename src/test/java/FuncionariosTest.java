import org.junit.jupiter.api.Test;
import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.datos.UsuarioDatos;

import static org.junit.jupiter.api.Assertions.*;

public class FuncionariosTest {
// mi primera pruebita simple funciona bien :)
    @Test
    void buscarPorIdVacioDebeRetornarListaVacia() {

        UsuarioDatos datos = new UsuarioDatos();
        FuncionarioLogic logica = new FuncionarioLogic(datos);

        var resultado = logica.buscarPorId("");

        assertTrue(resultado.isEmpty());
    }
    // TODO: Probar que buscarPorId(null) retorne una lista vacía

    // TODO: Probar que buscarPorNombre("") retorne una lista vacía

    // TODO: Probar que buscarPorNombre(null) retorne una lista vacía

    // TODO: Probar crear() con una cédula vacía
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con una cédula de menos de 9 dígitos
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con una cédula de más de 9 dígitos
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con una cédula que contenga letras
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con nombre vacío
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con nombre que contenga números
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con teléfono vacío
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() con teléfono que contenga letras
    // Debe lanzar IllegalArgumentException

    // TODO: Probar crear() correctamente
    // Verificar que se cree el funcionario

    // TODO: Verificar que crear() establezca el rol como "FUNCIONARIO"

    // TODO: Verificar que crear() establezca la contraseña igual a la cédula

    // TODO: Probar crear() con una cédula que ya existe
    // Debe lanzar IllegalArgumentException

    // TODO: Probar actualizar() con un funcionario que no existe
    // Debe lanzar IllegalArgumentException

    // TODO: Probar actualizar() con datos inválidos
    // Debe lanzar IllegalArgumentException

    // TODO: Probar actualizar() correctamente

    // TODO: Verificar que actualizar() conserve la contraseña existente

    // TODO: Verificar que actualizar() conserve el rol "FUNCIONARIO"

    // TODO: Probar eliminar() con un funcionario que no existe
    // Debe lanzar IllegalArgumentException

    // TODO: Probar eliminar() correctamente

    // TODO: Probar porId() cuando el funcionario existe

    // TODO: Probar porId() cuando el funcionario no existe
    // Debe retornar null

    // TODO: Probar obtenerTodos()
    // Verificar que retorne únicamente funcionarios
}