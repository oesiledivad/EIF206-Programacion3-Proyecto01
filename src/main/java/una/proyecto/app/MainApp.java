package una.proyecto.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/una/proyecto/ui/login-view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/una/proyecto/css/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Reserva");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}