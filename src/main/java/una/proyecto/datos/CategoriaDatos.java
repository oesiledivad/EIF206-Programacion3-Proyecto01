package una.proyecto.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import una.proyecto.model.Categoria;
import una.proyecto.model.ListaCategoria;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CategoriaDatos {

    private final String filePath;
    private final JAXBContext ctx;
    private List<Categoria> cache;

    public CategoriaDatos(String filePath) throws Exception {
        this.filePath = filePath;
        try {
            this.ctx = JAXBContext.newInstance(ListaCategoria.class, Categoria.class);
        } catch (JAXBException e) {
            throw new Exception("Error al inicializar JAXBContext: " + e.getMessage(), e);
        }
        this.cache = cargarTodo();
    }

    public void guardarTodo(List<Categoria> lista) {
        try {
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            ListaCategoria wrapper = new ListaCategoria();
            wrapper.setCategorias(lista);
            marshaller.marshal(wrapper, new File(filePath));
        } catch (JAXBException e) {
            throw new RuntimeException("Error al guardar las categorías en XML: " + e.getMessage(), e);
        }
    }

    public List<Categoria> cargarTodo() {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            ListaCategoria wrapper = (ListaCategoria) unmarshaller.unmarshal(file);
            List<Categoria> lista = wrapper.getCategorias();
            return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        } catch (JAXBException e) {
            throw new RuntimeException("Error de persistencia: falló el unmarshal de JAXB", e);
        }
    }

    public void crear(Categoria nueva) {
        cache.add(nueva);
        guardarTodo(cache);
    }

    public Categoria leerPorId(String id) {
        return cache.stream()
                .filter(c -> id.equals(c.getId()))
                .findFirst()
                .orElse(null);
    }

    public void actualizar(Categoria actualizada) {
        for (int i = 0; i < cache.size(); i++) {
            if (Objects.equals(cache.get(i).getId(), actualizada.getId())) {
                cache.set(i, actualizada);
                guardarTodo(cache);
                return;
            }
        }
    }

    public void eliminar(String id) {
        cache.removeIf(c -> Objects.equals(c.getId(), id));
        guardarTodo(cache);
    }

    public List<Categoria> obtenerTodos() {
        return new ArrayList<>(cache);
    }
}