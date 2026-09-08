package una.proyecto.service;

import una.proyecto.logic.EstadisticasLogic;
import una.proyecto.model.EstadisticaItem;

import java.time.LocalDate;
import java.util.List;

public class EstadisticasService {

    private final EstadisticasLogic estadisticasLogic;

    public EstadisticasService(EstadisticasLogic estadisticasLogic) {
        this.estadisticasLogic = estadisticasLogic;
    }

    public List<EstadisticaItem> obtenerEstadisticasRecursos(LocalDate desde, LocalDate hasta) {
        return estadisticasLogic.obtenerEstadisticasRecursos(desde, hasta);
    }

    public List<EstadisticaItem> obtenerEstadisticasActividades(LocalDate desde, LocalDate hasta) {
        return estadisticasLogic.obtenerEstadisticasActividades(desde, hasta);
    }
}
