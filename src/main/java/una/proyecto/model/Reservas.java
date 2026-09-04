package una.proyecto.model;
import jakarta.xml.bind.annotation.*;//esto me permite traerme toda slas etiquetas XML
import java.util.ArrayList;
import java.util.List;
@XmlRootElement(name="reservas")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reservas {
    @XmlElement(name="reserva")
    private List<Reserva> reservas= new ArrayList<>();
    public Reservas(){}
    public List<Reserva> getReservas(){return reservas;}
    public void setReservas(List<Reserva> list){this.reservas=list;}
}
