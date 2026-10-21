package una.proyecto.utils;

public class SessionManager {

    private static SessionManager instance;

    private String id;
    private String name;
    private String role;

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }

        return instance;
    }

    public void login(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public void logout() {
        this.id = null;
        this.name = null;
        this.role = null;
    }

    public boolean isLoggedIn() {
        return id != null;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
