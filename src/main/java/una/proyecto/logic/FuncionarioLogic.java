package una.proyecto.logic;

import una.proyecto.datos.UsuarioDAO;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuncionarioLogic {

    private final UsuarioDAO usuarioDAO;

    public FuncionarioLogic(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public List<Funcionario> obtenerTodos() {
        return usuarioDAO.loadUsers()
                .stream()
                .filter(user -> user instanceof Funcionario)
                .map(user -> (Funcionario) user)
                .collect(Collectors.toList());
    }

    public List<Funcionario> buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return new ArrayList<>();
        }
        String search = id.toLowerCase();
        return obtenerTodos().stream()
                .filter(f -> f.getId() != null && f.getId().toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    public List<Funcionario> buscarPorNombre(String name) {
        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }
        String search = name.toLowerCase();
        return obtenerTodos().stream()
                .filter(f -> f.getName() != null && f.getName().toLowerCase().contains(search))
                .collect(Collectors.toList());
    }

    public void crear(Funcionario nuevo) {
        if (nuevo.getId() == null || nuevo.getId().isBlank()) {
            throw new IllegalArgumentException("El id del funcionario no puede estar vacío");
        }
        if (nuevo.getName() == null || nuevo.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del funcionario no puede estar vacío");
        }
        if (nuevo.getPhone() == null || nuevo.getPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del funcionario no puede estar vacío");
        }

        // Regla de negocio (funcionalidad 3): la clave inicial queda igual al id
        nuevo.setRole("FUNCIONARIO");
        nuevo.setPassword(nuevo.getId());

        boolean agregado = usuarioDAO.addUser(nuevo);

        if (!agregado) {
            throw new IllegalArgumentException("Ya existe un usuario con id " + nuevo.getId());
        }
    }

    public void actualizar(Funcionario actualizado) {
        if (actualizado.getId() == null || actualizado.getId().isBlank()) {
            throw new IllegalArgumentException("El id del funcionario no puede estar vacío");
        }

        Usuario existente = usuarioDAO.findUserById(actualizado.getId());

        if (!(existente instanceof Funcionario)) {
            throw new IllegalArgumentException("El funcionario con id " + actualizado.getId() + " no existe");
        }

        // Regla de negocio: se conserva rol y clave actuales, no se pisan
        actualizado.setRole("FUNCIONARIO");
        actualizado.setPassword(existente.getPassword());

        boolean actualizadoOk = usuarioDAO.updateUser(actualizado);

        if (!actualizadoOk) {
            throw new IllegalArgumentException("No se pudo actualizar el funcionario");
        }
    }

    public void eliminar(String id) {
        Usuario existente = usuarioDAO.findUserById(id);

        if (!(existente instanceof Funcionario)) {
            throw new IllegalArgumentException("El funcionario con id " + id + " no existe");
        }

        boolean eliminado = usuarioDAO.deleteUser(id);

        if (!eliminado) {
            throw new IllegalArgumentException("No se pudo eliminar el funcionario");
        }
    }
}
