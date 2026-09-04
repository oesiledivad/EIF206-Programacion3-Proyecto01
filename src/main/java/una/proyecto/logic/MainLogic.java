package una.proyecto.logic;

import una.proyecto.utils.SessionManager;

import java.util.List;

public class MainLogic {

    public MainLogic() {
    }

    /**
     * Obtiene la información del usuario actual para la vista
     */
    public UserSessionInfo getCurrentUserInfo() {
        SessionManager session = SessionManager.getInstance();

        if (!session.isLoggedIn()) {
            return UserSessionInfo.guest();
        }

        String displayName = formatDisplayName(session.getName(), session.getId());
        String displayRole = formatRole(session.getRole());
        boolean isAdmin = session.isAdmin();

        return new UserSessionInfo(
                session.getId(),
                session.getName(),
                session.getRole(),
                displayName,
                displayRole,
                isAdmin
        );
    }

    /**
     * Obtiene los permisos de menú según el rol del usuario
     */
    public MenuPermissions getMenuPermissions() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        return new MenuPermissions(isAdmin);
    }

    /**
     * Cierra la sesión del usuario
     */
    public void logoutUser() {
        SessionManager.getInstance().logout();
    }

    /**
     * Verifica si un usuario tiene acceso a una vista específica
     */
    public boolean hasAccessToView(String viewName) {
        SessionManager session = SessionManager.getInstance();
        String role = session.getRole();

        // Acceso a vistas
        //if ("ADMIN".equalsIgnoreCase(role)) {
        //    return true;
        //}

        // Vistas disponibles para FUNCIONARIO
        List<String> userViews = List.of(
                "dashboard-view",
                "reservas-funcionario-view",
                "calendarizacion-view",
                "calendarizacion-actividades-view",
                "estadisticas-view"
        );

        // Vistas exclusivas para ADMIN
        List<String> adminViews = List.of(
                "funcionarios-administrador-view",
                "categorias-administrador-view",
                "recursos-administrador-view"
        );

        if (userViews.contains(viewName)) {
            return true;
        }

        return adminViews.contains(viewName) && "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Obtiene el título de la vista según el contexto
     */
    public String getViewTitle(String viewName) {
        return switch (viewName) {
            case "dashboard-view" -> "Dashboard";
            case "reservas-funcionario-view" -> "Reservaciones";
            case "funcionarios-administrador-view" -> "Funcionarios";
            case "categorias-administrador-view" -> "Categorías";
            case "recursos-administrador-view" -> "Recursos";
            case "calendarizacion-view" -> "Calendario";
            case "calendarizacion-actividades-view" -> "Actividades";
            case "estadisticas-view" -> "Estadísticas";
            default -> "Sistema de Reserva";
        };
    }

    // Métodos de utilidad privados
    private String formatDisplayName(String name, String id) {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        if (id != null && !id.trim().isEmpty()) {
            return id;
        }
        return "Usuario";
    }

    private String formatRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return "FUNCIONARIO";
        }

        // Normalizar roles
        return switch (role.toUpperCase()) {
            case "ADMIN", "ADMINISTRADOR" -> "ADMINISTRADOR";
            case "USER", "FUNCIONARIO" -> "FUNCIONARIO";
            default -> role;
        };
    }

    public static class UserSessionInfo {
        private final String id;
        private final String name;
        private final String role;
        private final String displayName;
        private final String displayRole;
        private final boolean isAdmin;

        public UserSessionInfo(String id, String name, String role, String displayName, String displayRole, boolean isAdmin) {
            this.id = id;
            this.name = name;
            this.role = role;
            this.displayName = displayName;
            this.displayRole = displayRole;
            this.isAdmin = isAdmin;
        }

        public static UserSessionInfo guest() {
            return new UserSessionInfo(null, "Invitado", "GUEST", "Invitado", "Invitado", false);
        }

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getRole() { return role; }
        public String getDisplayName() { return displayName; }
        public String getDisplayRole() { return displayRole; }
        public boolean isAdmin() { return isAdmin; }
        public boolean isLoggedIn() { return id != null && !id.isEmpty(); }
    }

    public static class MenuPermissions {
        private final boolean isAdmin;
        private final boolean canManageUsers;
        private final boolean canManageCategories;
        private final boolean canManageResources;
        private final boolean canMakeReservations;
        private final boolean canViewCalendar;
        private final boolean canViewActivities;
        private final boolean canViewStatistics;

        public MenuPermissions(boolean isAdmin) {
            this.isAdmin = isAdmin;
            this.canManageUsers = isAdmin;
            this.canManageCategories = isAdmin;
            this.canManageResources = isAdmin;
            this.canMakeReservations = !isAdmin;
            this.canViewCalendar = true;
            this.canViewActivities = true;
            this.canViewStatistics = true;
        }

        // Getters
        public boolean isAdmin() { return isAdmin; }
        public boolean canManageUsers() { return canManageUsers; }
        public boolean canManageCategories() { return canManageCategories; }
        public boolean canManageResources() { return canManageResources; }
        public boolean canMakeReservations() { return canMakeReservations; }
        public boolean canViewCalendar() { return canViewCalendar; }
        public boolean canViewActivities() { return canViewActivities; }
        public boolean canViewStatistics() { return canViewStatistics; }
    }
}