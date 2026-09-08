package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import una.proyecto.model.Categoria;
import una.proyecto.service.CategoriaService;
import una.proyecto.utils.AppFactory;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.ReportePDF;
import una.proyecto.utils.TablePDF;

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

    private final CategoriaService categoryService = AppFactory.createCategoriaService();

    private final ObservableList<Categoria> listaObservable = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        loadCategories();
        configureSelectionListener();
        configureSearchListener();
        clearForm();
    }

    private void configureTable() {
        colID.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion")
        );

        tablaCategorias.setItems(listaObservable);
    }

    private void loadCategories() {
        listaObservable.setAll(categoryService.obtenerTodas());
    }

    private void configureSelectionListener() {
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                txtID.setText(String.valueOf(newValue.getId()));
                txtID.setEditable(false);
                txtDescripcion.setText(newValue.getDescripcion());
            }
        });
    }

    private void configureSearchListener() {
        txtBuscarDescripcion.textProperty().addListener((observable, oldValue, newValue) -> searchCategories(newValue));
    }

    private void searchCategories(String searchText) {
        listaObservable.setAll(categoryService.buscarPorDescripcion(searchText));
    }

    @FXML
    private void saveCategory() {
        String description = txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim();

        if (description.isEmpty()) {
            showAlert("Error", "La descripción no puede estar vacía.");
            return;
        }

        try {
            boolean esNueva = txtID.getText() == null || txtID.getText().isBlank();

            if (esNueva) {
                // Crear: el id se autogenera dentro de CategoriaLogic.crear(...)
                Categoria nueva = new Categoria(null, description);
                categoryService.save(nueva);
            } else {
                // Editar una categoría ya seleccionada de la tabla
                Categoria actualizada = new Categoria(txtID.getText(), description);
                categoryService.update(actualizada);
            }

            loadCategories();
            clearForm();

        } catch (IllegalArgumentException e) {
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

        categoryService.delete(selectedCategory.getId());

        loadCategories();
        clearForm();
    }

    @FXML
    private void clearForm() {
        txtID.clear();
        txtID.setEditable(false);

        txtDescripcion.clear();
        txtBuscarDescripcion.clear();

        tablaCategorias.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }

    @FXML public void btnImprimir(){
        try {
            TablePDF nuevo = GeneradorPDFS.desdeTableView(tablaCategorias);
            ReportePDF reporte = new ReportePDF("Listado de Categorias", nuevo);
            GeneradorPDFS.generar(reporte, "Categorias.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        }catch (Exception e){
            showAlert("Error"," Error al generar PDF");
            e.printStackTrace();
        }
    }
}