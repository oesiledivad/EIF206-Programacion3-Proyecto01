package una.proyecto.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.Funcionarios;

public class FuncionariosAdministradorController {

    @FXML private TextField txtBuscarID;
    @FXML private TextField txtBuscarNombre;

    @FXML private TextField txtID;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;

    @FXML private TableView<Funcionarios> tblFuncionarios;
    @FXML private TableColumn<Funcionarios, String> colID;
    @FXML private TableColumn<Funcionarios, String> colNombre;
    @FXML private TableColumn<Funcionarios, String> colTelefono;

    @FXML
    private void initialize() {

        colID.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getId())
                )
        );

        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getNombre()
                )
        );

        colTelefono.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getTelefono()
                )
        );
    }

    @FXML
    private void guardarFuncionario() {

        int id = Integer.parseInt(txtID.getText());
        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();

        Funcionarios funcionario = new Funcionarios(id, nombre, telefono);

        tblFuncionarios.getItems().add(funcionario);

        txtID.clear();
        txtNombre.clear();
        txtTelefono.clear();
    }

    @FXML
    private void buscarFuncionario() {

        String buscarID = txtBuscarID.getText().trim();
        String buscarNombre = txtBuscarNombre.getText().trim();

        for (Funcionarios funcionario : tblFuncionarios.getItems()) {

            boolean coincideID = !buscarID.isEmpty()
                    && String.valueOf(funcionario.getId()).equals(buscarID);

            boolean coincideNombre = !buscarNombre.isEmpty()
                    && funcionario.getNombre().equalsIgnoreCase(buscarNombre);

            if (coincideID || coincideNombre) {

                txtID.setText(String.valueOf(funcionario.getId()));
                txtNombre.setText(funcionario.getNombre());
                txtTelefono.setText(funcionario.getTelefono());

                tblFuncionarios.getSelectionModel().select(funcionario);

                return;
            }
        }

        mostrarAlerta("Búsqueda", "No se encontró el funcionario.");
    }

    @FXML
    private void editarFuncionario() {

        Funcionarios funcionario = tblFuncionarios
                .getSelectionModel()
                .getSelectedItem();

        if (funcionario == null) {
            mostrarAlerta("Editar", "Seleccione un funcionario de la tabla.");
            return;
        }

        int id = Integer.parseInt(txtID.getText());
        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();

        funcionario.setId(id);
        funcionario.setNombre(nombre);
        funcionario.setTelefono(telefono);

        tblFuncionarios.refresh();

        mostrarAlerta("Editar", "Funcionario actualizado correctamente.");
    }

    @FXML
    private void eliminarFuncionario() {

        Funcionarios funcionario = tblFuncionarios
                .getSelectionModel()
                .getSelectedItem();

        if (funcionario == null) {
            mostrarAlerta("Eliminar", "Seleccione un funcionario de la tabla.");
            return;
        }

        tblFuncionarios.getItems().remove(funcionario);

        txtID.clear();
        txtNombre.clear();
        txtTelefono.clear();

        mostrarAlerta("Eliminar", "Funcionario eliminado correctamente.");
    }

    private void mostrarAlerta(String titulo, String mensaje) {

        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}