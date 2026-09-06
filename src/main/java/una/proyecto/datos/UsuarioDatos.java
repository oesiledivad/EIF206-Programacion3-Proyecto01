package una.proyecto.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import una.proyecto.datos.wrapper.ListaUsuarios;
import una.proyecto.model.Administrador;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Usuario;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDatos {

    private static final String XML_PATH = "data/xml/usuarios.xml";

    public UsuarioDatos() {
        verifyAndCreateDefaultFile();
    }

    /**
     * Verifica que el archivo XML del usuario exista.
     * Si no existe, crea la estructura de directorios
     * y un administrador predeterminado.
     */
    private void verifyAndCreateDefaultFile() {

        File file = new File(XML_PATH);

        System.out.println("Checking users file: " + file.getAbsolutePath());

        if (file.exists()) {

            System.out.println("Users file already exists.");

            return;
        }

        try {

            File parentDirectory = file.getParentFile();

            if (parentDirectory != null && !parentDirectory.exists()) {

                boolean created = parentDirectory.mkdirs();

                if (!created && !parentDirectory.exists()) {
                    throw new RuntimeException("Could not create directory: " + parentDirectory.getAbsolutePath());
                }
            }

            List<Usuario> initialUsers = new ArrayList<>();

            Administrador defaultAdmin =
                    new Administrador(
                            "admin",
                            "ADMIN",
                            "Administrador"
                    );

            defaultAdmin.setPassword("admin");

            initialUsers.add(defaultAdmin);

            saveUsers(initialUsers);

            System.out.println("Default users file created at: " + file.getAbsolutePath());

        } catch (Exception e) {

            System.err.println("Error creating default users file.");

            e.printStackTrace();
        }
    }

    /**
     * Crea el JAXB que utiliza este DAO.
     */
    private JAXBContext createJAXBContext() throws Exception {

        return JAXBContext.newInstance(
                ListaUsuarios.class,
                Usuario.class,
                Funcionario.class,
                Administrador.class
        );
    }

    /**
     * Carga todos los usuarios desde el archivo XML.
     */
    public List<Usuario> loadUsers() {

        File file = new File(XML_PATH);

        if (!file.exists()) {

            System.err.println("Users XML file does not exist: " + file.getAbsolutePath());

            return new ArrayList<>();
        }

        try {

            JAXBContext context = createJAXBContext();

            Unmarshaller unmarshaller = context.createUnmarshaller();

            ListaUsuarios wrapper = (ListaUsuarios) unmarshaller.unmarshal(file);

            if (wrapper == null || wrapper.getUsuarios() == null) {
                return new ArrayList<>();
            }

            return wrapper.getUsuarios();

        } catch (Exception e) {

            System.err.println("Error loading users from XML.");

            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    /**
     * Guarda la lista completa de usuarios en formato XML.
     * Se sobrescribe el archivo existente.
     */
    public void saveUsers(List<Usuario> users) {

        if (users == null) {
            return;
        }

        try {

            File file = new File(XML_PATH);

            File parentDirectory = file.getParentFile();

            if (parentDirectory != null && !parentDirectory.exists()) {

                boolean created = parentDirectory.mkdirs();

                if (!created && !parentDirectory.exists()) {
                    throw new RuntimeException("Could not create directory: " + parentDirectory.getAbsolutePath());
                }
            }

            JAXBContext context = createJAXBContext();

            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            ListaUsuarios wrapper = new ListaUsuarios();

            wrapper.setUsuarios(users);

            marshaller.marshal(wrapper, file);

            System.out.println("Users saved to: " + file.getAbsolutePath());

        } catch (Exception e) {

            System.err.println("Error saving users to XML.");

            e.printStackTrace();
        }
    }

    /**
     * Busca a un usuario por su ID.
     */
    public Usuario findUserById(String id) {

        if (id == null || id.isBlank()) {
            return null;
        }

        for (Usuario user : loadUsers()) {

            if (user.getId() != null && user.getId().equalsIgnoreCase(id)) {

                return user;
            }
        }

        return null;
    }

    /**
     * Agrega un nuevo usuario.
     *
     * @return true si se agregó el usuario,
     *         false si el ID ya existe.
     */
    public boolean addUser(Usuario newUser) {

        if (newUser == null || newUser.getId() == null || newUser.getId().isBlank()) {

            return false;
        }

        if (findUserById(newUser.getId()) != null) {
            return false;
        }

        List<Usuario> users = loadUsers();

        users.add(newUser);

        saveUsers(users);

        return true;
    }

    /**
     * Actualiza un usuario existente.
     *
     * @return true si se actualizó el usuario,
     *         false si el usuario no existe.
     */
    public boolean updateUser(Usuario updatedUser) {

        if (updatedUser == null || updatedUser.getId() == null || updatedUser.getId().isBlank()) {

            return false;
        }

        List<Usuario> users = loadUsers();

        for (int i = 0; i < users.size(); i++) {

            Usuario currentUser = users.get(i);

            if (currentUser.getId() != null && currentUser.getId().equalsIgnoreCase(updatedUser.getId())) {

                users.set(i, updatedUser);

                saveUsers(users);

                return true;
            }
        }

        return false;
    }

    /**
     * Elimina a un usuario por su ID.
     *
     * @return true si se eliminó al usuario,
     *         false si no se encontró al usuario.
     */
    public boolean deleteUser(String id) {

        if (id == null || id.isBlank()) {
            return false;
        }

        List<Usuario> users = loadUsers();

        boolean removed = users.removeIf(user -> user.getId() != null && user.getId().equalsIgnoreCase(id));

        if (!removed) {
            return false;
        }

        saveUsers(users);

        return true;
    }
}
