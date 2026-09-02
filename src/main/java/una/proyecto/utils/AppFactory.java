package una.proyecto.utils;

import una.proyecto.datos.RecursoDatos;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.service.RecursoService;

public class AppFactory {
    public static RecursoService createRecursoDatos(){
        try{
            RecursoDatos datosRecurso= new RecursoDatos("data/xml/recursos.xml");
            RecursoLogic logicRecurso= new RecursoLogic(datosRecurso);
            return new RecursoService(logicRecurso);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar RecursoService: " + e.getMessage(), e);
        }
    }
}
