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

    private void configureTable(){
        tableColumCategoria.setCellValueFactory(new PropertyValueFactory<>("idCategoria"));
        tableColumDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tableColumId.setCellValueFactory(new PropertyValueFactory<>("id"));
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
    @FXML public void  btnGuardarRecurso(){
        String descripcion = txtFieldDescripcion.getText();
        String id = textFieldID.getText();
        Categoria tipoCategoria = comboBoxCategoria.getValue();
        String idCategoria;
        if (tipoCategoria != null) {
            idCategoria = tipoCategoria.getId(); // corregido: antes usaba getDescripcion()
        } else {
            idCategoria = "";
        }

        if(descripcion.isEmpty() || id.isEmpty() || idCategoria.isEmpty()){
            showAlert("Error","La descripción no puede estar vacía.");
            return;
        }

        try{
            //String id, String idCategoria, String descripcion
            Recurso r1 = new Recurso(id, idCategoria, descripcion);
            recursoService.save(r1);
            listaObservable.setAll(recursoService.obtenerTodosRecursos());
            clearForm();
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