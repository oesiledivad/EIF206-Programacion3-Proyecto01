package una.proyecto.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController {

    @FXML
    public Label lblLogin;
    @FXML
    public Button btnLogin;
    @FXML
    public Button btnSalir;

    @FXML
    public void initialize() {
        lblLogin.setVisible(false);
        btnLogin.setOnAction(event -> handleLoginButton());
        btnSalir.setOnAction(event -> handleExitButton());
    }

    private void handleLoginButton( ) {
        lblLogin.setVisible(true);
        lblLogin.setText("Sin implementar");
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(5));

        pauseTransition.setOnFinished(event -> {
            lblLogin.setVisible(false);
        });
        pauseTransition.play();
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
}