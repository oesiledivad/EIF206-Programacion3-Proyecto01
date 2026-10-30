package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.utils.SessionManager;
import javafx.scene.control.SelectionMode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

public class ReservasController {

    @FXML private Button btnreserva;
    @FXML private Button btncancelarreserva;
    @FXML private Button btnlimpiar;
    @FXML private Button btnExtraer;
    @FXML private Button btnImprimir;
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

    private final ServiceProxy proxy = ServiceProxy.instance();
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
            List<String> recursos = reserva.getRecursosAsignadosIds();
            String texto = (recursos == null || recursos.isEmpty()) ? "" : String.join(", ", recursos);
            return new javafx.beans.property.SimpleStringProperty(texto);
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
            listaCategoria.setAll(proxy.categoriasObtenerTodas());
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
            List<Reserva> reservas = proxy.reservasObtener();

            listaReservaUsuario.setAll(reservas);
            tableviewmisreservas.refresh();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "No se pudieron cargar las reservas: " + e.getMessage());
        }
    }


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
        List<Categoria> categoriasSeleccionadas = new ArrayList<>(
                listviewcategorias.getSelectionModel().getSelectedItems()
        );

        // 4. Validar campos básicos antes de consultar al servidor
        if (actividad.isEmpty() || fecha == null || horaInicio == null || horaFin == null || categoriasSeleccionadas.isEmpty()) {
            showAlert("Error", "Por favor, complete todos los campos y seleccione al menos una categoría.");
            return;
        }

        // 5. Separar las categorías que tienen recursos de las que no
        List<Categoria> categoriasSinRecursos;
        List<Categoria> categoriasConRecursos;
        try {
            categoriasSinRecursos = proxy.categoriasSinRecursos(categoriasSeleccionadas);
            categoriasConRecursos = proxy.categoriasConRecursos(categoriasSeleccionadas);
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

        // 6. Crear la reserva. El servidor valida las horas, asigna los recursos y la guarda.
        try {
            Reserva creada = proxy.reservaCrear(actividad, fecha, horaInicio, horaFin, categoriasConRecursos);

            recargarReservas();
            limpiarFormulario();

            String recursos = creada.getRecursosAsignadosIds() == null
                    ? "" : String.join(", ", creada.getRecursosAsignadosIds());
            showAlert("Éxito", "La reserva " + creada.getId() + " ha sido registrada exitosamente."
                    + (recursos.isEmpty() ? "" : "\nRecursos asignados: " + recursos));

        } catch (Exception e) {
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
            proxy.reservaCancelar(reserva.getId());
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

        Task<Reserva> task = new Task<>() {
            @Override
            protected Reserva call() {
                return proxy.reservaExtraerConIA(frase);
            }
        };

        task.setOnSucceeded(event -> {
            Reserva extraida = task.getValue();

            txtareaactividad.setText(extraida.getActividad());
            datapickerfecha.setValue(extraida.getFecha());
            choiceboxhorainicio.setValue(extraida.getHoraInicio());
            choiceboxhorafin.setValue(extraida.getHoraFin());

            // Las categorías llegan como objetos nuevos desde el servidor:
            // se seleccionan las de la lista que tengan el mismo id.
            listviewcategorias.getSelectionModel().clearSelection();
            List<Categoria> categoriasExtraidas = extraida.getCategoriasDeRecursos();
            if (categoriasExtraidas != null) {
                for (Categoria extraidaCat : categoriasExtraidas) {
                    for (Categoria disponible : listaCategoria) {
                        if (disponible.getId().equals(extraidaCat.getId())) {
                            listviewcategorias.getSelectionModel().select(disponible);
                        }
                    }
                }
            }
        });

        task.setOnFailed(event -> {
            Throwable e = task.getException();
            showAlert("ERROR DE EXTRACCION", e.getMessage());
        });

        new Thread(task).start();
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
    @FXML
    public void btnImprimir() {
        try {
            TablePDF nuevo = new TablePDF();

            // Encabezados
            nuevo.setEncabezados(List.of(
                    columId.getText(),
                    columActividad.getText(),
                    columFecha.getText(),
                    columHora.getText(),
                    columRecurso.getText(),
                    columEstado.getText()
            ));

            // Filas
            for (Reserva reserva : tableviewmisreservas.getItems()) {

                String recursos = "";

                if (reserva.getRecursosAsignadosIds() != null
                        && !reserva.getRecursosAsignadosIds().isEmpty()) {

                    recursos = String.join(
                            ", ",
                            reserva.getRecursosAsignadosIds()
                    );
                }

                nuevo.agregarFila(List.of(
                        reserva.getId(),
                        reserva.getActividad(),
                        reserva.getFecha().toString(),
                        reserva.getHorario(),
                        recursos,
                        reserva.getEstado().toString()
                ));
            }

            ReportePDF reporte =
                    new ReportePDF("Reporte de mis reservas", nuevo);

            GeneradorPDFS.generar(
                    reporte,
                    "MisReservas.pdf"
            );

            showAlert(
                    "Éxito",
                    "PDF generado correctamente."
            );

        } catch (Exception e) {

            showAlert(
                    "Error",
                    "Error al generar PDF."
            );

            e.printStackTrace();
        }
    }

}