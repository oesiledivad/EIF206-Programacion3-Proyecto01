package una.proyecto.service;

import una.proyecto.model.Categoria;
import una.proyecto.model.ListaCategoria;
import una.proyecto.utils.XmlUtil;

import java.util.List;
import java.util.stream.Collectors;

public class CategoriaService {
    private final String RUTA_XML = "data/xml/categorias.xml";

    public List<Categoria> obtenerTodas() {
        return XmlUtil.readListXml(RUTA_XML, ListaCategoria.class, ListaCategoria::getCategorias);
    }
    public void save(Categoria categoria) {
        List<Categoria> lista = obtenerTodas();

        boolean encontrado = false;
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getId() == categoria.getId()) {
                lista.set(i, categoria);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            lista.add(categoria);
        }

        XmlUtil.writeListXml(RUTA_XML, ListaCategoria.class, lista, ListaCategoria::setCategorias);
    }

    public void delete(int id) {
        List<Categoria> lista = obtenerTodas();
        lista.removeIf(c -> c.getId() == id);
        XmlUtil.writeListXml(RUTA_XML, ListaCategoria.class, lista, ListaCategoria::setCategorias);
    }

    public List<Categoria> buscarPorDescripcion(String filtro) {
        if (filtro == null || filtro.isEmpty()) {
            return obtenerTodas();
        }
        return obtenerTodas().stream().filter(c -> c.getDescripcion().toLowerCase().contains(filtro.toLowerCase())).collect(Collectors.toList());
    }
}