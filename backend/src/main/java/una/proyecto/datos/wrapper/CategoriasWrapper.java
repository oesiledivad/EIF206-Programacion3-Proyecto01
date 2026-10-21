package una.proyecto.datos.wrapper;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import una.proyecto.model.Categoria;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "categorias")
public class CategoriasWrapper {
    private List<Categoria> categorias = new ArrayList<>();

    @XmlElement(name = "categoria")
    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}