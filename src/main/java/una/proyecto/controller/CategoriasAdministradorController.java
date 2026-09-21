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
import java.util.List;

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

    // Método para calcular el siguiente ID según las categorías registradas
    // Lo hice asi para que no se mostrara el txt fiel del id ahi vacio todo feo entonces
    // muestra el siguiente id y de una lo pone asi
    private String obtenerSiguienteIdCategoria() {
        List<Categoria> lista = categoryService.obtenerTodas();
        if (lista == null || lista.isEmpty()) {
            return "CAT-000001";
        }

        int maxId = lista.stream()
                .map(Categoria::getId)
                .filter(id -> id != null && id.startsWith("CAT-"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.replace("CAT-", ""));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);

        return String.format("CAT-%06d", maxId + 1);
    }
    @FXML
    public void initialize() {
        txtID.setEditable(false);
        loadCategories();
        configureTable();
        configureSelectionListener();
        configureSearchListener();
        txtID.setText(obtenerSiguienteIdCategoria());
    }

    @FXML
    private void buscarCategoria() {
        searchCategories(txtBuscarDescripcion.getText().trim());
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
    public void saveCategory() {
        String descripcion = txtDescripcion.getText().trim();
        String id = txtID.getText().trim();

        // 1. Validar que la descripción no esté vacía
        if (descripcion.isEmpty()) {
            showAlert("Error", "Debe ingresar una descripción para la categoría.");
            return;
        }

        try {
            // 2. Crear y guardar la categoría con el ID autogenerado que se muestra en pantalla
            Categoria nuevaCategoria = new Categoria(id, descripcion);
            categoryService.save(nuevaCategoria);

            // 3. Recargar la tabla con la lista actualizada
            loadCategories();

            // 4. Limpiar el formulario (esto colocará automáticamente el próximo ID correlativo, ej. CAT-000005)
            clearForm();

            showAlert("Éxito", "Categoría guardada correctamente.");
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

        categoryService.delete(selectedCategory.getId());

        loadCategories();
        clearForm();
    }
    @FXML
    private void clearForm() {
        txtDescripcion.clear();
        txtBuscarDescripcion.clear();
        tablaCategorias.getSelectionModel().clearSelection();

        // Al limpiar, vuelve a mostrar el nuevo ID correlativo disponible
        txtID.setText(obtenerSiguienteIdCategoria());
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