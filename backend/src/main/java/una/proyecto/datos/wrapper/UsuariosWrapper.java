package una.proyecto.datos.wrapper;

import jakarta.xml.bind.annotation.*;
import una.proyecto.model.Usuario;
import java.util.List;

@XmlRootElement(name = "usuarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class UsuariosWrapper {

    @XmlElement(name = "usuario")
    private List<Usuario> usuarios;

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}