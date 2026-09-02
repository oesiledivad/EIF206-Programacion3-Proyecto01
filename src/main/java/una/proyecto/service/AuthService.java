package una.proyecto.service;

import una.proyecto.datos.UsuarioDAO;
import una.proyecto.model.Usuario;

import java.util.List;

public class AuthService {

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public Usuario authenticate(String id, String password) {

        if (id == null || password == null) {
            return null;
        }

        List<Usuario> users = usuarioDAO.loadUsers();

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
