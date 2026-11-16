package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.concurrent.Task;
import una.proyecto.model.DashboardData;
import una.proyecto.proxy.ServiceProxy;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.utils.SessionManager;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    public Label lblUserWelcome;
    @FXML
    public VBox activeUsersCard;
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
    private TableView<Reserva> tblReservasRecientes;
    @FXML
    private TableColumn<Reserva, String> colId;
    @FXML
    private TableColumn<Reserva, String> colActividad;
    @FXML
    private TableColumn<Reserva, LocalDate> colFecha;
    @FXML
    private TableColumn<Reserva, String> colEstado;
    @FXML
    public TableColumn<Reserva, String> colHora;
    @FXML
    public TableColumn<Reserva, String> colFuncionario;

    @FXML
    public void initialize() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colActividad != null) colActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoActual"));
        if (colHora != null) {
            colHora.setCellValueFactory(cellData -> {
                Reserva r = cellData.getValue();
                if (r != null && r.getHoraInicio() != null && r.getHoraFin() != null) {
                    return new javafx.beans.property.SimpleStringProperty(
                            r.getHoraInicio() + " - " + r.getHoraFin()
                    );
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }

        if (colFuncionario != null) {
            colFuncionario.setCellValueFactory(cellData -> {
                Reserva r = cellData.getValue();
                if (r != null && r.getIdFuncionario() != null) {
                    Funcionario func = r.getFuncionarioReserva();
                    String nombreFuncionario = (func != null && func.getName() != null) ? func.getName() : r.getIdFuncionario();
                    return new javafx.beans.property.SimpleStringProperty(nombreFuncionario);
                }
                return new javafx.beans.property.SimpleStringProperty("");
            });
        }

        updateDashboard();
    }

    /**
     * Carga y procesa los datos filtrando estrictamente por permisos según SessionManager (ADMIN vs FUNCIONARIO).
     */
    private void loadDashboardData(DashboardData data) {
        SessionManager session = SessionManager.getInstance();
        String nombre = session.getName() != null ? session.getName() : "Usuario";
        boolean isAdmin = session.isAdmin();

        lblUserWelcome.setText(nombre);

        List<Reserva> reservasFiltradas;


        if (isAdmin) {
            reservasFiltradas = data.reservas();

            lblUsuariosActivos.setText(String.valueOf(data.totalFuncionarios()));

            if (colFuncionario != null) {
                colFuncionario.setVisible(true);
            }
        } else {
            reservasFiltradas = data.reservas();
            if (activeUsersCard != null) {
                activeUsersCard.setVisible(false);
            }

            if (colFuncionario != null) {
                colFuncionario.setVisible(false);
            }
        }

        lblTotalReservas.setText(String.valueOf(reservasFiltradas.size()));
        lblRecursosDisponibles.setText(String.valueOf(data.totalRecursos()));

        LocalDate hoy = LocalDate.now();
        long actividadesHoyCount = reservasFiltradas.stream()
                .filter(r -> r.getFecha() != null && r.getFecha().equals(hoy))
                .count();
        lblActividadesHoy.setText(String.valueOf(actividadesHoyCount));

        List<Reserva> recientes = reservasFiltradas.stream()
                .sorted((r1, r2) -> {
                    if (r1.getFecha() == null || r2.getFecha() == null) return 0;
                    return r2.getFecha().compareTo(r1.getFecha());
                })
                .limit(5)
                .collect(Collectors.toList());

        if (tblReservasRecientes != null) {
            ObservableList<Reserva> observableRecientes = FXCollections.observableArrayList(recientes);
            tblReservasRecientes.setItems(observableRecientes);
        }
    }

    private void updateDashboard() {
        SessionManager session = SessionManager.getInstance();
        String nombre = session.getName() != null ? session.getName() : "Usuario";
        if (lblUserWelcome != null) {
            lblUserWelcome.setText(nombre);
        }
        lblWelcome.setText("Cargando información...");
        Task<DashboardData> task = new Task<>() {
            @Override protected DashboardData call() { return ServiceProxy.instance().dashboard(); }
        };
        task.setOnSucceeded(event -> {
            lblWelcome.setText("Bienvenido de vuelta");
            loadDashboardData(task.getValue());
        });
        task.setOnFailed(event -> lblWelcome.setText("No se pudo cargar el panel. Verifique la conexión con el servidor."));
        Thread thread = new Thread(task, "Dashboard-Servidor");
        thread.setDaemon(true);
        thread.start();
    }
}