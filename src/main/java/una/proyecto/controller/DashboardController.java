package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import una.proyecto.utils.SessionManager;

public class DashboardController {
    @FXML
    public Label lblUserWelcome;
    @FXML
    private Label lblWelcome;

    @FXML
    private Label lblTotalReservas;

    @FXML
    private Label lblRecursosDisponibles;

    @FXML
    private Label lblActividadesHoy;

    @FXML
    private Label lblUsuariosActivos;

    @FXML
    private TableView<?> tblReservasRecientes;


    private String currentUserId;
    private String currentUserRole;

    /**
     * Recibe los datos del usuario desde MainController
     */
    public void setUserData(String userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        updateDashboard();
    }

    @FXML
    public void initialize() {


        // Cargar datos iniciales
        loadDashboardData();
    }

    private void loadDashboardData() {
        // TODO: Cargar datos reales desde el sistema
        // Por ahora datos de ejemplo
        SessionManager session = SessionManager.getInstance();
        String nombre = session.getName();
        lblUserWelcome.setText(nombre);
        lblTotalReservas.setText("1,284");
        lblRecursosDisponibles.setText("42");
        lblActividadesHoy.setText("8");
        lblUsuariosActivos.setText("156");
    }

    private void updateDashboard() {
        // TODO: Actualizar el saludo con el nombre del usuario
        String userName = currentUserId != null ? currentUserId : "Usuario";
        lblWelcome.setText("Bienvenido de vuelta, " + userName);

        // Recargar datos
        loadDashboardData();
    }
}