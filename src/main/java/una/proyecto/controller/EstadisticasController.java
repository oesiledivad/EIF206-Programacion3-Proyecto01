package una.proyecto.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.EstadisticaItem;

import java.time.LocalDate;

public class EstadisticasController {

    @FXML
    private BarChart<String, Number> barChartActividades;
    @FXML
    private BarChart<String, Number> barChartRecursos;
    @FXML
    private Button btnGenerarActividades;
    @FXML
    private Button btnGenerarRecursos;
    @FXML
    private Button btnImprimirActividades;
    @FXML
    private Button btnImprimirRecursos;
    @FXML
    private DatePicker dpActividadesDesde;
    @FXML
    private DatePicker dpActividadesHasta;
    @FXML
    private DatePicker dpRecursosDesde;
    @FXML
    private DatePicker dpRecursosHasta;
    @FXML
    private Label lblErrorActividades;
    @FXML
    private Label lblErrorRecursos;
    @FXML
    private TableView<EstadisticaItem> tableViewActividades;
    @FXML
    private TableView<EstadisticaItem> tableViewRecursos;

    private final ObservableList<EstadisticaItem> resourceData = FXCollections.observableArrayList(
            new EstadisticaItem("Sala 10 personas", 12),
            new EstadisticaItem("Laptop Windows 11", 8),
            new EstadisticaItem("Proyector 4K", 5),
            new EstadisticaItem("Sala 20 personas", 3),
            new EstadisticaItem("Tablet Android", 7)
    );

    private final ObservableList<EstadisticaItem> activityData = FXCollections.observableArrayList(
            new EstadisticaItem("Semana 31 (01-07 ago)", 4),
            new EstadisticaItem("Semana 32 (08-14 ago)", 7),
            new EstadisticaItem("Semana 33 (15-21 ago)", 5),
            new EstadisticaItem("Semana 34 (22-28 ago)", 9),
            new EstadisticaItem("Semana 35 (29-04 sep)", 3)
    );

    @FXML
    public void initialize() {
        setupDatePickers();
        setupTables();
        setupCharts();
        setupButtons();
        hideErrorMessages();

        loadResourceData();
        loadActivityData();
        Platform.runLater(() -> {
            barChartRecursos.requestLayout();
            barChartActividades.requestLayout();
        });
    }

    private void setupDatePickers() {
        LocalDate today = LocalDate.now();
        dpRecursosDesde.setValue(today.minusDays(30));
        dpRecursosHasta.setValue(today);
        dpActividadesDesde.setValue(today.minusDays(30));
        dpActividadesHasta.setValue(today);
    }

    @SuppressWarnings("unchecked")
    private void setupTables() {
        TableColumn<EstadisticaItem, String> colResourceCategory = new TableColumn<>("Categoría");
        columnFactoryCell(colResourceCategory, tableViewRecursos);

        TableColumn<EstadisticaItem, String> colActivityWeek = new TableColumn<>("Semana");
        columnFactoryCell(colActivityWeek, tableViewActividades);
    }

    private void columnFactoryCell(TableColumn<EstadisticaItem, String> colResourceCategory, TableView<EstadisticaItem> tableViewRecursos) {
        colResourceCategory.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<EstadisticaItem, Integer> colResourceQuantity = new TableColumn<>("Cantidad");
        colResourceQuantity.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        tableViewRecursos.getColumns().clear();
        tableViewRecursos.getColumns().addAll(colResourceCategory, colResourceQuantity);
    }

