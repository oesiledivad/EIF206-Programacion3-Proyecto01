package una.proyecto.utils;

import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.*;
import una.proyecto.model.Funcionario;
import una.proyecto.service.*;
import una.proyecto.datos.UsuarioDatos;

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
    public static FuncionarioService createFuncionarioService(){
        UsuarioDatos usuarioDatos = new UsuarioDatos();
        FuncionarioLogic funcionarioLogic = new FuncionarioLogic(usuarioDatos);
        return new FuncionarioService(funcionarioLogic);
    }
    public static CategoriaService createCategoriaService(){
        try{
            CategoriaDatos datosCategoria= new CategoriaDatos("data/xml/categorias.xml");
            CategoriaLogic logicCategoria= new CategoriaLogic(datosCategoria);
            return new CategoriaService(logicCategoria);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar CategoriaService: " + e.getMessage(), e);
        }
    }

    public static EstadisticasService createEstadisticasService() {
        try {
            ReservaDatos datosReserva = new ReservaDatos("data/xml/reservas.xml");
            CategoriaDatos datosCategoria = new CategoriaDatos("data/xml/categorias.xml");

            EstadisticasLogic logicEstadisticas = new EstadisticasLogic(datosReserva, datosCategoria);
            return new EstadisticasService(logicEstadisticas);
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar EstadisticasService: " + e.getMessage(), e);
        }
    }

}
