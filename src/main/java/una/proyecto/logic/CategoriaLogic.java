package una.proyecto.logic;

import una.proyecto.datos.CategoriaDatos;
import una.proyecto.model.Categoria;

import java.util.List;
import java.util.stream.Collectors;

public class CategoriaLogic {

    private final CategoriaDatos categoriaDatos;

    public CategoriaLogic(CategoriaDatos categoriaDatos) {
        this.categoriaDatos = categoriaDatos;
    }

    public List<Categoria> obtenerTodos() {
        return categoriaDatos.obtenerTodos();
    }

    public List<Categoria> buscarPorDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            return obtenerTodos();
        }
        String search = descripcion.toLowerCase();
        return obtenerTodos().stream()
                .filter(c -> c.getDescripcion() != null
                        && c.getDescripcion().toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    public void crear(Categoria nueva) {
        if (nueva.getDescripcion() == null || nueva.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría no puede estar vacía");
        }

        boolean existeDescripcion = obtenerTodos().stream()
                .anyMatch(c -> c.getDescripcion().equalsIgnoreCase(nueva.getDescripcion()));

        if (existeDescripcion) {
            throw new IllegalArgumentException("Ya existe una categoría con esa descripción");
        }

        nueva.setId(generarSiguienteId());

        categoriaDatos.crear(nueva);
    }

    public void actualizar(Categoria actualizada) {
        if (actualizada.getId() == null || actualizada.getId().isBlank()) {
            throw new IllegalArgumentException("El id de la categoría no puede estar vacío");
        }
        if (actualizada.getDescripcion() == null || actualizada.getDescripcion().isBlank()) {
            throw new IllegalArgumentException("La descripción de la categoría no puede estar vacía");
        }

        Categoria existente = categoriaDatos.leerPorId(actualizada.getId());

        if (existente == null) {
            throw new IllegalArgumentException("La categoría con id " + actualizada.getId() + " no existe");
        }

        categoriaDatos.actualizar(actualizada);
    }

    public void eliminar(String id) {
        Categoria existente = categoriaDatos.leerPorId(id);

        if (existente == null) {
            throw new IllegalArgumentException("La categoría con id " + id + " no existe");
        }

        // TODO: si deciden con el equipo agregar la regla de "no borrar
        // categoría con recursos asociados", va acá, preguntándole a
        // RecursoLogic/RecursoService — no usando una lista embebida.

        categoriaDatos.eliminar(id);
    }

    private String generarSiguienteId() {
        int maximo = obtenerTodos().stream()
                .map(Categoria::getId)
                .filter(id -> id != null && id.startsWith("CAT-"))
                .mapToInt(id -> Integer.parseInt(id.substring(4)))
                .max()
                .orElse(0);

        return String.format("CAT-%06d", maximo + 1);
    }
}