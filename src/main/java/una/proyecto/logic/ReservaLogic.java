package una.proyecto.logic;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.model.Reserva;
import java.util.List;

public class ReservaLogic {
    private final ReservaDatos reservaDatos;
    public ReservaLogic(ReservaDatos reservaDatos) {
        this.reservaDatos = reservaDatos;
    }
    public void crear(Reserva nuevaReserva) {
        if (nuevaReserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (nuevaReserva.getId() == null || nuevaReserva.getId().trim().isEmpty()) {
            nuevaReserva.setId(java.util.UUID.randomUUID().toString());
        }
        Reserva existente = reservaDatos.leerPorId(nuevaReserva.getId());
        if (existente != null) {
            throw new IllegalArgumentException("La reserva con id " + nuevaReserva.getId() + " ya existe");
        }
        reservaDatos.crear(nuevaReserva);
    }
    public boolean existe(String id) {
        return reservaDatos.leerPorId(id) != null;
    }

    public void eliminar(String id) {
        Reserva existente = reservaDatos.leerPorId(id);
        if (existente == null) {
            throw new IllegalArgumentException("La reserva con id " + id + " no existe");
        }
        reservaDatos.eliminar(id);
    }

    public Reserva leerPorId(String id) {
        return reservaDatos.leerPorId(id);
    }

    public List<Reserva> obtenerTodos() {
        return reservaDatos.obtenerTodos();
    }
}