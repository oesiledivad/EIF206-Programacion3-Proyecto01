package una.proyecto.service;

import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Reserva;

import java.util.List;

public class ReservaService {
    private final ReservaLogic reservaLogica;
    public ReservaService(ReservaLogic reservaLogic) {
        this.reservaLogica = reservaLogic;
    }
    public List<Reserva> obtenerTodasReservas() {
        return reservaLogica.obtenerTodos();
    }
    public void delete(String id) {
        reservaLogica.eliminar(id);
    }
    public void save(Reserva nuevaReserva) {
        reservaLogica.crear(nuevaReserva);
    }
    public Reserva buscarPorId(String id) {
        return reservaLogica.leerPorId(id);
    }
}