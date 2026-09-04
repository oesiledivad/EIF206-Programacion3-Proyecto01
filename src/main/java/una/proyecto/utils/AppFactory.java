package una.proyecto.utils;

import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.CategoriaLogic;
import una.proyecto.logic.RecursoLogic;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.service.RecursoService;
import una.proyecto.service.ReservaService;

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
    public static ReservaService createReservaService(){
        try{
            ReservaDatos datosReserva= new ReservaDatos("data/xml/reservas.xml");
            ReservaLogic logicReserva= new ReservaLogic(datosReserva);
            return new ReservaService(logicReserva);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar ReservaService: " + e.getMessage(), e);
        }
    }

}
