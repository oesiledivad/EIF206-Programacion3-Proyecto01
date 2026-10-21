package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
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