package una.proyecto.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class ReservasController {
    @FXML private Button btnestadisticas;
    @FXML private Button btncalendarizacion;
    @FXML private Button btnpanellateral;
    @FXML private Button btnactividades;
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
    @FXML public void initialize(){}
    private void handleEstadisticasButton(){}
    private void handleCalendarizacionButton(){}
    private void handlePanelLateralButton(){}
    private void handleActividadesButton(){}
    private void handleReservaButton(){
        String actividad= txtareaactividad.getText();
        LocalDate  date= datapickerfecha.getValue();
        int dia=0; int mes=0; int annio=0;
        if(date!=null){
             dia = date.getDayOfMonth();
            mes = date.getMonthValue();
             annio=date.getYear();
        }
        //guardamos los textfiel en variables y se crea una instancia de reserva
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
