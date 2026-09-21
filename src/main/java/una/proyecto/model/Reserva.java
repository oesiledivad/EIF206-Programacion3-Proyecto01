package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import una.proyecto.utils.LocalDateAdapter;
import una.proyecto.utils.LocalTimeAdapter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@XmlRootElement(name="reserva")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reserva {
    private String id;
    private String actividad;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate fecha;
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaInicio;
    @XmlJavaTypeAdapter(LocalTimeAdapter.class)
    private LocalTime horaFin;
    private String idFuncionario;

    @XmlTransient
    private Funcionario funcionarioReserva;
    private List<String> categoriasDeRecursosIds;
    @XmlTransient
    private List<Categoria> categoriasDeRecursos;
    private List<String> recursosAsignadosIds;
    private EstadoReserva estado;

    public Reserva() {}

    public Reserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String idFuncionario, List<Categoria> categoriasDeRecursos, EstadoReserva estado, Funcionario funcionario) {
        this.funcionarioReserva=funcionario;
        this.categoriasDeRecursos=categoriasDeRecursos;
        this.actividad = actividad;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.idFuncionario = idFuncionario;
        this.estado = estado;
    }

    public void setId(String id){this.id=id;}
    public void setActividad(String actividad){this.actividad=actividad;}
    public void setFecha(LocalDate fecha){this.fecha=fecha;}
    public void setHoraInicio(LocalTime horaInicio){this.horaInicio=horaInicio;}
    public void setHoraFin(LocalTime horaFin){this.horaFin=horaFin;}
    public void setIdFuncionario(String idFuncionario){this.idFuncionario=idFuncionario;}
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public String getId(){return this.id;}
    public String getActividad(){return this.actividad;}
    public LocalDate getFecha(){return this.fecha;}
    public LocalTime getHoraInicio(){return this.horaInicio;}
    public LocalTime getHoraFin(){return this.horaFin;}

    public String getHorario(){
        if (horaInicio == null && horaFin == null) {
            return "";
        }
        if (horaInicio == null) {
            return horaFin.toString();
        }
        if (horaFin == null) {
            return horaInicio.toString();
        }
        return horaInicio + " - " + horaFin;
    }
    @Override
    public String toString(){
        return id;
    }
    public String getIdFuncionario(){return this.idFuncionario;}

    public List<Categoria> getCategoriasDeRecursos(){return this.categoriasDeRecursos;}
    public void setCategoriasDeRecursos(List<Categoria> categoriasDeRecursos) {
        this.categoriasDeRecursos = categoriasDeRecursos;
    }

    public List<String> getCategoriasDeRecursosIds() {
        return categoriasDeRecursosIds;
    }
    public void setCategoriasDeRecursosIds(List<String> categoriasDeRecursosIds) {
        this.categoriasDeRecursosIds = categoriasDeRecursosIds;
    }

    public List<String> getRecursosAsignadosIds() {
        return recursosAsignadosIds;
    }
    public void setRecursosAsignadosIds(List<String> recursosAsignadosIds) {
        this.recursosAsignadosIds = recursosAsignadosIds;
    }
    public Funcionario getFuncionarioReserva(){
        return funcionarioReserva;
    }

    public EstadoReserva getEstado(){return this.estado;}

    public String getNombreFuncionario() {
        return funcionarioReserva.getName();
    }
}