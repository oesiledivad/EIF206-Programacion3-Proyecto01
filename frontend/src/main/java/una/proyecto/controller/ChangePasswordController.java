package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import una.proyecto.logic.UsuarioLogic;
import una.proyecto.model.Usuario;
import una.proyecto.service.AuthService;
import una.proyecto.utils.DialogController;
import una.proyecto.utils.SessionManager;

public class ChangePasswordController implements DialogController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasenaActual;
    @FXML private PasswordField txtNuevaContrasena;
    @FXML private PasswordField txtConfirmarContrasena;
    @FXML private Label lblError;
    @FXML private Button btnCambiar;
    @FXML private Button btnCancelar;

    private final AuthService authService = new AuthService();
    private final UsuarioLogic usuarioLogic = new UsuarioLogic();
    private final SessionManager sessionManager = SessionManager.getInstance();

    private Stage dialogStage;
    private boolean cambioExitoso = false;

    @FXML
    public void initialize() {
        configurarUsuario();
        configurarEventos();
        hideError();
    }

    /**
     * Configura el campo de usuario:
     * - Si hay sesión: muestra el usuario actual y bloquea el campo
     * - Si no hay sesión: permite escribir el usuario
     */
    private void configurarUsuario() {
        if (sessionManager.isLoggedIn()) {
            String usuarioActual = sessionManager.getId();
            if (usuarioActual != null && !usuarioActual.isEmpty()) {
                txtUsuario.setText(usuarioActual);
            }
            txtUsuario.setEditable(false);
            txtUsuario.setPromptText("Usuario actual (no editable)");
        } else {
            txtUsuario.setEditable(true);
            txtUsuario.setPromptText("Ingrese su usuario");
            txtUsuario.requestFocus();
        }
    }

    private void configurarEventos() {
        btnCambiar.setOnAction(event -> handleCambiarContrasena());
        btnCancelar.setOnAction(event -> handleCancelar());

        // Enter key support
        txtContrasenaActual.setOnAction(event -> handleCambiarContrasena());
        txtNuevaContrasena.setOnAction(event -> handleCambiarContrasena());
        txtConfirmarContrasena.setOnAction(event -> handleCambiarContrasena());
    }

    @Override
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isCambioExitoso() {
        return cambioExitoso;
    }

    private void handleCambiarContrasena() {
        String usuario = txtUsuario.getText().trim();
        String contrasenaActual = txtContrasenaActual.getText();
        String nuevaContrasena = txtNuevaContrasena.getText();
        String confirmarContrasena = txtConfirmarContrasena.getText();

        // Validar campos
        if (usuario.isEmpty()) {
            showError("El usuario es requerido.");
            return;
        }

        if (contrasenaActual.isEmpty()) {
            showError("Ingrese su contraseña actual.");
            return;
        }

        if (nuevaContrasena.isEmpty()) {
            showError("Ingrese una nueva contraseña.");
            return;
        }

        if (nuevaContrasena.length() < 6) {
            showError("La nueva contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!nuevaContrasena.equals(confirmarContrasena)) {
            showError("Las contraseñas no coinciden.");
            return;
        }

        if (nuevaContrasena.equals(contrasenaActual)) {
            showError("La nueva contraseña debe ser diferente a la actual.");
            return;
        }

        // Validar contraseña actual con AuthService
        Usuario usuarioAutenticado = authService.authenticate(usuario, contrasenaActual);
        if (usuarioAutenticado == null) {
            showError("La contraseña actual es incorrecta.");
            return;
        }

        // Cambiar contraseña usando UsuarioLogic
        try {
            usuarioLogic.cambiarContrasena(usuario, contrasenaActual, nuevaContrasena);
            cambioExitoso = true;

            // Mostrar mensaje de éxito
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Contraseña cambiada correctamente.");
            alert.showAndWait();

            // Cerrar diálogo
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (Exception e) {
            showError("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    private void handleCancelar() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.getStyleClass().add("text-danger");
    }

    private void hideError() {
        lblError.setText("");
        lblError.setVisible(false);
    }
}