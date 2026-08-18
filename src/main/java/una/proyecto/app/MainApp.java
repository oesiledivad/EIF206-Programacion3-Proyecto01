package una.proyecto.app;

import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerLight;
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
        //Application.setUserAgentStylesheet(new Prime().getUserAgentStylesheet());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Sistema de Reserva");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}