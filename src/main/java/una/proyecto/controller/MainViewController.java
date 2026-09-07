package una.proyecto.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import una.proyecto.logic.MainLogic;
import una.proyecto.logic.MainLogic.UserSessionInfo;
import una.proyecto.logic.MainLogic.MenuPermissions;
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

    // INYECCIÓN DE LOGICA
    private final MainLogic mainLogic  = new MainLogic();

    @FXML
    public void initialize() {
        setupMenuGroup();
        loadUserInfo();
        configureMenuByPermissions();
        loadDefaultView();
    }

    private void setupMenuGroup() {
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
    }

    /**
     * Carga la información del usuario usando la lógica de negocio
     */
    private void loadUserInfo() {
        UserSessionInfo userInfo = mainLogic.getCurrentUserInfo();

        lblUsername.setText(userInfo.getDisplayName());
        lblUserRole.setText(userInfo.getDisplayRole());
    }

    /**
     * Configura el menú según los permisos del usuario
     */
    private void configureMenuByPermissions() {
        MenuPermissions permissions = mainLogic.getMenuPermissions();

        // Visibilidad de elementos administrativos
        boolean showAdmin = permissions.isAdmin();

        btnFuncionarios.setVisible(showAdmin);
        btnFuncionarios.setManaged(showAdmin);
        btnCategorias.setVisible(showAdmin);
        btnCategorias.setManaged(showAdmin);
        btnRecursos.setVisible(showAdmin);
        btnRecursos.setManaged(showAdmin);

        if (sepAdmin != null) {
            sepAdmin.setVisible(showAdmin);
            sepAdmin.setManaged(showAdmin);
        }

        if (sepGeneral != null) {
            sepGeneral.setVisible(showAdmin);
            sepGeneral.setManaged(showAdmin);
        }
    }

    /**
     * Carga la vista por defecto
     */
    private void loadDefaultView() {
        btnDashboard.setSelected(true);
        loadViewWithAccessControl("dashboard-view");
    }

    /**
     * Carga una vista con control de acceso
     */
    private void loadViewWithAccessControl(String viewName) {
        if (!mainLogic.hasAccessToView(viewName)) {
            showError("No tiene permisos para acceder a esta vista");
            return;
        }

        String title = mainLogic.getViewTitle(viewName);
        loadView(viewName, title);
        // Estado actual del controlador
    }

    /**
     * Método genérico para cargar vistas
     */
    private void loadView(String viewName, String title) {
        try {
            String fxmlPath = "/una/proyecto/ui/" + viewName + ".fxml";
            Parent view = Navigation.loadView(fxmlPath);
            viewContainer.getChildren().setAll(view);

            updateWindowTitle(title);
            //centerWindowIfNotMaximized();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la vista: " + viewName);
        }
    }

    /**
     * Carga vista con datos específicos para el controlador
     */
    private void loadViewWithData(String viewName, String title, String userId) {
        try {
            String fxmlPath = "/una/proyecto/ui/" + viewName + ".fxml";
            Navigation.ViewLoaderResult result = Navigation.loadViewWithController(fxmlPath);
            viewContainer.getChildren().setAll(result.getRoot());

            updateWindowTitle(title);
            //centerWindowIfNotMaximized();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error al cargar la vista: " + viewName);
        }
    }

    // Métodos auxiliares de UI
    private void updateWindowTitle(String title) {
        if (mainLayout.getScene() != null) {
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            if (stage != null && title != null && !title.isEmpty()) {
                stage.setTitle("Sistema de Reserva - " + title);
                Navigation.updateWindowTitle(title);
            }
        }
    }

    private void centerWindowIfNotMaximized() {
        if (mainLayout.getScene() != null) {
            Stage stage = (Stage) mainLayout.getScene().getWindow();
            if (stage != null && !stage.isMaximized()) {
                stage.centerOnScreen();
            }
        }
    }

    // MÉTODOS DE NAVEGACIÓN

    @FXML
    public void goToDashboard(ActionEvent actionEvent) {
        loadViewWithAccessControl("dashboard-view");
    }

    @FXML
    public void goToReservations(ActionEvent actionEvent) {
        if (mainLogic.hasAccessToView("reservas-funcionario-view")) {
            loadViewWithData("reservas-funcionario-view", "Reservaciones",
                    SessionManager.getInstance().getId());
        } else {
            showError("No tiene permisos para acceder a reservaciones");
        }
    }

    @FXML
    public void goToFuncionarios(ActionEvent actionEvent) {
        loadViewWithAccessControl("funcionarios-administrador-view");
    }

    @FXML
    public void goToCategorias(ActionEvent actionEvent) {
        loadViewWithAccessControl("categorias-administrador-view");
    }

    @FXML
    public void goToRecursos(ActionEvent actionEvent) {
        loadViewWithAccessControl("recursos-administrador-view");
    }

    @FXML
    public void goToCalendario(ActionEvent actionEvent) {
        loadViewWithAccessControl("calendarizacion-view");
    }

    @FXML
    public void goToActividades(ActionEvent actionEvent) {
        loadViewWithAccessControl("calendarizacion-actividades-view");
    }

    @FXML
    public void goToEstadisticas(ActionEvent actionEvent) {
        loadViewWithAccessControl("estadisticas-view");
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar Sesión");
        confirm.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirm.setContentText("Se cerrará la sesión actual.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Usar logica para logout
            mainLogic.logoutUser();

            try {
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                Navigation.navigateTo(stage, "/una/proyecto/ui/login-view.fxml", "Sistema de Reserva - Login");
                Navigation.disableMaximizeButton();
                stage.setHeight(600);
                stage.setWidth(635);

                if (Navigation.getTitleBarController() != null) {
                    Navigation.getTitleBarController().setDraggable(false);
                }

            } catch (IOException e) {
                e.printStackTrace();
                showError("Error al volver al login");
            }
        }
    }

    // MÉTODOS DE UTILIDAD

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}