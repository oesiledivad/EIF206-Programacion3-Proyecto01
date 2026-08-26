package una.proyecto.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.Funcionario;

public class FuncionariosAdministradorController {

    @FXML private TextField txtBuscarID;
    @FXML private TextField txtBuscarNombre;

    @FXML private TextField txtID;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;

    @FXML private TableView<Funcionario> tblFuncionarios;
    @FXML private TableColumn<Funcionario, String> colID;
    @FXML private TableColumn<Funcionario, String> colNombre;
    @FXML private TableColumn<Funcionario, String> colTelefono;

    @FXML
    private void initialize() {

        colID.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        String.valueOf(cellData.getValue().getId())
                )
        );

        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getName()
                )
        );

        colTelefono.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getPhone()
                )
        );
    }

    @FXML
    private void guardarFuncionario() {

        String id = txtID.getText();
        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();
        String rol = "FUNCIONARIO";

        Funcionario funcionario = new Funcionario(id ,rol, nombre, telefono);

        tblFuncionarios.getItems().add(funcionario);

        txtID.clear();
        txtNombre.clear();
        txtTelefono.clear();
    }

    @FXML
    private void buscarFuncionario() {

        String buscarID = txtBuscarID.getText().trim();
        String buscarNombre = txtBuscarNombre.getText().trim();

        for (Funcionario funcionario : tblFuncionarios.getItems()) {

            boolean coincideID = !buscarID.isEmpty()
                    && String.valueOf(funcionario.getId()).equals(buscarID);

            boolean coincideNombre = !buscarNombre.isEmpty()
                    && funcionario.getName().equalsIgnoreCase(buscarNombre);

            if (coincideID || coincideNombre) {

                txtID.setText(String.valueOf(funcionario.getId()));
                txtNombre.setText(funcionario.getName());
                txtTelefono.setText(funcionario.getPhone());

                tblFuncionarios.getSelectionModel().select(funcionario);

                return;
            }
        }

        mostrarAlerta("Búsqueda", "No se encontró el funcionario.");
    }

    @FXML
    private void editarFuncionario() {

        Funcionario funcionario = tblFuncionarios
                .getSelectionModel()
                .getSelectedItem();

        if (funcionario == null) {
            mostrarAlerta("Editar", "Seleccione un funcionario de la tabla.");
            return;
        }

        // Para cuando se implemente la busqueda String id = txtID.getText();
        String nombre = txtNombre.getText();
        String telefono = txtTelefono.getText();

        funcionario.setName(nombre);
        funcionario.setPhone(telefono);

        tblFuncionarios.refresh();

        mostrarAlerta("Editar", "Funcionario actualizado correctamente.");
    }

    @FXML
    private void eliminarFuncionario() {

        Funcionario funcionario = tblFuncionarios
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