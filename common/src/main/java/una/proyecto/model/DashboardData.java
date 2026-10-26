package una.proyecto.model;

import java.io.Serializable;
import java.util.List;

public record DashboardData(List<Reserva> reservas, int totalRecursos,
                            int totalFuncionarios) implements Serializable {
    private static final long serialVersionUID = 1L;

    public DashboardData {
        reservas = List.copyOf(reservas);
    }
}
