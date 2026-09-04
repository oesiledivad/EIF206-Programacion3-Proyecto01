package una.proyecto.service;

import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Usuario;

import java.util.List;

public class AuthService {

    private final UsuarioDatos usuarioDatos;

    public AuthService() {
        this.usuarioDatos = new UsuarioDatos();
    }

    public Usuario authenticate(String id, String password) {

        if (id == null || password == null) {
            return null;
        }

        List<Usuario> users = usuarioDatos.loadUsers();

        for (Usuario user : users) {

            if (user.getId() != null
                    && user.getPassword() != null
                    && user.getId().equalsIgnoreCase(id)
                    && user.getPassword().equals(password)) {

                return user;
            }
        }

        return null;
    }
}
