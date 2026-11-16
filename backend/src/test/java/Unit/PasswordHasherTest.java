package Unit;

import org.junit.jupiter.api.Test;
import una.proyecto.security.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test
    void guardaSalAleatoriaYVerificaLaClave() {
        String first = PasswordHasher.hash("clave123");
        String second = PasswordHasher.hash("clave123");
        assertNotEquals(first, second);
        assertNotEquals("clave123", first);
        assertTrue(PasswordHasher.verify("clave123", first));
        assertTrue(PasswordHasher.verify("clave123", second));
        assertFalse(PasswordHasher.verify("otraClave", first));
    }

    @Test
    void conservaEspaciosYUnicode() {
        String hash = PasswordHasher.hash(" contraseña ñ ");
        assertTrue(PasswordHasher.verify(" contraseña ñ ", hash));
        assertFalse(PasswordHasher.verify("contraseña ñ", hash));
    }

    @Test
    void rechazaTextoPlanoHashesInvalidosYCostosDesmedidos() {
        for (String invalid : new String[]{"clave123", "", "pbkdf2-sha256$600000$???$???",
                "pbkdf2-sha256$2147483647$AA==$AA==", "pbkdf2-sha256$1$AA==$AA=="}) {
            assertFalse(PasswordHasher.verify("clave123", invalid));
        }
        assertFalse(PasswordHasher.verify(null, "hash"));
        assertFalse(PasswordHasher.verify("clave123", null));
    }

    @Test
    void noPermiteCrearHashDeUnaClaveVacia() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }
}
