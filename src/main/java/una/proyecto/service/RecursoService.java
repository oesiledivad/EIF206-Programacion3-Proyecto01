package una.proyecto.service;

import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.Recurso;
import una.proyecto.model.ListaRecursos;
import una.proyecto.utils.XmlUtil;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class RecursoService {
    private  RecursoLogic recursoLogica;
    public RecursoService(RecursoLogic recursoLogic){
            this.recursoLogica = recursoLogic;
    }
    public List<Recurso> obtenerTodosRecursos(){
        return recursoLogica.obtenerTodos();
    }
    public void update(Recurso actualizado){
        recursoLogica.actualizar(actualizado);
    }
    public void delete(String id){
       recursoLogica.eliminar(id);
    }
    public void save(Recurso  nuevoRecurso){
        recursoLogica.crear(nuevoRecurso);
    }
    public List<Recurso> buscarPorFiltros(Categoria categoria, String descripcion) {
        return recursoLogica.buscarFiltro(categoria, descripcion);
    }

}



