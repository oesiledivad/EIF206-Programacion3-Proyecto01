package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservasController {

    @FXML private Button btnreserva;
    @FXML private Button btncancelarreserva;
    @FXML private Button btnlimpiar;
    @FXML private TextArea txtareafrase;
    @FXML private TextArea txtareaactividad;
    @FXML private DatePicker datapickerfecha;
    @FXML private ChoiceBox choiceboxhorainicio;
    @FXML private ChoiceBox choiceboxhorafin;
    @FXML private ListView listviewcategorias;
    @FXML private TableView tableviewmisreservas;
    @FXML public void initialize(){

        btnreserva.setOnAction(event -> handleReservaButton());
        btnlimpiar.setOnAction(event -> handleLimpiarButton());
        btncancelarreserva.setOnAction(event -> handleCancelarReservaButton());

    }
    private void handleEstadisticasButton(){}
    private void handleCalendarizacionButton(){}
    private void handlePanelLateralButton(){}
    private void handleActividadesButton(){}
    private void handleReservaButton(){
        String actividad= txtareaactividad.getText();
        LocalDate  date= datapickerfecha.getValue();
        LocalTime horaInicio= (LocalTime) choiceboxhorainicio.getValue();
        LocalTime horaFin= (LocalTime) choiceboxhorafin.getValue();
        //ACA NECESITO COMO TENER ACCESO A LA PERSONA QUE HIZO LOGGIN, PARA PODER ENVIAR SU CEDULA
        //public Reserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String idFuncionario, List<String> idRecursosAsignados, EstadoReserva estado)
        if (actividad.isBlank() || date==null ||horaInicio==null || horaFin==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Campos incompletos");
            alert.setContentText("Por favor, complete todos los campos antes de realizar la reserva.");
            return;
        }
        Reserva nueva= new Reserva(actividad,date,horaInicio,horaFin,"",null, EstadoReserva.ACTIVA);
        //Reserva.service.add(nueva) <--- aca agregariamos la reserva

    }
    private void handleCancelarReservaButton(){



        ///aca se usa un listener se selecciona como la reserva de list view y se quita x reserva
    }
    private void handleLimpiarButton(){
    txtareafrase.clear();
    txtareaactividad.clear();
    listviewcategorias.getItems().clear();
    choiceboxhorainicio.getSelectionModel().clearSelection();
    choiceboxhorafin.getSelectionModel().clearSelection();
    }

}
