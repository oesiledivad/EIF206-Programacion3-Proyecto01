package una.proyecto.model;

public class Administrador extends Usuario {

    public Administrador() {
        super();
    }

    public Administrador(String id, String role, String name) {
        super(id, role, name);
    }

    @Override
    public void changePassword(String password) {
        setPassword(password);
    }
}