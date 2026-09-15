package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.Categoria;
import una.proyecto.model.FilaCalendarizacion;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;
import una.proyecto.utils.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalendarizacionController {

    @FXML
    private Button btncargar;
    @FXML
    private Button btnImprimir;
    @FXML
    private ChoiceBox<Categoria> choiceboxcategoria;
    @FXML
    private DatePicker datepicker;

    // Estos dos fx:id vienen del FXML: fx:id="tableView" y fx:id="colHoras"
    @FXML
    private TableView<HoraRow> tableView;
    @FXML
    private TableColumn<HoraRow, String> colHoras;

    private final ObservableList<Reserva> listaReservaUsuario = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategoria = FXCollections.observableArrayList();

    private final ReservaService reservaService = AppFactory.createReservaService();
    private final RecursoService recursoService = AppFactory.createRecursoDatos();
    private final CategoriaService categoriaService = AppFactory.createCategoriaService();

    @FXML
    private void initialize() {
        configurarFechaActual();
        configureChoiceBox();
        configurarColumnaHoras();
        configurarEventos();
    }

    private void configurarColumnaHoras() {
        colHoras.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getHora()));
        colHoras.setMinWidth(80);
        colHoras.setMaxWidth(80);
        colHoras.setPrefWidth(80);
    }

    private void configureChoiceBox() {
        listaCategoria.setAll(categoriaService.obtenerTodas());
        choiceboxcategoria.setItems(listaCategoria);

        if (!listaCategoria.isEmpty()) {
            choiceboxcategoria.getSelectionModel().selectFirst();
        }
    }

    private void configurarFechaActual() {
        datepicker.setValue(LocalDate.now());
    }

    private void configurarEventos() {
        btncargar.setOnAction(event -> handleButtonCargar());
    }

    private void handleButtonCargar() {

        // Paso 1: leer los filtros
        LocalDate fecha = datepicker.getValue();
        Categoria categoriaSeleccionada = choiceboxcategoria.getValue();
        if (fecha == null || categoriaSeleccionada == null) {
            return;
        }

        // Paso 2 y 3: traer recursos de la categoria y reconstruir columnas
        List<Recurso> recursos = recursoService.recursosPorCategoria(categoriaSeleccionada.getId());
        reconstruirColumnas(recursos);

        // Paso 5: generar las filas de hora vacias
        List<HoraRow> filas = generarFilasDeHora();

        // Paso 4, 6: filtrar reservas y llenar las filas correspondientes
        llenarFilasConReservas(filas, fecha, categoriaSeleccionada.getId());

        // Paso 7: cargar las filas ya llenas en la tabla
        tableView.setItems(FXCollections.observableArrayList(filas));
    }

    private List<HoraRow> generarFilasDeHora() {
        List<HoraRow> filas = new ArrayList<>();
        for (int h = 0; h <= 23; h++) {
            String horaTexto = String.format("%02d:00", h);
            filas.add(new HoraRow(horaTexto));
        }
        return filas;
    }

    private void reconstruirColumnas(List<Recurso> recursos) {
        tableView.getColumns().remove(1, tableView.getColumns().size());

        for (Recurso recurso : recursos) {
            TableColumn<HoraRow, String> col = new TableColumn<>(recurso.getDescripcion());
            col.setPrefWidth(150);

            col.setCellValueFactory(data ->
                    new javafx.beans.property.SimpleStringProperty(
                            data.getValue().getReserva(recurso.getId())));

            tableView.getColumns().add(col);
        }
    }

    private void llenarFilasConReservas(List<HoraRow> filas, LocalDate fecha, String idCategoria) {

        List<Reserva> reservas = reservaService.filtrarReservasService(fecha, idCategoria);
        System.out.println("Reservas encontradas: " + reservas.size());

        for (Reserva reserva : reservas) {
            System.out.println("Revisando reserva: " + reserva.getActividad());

            for (String idRecurso : reserva.getRecursosAsignadosIds()) {
                System.out.println("  Recurso asignado id: " + idRecurso);

                Recurso recurso = recursoService.recurPorId(idRecurso);
                System.out.println("  Recurso encontrado: " + (recurso != null ? recurso.getDescripcion() : "NULL"));

                if (recurso == null || !recurso.getIdCategoria().equals(idCategoria)) {
                    System.out.println("  --> Descartado, categoria no coincide. idCategoria buscado=" + idCategoria
                            + " vs recurso.getIdCategoria()=" + (recurso != null ? recurso.getIdCategoria() : "N/A"));
                    continue;
                }

                String texto = reserva.getActividad() + " - " + reserva.getFuncionarioReserva().getName();

                LocalTime hora = reserva.getHoraInicio();
                while (hora.isBefore(reserva.getHoraFin())) {
                    boolean encontrada = buscarFilaPorHora(filas, hora).isPresent();
                    System.out.println("  Hora " + hora + " -> fila encontrada: " + encontrada);

                    String idRecursoFinal = idRecurso;
                    buscarFilaPorHora(filas, hora)
                            .ifPresent(fila -> fila.setReserva(idRecursoFinal, texto));

                    hora = hora.plusHours(1);
                }
            }
        }
    }

    private Optional<HoraRow> buscarFilaPorHora(List<HoraRow> filas, LocalTime hora) {
        String horaTexto = String.format("%02d:00", hora.getHour());
        return filas.stream()
                .filter(f -> f.getHora().equals(horaTexto))
                .findFirst();
    }
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @FXML
    public void btnImprimir() {
        try {
            TablePDF nuevo = new TablePDF();

            // Encabezados
            nuevo.setEncabezados(List.of(
                    colHoras.getText()
            ));

            // Filas;
            for (HoraRow fila : tableView.getItems()) {

                nuevo.agregarFila(List.of(
                        fila.getHora()
                ));
            }
            System.out.println("PDF preparado");
            LocalDate fechaSeleccionada = datepicker.getValue();

            ReportePDF reporte = new ReportePDF("Reporte de actividades Calendario", nuevo);
            GeneradorPDFS.generar(reporte, "Calendarizacion.pdf");

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