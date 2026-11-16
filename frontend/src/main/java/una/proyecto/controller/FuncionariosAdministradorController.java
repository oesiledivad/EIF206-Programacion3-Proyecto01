package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.Funcionario;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

import java.util.Optional;

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

    // El controller ya no conoce el backend: todo pasa por el proxy (sockets).
    private final ServiceProxy proxy = ServiceProxy.instance();

    private final ObservableList<Funcionario> listaObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        configureSelectionListener();
        configureSearchListeners();
        loadFuncionarios();
    }

    private void configureTable() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tblFuncionarios.setItems(listaObservable);
    }

    private void loadFuncionarios() {
        try {
            listaObservable.setAll(proxy.funcionariosObtenerTodos());
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    // Al seleccionar una fila, el formulario pasa a modo "editar": la cédula no se puede cambiar.
    private void configureSelectionListener() {
        tblFuncionarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            txtID.setText(newValue.getId());
            txtNombre.setText(newValue.getName());
            txtTelefono.setText(newValue.getPhone());
            txtID.setEditable(false);
        });
    }

    private void configureSearchListeners() {
        txtBuscarID.textProperty().addListener((observable, oldValue, newValue) -> searchFuncionarios());
        txtBuscarNombre.textProperty().addListener((observable, oldValue, newValue) -> searchFuncionarios());
    }

    private void searchFuncionarios() {
        String id = txtBuscarID.getText().trim();
        String name = txtBuscarNombre.getText().trim();
        try {
            if (!id.isEmpty()) {
                listaObservable.setAll(proxy.funcionariosBuscarPorId(id));
            } else if (!name.isEmpty()) {
                listaObservable.setAll(proxy.funcionariosBuscarPorNombre(name));
            } else {
                listaObservable.setAll(proxy.funcionariosObtenerTodos());
            }
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    /**
     * Sin fila seleccionada -> crea (la clave inicial = cédula la pone el servidor).
     * Con fila seleccionada -> actualiza nombre y teléfono (la clave se conserva).
     */
    @FXML
    private void guardarFuncionario() {
        String id = txtID.getText().trim();
        String name = txtNombre.getText().trim();
        String phone = txtTelefono.getText().trim();
        boolean editando = tblFuncionarios.getSelectionModel().getSelectedItem() != null;

        Funcionario funcionario = new Funcionario(id, "FUNCIONARIO", name, phone);

        try {
            String mensaje;
            if (editando) {
                proxy.funcionarioActualizar(funcionario);
                mensaje = "Funcionario " + id + " actualizado correctamente.";
            } else {
                proxy.funcionarioCrear(funcionario);
                mensaje = "Funcionario " + id + " creado. Su clave inicial es su cédula.";
            }
            clearForm();
            showAlert("Funcionario", mensaje);
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void eliminarFuncionario() {
        Funcionario selected = tblFuncionarios.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Aviso", "Debe seleccionar un funcionario de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar funcionario");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea eliminar a " + selected.getName() + " (" + selected.getId() + ")?");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            proxy.funcionarioEliminar(selected.getId());
            clearForm();
            showAlert("Funcionario", "Funcionario eliminado correctamente.");
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
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

    @FXML
    public void btnImprimir() {
        try {
            TablePDF tabla = GeneradorPDFS.desdeTableView(tblFuncionarios);
            ReportePDF reporte = new ReportePDF("Reporte de Funcionarios", tabla);
            GeneradorPDFS.generar(reporte, "funcionarios.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        } catch (Exception e) {
            showAlert("Error", " Error al generar PDF");
            e.printStackTrace();
        }
    }
}