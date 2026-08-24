package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.Categorias;

public class CategoriasAdministradorController {

    @FXML
    private TextField txtBuscarDescripcion;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private Button btnBuscar;

    @FXML
    private Button btnImprimir;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnBorrar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Categorias> tablaCategorias;

    @FXML
    private TableColumn<Categorias, Integer> colID;

    @FXML
    private TableColumn<Categorias, String> colDescripcion;

    @FXML
    public void initialize() {
        colID.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );
    }
    @FXML
    private void guardarCategoria() {

        int id = Integer.parseInt(txtID.getText());
        String descripcion = txtDescripcion.getText();

        Categorias categoria = new Categorias(id, descripcion);

        tablaCategorias.getItems().add(categoria);

        txtID.clear();
        txtDescripcion.clear();
    }
    @FXML
    private void borrarCategoria() {

        Categorias categoriaSeleccionada =
                tablaCategorias.getSelectionModel().getSelectedItem();

        if (categoriaSeleccionada != null) {
            tablaCategorias.getItems().remove(categoriaSeleccionada);
        }
    }
    @FXML
    private void limpiarCampos() {
        txtID.clear();
        txtDescripcion.clear();
    }
}