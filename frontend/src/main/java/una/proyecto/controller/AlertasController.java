package una.proyecto.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.javafx.FontIcon;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.presentation.logic.MainLogic;

import java.util.List;
import java.util.function.Consumer;

public class AlertasController {

    @FXML private TableView<Reserva> tablaAlertas;
    @FXML private TableColumn<Reserva, String> colActividad;
    @FXML private TableColumn<Reserva, Void> colVer;
    @FXML private TableColumn<Reserva, Void> colDescartar;

    private final ObservableList<Reserva> alertas = FXCollections.observableArrayList();
    private final MainLogic mainLogic = new MainLogic();

    @FXML
    public void initialize() {
        colActividad.setCellValueFactory(celda -> new ReadOnlyStringWrapper(textoDeAlerta(celda.getValue())));
        colVer.setCellFactory(columna -> new BotonCelda("fa-folder-open", this::verAlerta));
        colDescartar.setCellFactory(columna -> new BotonCelda("fa-times-circle", this::descartarAlerta));
        tablaAlertas.setItems(alertas);

        abrirCanalDeAlertas();
    }

    // ================= Mostrar =================

    // La conexion puede tardar, asi que se abre fuera del hilo de la interfaz.
    private void abrirCanalDeAlertas() {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() {
                // El listener corre en el hilo de red: se pasa al hilo de JavaFX
                // antes de tocar la lista que esta ligada a la tabla.
                mainLogic.iniciarAlertas(reserva -> Platform.runLater(() -> alertas.add(0, reserva)));
                return null;
            }
        };
        tarea.setOnFailed(evento -> {
            Alert aviso = new Alert(Alert.AlertType.WARNING);
            aviso.setTitle("Alertas");
            aviso.setHeaderText(null);
            aviso.setContentText("No se pudo abrir el canal de alertas: " + tarea.getException().getMessage());
            aviso.show();
        });
        Thread hilo = new Thread(tarea, "Abrir-Alertas");
        hilo.setDaemon(true);
        hilo.start();
    }

    // ================= Ver =================

    private void verAlerta(Reserva reserva) {
        // Los nombres de los recursos se piden al servidor: se hace en segundo plano.
        Task<List<String>> tarea = new Task<>() {
            @Override
            protected List<String> call() {
                return mainLogic.descripcionesDeRecursos(reserva);
            }
        };
        tarea.setOnSucceeded(evento -> mostrarDetalle(reserva, tarea.getValue()));
        // Si falla la consulta, se muestra el detalle con los ids que ya vienen en la reserva.
        tarea.setOnFailed(evento -> mostrarDetalle(reserva,
                reserva.getRecursosAsignadosIds() == null ? List.of() : reserva.getRecursosAsignadosIds()));
        Thread hilo = new Thread(tarea, "Detalle-Alerta");
        hilo.setDaemon(true);
        hilo.start();
    }
    private void mostrarDetalle(Reserva reserva, List<String> recursos) {
        ListView<String> listaRecursos = new ListView<>(FXCollections.observableArrayList(recursos));
        listaRecursos.setPrefHeight(110);
        GridPane contenido = new GridPane();
        contenido.setHgap(12);
        contenido.setVgap(6);
        contenido.addRow(0, new Label("Encargado:"), new Label(nombreEncargado(reserva)));
        contenido.addRow(1, new Label("Fecha:"), new Label(String.valueOf(reserva.getFecha())));
        contenido.addRow(2, new Label("Desde:"), new Label(String.valueOf(reserva.getHoraInicio())));
        contenido.addRow(3, new Label("Hasta:"), new Label(String.valueOf(reserva.getHoraFin())));
        contenido.addRow(4, new Label("Recursos:"), listaRecursos);

        Alert detalle = new Alert(Alert.AlertType.INFORMATION);
        detalle.initOwner(tablaAlertas.getScene().getWindow());
        detalle.setTitle(reserva.getActividad());
        detalle.setHeaderText(null);
        detalle.getDialogPane().setContent(contenido);
        detalle.showAndWait();
    }

    // ================= Descartar =================

    // Solo quita la alerta de la lista local. No se llama al Backend.
    private void descartarAlerta(Reserva reserva) {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.YES, ButtonType.NO);
        confirmar.setTitle("Descartar alerta");
        confirmar.setHeaderText("¿Desea descartar esta alerta?");
        confirmar.setContentText(textoDeAlerta(reserva));
        if (confirmar.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            alertas.remove(reserva);
        }
    }


    private String nombreEncargado(Reserva reserva) {
        Funcionario encargado = reserva.getFuncionarioReserva();
        return encargado != null ? encargado.getName() : reserva.getIdFuncionario();
    }

    private String textoDeAlerta(Reserva reserva) {
        return nombreEncargado(reserva) + " - " + reserva.getActividad();
    }

    // Celda con un boton de icono. Sirve para las columnas Ver y Descartar.
    private static class BotonCelda extends TableCell<Reserva, Void> {
        private final Button boton = new Button();

        BotonCelda(String icono, Consumer<Reserva> accion) {
            boton.setGraphic(new FontIcon(icono));
            boton.setStyle("-fx-background-color: transparent;");
            boton.setOnAction(evento -> accion.accept(getTableView().getItems().get(getIndex())));
            setAlignment(Pos.CENTER);
        }

        @Override
        protected void updateItem(Void item, boolean vacia) {
            super.updateItem(item, vacia);
            setGraphic(vacia ? null : boton);
        }
    }
}