package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;

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
    private ComboBox<?> comboBoxCategoria;

    @FXML
    private ComboBox<?> comboBoxCategoriaFiltro;

    @FXML
    private TableView<?> tableViewRecursos;

    @FXML
    private TextField textFieldID;

    @FXML
    private TextField txtFieldDescripcion;

    @FXML
    private TextField txtFieldDescripcionFiltro;
    private final RecursoService recursoService  = new RecursoService();
    private final ObservableList<Recurso> listaObservable = FXCollections.observableArrayList();
    private final CategoriaService categoriaService = new CategoriaService(); // ajusta el nombre si tu clase se llama distinto
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    @FXML
    public void initialize(){
        lblErrorFiltro.setVisible(false);
    }
    @FXML public void  btnBuscarRecurso(){}
    @FXML public void  btnLimpiarCasilla(){}
    @FXML public void  btnBorrarRecurso(){}
    @FXML public void  btnGuardarRecurso(){}
}
