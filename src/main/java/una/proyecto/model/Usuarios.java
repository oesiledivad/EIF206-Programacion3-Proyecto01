package una.proyecto.model;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "usuarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class Usuarios {

    @XmlElements({
            @XmlElement(name = "funcionario", type = Funcionario.class),
            @XmlElement(name = "administrador", type = Administrador.class)
    })
    private List<Usuario> lista = new ArrayList<>();

    public Usuarios() {}
    public List<Usuario> getLista() { return lista; }
    public void setLista(List<Usuario> lista) { this.lista = lista; }
}