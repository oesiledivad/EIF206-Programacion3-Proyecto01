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
import una.proyecto.model.Categoria;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

import java.util.Optional;

public class CategoriasAdministradorController {

    @FXML
    private TextField txtBuscarDescripcion;

    @FXML
    private TextField txtID;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TableView<Categoria> tablaCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colID;

    @FXML
    private TableColumn<Categoria, String> colDescripcion;

    // El controller ya no conoce el backend: todo pasa por el proxy (sockets).
    private final ServiceProxy proxy = ServiceProxy.instance();

    private final ObservableList<Categoria> listaObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        txtID.setEditable(false); // vacío = categoría nueva; MySQL asigna el id al guardar
        configureTable();
        configureSelectionListener();
        configureSearchListener();
        loadCategories();
    }

    @FXML
    private void buscarCategoria() {
        searchCategories(txtBuscarDescripcion.getText().trim());
    }

    private void configureTable() {
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tablaCategorias.setItems(listaObservable);
    }

    private void loadCategories() {
        try {
            listaObservable.setAll(proxy.categoriasObtenerTodas());
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    // Al seleccionar una fila, el formulario pasa a modo "editar".
    private void configureSelectionListener() {
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtID.setText(newValue.getId());
                txtDescripcion.setText(newValue.getDescripcion());
            }
        });
    }

    private void configureSearchListener() {
        txtBuscarDescripcion.textProperty().addListener((observable, oldValue, newValue) -> searchCategories(newValue));
    }

    private void searchCategories(String searchText) {
        try {
            listaObservable.setAll(proxy.categoriasBuscarPorDescripcion(searchText));
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    // Sin id -> crear. Con id (fila seleccionada) -> actualizar.
    @FXML
    public void saveCategory() {
        String descripcion = txtDescripcion.getText().trim();
        String id = txtID.getText().trim();

        if (descripcion.isEmpty()) {
            showAlert("Error", "Debe ingresar una descripción para la categoría.");
            return;
        }

        try {
            String mensaje;
            if (id.isEmpty()) {
                Categoria creada = proxy.categoriaCrear(new Categoria(null, descripcion));
                mensaje = "Categoría " + creada.getId() + " creada correctamente.";
            } else {
                proxy.categoriaActualizar(new Categoria(id, descripcion));
                mensaje = "Categoría " + id + " actualizada correctamente.";
            }
            loadCategories();
            clearForm();
            showAlert("Éxito", mensaje);
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void deleteCategory() {
        Categoria selectedCategory = tablaCategorias.getSelectionModel().getSelectedItem();

        if (selectedCategory == null) {
            showAlert("Aviso", "Debe seleccionar una categoría de la tabla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar categoría");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Desea eliminar la categoría \"" + selectedCategory.getDescripcion() + "\"?");
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            proxy.categoriaEliminar(selectedCategory.getId());
            loadCategories();
            clearForm();
            showAlert("Éxito", "Categoría eliminada correctamente.");
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        txtDescripcion.clear();
        txtBuscarDescripcion.clear();
        tablaCategorias.getSelectionModel().clearSelection();
        txtID.clear(); // vuelve a modo "crear"; el FXML muestra "Autogenerado"
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
            TablePDF nuevo = GeneradorPDFS.desdeTableView(tablaCategorias);
            ReportePDF reporte = new ReportePDF("Listado de Categorias", nuevo);
            GeneradorPDFS.generar(reporte, "Categorias.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        } catch (Exception e) {
            showAlert("Error", " Error al generar PDF");
            e.printStackTrace();
        }
    }
}