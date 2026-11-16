package una.proyecto.logic;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Usuario;
import una.proyecto.security.PasswordHasher;

public class UsuarioLogic {
    private final UsuarioDatos usuarioDatos;

    public UsuarioLogic() { this(new UsuarioDatos()); }

    public UsuarioLogic(UsuarioDatos usuarioDatos) { this.usuarioDatos = usuarioDatos; }

    public void cambiarContrasena(String id, String contrasenaActual, String nuevaContrasena) {
        Usuario user = usuarioDatos.findUserById(id);
        if (user == null || !user.isActive()) throw new IllegalArgumentException("El usuario no existe.");
        String oldHash = usuarioDatos.findPasswordHash(id);
        if (!PasswordHasher.verify(contrasenaActual, oldHash)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }
        if (nuevaContrasena == null || nuevaContrasena.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }
        if (nuevaContrasena.equals(contrasenaActual)) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }
        if (!usuarioDatos.changePassword(user.getId(), oldHash, PasswordHasher.hash(nuevaContrasena))) {
            throw new IllegalArgumentException("La sesión o contraseña cambió; vuelva a ingresar.");
        }
    }

    public Usuario obtenerUsuario(String id) {
        return id == null || id.isBlank() ? null : usuarioDatos.findUserById(id);
    }
}
