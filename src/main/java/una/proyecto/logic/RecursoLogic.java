package una.proyecto.logic;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class RecursoLogic {
    private RecursoDatos nuevo;

    public RecursoLogic(RecursoDatos recursosDatos) {
        this.nuevo = recursosDatos;
    }

    public void crear(Recurso nuevoRecurso) {
        if (nuevoRecurso.getId() == null || nuevoRecurso.getId().isEmpty()) {
            throw new IllegalArgumentException("El id del recurso no puede ser nulo o vacío");
        }
        Recurso existente = nuevo.leerPorId(nuevoRecurso.getId());
        if (existente != null) {
            throw new IllegalArgumentException("El recurso con id " + nuevoRecurso.getId() + " ya existe");
        }
        nuevo.crear(nuevoRecurso);
    }

    public void actualizar(Recurso actualizado){
        if(actualizado.getId()==null){
            throw new IllegalArgumentException("El id del recurso no puede ser nulo");
        }
        Recurso existente = nuevo.leerPorId(actualizado.getId());
        if(existente==null){
            throw new IllegalArgumentException("El recurso con id " + actualizado.getId() + " no existe");
        }
        nuevo.actualizar(actualizado);
    }

    public boolean existe(String id) {
        return nuevo.leerPorId(id) != null;
    }

    public void eliminar(String id) {
        Recurso existente = nuevo.leerPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("El recurso con id " + id + " no existe");
        }
        nuevo.eliminar(id);
    }

    public List<Recurso> obtenerTodos() {
        return nuevo.obtenerTodos();
    }

    public List<String> idRecursos(String idCategoria){
        List<Recurso> recursos=obtenerTodos();
        List<String> ids= new ArrayList<>();
        for(Recurso recur : recursos){
            if(recur.getIdCategoria().equals(idCategoria)){
                ids.add(recur.getId());
            }
        }
        return ids;
    }

    public List<Recurso> buscarFiltro(Categoria categoria, String descripcion){
        return nuevo.obtenerTodos().stream()
                .filter(r -> categoria == null || r.getIdCategoria().equals(categoria.getId())) // corregido: getId() en vez de getDescripcion()
                .filter(r -> descripcion == null || descripcion.isEmpty() || r.getDescripcion().toLowerCase().contains(descripcion.toLowerCase()))
                .collect(toList());
    }

    public String obtenerRecursosPorCategorias(List<Categoria> categoriasSeleccionadas) {
        if (categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            return null;
        }

        Set<String> idsRecursos = new LinkedHashSet<>();
        List<Recurso> todosLosRecursos = nuevo.obtenerTodos();

        for (Categoria categoria : categoriasSeleccionadas) {
            for (Recurso recurso : todosLosRecursos) {
                if (recurso.getIdCategoria().equals(categoria.getId())) {
                    idsRecursos.add(recurso.getId());
                }
            }
        }

        if (idsRecursos.isEmpty()) {
            return null;
        }

        return String.join(", ", idsRecursos);
    }

}