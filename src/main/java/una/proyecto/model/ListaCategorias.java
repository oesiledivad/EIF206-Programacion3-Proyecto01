package una.proyecto.model;

import java.util.ArrayList;
import java.util.List;

public class ListaCategorias {

    private List<Categorias> categorias;

    public ListaCategorias() {
        categorias = new ArrayList<>();
    }

    // Agregar una categoría
    public void agregar(Categorias categoria) {
        int nuevoId = obtenerSiguienteId();
        categoria.setId(nuevoId);
        categorias.add(categoria);
    }

    // Obtener el siguiente ID disponible
    private int obtenerSiguienteId() {
        int mayorId = 0;

        for (Categorias categoria : categorias) {
            if (categoria.getId() > mayorId) {
                mayorId = categoria.getId();
            }
        }

        return mayorId + 1;
    }

    // Buscar una categoría por descripción
    public Categorias buscarPorDescripcion(String descripcion) {

        for (Categorias categoria : categorias) {
            if (categoria.getDescripcion().equalsIgnoreCase(descripcion)) {
                return categoria;
            }
        }

        return null;
    }

    // Buscar una categoría por ID
    public Categorias buscarPorId(int id) {

        for (Categorias categoria : categorias) {
            if (categoria.getId() == id) {
                return categoria;
            }
        }

        return null;
    }

    // Modificar una categoría
    public boolean modificar(int id, String nuevaDescripcion) {

        Categorias categoria = buscarPorId(id);

        if (categoria != null) {
            categoria.setDescripcion(nuevaDescripcion);
            return true;
        }

        return false;
    }

    // Eliminar una categoría
    public boolean eliminar(int id) {

        Categorias categoria = buscarPorId(id);

        if (categoria != null) {
            categorias.remove(categoria);
            return true;
        }

        return false;
    }

    // Obtener todas las categorías
    public List<Categorias> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categorias> categorias) {
        this.categorias = categorias;
    }
}
