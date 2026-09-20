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
import javafx.scene.layout.VBox;
import una.proyecto.model.EstadisticaItem;
import una.proyecto.service.EstadisticasService;
import una.proyecto.utils.AppFactory;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

import java.time.LocalDate;
import java.util.List;

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
    private Button btnTabRecursos;
    @FXML
    private Button btnTabActividades;

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
    @FXML
    private VBox vboxEstadisticasRecursos;
    @FXML
    private VBox vboxEstadisticasActividades;

    private final EstadisticasService estadisticasService = AppFactory.createEstadisticasService();

    private final ObservableList<EstadisticaItem> resourceData = FXCollections.observableArrayList();
    private final ObservableList<EstadisticaItem> activityData = FXCollections.observableArrayList();

    private final XYChart.Series<String, Number> resourceSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> activitySeries = new XYChart.Series<>();

    @FXML
    public void initialize() {
        try {
            setupDatePickers();
            setupTables();
            setupCharts();
            setupButtons();
            hideErrorMessages();

            actualizarEstadoPestanas(true);

            Platform.runLater(() -> {
                loadResourceData();
                loadActivityData();
            });
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error de inicialización", e.getMessage());
        }
    }


    @FXML
    private void handleTabRecursos() {
        vboxEstadisticasRecursos.setVisible(true);
        vboxEstadisticasRecursos.setManaged(true);

        vboxEstadisticasActividades.setVisible(false);
        vboxEstadisticasActividades.setManaged(false);

        actualizarEstadoPestanas(true);
    }

    @FXML
    private void handleTabActividades() {
        vboxEstadisticasRecursos.setVisible(false);
        vboxEstadisticasRecursos.setManaged(false);

        vboxEstadisticasActividades.setVisible(true);
        vboxEstadisticasActividades.setManaged(true);

        actualizarEstadoPestanas(false);
    }

    private void actualizarEstadoPestanas(boolean recursosActivo) {
        if (btnTabRecursos != null && btnTabActividades != null) {
            if (recursosActivo) {
                btnTabRecursos.getStyleClass().remove("tab-inactive");
                if (!btnTabRecursos.getStyleClass().contains("tab-active")) {
                    btnTabRecursos.getStyleClass().add("tab-active");
                }

                btnTabActividades.getStyleClass().remove("tab-active");
                if (!btnTabActividades.getStyleClass().contains("tab-inactive")) {
                    btnTabActividades.getStyleClass().add("tab-inactive");
                }
            } else {
                btnTabActividades.getStyleClass().remove("tab-inactive");
                if (!btnTabActividades.getStyleClass().contains("tab-active")) {
                    btnTabActividades.getStyleClass().add("tab-active");
                }

                btnTabRecursos.getStyleClass().remove("tab-active");
                if (!btnTabRecursos.getStyleClass().contains("tab-inactive")) {
                    btnTabRecursos.getStyleClass().add("tab-inactive");
                }
            }
        }
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
        try {
            // Configuración Tabla Recursos
            TableColumn<EstadisticaItem, String> colResNombre = new TableColumn<>("Categoría");
            colResNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

            TableColumn<EstadisticaItem, Integer> colResCantidad = new TableColumn<>("Cantidad");
            colResCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

            tableViewRecursos.getColumns().setAll(colResNombre, colResCantidad);
            tableViewRecursos.setItems(resourceData);

            // Configuración Tabla Actividades
            TableColumn<EstadisticaItem, String> colActNombre = new TableColumn<>("Semana");
            colActNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

            TableColumn<EstadisticaItem, Integer> colActCantidad = new TableColumn<>("Cantidad");
            colActCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

            tableViewActividades.getColumns().setAll(colActNombre, colActCantidad);
            tableViewActividades.setItems(activityData);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al configurar tablas", e.getMessage());
        }
    }

    private void setupCharts() {
        try {
            // Gráfico Recursos
            CategoryAxis xAxisRes = (CategoryAxis) barChartRecursos.getXAxis();
            xAxisRes.setLabel("Categoría");
            NumberAxis yAxisRes = (NumberAxis) barChartRecursos.getYAxis();
            yAxisRes.setLabel("Cantidad de reservas");

            resourceSeries.setName("Recursos Reservados");
            barChartRecursos.getData().add(resourceSeries);
            barChartRecursos.setLegendVisible(false);

            // Gráfico Actividades
            CategoryAxis xAxisAct = (CategoryAxis) barChartActividades.getXAxis();
            xAxisAct.setLabel("Semana");
            NumberAxis yAxisAct = (NumberAxis) barChartActividades.getYAxis();
            yAxisAct.setLabel("Cantidad de actividades");

            activitySeries.setName("Actividades Programadas");
            barChartActividades.getData().add(activitySeries);
            barChartActividades.setLegendVisible(false);
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al configurar gráficos", e.getMessage());
        }
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

    // ACCIONES Y LÓGICA DE CARGA

    private void loadResourceData() {
        try {
            LocalDate fromDate = dpRecursosDesde.getValue();
            LocalDate toDate = dpRecursosHasta.getValue();

            if (!validateDates(fromDate, toDate, true)) {
                return;
            }

            hideErrorMessages();

            resourceData.clear();

            List<EstadisticaItem> items = estadisticasService.obtenerEstadisticasRecursos(fromDate, toDate);

            if (items != null && !items.isEmpty()) {
                resourceData.setAll(items);
                updateChartSeries(resourceSeries, items);
            } else {
                showResourceError("No se encontraron datos para el período seleccionado.");
                resourceSeries.getData().clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showResourceError("Error al cargar recursos: " + e.getMessage());
            resourceSeries.getData().clear();
        }
    }

    private void loadActivityData() {
        try {
            LocalDate fromDate = dpActividadesDesde.getValue();
            LocalDate toDate = dpActividadesHasta.getValue();

            if (!validateDates(fromDate, toDate, false)) {
                return;
            }

            hideErrorMessages();

            activityData.clear();

            List<EstadisticaItem> items = estadisticasService.obtenerEstadisticasActividades(fromDate, toDate);

            if (items != null && !items.isEmpty()) {
                activityData.setAll(items);
                updateChartSeries(activitySeries, items);
            } else {
                showActivityError("No se encontraron datos para el período seleccionado.");
                activitySeries.getData().clear();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showActivityError("Error al cargar actividades: " + e.getMessage());
            activitySeries.getData().clear();
        }
    }

    private boolean validateDates(LocalDate fromDate, LocalDate toDate, boolean isResource) {
        if (fromDate == null || toDate == null) {
            String msg = "Seleccione ambas fechas.";
            if (isResource) showResourceError(msg); else showActivityError(msg);
            return false;
        }

        if (fromDate.isAfter(toDate)) {
            String msg = "La fecha 'Desde' debe ser anterior o igual a 'Hasta'.";
            if (isResource) showResourceError(msg); else showActivityError(msg);
            return false;
        }

        return true;
    }

    private void updateChartSeries(XYChart.Series<String, Number> series, List<EstadisticaItem> items) {
        Platform.runLater(() -> {
            series.getData().clear();
            if (items != null) {
                for (EstadisticaItem item : items) {
                    if (item != null && item.getNombre() != null) {
                        series.getData().add(new XYChart.Data<>(item.getNombre(), item.getCantidad()));
                    }
                }
            }
        });
    }

    // MANEJO DE ERRORES

    private void showResourceError(String message) {
        Platform.runLater(() -> {
            lblErrorRecursos.setText(message);
            lblErrorRecursos.setVisible(true);
        });
    }

    private void showActivityError(String message) {
        Platform.runLater(() -> {
            lblErrorActividades.setText(message);
            lblErrorActividades.setVisible(true);
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();

            if (tableViewRecursos != null && tableViewRecursos.getScene() != null) {
                if (tableViewRecursos.getScene().getRoot() != null) {
                    tableViewRecursos.getScene().getRoot().requestLayout();
                }
            }
        });
    }

    // IMPRESIÓN / REPORTES PDF

    private void printResourceReport() {
        try {
            if (resourceData.isEmpty()) {
                showResourceError("No hay datos para imprimir. Genere primero las estadísticas.");
                return;
            }

            TablePDF tabla = GeneradorPDFS.desdeTableView(tableViewRecursos);
            ReportePDF reporte = new ReportePDF(
                    "Estadísticas de Recursos",
                    dpRecursosDesde.getValue(),
                    dpRecursosHasta.getValue(),
                    tabla,
                    barChartRecursos
            );
            GeneradorPDFS.generar(reporte, "estadisticas_recursos.pdf");
            mostrarAlertaInfo("PDF generado", "El reporte de recursos fue generado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showResourceError("No se pudo generar el PDF: " + e.getMessage());
        }
    }

    private void printActivityReport() {
        try {
            if (activityData.isEmpty()) {
                showActivityError("No hay datos para imprimir. Genere primero las estadísticas.");
                return;
            }

            TablePDF tabla = GeneradorPDFS.desdeTableView(tableViewActividades);
            ReportePDF reporte = new ReportePDF(
                    "Estadísticas de Actividades",
                    dpActividadesDesde.getValue(),
                    dpActividadesHasta.getValue(),
                    tabla,
                    barChartActividades
            );
            GeneradorPDFS.generar(reporte, "estadisticas_actividades.pdf");
            mostrarAlertaInfo("PDF generado", "El reporte de actividades fue generado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            showActivityError("No se pudo generar el PDF: " + e.getMessage());
        }
    }

    private void mostrarAlertaInfo(String titulo, String contenido) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(contenido);
            alert.showAndWait();

            if (tableViewRecursos != null && tableViewRecursos.getScene() != null) {
                if (tableViewRecursos.getScene().getRoot() != null) {
                    tableViewRecursos.getScene().getRoot().requestLayout();
                }
            }
        });
    }
}