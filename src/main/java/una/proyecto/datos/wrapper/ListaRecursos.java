package una.proyecto.datos.wrapper;

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
