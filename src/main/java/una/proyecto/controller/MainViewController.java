package una.proyecto.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import una.proyecto.utils.Navigation;
import una.proyecto.utils.SessionManager;

import java.io.IOException;

public class MainViewController {

    @FXML
    public BorderPane mainLayout;
    @FXML
    public VBox sidebar;
    @FXML
    public ToggleButton btnDashboard;
    @FXML
    public ToggleGroup menuGroup;
    @FXML
    public ToggleButton btnReservas;
    @FXML
    public ToggleButton btnFuncionarios;
    @FXML
    public ToggleButton btnCategorias;
    @FXML
    public ToggleButton btnRecursos;
    @FXML
    public ToggleButton btnCalendario;
    @FXML
    public ToggleButton btnActividades;
    @FXML
    public ToggleButton btnEstadisticas;
    @FXML
    public Label lblUsername;
    @FXML
    public Label lblUserRole;
    @FXML
    public Button btnLogout;
    @FXML
    public StackPane viewContainer;
    @FXML
    public Separator sepGeneral;
    @FXML
    public Separator sepAdmin;
    @FXML
    public VBox sidebarMenu;

    @FXML
    public void initialize() {
        if (menuGroup == null) {
            menuGroup = new ToggleGroup();
            btnDashboard.setToggleGroup(menuGroup);
            btnReservas.setToggleGroup(menuGroup);
            btnFuncionarios.setToggleGroup(menuGroup);
            btnCategorias.setToggleGroup(menuGroup);
            btnRecursos.setToggleGroup(menuGroup);
            btnCalendario.setToggleGroup(menuGroup);
            btnActividades.setToggleGroup(menuGroup);
            btnEstadisticas.setToggleGroup(menuGroup);
        }

        // Cargar los datos del usuario directamente desde el SessionManager Singleton
        cargarDatosSesion();

        // Seleccionar Dashboard por defecto
        btnDashboard.setSelected(true);

        // Cargar el dashboard por defecto
        loadView("dashboard-view");

        // Configurar visibilidad según rol
        configureMenuByRole();
    }

    /**
     * Carga la información de la sesión activa en los componentes visuales
     */
    private void cargarDatosSesion() {
        SessionManager session = SessionManager.getInstance();
        String nombre = session.getName();
        String id = session.getId();
        String rol = session.getRole();

        lblUsername.setText(nombre != null ? nombre : (id != null ? id : "Usuario"));
        lblUserRole.setText(rol != null ? rol : "FUNCIONARIO");
    }

    /**
     * Configura qué opciones del menú son visibles según el rol del usuario
     */
    private void configureMenuByRole() {
        boolean isAdmin = SessionManager.getInstance().isAdmin();

        btnFuncionarios.setVisible(isAdmin);
        btnFuncionarios.setManaged(isAdmin);
        btnCategorias.setVisible(isAdmin);
        btnCategorias.setManaged(isAdmin);
        btnRecursos.setVisible(isAdmin);
        btnRecursos.setManaged(isAdmin);

        if (sepAdmin != null) {
            sepAdmin.setVisible(isAdmin);
            sepAdmin.setManaged(isAdmin);
        }

        if (sepGeneral != null) {
            sepGeneral.setVisible(isAdmin);
            sepGeneral.setManaged(isAdmin);
        }
    }

    /**
     * Método genérico para cargar vistas en el StackPane
     */
    private void loadView(String viewName) {
        loadView(viewName, null);
    }

    private void loadView(String viewName, String title) {
        try {
            String fxmlPath = "/una/proyecto/ui/" + viewName + ".fxml";
            Parent view = Navigation.loadView(fxmlPath);

            viewContainer.getChildren().setAll(view);

            if (mainLayout.getScene() != null) {
                Stage stage = (Stage) mainLayout.getScene().getWindow();

                if (stage != null) {
                    if (title != null && !title.isEmpty()) {
                        stage.setTitle(title);
                        Navigation.updateWindowTitle(title);
                    }

                    if (!stage.isMaximized()) {
                        stage.centerOnScreen();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la vista: " + viewName);
        }
    }

    /**
     * Método para cargar vista y pasar datos al controlador
     */
    private void loadViewWithData(String viewName, String title, String currentUserId) {
        try {
            String fxmlPath = "/una/proyecto/ui/" + viewName + ".fxml";
            Navigation.ViewLoaderResult result = Navigation.loadViewWithController(fxmlPath);

            viewContainer.getChildren().setAll(result.getRoot());

            if (mainLayout.getScene() != null) {
                Stage stage = (Stage) mainLayout.getScene().getWindow();

                if (stage != null) {
                    if (title != null && !title.isEmpty()) {
                        stage.setTitle(title);
                        Navigation.updateWindowTitle(title);
                    }

                    if (!stage.isMaximized()) {
                        stage.centerOnScreen();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la vista: " + viewName);
        }
    }

    // METODOS DE NAVEGACIÓN

    @FXML
    public void goToDashboard(ActionEvent actionEvent) {
        loadView("dashboard-view", "Dashboard");
    }

    @FXML
    public void goToReservations(ActionEvent actionEvent) {
        loadViewWithData("reservas-funcionario-view", "Reservaciones", SessionManager.getInstance().getId());
    }

    @FXML
    public void goToFuncionarios(ActionEvent actionEvent) {
        loadView("funcionarios-administrador-view", "Funcionarios");
    }

    @FXML
    public void goToCategorias(ActionEvent actionEvent) {
        loadView("categorias-administrador-view", "Categorias");
    }

    @FXML
    public void goToRecursos(ActionEvent actionEvent) {
        loadView("recursos-administrador-view", "Recursos");
    }

    @FXML
    public void goToCalendario(ActionEvent actionEvent) {
        loadView("calendarizacion-view");
    }

    @FXML
    public void goToActividades(ActionEvent actionEvent) {
        loadView("actividades-view");
    }

    @FXML
    public void goToEstadisticas(ActionEvent actionEvent) {
        loadView("estadisticas-view", "Estadisticas");
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar Sesión");
        confirm.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirm.setContentText("Se cerrará la sesión actual.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.getInstance().logout();

            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                Navigation.navigateTo(stage, "/una/proyecto/ui/login-view.fxml", "Sistema de Reserva - Login");
                Navigation.disableMaximizeButton();
                stage.setHeight(600);
                stage.setWidth(635);
                Navigation.getTitleBarController().setDraggable(false);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error al volver al login");
            }
        }
    }

    // METODOS DE UTILIDAD

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}