package Unit;

import org.junit.jupiter.api.Test;
import una.proyecto.model.Funcionario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioSerializationTest {
    @Test
    void enviaElPerfilYEstadoPeroNoLaCredencial() throws Exception {
        Funcionario user = new Funcionario("123456789", "FUNCIONARIO", "Juan", "88888888");
        user.setPassword("credencial-que-no-debe-viajar");
        user.setActive(false);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(user);
        }
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            Funcionario result = (Funcionario) input.readObject();
            assertEquals(user.getId(), result.getId());
            assertEquals(user.getPhone(), result.getPhone());
            assertFalse(result.isActive());
            assertNull(result.getPassword());
        }
    }
}