    private void setupCharts() {
        setupResourceChart();
        setupActivityChart();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setupResourceChart() {
        barChartRecursos.getData().clear();

        CategoryAxis xAxis = (CategoryAxis) barChartRecursos.getXAxis();
        xAxis.setLabel("Categoría");

        NumberAxis yAxis = (NumberAxis) barChartRecursos.getYAxis();
        yAxis.setLabel("Cantidad de reservas");

        chartSeriesMaker(resourceData);
        barChartRecursos.setAnimated(true);
        barChartRecursos.setLegendVisible(false);
    }

    private void chartSeriesMaker(ObservableList<EstadisticaItem> resourceData) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Recursos Reservados");

        for (EstadisticaItem item : resourceData) {
            series.getData().add(new XYChart.Data<>(item.getNombre(), item.getCantidad()));
        }

        barChartRecursos.getData().add(series);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setupActivityChart() {
        barChartActividades.getData().clear();

        CategoryAxis xAxis = (CategoryAxis) barChartActividades.getXAxis();
        xAxis.setLabel("Semana");

        NumberAxis yAxis = (NumberAxis) barChartActividades.getYAxis();
        yAxis.setLabel("Cantidad de actividades");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Actividades Programadas");

        for (EstadisticaItem item : activityData) {
            series.getData().add(new XYChart.Data<>(item.getNombre(), item.getCantidad()));
        }

        barChartActividades.getData().add(series);
        barChartActividades.setAnimated(true);
        barChartActividades.setLegendVisible(false);
    }

    private void setupButtons() {
        btnGenerarRecursos.setOnAction(event -> loadResourceData());
        btnGenerarActividades.setOnAction(event -> loadActivityData());
        btnImprimirRecursos.setOnAction(event -> printResourceReport());
        btnImprimirActividades.setOnAction(event -> printActivityReport());
    }

    private void hideErrorMessages() {
        lblErrorRecursos.setVisible(false);
        lblErrorRecursos.setText("");
        lblErrorActividades.setVisible(false);
        lblErrorActividades.setText("");
    }

    // DATA LOADING

    private void loadResourceData() {
        try {
            LocalDate fromDate = dpRecursosDesde.getValue();
            LocalDate toDate = dpRecursosHasta.getValue();

            if (fromDate == null || toDate == null) {
                showResourceError("Seleccione ambas fechas.");
                return;
            }

            if (fromDate.isAfter(toDate)) {
                showResourceError("La fecha 'Desde' debe ser anterior a 'Hasta'.");
                return;
            }

            hideErrorMessages();

            tableViewRecursos.setItems(resourceData);
            updateResourceChart(resourceData);

        } catch (Exception e) {
            showResourceError("Error al cargar datos: " + e.getMessage());
        }
    }

    private void loadActivityData() {
        try {
            LocalDate fromDate = dpActividadesDesde.getValue();
            LocalDate toDate = dpActividadesHasta.getValue();

            if (fromDate == null || toDate == null) {
                showActivityError("Seleccione ambas fechas.");
                return;
            }

            if (fromDate.isAfter(toDate)) {
                showActivityError("La fecha 'Desde' debe ser anterior a 'Hasta'.");
                return;
            }

            hideErrorMessages();

            tableViewActividades.setItems(activityData);
            updateActivityChart(activityData);

        } catch (Exception e) {
            showActivityError("Error al cargar datos: " + e.getMessage());
        }
    }

    // CHART UPDATES

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateResourceChart(ObservableList<EstadisticaItem> data) {
        barChartRecursos.getData().clear();

        chartSeriesMaker(data);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateActivityChart(ObservableList<EstadisticaItem> data) {
        barChartActividades.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Actividades Programadas");

        for (EstadisticaItem item : data) {
            series.getData().add(new XYChart.Data<>(item.getNombre(), item.getCantidad()));
        }

        barChartActividades.getData().add(series);
    }

    // ERROR HANDLING

    private void showResourceError(String message) {
        lblErrorRecursos.setText(message);
        lblErrorRecursos.setVisible(true);
    }

    private void showActivityError(String message) {
        lblErrorActividades.setText(message);
        lblErrorActividades.setVisible(true);
    }

    // PDF REPORTS

    private void printResourceReport() {
        System.out.println("Imprimiendo reporte de recursos...");
        System.out.println(barChartActividades.getHeight());
        System.out.println(barChartActividades.getWidth());
        System.out.println(barChartRecursos.getHeight());
        System.out.println(barChartRecursos.getWidth());
    }

    private void printActivityReport() {
        System.out.println("Imprimiendo reporte de actividades...");
    }
}