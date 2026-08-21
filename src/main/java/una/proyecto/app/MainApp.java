package una.proyecto.app;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import una.proyecto.utils.Navigation;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        Navigation.navigateTo(primaryStage,
                "/una/proyecto/ui/login-view.fxml",
                "Sistema de Reserva");
    }
}