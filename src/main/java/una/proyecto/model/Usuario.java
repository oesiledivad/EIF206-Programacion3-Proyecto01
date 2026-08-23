package una.proyecto.model;






import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Funcionario.class, Administrador.class}) // Indica a JAXB cuáles son sus clases hijas


public abstract class Usuario {
    @XmlElement
private String clave;
    @XmlElement
private String id;
    @XmlElement
private String rol;

public Usuario(String id, String rol){
    this.clave=id;//luego se modigica con el metodo changePassword
    this.rol=rol;
    this.id=id;
}
public abstract void  chagePassword(String password);
public String getClave(){return this.clave;}
public String getId(){return this.id;}
public String getRol(){return this.rol;}
public void setClave(String clave){this.clave=clave;}

}

