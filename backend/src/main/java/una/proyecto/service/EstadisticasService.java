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
        validarPeriodo(desde, hasta);
        return estadisticasLogic.obtenerEstadisticasRecursos(desde, hasta);
    }

    public List<EstadisticaItem> obtenerEstadisticasActividades(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        return estadisticasLogic.obtenerEstadisticasActividades(desde, hasta);
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Seleccione ambas fechas.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'Desde' debe ser anterior o igual a 'Hasta'.");
        }
    }
}
