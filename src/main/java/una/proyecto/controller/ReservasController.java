package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;
import una.proyecto.utils.AppFactory;
import una.proyecto.utils.SessionManager;
import javafx.scene.control.SelectionMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ReservasController {

    @FXML private Button btnreserva;
    @FXML private Button btncancelarreserva;
    @FXML private Button btnlimpiar;
    @FXML private TextArea txtareafrase;
    @FXML private TextArea txtareaactividad;
    @FXML private DatePicker datapickerfecha;
    @FXML private ChoiceBox <LocalTime>choiceboxhorainicio;
    @FXML private ChoiceBox <LocalTime>choiceboxhorafin;
    @FXML private ListView<Categoria> listviewcategorias;
    @FXML private TableView<Reserva> tableviewmisreservas;

    @FXML private TableColumn<Reserva, String> columId;
    @FXML private TableColumn<Reserva, String> columActividad;
    @FXML private TableColumn<Reserva, LocalDate> columFecha;
    @FXML private TableColumn<Reserva, String> columHora;
    @FXML private TableColumn<Reserva, String> columRecurso;
    @FXML private TableColumn<Reserva,EstadoReserva> columEstado;
    private ObservableList<Reserva> listaReserva= FXCollections.observableArrayList();
    private ObservableList<Categoria> listaCategoria= FXCollections.observableArrayList();
    // En tu ReservaController (fuera de los métodos, como atributo de clase):
    private final ReservaService reservaService = AppFactory.createReservaService();

    @FXML public void initialize(){
        configurarChoiceBox();
        configureTableView();
        configureListView();
        cargarCategorias();
        cargarReservas();
        btnreserva.setOnAction(event -> handleReservaButton());
        btnlimpiar.setOnAction(event -> handleLimpiarButton());
        btncancelarreserva.setOnAction(event -> handleCancelarReservaButton());

    }
    //Configura los choice box y le agrega todas las horas
    private void configurarChoiceBox(){
        ObservableList<LocalTime> listaHoraria = FXCollections.observableArrayList();

        for (int hora = 0; hora <= 23; hora++) {
            listaHoraria.add(LocalTime.of(hora, 0));
        }

        choiceboxhorafin.setItems(listaHoraria);
        choiceboxhorainicio.setItems(listaHoraria);
    }

    //aca busca getEstado();
    private void configureTableView(){
        columId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        columFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columHora.setCellValueFactory(new PropertyValueFactory<>("horario"));
        columEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        /*columRecurso.setCellValueFactory(cellData -> {
            List<String> recursos = cellData.getValue().getIdRecursosAsignados();
            if (recursos == null || recursos.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            return new javafx.beans.property.SimpleStringProperty(String.join(", ", recursos));
        });
        */

        tableviewmisreservas.setItems(listaReserva);
    }
    private void configureListView(){
        listviewcategorias.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listviewcategorias.setItems(listaCategoria);
    }
    // Carga las categorías existentes desde el backend al iniciar la pantalla.
    // Ajustá "createCategoriaService()" / "findAll()" al nombre real de tu service.
    private void cargarCategorias(){
        try {
            var categoriaService = new CategoriaService();
            listaCategoria.addAll(categoriaService.obtenerTodas());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }
    // Carga las reservas existentes del usuario/backend al iniciar la pantalla.
    private void cargarReservas(){
        try {
            listaReserva.addAll(reservaService.obtenerTodasReservas());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las reservas: " + e.getMessage());
        }
    }
    private void handleReservaButton() {
        if (SessionManager.getInstance().isAdmin()) {
            String actividad = txtareaactividad.getText();
            LocalDate date = datapickerfecha.getValue();
            LocalTime horaInicio = choiceboxhorainicio.getValue();
            LocalTime horaFin = choiceboxhorafin.getValue();
            String idUsuario = SessionManager.getInstance().getId();
            List<Categoria> asignada = new ArrayList<>(
                    listviewcategorias.getSelectionModel().getSelectedItems()
            );

            // 1. Validar que ningún campo esencial esté vacío (incluyendo la categoría seleccionada)
            if (actividad == null || actividad.isBlank() || date == null || horaInicio == null || horaFin == null || asignada == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Campos incompletos");
                alert.setContentText("Por favor, complete todos los campos y seleccione una categoría antes de realizar la reserva.");
                alert.showAndWait();
                return;
            }

            // 3. Crear el objeto Reserva
            Reserva nueva = new Reserva(actividad, date, horaInicio, horaFin, idUsuario, asignada, EstadoReserva.ACTIVA);
            // 4. Guardar en backend y actualizar la ObservableList de la TableView
            reservaService.save(nueva);
            listaReserva.add(nueva);
            showAlert("Éxito", "La reserva ha sido registrada exitosamente.");
        } else {
            showAlert("No se pudo realizar la reserva", "El usuario no es funcionario.");
        }
    }
    private void handleCancelarReservaButton() {
        // 1. Obtener la reserva seleccionada en la TableView
        Reserva reserva = tableviewmisreservas.getSelectionModel().getSelectedItem();

        // 2. Validar que el usuario haya seleccionado una fila
        if (reserva == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No seleccionó una reserva");
            alert.setContentText("Por favor, seleccione una reserva de la tabla para cancelar.");
            alert.showAndWait();
            return;
        }

        // 3. Eliminar la reserva del backend / servicio
        reservaService.delete(reserva.getId());

        // 4. Eliminar de la ObservableList para que la tabla se actualice automáticamente
        listaReserva.remove(reserva);

        // 5. Mostrar mensaje de confirmación
        showAlert("Éxito", "La reserva ha sido cancelada correctamente.");
    }
    private void handleLimpiarButton(){
        txtareafrase.clear();
        txtareaactividad.clear();
        listviewcategorias.getSelectionModel().clearSelection();
        choiceboxhorainicio.getSelectionModel().clearSelection();
        choiceboxhorafin.getSelectionModel().clearSelection();
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

}