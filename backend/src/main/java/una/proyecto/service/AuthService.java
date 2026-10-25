package una.proyecto.service;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Usuario;
import una.proyecto.security.PasswordHasher;

public class AuthService {
    private final UsuarioDatos usuarioDatos;

    public AuthService() { this(new UsuarioDatos()); }

    public AuthService(UsuarioDatos usuarioDatos) { this.usuarioDatos = usuarioDatos; }

    public Usuario authenticate(String id, String password) {
        if (id == null || id.isBlank() || password == null || password.isEmpty()) return null;
        Usuario user = usuarioDatos.findUserById(id);
        if (user == null || !user.isActive()) return null;
        if (!PasswordHasher.verify(password, usuarioDatos.findPasswordHash(id))) return null;
        user.setPassword(null);
        return user;
    }

    public Usuario activeUser(String id) {
        Usuario user = usuarioDatos.findUserById(id);
        return user != null && user.isActive() ? user : null;
    }
}
