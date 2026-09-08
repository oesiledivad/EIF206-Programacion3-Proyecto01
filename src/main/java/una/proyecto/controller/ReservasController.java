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
import java.util.stream.Collectors;

public class ReservasController {

    @FXML private Button btnreserva;
    @FXML private Button btncancelarreserva;
    @FXML private Button btnlimpiar;
    @FXML private Button btnExtraer;
    @FXML private TextArea txtareafrase;
    @FXML private TextArea txtareaactividad;
    @FXML private DatePicker datapickerfecha;
    @FXML private ChoiceBox<LocalTime> choiceboxhorainicio;
    @FXML private ChoiceBox<LocalTime> choiceboxhorafin;
    @FXML private ListView<Categoria> listviewcategorias;
    @FXML private TableView<Reserva> tableviewmisreservas;

    @FXML private TableColumn<Reserva, String> columId;
    @FXML private TableColumn<Reserva, String> columActividad;
    @FXML private TableColumn<Reserva, LocalDate> columFecha;
    @FXML private TableColumn<Reserva, String> columHora;
    @FXML private TableColumn<Reserva, String> columRecurso;
    @FXML private TableColumn<Reserva, EstadoReserva> columEstado;

    private final ObservableList<Reserva> listaReservaUsuario = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategoria = FXCollections.observableArrayList();

