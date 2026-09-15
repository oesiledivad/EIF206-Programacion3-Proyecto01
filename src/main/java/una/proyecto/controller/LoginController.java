package una.proyecto.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import org.kordamp.ikonli.javafx.FontIcon;
import una.proyecto.logic.LoginLogic;
import una.proyecto.utils.Navigation;
import una.proyecto.utils.ThemeManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class LoginController {
    @FXML
    public VBox testVBoxLogin;

    @FXML
    public FontIcon darkModeIcon;
    @FXML
    public Label lblCurrentDate;

    @FXML
    private TextField txtUserId;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnSalir;

    @FXML
    private Button btnDarkMode;

    @FXML
    private Button btnCambiarContrasena;

    @FXML
    private Label lblError;

    private final LoginLogic loginLogic = new LoginLogic();

    @FXML
    public void initialize() {
        setupUIComponents();
        setupEventHandlers();
        setupClock();
        setupTheme();
        setupKeyboardShortcuts();

        Platform.runLater(() -> txtUserId.requestFocus());
    }

    private void setupUIComponents() {
        lblError.setVisible(false);
    }

    private void setupEventHandlers() {
        btnLogin.setOnAction(event -> handleLogin());
        btnSalir.setOnAction(event -> handleExitButton());
        btnDarkMode.setOnAction(event -> toggleThemeWithFade());
        btnCambiarContrasena.setOnAction(event -> abrirDialogoCambioContrasena());
    }

    private void setupClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> lblCurrentDate.setText(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd 'de' MMMM 'del' yyyy, HH:mm:ss", new Locale("es", "ES"))
        ))), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void setupTheme() {
        boolean isDarkMode = btnDarkMode.getScene() != null && btnDarkMode.getScene().getRoot().getStyleClass().contains("dark-mode");
        updateIconTheme(isDarkMode);
    }

    private void setupKeyboardShortcuts() {
        testVBoxLogin.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
                event.consume();
            }
        });
    }

    private void toggleThemeWithFade() {
        var root = btnDarkMode.getScene().getRoot();
        boolean isDarkMode = root.getStyleClass().contains("dark-mode");

        FadeTransition fadeOut = new FadeTransition(Duration.millis(180), testVBoxLogin);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.3);

        fadeOut.setOnFinished(e -> {
            if (isDarkMode) {
                root.getStyleClass().remove("dark-mode");
                updateIconTheme(false);
                ThemeManager.setDarkMode(false);
            } else {
                root.getStyleClass().add("dark-mode");
                updateIconTheme(true);
                ThemeManager.setDarkMode(true);
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(180), testVBoxLogin);
            fadeIn.setFromValue(0.3);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void updateIconTheme(boolean isDarkMode) {
        if (isDarkMode) {
            darkModeIcon.setIconLiteral("fa-sun-o");
        } else {
            darkModeIcon.setIconLiteral("fa-moon-o");
        }
    }

    private void handleExitButton() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir de la aplicación");
        alert.setHeaderText("¿Estás seguro de que deseas salir?");
        alert.setContentText("Se cerrará la sesión actual.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    @FXML
    private void handleLogin() {
        String userId = txtUserId.getText().trim();
        String password = txtPassword.getText().trim();

        if (userId.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese ID y clave");
            return;
        }

        setUIStateForLogin(true);

        PauseTransition delay = createDelay(userId, password);
        delay.play();
    }

    private PauseTransition createDelay(String userId, String password) {
        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(event -> {
            LoginLogic.LoginResult result = loginLogic.validateCredentials(userId, password);

            if (result.isSuccess()) {
                navigateToMainScreen();
            } else {
                showError(result.getMessage());
                txtPassword.clear();
                txtUserId.requestFocus();
                setUIStateForLogin(false);
            }
        });
        return delay;
    }

    private void navigateToMainScreen() {
        try {
            Stage stage = (Stage) btnLogin.getScene().getWindow();

            stage.resizableProperty().setValue(Boolean.TRUE);
            stage.setResizable(true);
            stage.setMaximized(false);
            stage.setFullScreen(false);
            Navigation.navigateToWithController(
                    stage,
                    "/una/proyecto/ui/main-view.fxml",
                    "Sistema de Reserva - Panel Principal"
            );

            Navigation.enableMaximizeButton();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la aplicación");
            setUIStateForLogin(false);
        }
    }

    /**
     * Abre el diálogo para cambiar la contraseña usando Navigation.openDialog
     */
    private void abrirDialogoCambioContrasena() {
        try {
            ChangePasswordController controller = Navigation.openDialogAndGetController(
                    "/una/proyecto/ui/change-password-dialog.fxml",
                    "Cambiar Contraseña",
                    btnLogin.getScene().getWindow()
            );

            if (controller != null && controller.isCambioExitoso()) {
                txtPassword.clear();
                showSuccess("Contraseña cambiada exitosamente. Por favor inicie sesión con su nueva contraseña.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al abrir el diálogo de cambio de contraseña: " + e.getMessage());
        }
    }

    private void setUIStateForLogin(boolean inProcess) {
        if (inProcess) {
            btnLogin.setText("Iniciando Sesión...");
            btnLogin.setDisable(true);
            btnSalir.setDisable(true);
            btnDarkMode.setDisable(true);
            btnCambiarContrasena.setDisable(true);
            txtUserId.setDisable(true);
            txtPassword.setDisable(true);
            btnLogin.getScene().setCursor(Cursor.WAIT);
        } else {
            btnLogin.setText("Iniciar Sesión");
            btnLogin.setDisable(false);
            btnSalir.setDisable(false);
            txtUserId.setDisable(false);
            txtPassword.setDisable(false);
            btnDarkMode.setDisable(false);
            btnCambiarContrasena.setDisable(false);
            btnLogin.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.getStyleClass().setAll("text-danger");

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> lblError.setVisible(false));
        pause.play();
    }

    private void showSuccess(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.getStyleClass().setAll("text-success");

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> lblError.setVisible(false));
        pause.play();
    }
}