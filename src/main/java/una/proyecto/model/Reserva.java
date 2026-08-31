package una.proyecto.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Reserva {
    private String id;
    private String actividad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String idFuncionario;
    private List<String> idRecursosAsignados;
    private EstadoReserva estado;
    public Reserva() {
    }
    public Reserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String idFuncionario, List<String> idRecursosAsignados, EstadoReserva estado) {
        this.id = "";
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.idFuncionario = idFuncionario;
        this.idRecursosAsignados = idRecursosAsignados;
        this.estado = estado;
    }
    public void setId(String id){this.id=id;}
    public void setActividad(String actividad){this.actividad=actividad;}
    public void setFecha(LocalDate fecha){this.fecha=fecha;}
    public void setHoraInicio(LocalTime horaInicio){this.horaInicio=horaInicio;}
    public void setHoraFin(LocalTime horaFin){this.horaFin=horaFin;}
    public void setIdFuncionario(String idFuncionario){this.idFuncionario=idFuncionario;}
    public void setIdRecursosAsignados(List<String> idRecursosAsignados){this.idRecursosAsignados=idRecursosAsignados;}
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public String getId(){return this.id;}
    public String getActividad(){return this.actividad;}
    public LocalDate getFecha(){return this.fecha;}
    public LocalTime getHoraInicio(){return this.horaInicio;}
    public LocalTime getHoraFin(){return this.horaFin;}
    public String getIdFuncionario(){return this.idFuncionario;}
    public List<String> getIdRecursosAsignados(){return this.idRecursosAsignados;}
    public EstadoReserva getEstado(){return this.estado;}
}