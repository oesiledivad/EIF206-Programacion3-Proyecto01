package una.proyecto.utils;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class WindowHelper {

    private static double xOffset = 0;
    private static double yOffset = 0;

    /**
     * Configura el comportamiento de arrastre y los botones de la barra de título personalizada.
     */
    public static void makeWindowDraggable(HBox titleBar, Button btnMinimize, Button btnMaximize, Button btnClose, Runnable onMaximizeChanged) {
        titleBar.setOnMousePressed((MouseEvent event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged((MouseEvent event) -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        if (btnMinimize != null) {
            btnMinimize.setOnAction(event -> {
                Stage stage = (Stage) btnMinimize.getScene().getWindow();

                FadeTransition fade = new FadeTransition(Duration.millis(3500), stage.getScene().getRoot());
                fade.setFromValue(1.0);
                fade.setToValue(0.0);
                fade.setOnFinished(e -> {
                    stage.setIconified(true);
                    stage.getScene().getRoot().setOpacity(1.0);
                });
                fade.play();
            });
        }

        if (btnMaximize != null) {
            btnMaximize.setOnAction(event -> {

                Stage stage = (Stage) btnMaximize.getScene().getWindow();
                var root = stage.getScene().getRoot();

                FadeTransition fadeOut =
                        new FadeTransition(Duration.millis(120), root);

                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.4);

                fadeOut.setOnFinished(e -> {

                    stage.setMaximized(!stage.isMaximized());
                    if (onMaximizeChanged != null) {
                        onMaximizeChanged.run();
                    }

                    Platform.runLater(() -> {

                        FadeTransition fadeIn = new FadeTransition(Duration.millis(120), root);

                        fadeIn.setFromValue(0.4);
                        fadeIn.setToValue(1.0);

                        fadeIn.play();
                    });
                });

                fadeOut.play();
            });
        }

        if (btnClose != null) {
            btnClose.setOnAction(event -> {
                Stage stage = (Stage) btnClose.getScene().getWindow();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar salida");
                alert.setHeaderText("¿Estás seguro de que deseas salir?");
                alert.setContentText("Se cerrará la sesión actual y la aplicación.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {

                    FadeTransition fade = new FadeTransition(Duration.millis(200), stage.getScene().getRoot());
                    fade.setFromValue(1.0);
                    fade.setToValue(0.0);
                    fade.setOnFinished(e -> {
                        Platform.exit();
                        System.exit(0);
                    });
                    fade.play();
                }
            });
        }
    }
}