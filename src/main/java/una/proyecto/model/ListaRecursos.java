package una.proyecto.model;
/*

* package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "categorias")
public class ListaCategoria {
    private List<Categoria> categorias = new ArrayList<>();

    @XmlElement(name = "categoria")
    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }
}

* */
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import una.proyecto.model.Recurso;
@XmlRootElement(name="recursos")
public class ListaRecursos {
    private  List<Recurso> recursos =new ArrayList<>();
    @XmlElement(name="recurso")
    public   List<Recurso> getRecursos() {
        return recursos;
    }
    public void setRecursos(List<Recurso> recursos) {
        this.recursos = recursos;
    }
}
