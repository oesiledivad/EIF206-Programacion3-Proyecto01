package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "categorias")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListaCategorias {

    @XmlElement(name = "categoria")
    private List<Categorias> categorias;

    public ListaCategorias() {
        categorias = new ArrayList<>();
    }

    public void agregar(Categorias categoria) {

        int nuevoId = obtenerSiguienteId();

        categoria.setId(nuevoId);

        categorias.add(categoria);
    }

    private int obtenerSiguienteId() {

        int mayorId = 0;

        for (Categorias categoria : categorias) {

            if (categoria.getId() > mayorId) {
                mayorId = categoria.getId();
            }
        }

        return mayorId + 1;
    }

    public Categorias buscarPorDescripcion(String descripcion) {

        for (Categorias categoria : categorias) {

            if (categoria.getDescripcion().equalsIgnoreCase(descripcion)) {
                return categoria;
            }
        }

        return null;
    }

    public Categorias buscarPorId(int id) {

        for (Categorias categoria : categorias) {

            if (categoria.getId() == id) {
                return categoria;
            }
        }

        return null;
    }

    public boolean modificar(int id, String nuevaDescripcion) {

        Categorias categoria = buscarPorId(id);

        if (categoria != null) {
            categoria.setDescripcion(nuevaDescripcion);
            return true;
        }

        return false;
    }

    public boolean eliminar(int id) {

        Categorias categoria = buscarPorId(id);

        if (categoria != null) {
            categorias.remove(categoria);
            return true;
        }

        return false;
    }

    public List<Categorias> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categorias> categorias) {
        this.categorias = categorias;
    }
}