    private final ReservaService reservaService = AppFactory.createReservaService();
    private final RecursoService recursoService = AppFactory.createRecursoDatos();
    private final CategoriaService categoriaService = AppFactory.createCategoriaService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        configurarChoiceBox();
        configureTableView();
        configureListView();
        configurarFechaActual();
        cargarDatosIniciales();
        configurarEventos();
    }

    private void configurarChoiceBox() {
        ObservableList<LocalTime> listaHoraria = FXCollections.observableArrayList();
        for (int hora = 0; hora <= 23; hora++) {
            listaHoraria.add(LocalTime.of(hora, 0));
        }
        choiceboxhorafin.setItems(listaHoraria);
        choiceboxhorainicio.setItems(listaHoraria);
    }

    private void configureTableView() {
        columId.setCellValueFactory(new PropertyValueFactory<>("id"));
        columActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        columFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        columHora.setCellValueFactory(new PropertyValueFactory<>("horario"));
        columEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        columRecurso.setCellValueFactory(cellData -> {
            Reserva reserva = cellData.getValue();
            String recursos = recursoService.obtenerRecursosParaTabe(reserva.getCategoriasDeRecursos());
            return new javafx.beans.property.SimpleStringProperty(recursos != null ? recursos : "");
        });

        tableviewmisreservas.setItems(listaReservaUsuario);
    }

    private void configureListView() {
        listviewcategorias.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listviewcategorias.setItems(listaCategoria);
    }

    private void configurarFechaActual() {
        datapickerfecha.setValue(LocalDate.now());
    }

    private void configurarEventos() {
        btnreserva.setOnAction(event -> handleReservaButton());
        btnlimpiar.setOnAction(event -> handleLimpiarButton());
        btncancelarreserva.setOnAction(event -> handleCancelarReservaButton());
        if (btnExtraer != null) {
            btnExtraer.setOnAction(event -> handleExtraerButton());
        }
    }

    // CARGA DE DATOS

    private void cargarDatosIniciales() {
        cargarCategorias();
        cargarReservasUsuario();
    }

    private void cargarCategorias() {
        try {
            listaCategoria.setAll(categoriaService.obtenerTodas());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    /**
     * Carga solo las reservas del usuario actual
     */
    private void cargarReservasUsuario() {
        try {
            String idUsuario = sessionManager.getId();
            if (idUsuario == null) {
                listaReservaUsuario.clear();
                return;
            }

            // Obtener reservas del usuario
            List<Reserva> reservas = reservaService.obtenerReservasPorFuncionario(idUsuario);
            reservaService.repoblarCategoriasService(reservas, listaCategoria);

            listaReservaUsuario.setAll(reservas);
            tableviewmisreservas.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las reservas: " + e.getMessage());
        }
    }

    /**
     * Recarga las reservas del usuario
     */
    private void recargarReservas() {
        cargarReservasUsuario();
    }

    // MANEJADORES DE EVENTOS

    private void handleReservaButton() {
        // 1. Validar sesión
        if (!sessionManager.isLoggedIn()) {
            showAlert("Error", "Debe iniciar sesión para realizar una reserva.");
            return;
        }

        // 2. Validar permisos
        if (sessionManager.isAdmin()) {
            showAlert("Error", "Solo los funcionarios pueden realizar reservas.");
            return;
        }

        // 3. Obtener datos del formulario
        String actividad = txtareaactividad.getText().trim();
        LocalDate fecha = datapickerfecha.getValue();
        LocalTime horaInicio = choiceboxhorainicio.getValue();
        LocalTime horaFin = choiceboxhorafin.getValue();
        String idUsuario = sessionManager.getId();
        List<Categoria> categoriasSeleccionadas = new ArrayList<>(
                listviewcategorias.getSelectionModel().getSelectedItems()
        );

        // 4. Validar campos básicos ANTES de tocar categorías/recursos
        if (actividad.isEmpty() || fecha == null || horaInicio == null || horaFin == null || categoriasSeleccionadas.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos y seleccione al menos una categoría.");
            return;
        }

        // 5. Filtrar categorías según si poseen recursos o no
        List<Categoria> categoriasSinRecursos;
        List<Categoria> categoriasConRecursos;
        try {
            categoriasSinRecursos = categoriaService.categoriaNoPoseeRecursos(categoriasSeleccionadas);
            categoriasConRecursos = categoriaService.categoriaConRecursos(categoriasSeleccionadas);
        } catch (RuntimeException e) {
            showAlert("Error", e.getMessage());
            return;
        }

        // Caso 1: ninguna categoría seleccionada tiene recursos -> cortar
        if (categoriasSeleccionadas.size() == categoriasSinRecursos.size()) {
            showAlert("Error", "Todas las categorías seleccionadas no poseen recursos disponibles.");
            return;
        }

        // Caso 2: algunas no tienen recursos -> avisar, pero seguir con las que sí
        if (!categoriasSinRecursos.isEmpty()) {
            showAlert("Advertencia", "Las siguientes categorías no poseen recursos disponibles y no serán incluidas: "
                    + categoriasSinRecursos.stream()
                    .map(Categoria::getDescripcion)
                    .collect(Collectors.joining(", ")));
        }

        // 6. Validar horas
        try {
            reservaService.verificarHorasService(fecha, horaInicio, horaFin);
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de validación");
            alert.setHeaderText("Error en las horas seleccionadas");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return;
        }

        // 7. Crear y guardar reserva (SOLO con las categorías que sí tienen recursos)
        try {
            Reserva nueva = reservaService.crearReserva(
                    actividad,
                    fecha,
                    horaInicio,
                    horaFin,
                    idUsuario,
                    categoriasConRecursos,
                    EstadoReserva.ACTIVA
            );

            reservaService.save(nueva);

            recargarReservas();
            limpiarFormulario();
            showAlert("Éxito", "La reserva ha sido registrada exitosamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo realizar la reserva: " + e.getMessage());
        }
    }

    private void handleCancelarReservaButton() {
        // 1. Validar sesión
        if (!sessionManager.isLoggedIn()) {
            showAlert("Error", "Debe iniciar sesión para cancelar una reserva.");
            return;
        }

        // 2. Obtener reserva seleccionada
        Reserva reserva = tableviewmisreservas.getSelectionModel().getSelectedItem();

        if (reserva == null) {
            showAlert("Error", "Por favor, seleccione una reserva de la tabla para cancelar.");
            return;
        }

        // 3. Validar propiedad
        if (!sessionManager.getId().equals(reserva.getIdFuncionario())) {
            showAlert("Error", "No puede cancelar una reserva que no le pertenece.");
            return;
        }

        // 4. Confirmar cancelación
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar cancelación");
        confirm.setHeaderText("¿Está seguro de cancelar esta reserva?");
        confirm.setContentText("Reserva: " + reserva.getActividad() + " - " + reserva.getFecha());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        // 5. Eliminar reserva
        try {
            reservaService.delete(reserva.getId());
            recargarReservas();
            showAlert("Éxito", "La reserva ha sido cancelada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo cancelar la reserva: " + e.getMessage());
        }
    }

    private void handleLimpiarButton() {
        txtareafrase.clear();
        txtareaactividad.clear();
        listviewcategorias.getSelectionModel().clearSelection();
        choiceboxhorainicio.getSelectionModel().clearSelection();
        choiceboxhorafin.getSelectionModel().clearSelection();
        datapickerfecha.setValue(LocalDate.now());
    }

    private void handleExtraerButton() {
        String frase = txtareafrase.getText().trim();
        if (frase.isEmpty()) {
            showAlert("Información", "Ingrese una frase para extraer información.");
            return;
        }
        // TODO: Implementar extracción
        showAlert("Extraer", "Función de extracción en desarrollo.\nFrase ingresada: " + frase);
    }

    // UTILIDADES

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void limpiarFormulario() {
        txtareafrase.clear();
        txtareaactividad.clear();
        listviewcategorias.getSelectionModel().clearSelection();
        choiceboxhorainicio.getSelectionModel().clearSelection();
        choiceboxhorafin.getSelectionModel().clearSelection();
        datapickerfecha.setValue(LocalDate.now());
    }
}