package una.proyecto.service;

import una.proyecto.bootstrap.AppFactory;

// Fachada del backend: crea una sola instancia de cada servicio
// y la comparte con todos los Workers.
// Solo guarda referencias; la logica sigue viviendo en cada Service.
public class Servicios {

    private final AuthService authService;
    private final ReservaService reservaService;
    private final RecursoService recursoService;
    private final CategoriaService categoriaService;
    private final FuncionarioService funcionarioService;
    private final EstadisticasService estadisticasService;
    private final CalendarizacionService calendarizacionService;

    public Servicios() {
        this.authService = new AuthService();
        this.reservaService = AppFactory.createReservaService();
        this.recursoService = AppFactory.createRecursoDatos();
        this.categoriaService = AppFactory.createCategoriaService();
        this.funcionarioService = AppFactory.createFuncionarioService();
        this.estadisticasService = AppFactory.createEstadisticasService();
        this.calendarizacionService = new CalendarizacionService();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ReservaService getReservaService() {
        return reservaService;
    }

    public RecursoService getRecursoService() {
        return recursoService;
    }

    public CategoriaService getCategoriaService() {
        return categoriaService;
    }

    public FuncionarioService getFuncionarioService() {
        return funcionarioService;
    }

    public EstadisticasService getEstadisticasService() {
        return estadisticasService;
    }

    public CalendarizacionService getCalendarizacionService() {
        return calendarizacionService;
    }
}