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
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.TablePDF;
import una.proyecto.utils.AppFactory;

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

    private final FuncionarioService funcionarioService = AppFactory.createFuncionarioService();

    private final ObservableList<Funcionario> listaObservable =
            FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        configureTable();
        configureSelectionListener();
        configureSearchListeners();
        loadFuncionarios();
    }

    /**
     * Configura la tabla de funcionarios.
     */
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

    /**
     * Carga a todos los funcionarios desde el archivo usuarios.xml.
     */
    private void loadFuncionarios() {

        listaObservable.setAll(funcionarioService.getAllEmployees());
    }

    /**
     * Configura el table selection listener.
     */
    private void configureSelectionListener() {

        tblFuncionarios.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {

                    if (newValue == null) {
                        return;
                    }

                    txtID.setText(newValue.getId());
                    txtNombre.setText(newValue.getName());
                    txtTelefono.setText(newValue.getPhone());

                    txtID.setEditable(false);
                });
    }

    /**
     * Configura los campos de búsqueda.
     */
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

    /**
     * Busca funcionarios por ID o nombre.
     */
    private void searchFuncionarios() {

        String id = txtBuscarID.getText().trim();
        String name = txtBuscarNombre.getText().trim();

        if (!id.isEmpty()) {

            listaObservable.setAll(funcionarioService.findById(id));

        } else if (!name.isEmpty()) {

            listaObservable.setAll(funcionarioService.findByName(name));

        } else {

            loadFuncionarios();
        }
    }

    /**
     * Crea un nuevo funcionario.
     * <p>
     * La contraseña inicial del funcionario la asigna automáticamente
     * FuncionarioService utilizando el ID del empleado.
     */
    @FXML
    private void guardarFuncionario() {

        String id = txtID.getText().trim();
        String name = txtNombre.getText().trim();
        String phone = txtTelefono.getText().trim();

        Funcionario funcionario = new Funcionario(id, "FUNCIONARIO", name, phone);

        try {
            funcionarioService.addEmployee(funcionario);
            loadFuncionarios();
            clearForm();
            showAlert("Funcionario", "Funcionario guardado correctamente.");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    /**
     * Actualiza al funcionario seleccionado.
     */
    @FXML
    private void editarFuncionario() {

        Funcionario selected = tblFuncionarios.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Aviso", "Debe seleccionar un funcionario de la tabla.");
            return;
        }

        selected.setName(txtNombre.getText().trim());
        selected.setPhone(txtTelefono.getText().trim());

        try {
            funcionarioService.updateEmployee(selected);
            loadFuncionarios();
            clearForm();
            showAlert("Funcionario", "Funcionario actualizado correctamente.");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    /**
     * Elimina al funcionario seleccionado.
     */
    @FXML
    private void eliminarFuncionario() {

        Funcionario selected = tblFuncionarios.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Aviso", "Debe seleccionar un funcionario de la tabla.");
            return;
        }

        try {
            funcionarioService.deleteEmployee(selected.getId());
            loadFuncionarios();
            clearForm();
            showAlert("Funcionario", "Funcionario eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage());
        }
    }

    /**
     * Borra el formulario y los campos de búsqueda.
     */
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

    /**
     * Muestra un cuadro de diálogo informativo.
     */
    private void showAlert(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
    @FXML public void btnImprimir(){
        try {
            TablePDF nuevo = GeneradorPDFS.desdeTableView(tblFuncionarios);
            GeneradorPDFS.generarPDF(nuevo, "Funcionarios.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        }catch (Exception e){
            showAlert("Error"," Error al generar PDF");
            e.printStackTrace();
        }
    }
}

