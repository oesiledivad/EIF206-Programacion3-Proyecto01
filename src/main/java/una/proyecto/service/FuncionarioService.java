package una.proyecto.service;

import una.proyecto.datos.UsuarioDAO;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FuncionarioService {

    private final UsuarioDAO usuarioDAO;

    public FuncionarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Devuelve todos los empleados almacenados en usuarios.xml.
     */
    public List<Funcionario> getAllEmployees() {

        return usuarioDAO.loadUsers()
                .stream()
                .filter(user -> user instanceof Funcionario)
                .map(user -> (Funcionario) user)
                .collect(Collectors.toList());
    }

    /**
     * Busca empleados por ID.
     */
    public List<Funcionario> findById(String id) {

        if (id == null || id.isBlank()) {
            return new ArrayList<>();
        }

        String search = id.toLowerCase();

        return getAllEmployees()
                .stream()
                .filter(employee ->
                        employee.getId() != null
                                && employee.getId()
                                .toLowerCase()
                                .contains(search)
                )
                .collect(Collectors.toList());
    }

    /**
     * Busca empleados por nombre.
     */
    public List<Funcionario> findByName(String name) {

        if (name == null || name.isBlank()) {
            return new ArrayList<>();
        }

        String search = name.toLowerCase();

        return getAllEmployees()
                .stream()
                .filter(employee ->
                        employee.getName() != null
                                && employee.getName()
                                .toLowerCase()
                                .contains(search)
                )
                .collect(Collectors.toList());
    }

    /**
     * Agrega un nuevo empleado.
     * <p>
     * La contraseña inicial es el ID del empleado.
     */
    public boolean addEmployee(Funcionario employee) {

        if (employee == null || employee.getId() == null || employee.getId().isBlank()) {

            return false;
        }

        employee.setRole("FUNCIONARIO");

        employee.setPassword(employee.getId());

        return usuarioDAO.addUser(employee);
    }

    /**
     * Actualiza un empleado existente.
     * <p>
     * Se conserva la contraseña actual.
     */
    public boolean updateEmployee(Funcionario employee) {

        if (employee == null || employee.getId() == null || employee.getId().isBlank()) {
            return false;
        }

        Usuario existingUser = usuarioDAO.findUserById(employee.getId());

        if (!(existingUser instanceof Funcionario)) {
            return false;
        }

        employee.setRole("FUNCIONARIO");

        employee.setPassword(existingUser.getPassword());

        return usuarioDAO.updateUser(employee);
    }

    /**
     * Elimina a un empleado por su ID.
     */
    public boolean deleteEmployee(String id) {

        if (id == null || id.isBlank()) {
            return false;
        }

        Usuario user = usuarioDAO.findUserById(id);

        if (!(user instanceof Funcionario)) {
            return false;
        }

        return usuarioDAO.deleteUser(id);
    }
}
