package una.proyecto.service;

import una.proyecto.logic.CategoriaLogic;
import una.proyecto.model.Categoria;

import java.util.List;

public class CategoriaService {

    private final CategoriaLogic categoriaLogica;

    public CategoriaService(CategoriaLogic categoriaLogic) {
        this.categoriaLogica = categoriaLogic;
    }

    public List<Categoria> obtenerTodas() {
        return categoriaLogica.obtenerTodos();
    }

    public List<Categoria> buscarPorDescripcion(String descripcion) {
        return categoriaLogica.buscarPorDescripcion(descripcion);
    }

    public void save(Categoria nueva) {
        categoriaLogica.crear(nueva);
    }

    public void update(Categoria actualizada) {
        categoriaLogica.actualizar(actualizada);
    }

    public void delete(String id) {
        categoriaLogica.eliminar(id);
    }
    public List<Categoria> categoriaNoPoseeRecursos (List<Categoria> categoriasEscogidas){
        return categoriaLogica.categoriasSinRecursos(categoriasEscogidas);
    }
    public List<Categoria> categoriaConRecursos (List<Categoria> categoriasEscogidas){
        return categoriaLogica.categoriaConRecursos(categoriasEscogidas);
    }
}