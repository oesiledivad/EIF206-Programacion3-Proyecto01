package una.proyecto.service;

import una.proyecto.model.Reserva;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalendarizacionService {

    private final ReservaService reservaService;

    public CalendarizacionService() {
        this.reservaService = new ReservaService();
    }

    public List<Reserva> obtenerReservasDeLaSemana(LocalDate fecha) {
        LocalDate inicioSemana = fecha.with(DayOfWeek.MONDAY);
        LocalDate finSemana = fecha.with(DayOfWeek.SUNDAY);

        List<Reserva> reservasSemana = new ArrayList<>();
        List<Reserva> todasLasReservas = reservaService.obtenerTodasReservas();

        for (Reserva reserva : todasLasReservas) {
            if (!reserva.getFecha().isBefore(inicioSemana)
                    && !reserva.getFecha().isAfter(finSemana)) {

                reservasSemana.add(reserva);
            }
        }
        return reservasSemana;
    }
}