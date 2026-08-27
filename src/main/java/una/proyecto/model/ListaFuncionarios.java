package una.proyecto.model;

import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "funcionarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListaFuncionarios {

    @XmlElement(name = "funcionario")
    private List<Funcionario> funcionarios = new ArrayList<>();

    public ListaFuncionarios() {
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}
