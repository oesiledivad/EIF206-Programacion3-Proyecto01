package una.proyecto.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

public class Navigation {

    private static final String CSS_PATH = "/una/proyecto/css/style.css";

    // CAMBIAR ESCENA

    /**
     * Cambia la escena actual por otra vista FXML.
     */
    public static void navigateTo(
            Stage stage,
            String fxmlPath,
            String title
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));

        Parent root = loader.load();
        Scene scene = createScene(root);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    /**
     * Cambia la escena y retorna el controlador de la vista.
     */
    public static <T> T navigateToWithController(
            Stage stage,
            String fxmlPath,
            String title
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));

        Parent root = loader.load();
        Scene scene = createScene(root);

        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        return loader.getController();
    }

    // CAMBIAR ESCENA (desde cualquier Node)

    /**
     * Cambia la escena obteniendo el Stage desde un Node.
     */
    public static void navigateFromNode(
            Node node,
            String fxmlPath,
            String title
    ) throws IOException {

        Stage stage = (Stage) node.getScene().getWindow();
        navigateTo(stage, fxmlPath, title);
    }

    /**
     * Cambia la escena y retorna el controlador,
     * obteniendo el Stage desde un Node.
     */
    public static <T> T navigateFromNodeWithController(
            Node node,
            String fxmlPath,
            String title
    ) throws IOException {

        Stage stage = (Stage) node.getScene().getWindow();
        return navigateToWithController(stage, fxmlPath, title);
    }

    // CARGAR VISTAS

    /**
     * Carga un FXML y retorna su nodo raíz.
     */
    public static Parent loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Navigation.class.getResource(fxmlPath)
        );
        return loader.load();
    }

    /**
     * Carga un FXML y retorna su controlador.
     */
    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Navigation.class.getResource(fxmlPath)
        );
        loader.load();
        return loader.getController();
    }

    /**
     * Carga un FXML y retorna tanto la vista como su controlador.
     */
    public static ViewLoaderResult loadViewWithController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Navigation.class.getResource(fxmlPath)
        );
        Parent root = loader.load();
        return new ViewLoaderResult(root, loader.getController());
    }

    /**
     * Carga un FXML y permite configurar el controlador antes de retornar.
     */
    public static Parent loadViewWithConfig(
            String fxmlPath,
            Consumer<Object> controllerConfig
    ) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Navigation.class.getResource(fxmlPath)
        );
        Parent root = loader.load();
        Object controller = loader.getController();

        if (controller != null && controllerConfig != null) {
            controllerConfig.accept(controller);
        }

        return root;
    }

    // METODOS PRIVADOS

    private static Scene createScene(Parent root) {
        Scene scene = new Scene(root);
        addStylesheet(scene);
        return scene;
    }

    private static void addStylesheet(Scene scene) {
        try {
            String css = Navigation.class.getResource(CSS_PATH).toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el archivo CSS: " + CSS_PATH);
        }
    }

    // RESULTADO DE CARGAR UNA VISTA

    public static class ViewLoaderResult {
        private final Parent root;
        private final Object controller;

        public ViewLoaderResult(Parent root, Object controller) {
            this.root = root;
            this.controller = controller;
        }

        public Parent getRoot() {
            return root;
        }
        public Object getController() {
            return controller;
        }
    }
}