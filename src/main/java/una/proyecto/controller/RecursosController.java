package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;
import una.proyecto.utils.AppFactory;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

import java.util.List;

public class RecursosController {
    @FXML
    public Label lblErrorFiltro;
    @FXML
    private Button btnBorrarRecurso;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnGuardarRecurso;

    @FXML
    private Button btnImprimir;

    @FXML
    private Button btnLimpiarRecurso;

    @FXML
    private ComboBox<Categoria> comboBoxCategoria;

    @FXML
    private ComboBox<Categoria> comboBoxCategoriaFiltro;

    @FXML
    private TableView<Recurso> tableViewRecursos;

    @FXML
    private TextField textFieldID;

    @FXML
    private TextField txtFieldDescripcion;

    @FXML
    private TextField txtFieldDescripcionFiltro;

    @FXML private TableColumn<Recurso, String> tableColumId;
    @FXML private TableColumn<Recurso, String> tableColumCategoria;
    @FXML private TableColumn<Recurso, String> tableColumDescripcion;



    private final RecursoService recursoService  = AppFactory.createRecursoDatos();
    private final ObservableList<Recurso> listaObservable = FXCollections.observableArrayList();
    private final CategoriaService categoriaService = AppFactory.createCategoriaService();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();


    @FXML
    public void initialize(){
        lblErrorFiltro.setVisible(false);
        cargarTodoRecursos();
        cargarCategorias();
        configureTable();
        configureComboBoxFormulario();
        configureComboBoxFiltro();
    }

    private void configureTable() {
        // Apunta exactamente a getId() en Recurso
        tableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Apunta exactamente a getDescripcion() en Recurso
        tableColumDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Renderiza la descripción de la Categoría buscando por su idCategoria
        tableColumCategoria.setCellValueFactory(cellData -> {
            String catId = cellData.getValue().getIdCategoria();
            Categoria cat = listaCategorias.stream()
                    .filter(c -> c.getId().equals(catId))
                    .findFirst()
                    .orElse(null);
            return new javafx.beans.property.SimpleStringProperty(
                    cat != null ? cat.getDescripcion() : catId
            );
        });

        // IMPORTANTE: Asignar la lista observable a la tabla
        tableViewRecursos.setItems(listaObservable);
    }
    private void cargarCategorias(){
        listaCategorias.setAll(categoriaService.obtenerTodas());
    }
    private void cargarTodoRecursos(){
        listaObservable.setAll(recursoService.obtenerTodosRecursos());
    }
    private void configureComboBoxFiltro(){
        comboBoxCategoriaFiltro.setItems(listaCategorias);
        comboBoxCategoriaFiltro.setConverter(crearConverter());
    }

    private StringConverter<Categoria> crearConverter(){
        return new StringConverter<Categoria>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria == null ? "" : categoria.getDescripcion();
            }
            @Override
            public Categoria fromString(String string) {
                return null;
            }
        };
    }
    private void configureComboBoxFormulario(){
        comboBoxCategoria.setItems(listaCategorias);
        comboBoxCategoria.setConverter(crearConverter());
    }

    @FXML public void btnImprimir(){
        try {
            TablePDF nuevo = GeneradorPDFS.desdeTableView(tableViewRecursos);
            ReportePDF reporte = new ReportePDF("Listado de Recursos", nuevo);
            GeneradorPDFS.generar(reporte, "Recursos.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        }catch (Exception e){
            showAlert("Error"," Error al generar PDF");
        }
    }
    @FXML public void  btnBuscarRecurso(){
        String descripcion = txtFieldDescripcionFiltro.getText();
        Categoria nueva = comboBoxCategoriaFiltro.getValue();
        List<Recurso> listaRecursosEspecificos = recursoService.buscarPorFiltros(nueva, descripcion);
        listaObservable.setAll(listaRecursosEspecificos);
    }
    @FXML public void  btnLimpiarCasilla(){
        clearForm();
    }
    @FXML public void btnBorrarRecurso(){
        Recurso seleccionado = tableViewRecursos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            showAlert("Error", "Selecciona un recurso de la tabla para borrar.");
            return;
        }

        recursoService.delete(seleccionado.getId());
        listaObservable.setAll(recursoService.obtenerTodosRecursos());
        clearForm();
    }
    @FXML public void btnGuardarRecurso() {
        String idActivo = textFieldID.getText().trim();
        String descripcion = txtFieldDescripcion.getText().trim();
        Categoria tipoCategoria = comboBoxCategoria.getValue();

        // Validar que TODOS los campos requeridos tengan datos
        if (idActivo.isEmpty() || descripcion.isEmpty() || tipoCategoria == null) {
            showAlert("Error", "Debe ingresar el ID (Número de Activo), la descripción y seleccionar una categoría.");
            return;
        }

        try {
            Recurso r1 = new Recurso(idActivo, tipoCategoria.getId(), descripcion);
            recursoService.save(r1);
            listaObservable.setAll(recursoService.obtenerTodosRecursos());
            clearForm();
            showAlert("Éxito", "Recurso registrado correctamente.");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
    @FXML
    private void clearForm() {
        textFieldID.clear();
        txtFieldDescripcion.clear();
        txtFieldDescripcionFiltro.clear();
        comboBoxCategoria.setValue(null);
        comboBoxCategoriaFiltro.setValue(null);
        tableViewRecursos.getSelectionModel().clearSelection();
    }


}