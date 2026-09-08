package una.proyecto.logic;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Usuario;

import java.util.List;

public class UsuarioLogic {

    private final UsuarioDatos usuarioDatos;

    public UsuarioLogic() {
        this.usuarioDatos = new UsuarioDatos();
    }

    /**
     * Cambia la contraseña de un usuario
     * @param id ID del usuario
     * @param contrasenaActual Contraseña actual
     * @param nuevaContrasena Nueva contraseña
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public void cambiarContrasena(String id, String contrasenaActual, String nuevaContrasena) {
        // Validar que el usuario exista
        List<Usuario> usuarios = usuarioDatos.loadUsers();
        Usuario usuario = usuarios.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));

        // Validar contraseña actual
        if (!usuario.getPassword().equals(contrasenaActual)) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta.");
        }

        // Validar nueva contraseña (mínimo 6 caracteres)
        if (nuevaContrasena == null || nuevaContrasena.length() < 6) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
        }

        // Validar que sea diferente a la actual
        if (nuevaContrasena.equals(contrasenaActual)) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual.");
        }

        usuario.changePassword(nuevaContrasena);

        // Guardar cambios
        usuarioDatos.saveUsers(usuarios);
    }

    /**
     * Obtiene un usuario por su ID
     */
    public Usuario obtenerUsuario(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }

        List<Usuario> usuarios = usuarioDatos.loadUsers();
        return usuarios.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }
}