package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import una.proyecto.model.Categoria;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;
import una.proyecto.utils.AppFactory;

import java.time.LocalDate;

public class CalendarizacionController {



    @FXML private Button btncargar;//
    @FXML private Button btnimprimir;//
    @FXML private ChoiceBox <Categoria> choiceboxcategoria;
    @FXML private DatePicker datepicker;

    private final ObservableList<Reserva> listaReservaUsuario = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategoria = FXCollections.observableArrayList();

    private final ReservaService reservaService = AppFactory.createReservaService();
    private final RecursoService recursoService = AppFactory.createRecursoDatos();
    private final CategoriaService categoriaService = AppFactory.createCategoriaService();

    @FXML private void initialize(){

        configurarFechaActual();
        configureChoiceBox();
    }
    private void configureChoiceBox() {
        listaCategoria.setAll(categoriaService.obtenerTodas());
        choiceboxcategoria.setItems(listaCategoria);

        if (!listaCategoria.isEmpty()) {
            choiceboxcategoria.getSelectionModel().selectFirst();
        }
    }
    private void configurarFechaActual() {datepicker.setValue(LocalDate.now());
    }
    private void configurarEventos(){

            btncargar.setOnAction(event -> handleButtonCargar());
            btnimprimir.setOnAction(event -> handleButtonImprimir());


    }
    private void handleButtonImprimir(){

    }
    private void handleButtonCargar(){

    }

}
