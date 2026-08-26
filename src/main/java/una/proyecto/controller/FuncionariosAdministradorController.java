package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.Funcionario;
import una.proyecto.service.FuncionarioService;

public class FuncionariosAdministradorController {

    @FXML
    private TextField txtBuscarID;

    @FXML
    private TextField txtBuscarNombre;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TableView<Funcionario> tblFuncionarios;

    @FXML
    private TableColumn<Funcionario, String> colID;

    @FXML
    private TableColumn<Funcionario, String> colNombre;

    @FXML
    private TableColumn<Funcionario, String> colTelefono;

    private final FuncionarioService funcionarioService = new FuncionarioService();

    private final ObservableList<Funcionario> listaObservable =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        loadFuncionarios();
        configureSelectionListener();
        configureSearchListeners();
    }

    private void configureTable() {

        colID.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colNombre.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        colTelefono.setCellValueFactory(
                new PropertyValueFactory<>("phone")
        );

        tblFuncionarios.setItems(listaObservable);
    }

    private void loadFuncionarios() {
        listaObservable.setAll(
                funcionarioService.obtenerTodos()
        );
    }

    private void configureSelectionListener() {

        tblFuncionarios.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue != null) {

                        txtID.setText(newValue.getId());
                        txtID.setEditable(false);

                        txtNombre.setText(newValue.getName());
                        txtTelefono.setText(newValue.getPhone());
                    }
                });
    }

    private void configureSearchListeners() {

        txtBuscarID.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        searchFuncionarios()
        );

        txtBuscarNombre.textProperty().addListener(
                (observable, oldValue, newValue) ->
                        searchFuncionarios()
        );
    }

    private void searchFuncionarios() {

        String id = txtBuscarID.getText().trim();
        String nombre = txtBuscarNombre.getText().trim();

        if (!id.isEmpty()) {

            listaObservable.setAll(
                    funcionarioService.buscarPorId(id)
            );

        } else if (!nombre.isEmpty()) {

            listaObservable.setAll(
                    funcionarioService.buscarPorNombre(nombre)
            );

        } else {

            loadFuncionarios();
        }
    }

    @FXML
    private void guardarFuncionario() {

        String id = txtID.getText().trim();
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (id.isEmpty()) {
            showAlert("Error", "El ID no puede estar vacío.");
            return;
        }

        if (nombre.isEmpty()) {
            showAlert("Error", "El nombre no puede estar vacío.");
            return;
        }

        if (telefono.isEmpty()) {
            showAlert("Error", "El teléfono no puede estar vacío.");
            return;
        }

        Funcionario funcionario =
                new Funcionario(id, "FUNCIONARIO", nombre, telefono);

        funcionarioService.save(funcionario);

        loadFuncionarios();
        clearForm();

        showAlert(
                "Funcionario",
                "Funcionario guardado correctamente."
        );
    }

    @FXML
    private void editarFuncionario() {

        Funcionario seleccionado =
                tblFuncionarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            showAlert(
                    "Aviso",
                    "Debe seleccionar un funcionario de la tabla."
            );
            return;
        }

        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty()) {
            showAlert("Error", "El nombre no puede estar vacío.");
            return;
        }

        if (telefono.isEmpty()) {
            showAlert("Error", "El teléfono no puede estar vacío.");
            return;
        }

        seleccionado.setName(nombre);
        seleccionado.setPhone(telefono);

        funcionarioService.save(seleccionado);

        loadFuncionarios();
        clearForm();

        showAlert(
                "Funcionario",
                "Funcionario actualizado correctamente."
        );
    }

    @FXML
    private void eliminarFuncionario() {

        Funcionario seleccionado =
                tblFuncionarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            showAlert(
                    "Aviso",
                    "Debe seleccionar un funcionario de la tabla."
            );
            return;
        }

        funcionarioService.delete(seleccionado.getId());

        loadFuncionarios();
        clearForm();

        showAlert(
                "Funcionario",
                "Funcionario eliminado correctamente."
        );
    }

    @FXML
    private void clearForm() {

        txtID.clear();
        txtID.setEditable(true);

        txtNombre.clear();
        txtTelefono.clear();

        txtBuscarID.clear();
        txtBuscarNombre.clear();

        tblFuncionarios.getSelectionModel().clearSelection();

        loadFuncionarios();
    }

    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}