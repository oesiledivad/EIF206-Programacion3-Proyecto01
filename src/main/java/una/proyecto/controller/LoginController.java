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
import org.kordamp.ikonli.javafx.FontIcon;
import una.proyecto.utils.Navigation;
import una.proyecto.utils.ThemeManager;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    public VBox testVBoxLogin;

    @FXML
    public FontIcon darkModeIcon;

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
    private Label lblError;

    @FXML
    public void initialize() {
        lblError.setVisible(false);

        btnLogin.setOnAction(event -> handleLogin());
        btnSalir.setOnAction(event -> handleExitButton());

        boolean isDarkMode = btnDarkMode.getScene() != null && btnDarkMode.getScene().getRoot().getStyleClass().contains("dark-mode");
        updateIconTheme(isDarkMode);

        btnDarkMode.setOnAction(event -> toggleThemeWithFade());
        testVBoxLogin.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
                event.consume();
            }
        });

        Platform.runLater(() -> txtUserId.requestFocus());
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

        btnLogin.setText("Iniciando Sesion...");
        btnLogin.setDisable(true);
        btnSalir.setDisable(true);
        btnDarkMode.setDisable(true);
        txtUserId.setDisable(true);
        txtPassword.setDisable(true);
        btnLogin.getScene().setCursor(Cursor.WAIT);

        PauseTransition delay = new PauseTransition(Duration.millis(1000));
        delay.setOnFinished(event -> {

            if (authenticate(userId, password)) {
                try {
                    Stage stage = (Stage) btnLogin.getScene().getWindow();

                    MainViewController mainController =
                            Navigation.navigateToWithController(
                                    stage,
                                    "/una/proyecto/ui/main-view.fxml",
                                    "Sistema de Reserva - Panel Principal"
                            );

                    stage.setResizable(true);

                    mainController.setUserData(
                            userId,
                            getUserName(userId),
                            getUserRole(userId)
                    );

                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Error al cargar la aplicación");
                    restoreControls();
                }
            } else {
                showError("ID o clave incorrectos");
                txtPassword.clear();
                txtUserId.requestFocus();
                restoreControls();
            }
        });

        delay.play();
    }

    private void restoreControls() {
        btnLogin.setText("Iniciar Sesion");
        btnLogin.setDisable(false);
        btnSalir.setDisable(false);
        txtUserId.setDisable(false);
        txtPassword.setDisable(false);
        btnDarkMode.setDisable(false);
        btnLogin.getScene().setCursor(Cursor.DEFAULT);

    }

    // METODOS DE AUTENTICACION

    private boolean authenticate(String userId, String password) {
        // TODO: Implementar autenticación contra archivo XML
        return (userId.equals("admin") || userId.equals("user"))
                && password.length() >= 4;
    }

    private String getUserName(String userId) {
        // TODO: Obtener nombre desde XML
        if ("admin".equals(userId)) return "Administrador";
        if ("user".equals(userId)) return "Farmeador Aura";
        return userId;
    }

    private String getUserRole(String userId) {
        // TODO: Obtener rol desde XML
        if ("admin".equals(userId)) return "ADMIN";
        return "FUNCIONARIO";
    }

    // METODOS DE UTILIDAD

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setVisible(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> lblError.setVisible(false));
        pause.play();
    }
}