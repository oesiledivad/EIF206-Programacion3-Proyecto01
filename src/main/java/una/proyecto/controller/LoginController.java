package una.proyecto.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import una.proyecto.utils.Navigation;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    public VBox testVBoxLogin;
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
        btnDarkMode.setOnAction(event -> toggleDarkMode());
        testVBoxLogin.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin();
            }
        });

        Platform.runLater(() -> txtUserId.requestFocus());
    }

    private void toggleDarkMode() {
        // TODO: arreglar esto y que persista
        var root = btnDarkMode.getScene().getRoot();

        if (root.getStyleClass().contains("dark-mode")) {
            root.getStyleClass().remove("dark-mode");
            btnDarkMode.setText("Modo oscuro");
        } else {
            root.getStyleClass().add("dark-mode");
            btnDarkMode.setText("Modo claro");
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
                stage.setWidth(1200);
                stage.setHeight(800);
                stage.setMinWidth(900);
                stage.setMinHeight(600);

                mainController.setUserData(
                        userId,
                        getUserName(userId),
                        getUserRole(userId)
                );

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error al cargar la aplicación");
            }
        } else {
            showError("ID o clave incorrectos");
            txtPassword.clear();
            txtUserId.requestFocus();
        }
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