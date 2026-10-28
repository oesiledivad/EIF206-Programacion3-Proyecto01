package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.Categoria;
import una.proyecto.model.FilaCalendarizacion;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.utils.*;

import una.proyecto.proxy.ServiceProxy;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final ServiceProxy proxy = ServiceProxy.instance();

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
        try {
            listaCategoria.setAll(proxy.categoriasObtenerTodas());
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
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
        LocalDate fecha = datepicker.getValue();
        Categoria categoriaSeleccionada = choiceboxcategoria.getValue();
        if (fecha == null || categoriaSeleccionada == null) {
            showAlert("Aviso", "Debe seleccionar una fecha y una categoría.");
            return;
        }

        try {
            // 1. Columnas: un recurso de la categoría por columna
            List<Recurso> recursos = proxy.calendarizacionRecursosPorCategoria(categoriaSeleccionada.getId());
            reconstruirColumnas(recursos);

            // 2. Filas de 00:00 a 23:00 vacías
            List<HoraRow> filas = generarFilasDeHora();

            // 3. Reservas de ese día y categoría, ubicadas en su recurso y sus horas
            List<Reserva> reservas = proxy.calendarizacionReservasDelDia(fecha, categoriaSeleccionada.getId());
            llenarFilasConReservas(filas, reservas, recursos);

            tableView.setItems(FXCollections.observableArrayList(filas));
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
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

    private void llenarFilasConReservas(List<HoraRow> filas, List<Reserva> reservas, List<Recurso> recursos) {
        // Ids de los recursos que son columnas: así no hay que pedir cada recurso al servidor.
        Set<String> idsColumnas = recursos.stream().map(Recurso::getId).collect(Collectors.toSet());

        for (Reserva reserva : reservas) {
            String encargado = reserva.getFuncionarioReserva() != null
                    ? reserva.getFuncionarioReserva().getName()
                    : reserva.getIdFuncionario();
            String texto = reserva.getActividad() + " - " + encargado;

            for (String idRecurso : reserva.getRecursosAsignadosIds()) {
                if (!idsColumnas.contains(idRecurso)) {
                    continue; // recurso de otra categoría (la reserva pidió varias)
                }
                LocalTime hora = reserva.getHoraInicio();
                while (hora.isBefore(reserva.getHoraFin())) {
                    buscarFilaPorHora(filas, hora).ifPresent(fila -> fila.setReserva(idRecurso, texto));
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