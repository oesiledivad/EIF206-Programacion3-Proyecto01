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

    // Usuario actual (se setea desde el login)
    private String currentUserId;
    private String currentUserRole;

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

        // Seleccionar Dashboard por defecto
        btnDashboard.setSelected(true);

        // Cargar el dashboard por defecto
        loadView("dashboard-view");

        // Configurar visibilidad según rol (admin/employee)
        configureMenuByRole();
    }

    /**
     * Configura qué opciones del menú son visibles según el rol del usuario
     */
    private void configureMenuByRole() {
        boolean isAdmin = "ADMIN".equals(currentUserRole);

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
     * Establece los datos del usuario después del login
     */
    public void setUserData(String userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserRole = role;
        lblUsername.setText(userName != null ? userName : userId);
        lblUserRole.setText(role != null ? role : "Usuario");
        configureMenuByRole();
    }

    /**
     * Método genérico para cargar vistas en el StackPane
     */
    private void loadView(String viewName) {

        try {

            Parent view = Navigation.loadView("/una/proyecto/ui/" + viewName + ".fxml");

            viewContainer.getChildren().setAll(view);

        } catch (IOException e) {

            e.printStackTrace();

            showError("Error al cargar la vista: " + viewName);
        }
    }

    private void loadView(String viewName, String title) {

        try {

            Parent view = Navigation.loadView("/una/proyecto/ui/" + viewName + ".fxml");

            viewContainer.getChildren().setAll(view);

            Stage stage = (Stage) mainLayout.getScene().getWindow();

            stage.setTitle(title);

        } catch (IOException e) {

            e.printStackTrace();

            showError(
                    "Error al cargar la vista: " + viewName
            );
        }
    }

    /**
     * Método para cargar vista y pasar datos al controlador
     */
    private void loadViewWithData(String viewName, String currentUserId) {

        try {

            String fxmlPath = "/una/proyecto/ui/" + viewName + ".fxml";

            Navigation.ViewLoaderResult result = Navigation.loadViewWithController(fxmlPath);

            Object controller =  result.getController();

            viewContainer.getChildren().clear();
            viewContainer.getChildren().add(result.getRoot());

        } catch (IOException e) {

            e.printStackTrace();

            showError("Error al cargar la vista: " + viewName);
        }
    }

    // METODOS DE NAVEGACIÓN

    @FXML
    public void goToDashboard(ActionEvent actionEvent) {
        loadView("dashboard-view");
        // TODO: Actualizar datos del dashboard
    }

    @FXML
    public void goToReservations(ActionEvent actionEvent) {
        loadViewWithData("reservations-view", currentUserId);
    }

    @FXML
    public void goToFuncionarios(ActionEvent actionEvent) {
        loadView("funcionarios-administrador-view", "Sistema de Reservas - Funcionarios");
    }

    @FXML
    public void goToCategorias(ActionEvent actionEvent) {
        loadView("categorias-administrador-view");
    }

    @FXML
    public void goToRecursos(ActionEvent actionEvent) {
        loadView("recursos-view");
    }

    @FXML
    public void goToCalendario(ActionEvent actionEvent) {
        loadView("calendario-view");
    }

    @FXML
    public void goToActividades(ActionEvent actionEvent) {
        loadView("actividades-view");
    }

    @FXML
    public void goToEstadisticas(ActionEvent actionEvent) {
        loadView("estadisticas-view");
    }

    @FXML
    public void handleLogout(ActionEvent actionEvent) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);

        confirm.setTitle("Cerrar Sesión");
        confirm.setHeaderText("¿Está seguro que desea cerrar sesión?");
        confirm.setContentText("Se cerrará la sesión actual.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {

            currentUserId = null;
            currentUserRole = null;

            try {

                Stage stage = (Stage) btnLogout.getScene().getWindow();

                Navigation.navigateTo(stage, "/una/proyecto/ui/login-view.fxml", "Sistema de Reserva - Login");

                stage.setResizable(false);

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