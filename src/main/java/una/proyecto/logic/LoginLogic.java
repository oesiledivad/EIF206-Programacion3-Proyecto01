package una.proyecto.logic;

import una.proyecto.model.Usuario;
import una.proyecto.service.AuthService;
import una.proyecto.utils.SessionManager;

public class LoginLogic {

    private final AuthService authService;

    public LoginLogic() {
        this.authService = new AuthService();
    }
    // constructor para tests
    public LoginLogic(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Valida las credenciales del usuario según reglas de negocio
     * @param userId ID del usuario
     * @param password Contraseña del usuario
     * @return Resultado de autenticación con información detallada
     */
    public LoginResult validateCredentials(String userId, String password) {
        // Validacion
        if (userId == null || userId.trim().isEmpty()) {
            return LoginResult.error("El ID de usuario es requerido");
        }

        if (password == null || password.trim().isEmpty()) {
            return LoginResult.error("La contraseña es requerida");
        }

        // Validacion de formato
        if (!isValidUserIdFormat(userId)) {
            return LoginResult.error("Formato de ID inválido");
        }

        if (!isValidPasswordFormat(password)) {
            return LoginResult.error("La contraseña debe de ser mayor a 6 carácteres");
        }

        // Autenticacion contra el sistema
        Usuario usuario = authService.authenticate(userId.trim(), password.trim());

        if (usuario == null) {
            return LoginResult.error("Credenciales incorrectas");
        }

        // Verificar si el usuario esta activo
        // TODO: cuando se arreglen otras cosas
       // if (!usuario.isActive()) {
       //     return LoginResult.error("Usuario inactivo, contacte al administrador");
       // }

        // Iniciar sesion en el sistema
        SessionManager.getInstance().login(
                usuario.getId(),
                usuario.getName(),
                usuario.getRole()
        );

        return LoginResult.success(usuario, "Login exitoso");
    }

    private boolean isValidUserIdFormat(String userId) {
        if (userId == null) {
            return false;
        }

        userId = userId.trim();

        // Administrador
        if (userId.equalsIgnoreCase("admin")) {
            return true;
        }

        // Funcionario: cedula costarricense de 9 dígitos
        return userId.matches("^\\d{9}$");
    }

    private boolean isValidPasswordFormat(String password) {
        if (password == null) {
            return false;
        }

        // Contraseña del administrador
        if (password.equals("admin")) {
            return true;
        }

        // Contraseña de funcionarios
        return password.length() >= 6;
    }


    /**
     * Resultado de validación de login
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final Usuario usuario;

        private LoginResult(boolean success, String message, Usuario usuario) {
            this.success = success;
            this.message = message;
            this.usuario = usuario;
        }

        public static LoginResult success(Usuario usuario, String message) {
            return new LoginResult(true, message, usuario);
        }

        public static LoginResult error(String message) {
            return new LoginResult(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Usuario getUsuario() { return usuario; }
    }
}