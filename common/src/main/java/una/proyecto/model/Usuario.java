package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlSeeAlso;

import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
        Funcionario.class,
        Administrador.class
})
public abstract class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String role;
    private transient String password;
    private String name;
    private boolean active = true;

    /**
     * Empty constructor required by JAXB.
     */
    public Usuario() {
    }

    /**
     * Creates a user with the basic information.
     *
     * @param id user identification
     * @param role user role
     * @param name user's name
     */
    public Usuario(String id, String role, String name) {
        this.id = id;
        this.role = role;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Changes the user's password.
     *
     * @param password new password
     */
    public abstract void changePassword(String password);
}