package una.proyecto.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.FilaCalendarizacion;
import una.proyecto.model.Reserva;
import una.proyecto.service.CalendarizacionService;
import una.proyecto.model.EstadoReserva;
import una.proyecto.utils.GeneradorPDFS;
import una.proyecto.utils.TablePDF;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CalendarizacionActividadesController {

    @FXML
    private DatePicker dtpicker;

    @FXML
    private Button btnCargar;

    @FXML
    private Button btnImprimir;

    @FXML
    private TreeTableView<FilaCalendarizacion> tblActividades;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumHora;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumLunes;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumMartes;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumMiercoles;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumJueves;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumViernes;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumSabado;

    @FXML
    private TreeTableColumn<FilaCalendarizacion, String> tblColumDomingo;

    private final CalendarizacionService calendarizacionService =
            new CalendarizacionService();

    private final DateTimeFormatter formatoHora =
            DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {

        configurarColumnas();

        dtpicker.setValue(LocalDate.now());

        cargarSemana();
    }

    private void configurarColumnas() {

        tblColumHora.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getHora()
                )
        );

        tblColumLunes.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getLunes()
                )
        );

        tblColumMartes.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getMartes()
                )
        );

        tblColumMiercoles.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getMiercoles()
                )
        );

        tblColumJueves.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getJueves()
                )
        );

        tblColumViernes.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getViernes()
                )
        );

        tblColumSabado.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getSabado()
                )
        );

        tblColumDomingo.setCellValueFactory(
                dato -> new javafx.beans.property.SimpleStringProperty(
                        dato.getValue().getValue().getDomingo()
                )
        );
    }
    @FXML
    private void cargarSemana() {

        LocalDate fechaSeleccionada = dtpicker.getValue();

        // Validar que exista una fecha
        if (fechaSeleccionada == null) {
            mostrarAlerta(
                    "Fecha requerida",
                    "Debe seleccionar una fecha para cargar la semana."
            );
            return;
        }

        // Obtener el lunes de la semana seleccionada
        LocalDate lunes =
                fechaSeleccionada.with(DayOfWeek.MONDAY);

        // Actualizar los encabezados de los días
        actualizarEncabezados(lunes);

        // Obtener las reservas de esa semana
        List<Reserva> reservas =
                calendarizacionService.obtenerReservasDeLaSemana(
                        fechaSeleccionada
                );

        // Crear la raíz de la tabla
        TreeItem<FilaCalendarizacion> raiz =
                new TreeItem<>(
                        new FilaCalendarizacion("")
                );

        // Crear las filas correspondientes a cada hora
        for (int hora = 0; hora < 24; hora++) {

            LocalTime horaActual =
                    LocalTime.of(hora, 0);

            FilaCalendarizacion fila =
                    new FilaCalendarizacion(
                            horaActual.format(formatoHora)
                    );

            // Revisar las reservas de la semana
            for (Reserva reserva : reservas) {

                // Ignorar reservas incompletas
                if (reserva.getFecha() == null
                        || reserva.getHoraInicio() == null
                        || reserva.getHoraFin() == null
                        || reserva.getEstado() == null) {

                    continue;
                }

                // Solo mostrar reservas activas
                if (reserva.getEstado() != EstadoReserva.ACTIVA) {
                    continue;
                }

                // Verificar si la hora actual pertenece a la reserva
                if (!horaActual.isBefore(reserva.getHoraInicio())
                        && horaActual.isBefore(reserva.getHoraFin())) {

                    String informacion =
                            reserva.getActividad()
                                    + "\nFuncionario: "
                                    + reserva.getIdFuncionario();

                    int diferenciaDias =
                            (int) (
                                    reserva.getFecha().toEpochDay()
                                            - lunes.toEpochDay()
                            );

                    switch (diferenciaDias) {

                        case 0:
                            fila.setLunes(informacion);
                            break;

                        case 1:
                            fila.setMartes(informacion);
                            break;

                        case 2:
                            fila.setMiercoles(informacion);
                            break;

                        case 3:
                            fila.setJueves(informacion);
                            break;

                        case 4:
                            fila.setViernes(informacion);
                            break;

                        case 5:
                            fila.setSabado(informacion);
                            break;

                        case 6:
                            fila.setDomingo(informacion);
                            break;
                    }
                }
            }

            raiz.getChildren().add(
                    new TreeItem<>(fila)
            );
        }

        // Actualizar la tabla
        tblActividades.setRoot(raiz);
        tblActividades.setShowRoot(false);
    }

    private void actualizarEncabezados(LocalDate lunes) {

        tblColumLunes.setText(
                "Lunes\n" + lunes.format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumMartes.setText(
                "Martes\n" + lunes.plusDays(1)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumMiercoles.setText(
                "Miércoles\n" + lunes.plusDays(2)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumJueves.setText(
                "Jueves\n" + lunes.plusDays(3)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumViernes.setText(
                "Viernes\n" + lunes.plusDays(4)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumSabado.setText(
                "Sábado\n" + lunes.plusDays(5)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );

        tblColumDomingo.setText(
                "Domingo\n" + lunes.plusDays(6)
                        .format(DateTimeFormatter.ofPattern("dd/MM"))
        );
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    public void btnImprimir() {
        try {
            TablePDF nuevo = new TablePDF();

            // Encabezados
            nuevo.setEncabezados(List.of(
                    tblColumHora.getText(),
                    tblColumLunes.getText(),
                    tblColumMartes.getText(),
                    tblColumMiercoles.getText(),
                    tblColumJueves.getText(),
                    tblColumViernes.getText(),
                    tblColumSabado.getText(),
                    tblColumDomingo.getText()
            ));

            // Filas
            for (TreeItem<FilaCalendarizacion> item :
                    tblActividades.getRoot().getChildren()) {

                FilaCalendarizacion fila = item.getValue();

                nuevo.agregarFila(List.of(
                        fila.getHora(),
                        fila.getLunes(),
                        fila.getMartes(),
                        fila.getMiercoles(),
                        fila.getJueves(),
                        fila.getViernes(),
                        fila.getSabado(),
                        fila.getDomingo()
                ));
            }

            GeneradorPDFS.generarPDF(nuevo, "Calendarizacion.pdf");

            mostrarAlerta(
                    "Éxito",
                    "PDF generado correctamente."
            );

        } catch (Exception e) {
            mostrarAlerta(
                    "Error",
                    "Error al generar PDF."
            );

            e.printStackTrace();
        }
    }
}