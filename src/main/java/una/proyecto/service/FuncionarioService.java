package una.proyecto.service;

import una.proyecto.logic.FuncionarioLogic;
import una.proyecto.model.Funcionario;

import java.util.List;

public class FuncionarioService {

    private final FuncionarioLogic funcionarioLogica;

    public FuncionarioService(FuncionarioLogic funcionarioLogic) {
        this.funcionarioLogica = funcionarioLogic;
    }

    public List<Funcionario> getAllEmployees() {
        return funcionarioLogica.obtenerTodos();
    }

    public List<Funcionario> findById(String id) {
        return funcionarioLogica.buscarPorId(id);
    }

    public List<Funcionario> findByName(String name) {
        return funcionarioLogica.buscarPorNombre(name);
    }

    public void addEmployee(Funcionario nuevo) {
        funcionarioLogica.crear(nuevo);
    }

    public void updateEmployee(Funcionario actualizado) {
        funcionarioLogica.actualizar(actualizado);
    }

    public void deleteEmployee(String id) {
        funcionarioLogica.eliminar(id);
    }
    public Funcionario obetenerUsuarioPorId(String id){
        return funcionarioLogica.porId(id);
    }
}