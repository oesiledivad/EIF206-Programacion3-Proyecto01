package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    @FXML
    public void initialize(){
        lblErrorFiltro.setVisible(false);
    }

}
