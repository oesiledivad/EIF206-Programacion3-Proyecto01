package una.proyecto.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Funcionario extends Usuario {

    @XmlElement
    private String phone;

    public Funcionario() {
        super();
    }

    public Funcionario(
            String id,
            String role,
            String name,
            String phone
    ) {
        super(id, role, name);
        this.phone = phone;
    }

    public Funcionario(String id, String funcionario, String nombreTest) {
        super(id, funcionario, nombreTest);
    }

    @Override
    public void changePassword(String password) {
        setPassword(password);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}


