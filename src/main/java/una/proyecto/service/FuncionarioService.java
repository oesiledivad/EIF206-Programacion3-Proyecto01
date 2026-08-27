package una.proyecto.service;

import una.proyecto.model.Funcionario;
import una.proyecto.model.ListaFuncionarios;
import una.proyecto.utils.XmlUtil;

import java.util.List;
import java.util.stream.Collectors;

public class FuncionarioService {
    private final String RUTA_XML = "data/xml/funcionarios.xml";

    public List<Funcionario> obtenerTodos() {
        return XmlUtil.readListXml(
                RUTA_XML,
                ListaFuncionarios.class,
                ListaFuncionarios::getFuncionarios
        );
    }
    public void save(Funcionario funcionario) {

        List<Funcionario> lista = obtenerTodos();

        boolean encontrado = false;

        for (int i = 0; i < lista.size(); i++) {

            if (lista.get(i).getId().equals(funcionario.getId())) {

                lista.set(i, funcionario);
                encontrado = true;
                break;
            }
        }

        if (!encontrado) {
            lista.add(funcionario);
        }

        XmlUtil.writeListXml(
                RUTA_XML,
                ListaFuncionarios.class,
                lista,
                ListaFuncionarios::setFuncionarios
        );
    }
    public void delete(String id) {

        List<Funcionario> lista = obtenerTodos();

        lista.removeIf(f -> f.getId().equals(id));

        XmlUtil.writeListXml(
                RUTA_XML,
                ListaFuncionarios.class,
                lista,
                ListaFuncionarios::setFuncionarios
        );
    }
    public List<Funcionario> buscarPorId(String filtro) {

        if (filtro == null || filtro.isEmpty()) {
            return obtenerTodos();
        }

        return obtenerTodos()
                .stream()
                .filter(f -> f.getId().toLowerCase()
                        .contains(filtro.toLowerCase()))
                .collect(Collectors.toList());
    }
    public List<Funcionario> buscarPorNombre(String filtro) {

        if (filtro == null || filtro.isEmpty()) {
            return obtenerTodos();
        }

        return obtenerTodos()
                .stream()
                .filter(f -> f.getName().toLowerCase()
                        .contains(filtro.toLowerCase()))
                .collect(Collectors.toList());
    }
}

