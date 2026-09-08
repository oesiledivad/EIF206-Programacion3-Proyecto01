package una.proyecto.utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import una.proyecto.controller.TitleBarController;

import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public final class Navigation {

    private static final String CSS_PATH = "/una/proyecto/css/style.css";
    private static TitleBarController titleBarController;

    private Navigation() {
    }

    public static void setTitleBarController(TitleBarController controller) {
        titleBarController = controller;
    }

    // NAVEGACIÓN PRINCIPAL

    /**
     * Cambia la escena actual por otra vista FXML.
     *
     * @param stage     Stage donde se mostrará la vista.
     * @param fxmlPath  Ruta del archivo FXML.
     * @param title     Título de la ventana.
     */
    public static void navigateTo(Stage stage, String fxmlPath, String title) throws IOException {
        ViewLoaderResult result = loadViewWithController(fxmlPath);
        showView(stage, result.getRoot(), title);
        updateWindowTitle(title);
    }


    /**
     * Cambia la escena y retorna el controlador de la vista.
     *
     * @param stage     Stage donde se mostrará la vista.
     * @param fxmlPath  Ruta del archivo FXML.
     * @param title     Título de la ventana.
     * @param <T>       Tipo del controlador.
     * @return          Controlador de la vista.
     */
    @SuppressWarnings("unchecked")
    public static <T> T navigateToWithController(Stage stage, String fxmlPath, String title) throws IOException {
        ViewLoaderResult result = loadViewWithController(fxmlPath);
        showView(stage, result.getRoot(), title);
        return (T) result.getController();
    }

    /**
     * Cambia la escena obteniendo el Stage desde un Node.
     *
     * @param node      Nodo desde el cual se obtiene el Stage.
     * @param fxmlPath  Ruta del archivo FXML.
     * @param title     Título de la ventana.
     */
    public static void navigateFromNode(Node node, String fxmlPath, String title) throws IOException {
        Stage stage = getStage(node);
        navigateTo(stage, fxmlPath, title);
    }

    /**
     * Cambia la escena y retorna el controlador,
     * obteniendo el Stage desde un Node.
     *
     * @param node      Nodo desde el cual se obtiene el Stage.
     * @param fxmlPath  Ruta del archivo FXML.
     * @param title     Título de la ventana.
     * @param <T>       Tipo del controlador.
     * @return          Controlador de la vista.
     */
    @SuppressWarnings("unchecked")
    public static <T> T navigateFromNodeWithController(Node node, String fxmlPath, String title) throws IOException {
        Stage stage = getStage(node);
        return navigateToWithController(stage, fxmlPath, title);
    }

    // DIÁLOGOS MODALES
    /**
     * Abre un diálogo modal a partir de un archivo FXML.
     *
     * @param fxmlPath    Ruta del archivo FXML del diálogo.
     * @param title       Título de la ventana del diálogo.
     * @param ownerWindow Ventana propietaria (dueño) del diálogo.
     * @param <T>         Tipo del controlador.
     * @return            El controlador del diálogo, para configurarlo antes de mostrarlo.
     * @throws IOException Si no se puede cargar el FXML.
     */
    public static <T> DialogResult<T> openDialog(String fxmlPath, String title, Window ownerWindow) throws IOException {
        return openDialog(fxmlPath, title, ownerWindow, null);
    }

    /**
     * Abre un diálogo modal a partir de un archivo FXML con configuración opcional del controlador.
     *
     * @param fxmlPath         Ruta del archivo FXML del diálogo.
     * @param title            Título de la ventana del diálogo.
     * @param ownerWindow      Ventana propietaria (dueño) del diálogo.
     * @param controllerConfig Configuración opcional para el controlador antes de mostrar.
     * @param <T>              Tipo del controlador.
     * @return                 El controlador del diálogo.
     * @throws IOException     Si no se puede cargar el FXML.
     */
    @SuppressWarnings("unchecked")
    public static <T> DialogResult<T> openDialog(String fxmlPath, String title, Window ownerWindow,
                                                 Consumer<T> controllerConfig) throws IOException {

        // Cargar FXML
        FXMLLoader loader = createLoader(fxmlPath);
        Parent root = loader.load();
        T controller = loader.getController();

        // Aplicar configuración al controlador si existe
        if (controllerConfig != null && controller != null) {
            controllerConfig.accept(controller);
        }

        // Crear Stage del diálogo
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(ownerWindow);
        dialogStage.setResizable(false);

        // Configurar escena
        Scene scene = new Scene(root);

        // Aplicar tema actual si existe
        if (ThemeManager.IsDarkMode()) {
            scene.getRoot().getStyleClass().add("dark-mode");
        }

        // Agregar stylesheet
        addStylesheet(scene);

        dialogStage.setScene(scene);

        // Pasar el Stage al controlador si implementa DialogController
        if (controller instanceof DialogController) {
            ((DialogController) controller).setDialogStage(dialogStage);
        }

        // Mostrar diálogo
        dialogStage.showAndWait();

        return new DialogResult<>(dialogStage, controller);
    }

    /**
     * Abre un diálogo modal y retorna el controlador.
     * Versión simplificada para casos donde solo se necesita el controlador.
     *
     * @param fxmlPath    Ruta del archivo FXML del diálogo.
     * @param title       Título de la ventana del diálogo.
     * @param ownerWindow Ventana propietaria (dueño) del diálogo.
     * @param <T>         Tipo del controlador.
     * @return            El controlador del diálogo.
     * @throws IOException Si no se puede cargar el FXML.
     */
    public static <T> T openDialogAndGetController(String fxmlPath, String title, Window ownerWindow) throws IOException {
        DialogResult<T> result = openDialog(fxmlPath, title, ownerWindow);
        return result.getController();
    }

    /**
     * Abre un diálogo modal y retorna el controlador con configuración.
     *
     * @param fxmlPath         Ruta del archivo FXML del diálogo.
     * @param title            Título de la ventana del diálogo.
     * @param ownerWindow      Ventana propietaria (dueño) del diálogo.
     * @param controllerConfig Configuración del controlador.
     * @param <T>              Tipo del controlador.
     * @return                 El controlador del diálogo.
     * @throws IOException     Si no se puede cargar el FXML.
     */
    public static <T> T openDialogAndGetController(String fxmlPath, String title, Window ownerWindow,
                                                   Consumer<T> controllerConfig) throws IOException {
        DialogResult<T> result = openDialog(fxmlPath, title, ownerWindow, controllerConfig);
        return result.getController();
    }

    // CARGA DE VISTAS

    /**
     * Carga un FXML y retorna su nodo raíz.
     *
     * @param fxmlPath Ruta del archivo FXML.
     * @return         Nodo raíz de la vista.
     */
    public static Parent loadView(String fxmlPath) throws IOException {
        FXMLLoader loader = createLoader(fxmlPath);
        return loader.load();
    }

    /**
     * Carga un FXML y retorna su controlador.
     *
     * @param fxmlPath Ruta del archivo FXML.
     * @param <T>      Tipo del controlador.
     * @return         Controlador de la vista.
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadController(String fxmlPath) throws IOException {
        FXMLLoader loader = createLoader(fxmlPath);
        loader.load();
        return (T) loader.getController();
    }

    /**
     * Carga un FXML y retorna tanto la vista como su controlador.
     *
     * @param fxmlPath Ruta del archivo FXML.
     * @return         Resultado con root y controller.
     */
    public static ViewLoaderResult loadViewWithController(String fxmlPath) throws IOException {
        FXMLLoader loader = createLoader(fxmlPath);
        Parent root = loader.load();
        return new ViewLoaderResult(root, loader.getController());
    }

    /**
     * Carga un FXML y permite configurar el controlador
     * antes de retornar la vista.
     *
     * @param fxmlPath         Ruta del archivo FXML.
     * @param controllerConfig Configuración que se aplicará al controlador.
     * @return                 Nodo raíz de la vista.
     */
    public static Parent loadViewWithConfig(String fxmlPath, Consumer<Object> controllerConfig) throws IOException {
        ViewLoaderResult result = loadViewWithController(fxmlPath);
        Object controller = result.getController();
        if (controller != null && controllerConfig != null) {
            controllerConfig.accept(controller);
        }
        return result.getRoot();
    }

    // MÉTODOS PRIVADOS

    /**
     * Crea un FXMLLoader para la ruta indicada.
     * <p>
     * Centraliza la creación de FXMLLoader para evitar
     * repetir la resolución del recurso en varios métodos.
     */
    private static FXMLLoader createLoader(String fxmlPath) {
        URL resource = Navigation.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalArgumentException("No se encontró el archivo FXML: " + fxmlPath);
        }
        return new FXMLLoader(resource);
    }


    /**
     * Muestra una vista en un Stage.
     *
     * Centraliza toda la lógica relacionada con:
     * - creación de Scene
     * - configuración del Stage
     * - aplicación del tema
     * - visualización de la ventana
     */
    private static void showView(Stage stage, Parent root, String title) {
        Scene scene = createScene(root);
        configureStage(stage, scene, title);
        stage.show();
    }

    /**
     * Configura el Stage con la Scene indicada, evitando que rebase la pantalla.
     */
    private static void configureStage(Stage stage, Scene scene, String title) {
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(true);
        ResizeHelper.addResizeListener(stage);

        Screen screen = Screen.getPrimary();
        Rectangle2D visualBounds = screen.getVisualBounds();

        if (stage.getWidth() > visualBounds.getWidth() || stage.getHeight() > visualBounds.getHeight()) {
            stage.setWidth(Math.min(stage.getWidth(), visualBounds.getWidth() * 0.95));
            stage.setHeight(Math.min(stage.getHeight(), visualBounds.getHeight() * 0.95));
        }

        if (stage.getMinWidth() <= 0) stage.setMinWidth(600);
        if (stage.getMinHeight() <= 0) stage.setMinHeight(635);

        ThemeManager.applyTheme(scene);
    }


    /**
     * Crea una Scene y agrega el stylesheet global.
     */
    private static Scene createScene(Parent root) {
        Scene scene = new Scene(root);
        addStylesheet(scene);
        return scene;
    }

    /**
     * Agrega el stylesheet global a la Scene.
     */
    private static void addStylesheet(Scene scene) {
        URL resource = Navigation.class.getResource(CSS_PATH);
        if (resource == null) {
            System.err.println("No se pudo encontrar el archivo CSS: " + CSS_PATH);
            return;
        }
        scene.getStylesheets().add(resource.toExternalForm());
    }

    /**
     * Obtiene el Stage asociado a un Node.
     */
    private static Stage getStage(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("El Node no puede ser null.");
        }
        if (node.getScene() == null) {
            throw new IllegalStateException("El Node no está asociado a una Scene.");
        }
        if (node.getScene().getWindow() == null) {
            throw new IllegalStateException("La Scene del Node no tiene un Window asociado.");
        }
        return (Stage) node.getScene().getWindow();
    }

    // CLASES DE RESULTADO

    /**
     * Contiene el resultado de cargar un FXML: el nodo raíz y su controlador.
     */
    public static final class ViewLoaderResult {
        private final Parent root;
        private final Object controller;

        public ViewLoaderResult(Parent root, Object controller) {
            this.root = root;
            this.controller = controller;
        }

        public Parent getRoot() { return root; }
        public Object getController() { return controller; }
    }

    /**
     * Contiene el resultado de abrir un diálogo: el Stage y el controlador.
     */
    public static final class DialogResult<T> {
        private final Stage dialogStage;
        private final T controller;

        public DialogResult(Stage dialogStage, T controller) {
            this.dialogStage = dialogStage;
            this.controller = controller;
        }

        public Stage getDialogStage() { return dialogStage; }
        public T getController() { return controller; }
    }

    // MÉTODOS PARA TITLE BAR

    public static void updateWindowTitle(String title) {
        if (titleBarController != null) {
            titleBarController.setWindowTitle(title);
        }
    }

    public static void disableMinimizeButton() {
        if (titleBarController != null) {
            titleBarController.disableMinimizeButton();
        }
    }

    public static void disableMaximizeButton() {
        if (titleBarController != null) {
            titleBarController.disableMaximizeButton();
        }
    }

    public static void enableMaximizeButton() {
        if (titleBarController != null) {
            titleBarController.enableMaximizeButton();
        }
    }

    public static void enableMinimizeButton() {
        if (titleBarController != null) {
            titleBarController.enableMinimizeButton();
        }
    }

    public static TitleBarController getTitleBarController() {
        return titleBarController;
    }
}