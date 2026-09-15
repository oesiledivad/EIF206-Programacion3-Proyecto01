package una.proyecto.datos.wrapper;

import jakarta.xml.bind.annotation.*;
import una.proyecto.model.Funcionario;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "funcionarios")
@XmlAccessorType(XmlAccessType.FIELD)
public class FuncionariosWrapper {

    @XmlElement(name = "funcionario")
    private List<Funcionario> funcionarios = new ArrayList<>();

    public FuncionariosWrapper() {
    }

    public List<Funcionario> getFuncionarios() {
        return funcionarios;
    }

    public void setFuncionarios(List<Funcionario> funcionarios) {
        this.funcionarios = funcionarios;
    }
}
