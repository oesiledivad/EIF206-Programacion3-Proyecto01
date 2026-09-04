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
        try {
            int id = Integer.parseInt(txtID.getText());
            String description = txtDescripcion.getText().trim();

            if (description.isEmpty()) {
                showAlert("Error","La descripción no puede estar vacía.");
                return;
            }

            Categoria category = new Categoria( String.valueOf(id), description);

            categoryService.save(category);

            loadCategories();
            clearForm();

        } catch (NumberFormatException e) {
            showAlert("Error", "El ID debe ser un número entero válido.");
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
        txtID.setEditable(true);

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
            GeneradorPDFS.generarPDF(nuevo, "Categorias.pdf");
            showAlert("Éxito", "PDF generado correctamente.");
        }catch (Exception e){
            showAlert("Error"," Error al generar PDF");
            e.printStackTrace();
        }
    }
}
