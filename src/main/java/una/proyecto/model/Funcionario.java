package una.proyecto.model ;


import jakarta.xml.bind.annotation.*;
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Funcionario")
public class Funcionario extends Usuario {
    @XmlElement
    private String phone;
    @XmlElement
    private String name;
    //Posee una lista de reservas
    public Funcionario(String clave, String id, String rol, String name, String phone){
        super(id,rol);
        this.phone=phone;
        this.name=name;
    }
    @Override
    public void chagePassword(String password){
    this.setClave(password);
    }
    public String getPhone(){
        return this.phone;
    }
    public String getName(){
        return this.name;
    }
}
