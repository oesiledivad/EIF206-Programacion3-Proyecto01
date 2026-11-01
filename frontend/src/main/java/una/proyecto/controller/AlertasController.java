package una.proyecto.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.presentation.logic.MainLogic;

import java.util.List;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

public class AlertasController {

    @FXML private ListView<Reserva> listaAlertas;
    @FXML private Label contadorAlertas;

    private final ObservableList<Reserva> alertas = FXCollections.observableArrayList();
    private final MainLogic mainLogic = new MainLogic();

    @FXML
    public void initialize() {
        listaAlertas.setCellFactory(lista -> new AlertaCelda());
        listaAlertas.setItems(alertas);
        contadorAlertas.textProperty().bind(Bindings.size(alertas).asString());

        abrirCanalDeAlertas();
    }

    // ================= Mostrar =================

    // La conexion puede tardar, asi que se abre fuera del hilo de la interfaz.
    private void abrirCanalDeAlertas() {
        Task<Void> tarea = new Task<>() {
            @Override
            protected Void call() {
                // El listener corre en el hilo de red: se pasa al hilo de JavaFX
                // antes de actualizar la lista de alertas.
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
        configurarDialogo(detalle);
        detalle.setTitle(reserva.getActividad());
        detalle.setHeaderText(null);
        detalle.getDialogPane().setContent(contenido);
        detalle.showAndWait();
    }

    // ================= Descartar =================

    // Solo quita la alerta de la lista local. No se llama al Backend.
    private void descartarAlerta(Reserva reserva) {
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION, null, ButtonType.YES, ButtonType.NO);
        configurarDialogo(confirmar);
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

    private void configurarDialogo(Alert dialogo) {
        dialogo.initOwner(listaAlertas.getScene().getWindow());
        dialogo.getDialogPane().getStylesheets().add(
                getClass().getResource("/una/proyecto/css/style.css").toExternalForm());
        if (listaAlertas.getScene().getRoot().getStyleClass().contains("dark-mode")) {
            dialogo.getDialogPane().getStyleClass().add("dark-mode");
        }
    }

    private class AlertaCelda extends ListCell<Reserva> {
        @FXML private Label actividad;
        @FXML private Label encargado;
        @FXML private Label horario;
        private final Parent contenido;

        AlertaCelda() {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/una/proyecto/ui/alerta-item.fxml"));
            loader.setController(this);
            try {
                contenido = loader.load();
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo cargar la tarjeta de alerta.", e);
            }
            setText(null);
            prefWidthProperty().bind(listaAlertas.widthProperty().subtract(20));
            ((Region) contenido).prefWidthProperty().bind(widthProperty());
        }

        @FXML private void verDetalle() {
            if (getItem() != null) verAlerta(getItem());
        }

        @FXML private void descartar() {
            if (getItem() != null) descartarAlerta(getItem());
        }

        @Override
        protected void updateItem(Reserva reserva, boolean vacia) {
            super.updateItem(reserva, vacia);
            if (vacia || reserva == null) {
                setGraphic(null);
                return;
            }
            actividad.setText(reserva.getActividad());
            encargado.setText(nombreEncargado(reserva));
            String fecha = reserva.getFecha() == null ? "Sin fecha" :
                    reserva.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm");
            String inicio = reserva.getHoraInicio() == null ? "--:--" : reserva.getHoraInicio().format(hora);
            String fin = reserva.getHoraFin() == null ? "--:--" : reserva.getHoraFin().format(hora);
            horario.setText(fecha + " · " + inicio + "–" + fin);
            setGraphic(contenido);
        }
    }
}
