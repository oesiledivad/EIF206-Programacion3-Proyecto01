package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;
import una.proyecto.service.CalendarizacionService;
import una.proyecto.service.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de la vista semanal de actividades (CalendarizacionService),
 * con ReservaService simulado: no usan MySQL.
 */
@ExtendWith(MockitoExtension.class)
public class CalendarizacionTest {

    // Semana de prueba: lunes 2 de noviembre a domingo 8 de noviembre de 2026.
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final LocalDate DOMINGO = LocalDate.of(2026, 11, 8);

    @Mock
    private ReservaService reservaService;

    private CalendarizacionService calendarizacion;

    @BeforeEach
    void setUp() {
        calendarizacion = new CalendarizacionService(reservaService);
    }

    private Reserva reserva(String id, LocalDate fecha) {
        Reserva r = new Reserva("Actividad " + id, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0),
                "111111111", null, EstadoReserva.ACTIVA, null);
        r.setId(id);
        return r;
    }

    private List<String> ids(List<Reserva> reservas) {
        return reservas.stream().map(Reserva::getId).toList();
    }

    @Test
    void incluyeDesdeElLunesHastaElDomingoDeLaSemana() {
        when(reservaService.obtenerTodasReservas()).thenReturn(List.of(
                reserva("LUNES", LUNES), reserva("MIERCOLES", LUNES.plusDays(2)), reserva("DOMINGO", DOMINGO)));

        List<Reserva> semana = calendarizacion.obtenerReservasDeLaSemana(LUNES.plusDays(3));

        assertEquals(List.of("LUNES", "MIERCOLES", "DOMINGO"), ids(semana));
    }

    @Test
    void excluyeLasReservasDeLaSemanaAnteriorYDeLaSiguiente() {
        when(reservaService.obtenerTodasReservas()).thenReturn(List.of(
                reserva("DOMINGO_ANTERIOR", LUNES.minusDays(1)),
                reserva("ESTA_SEMANA", LUNES.plusDays(1)),
                reserva("LUNES_SIGUIENTE", DOMINGO.plusDays(1))));

        List<Reserva> semana = calendarizacion.obtenerReservasDeLaSemana(LUNES);

        assertEquals(List.of("ESTA_SEMANA"), ids(semana));
    }

    @Test
    void cualquierDiaDeLaSemanaDevuelveLaMismaSemana() {
        when(reservaService.obtenerTodasReservas()).thenReturn(List.of(reserva("R1", LUNES.plusDays(4))));

        assertEquals(List.of("R1"), ids(calendarizacion.obtenerReservasDeLaSemana(LUNES)));
        assertEquals(List.of("R1"), ids(calendarizacion.obtenerReservasDeLaSemana(DOMINGO)));
    }

    @Test
    void semanaSinReservasDevuelveListaVacia() {
        when(reservaService.obtenerTodasReservas()).thenReturn(List.of(reserva("OTRA", LUNES.plusWeeks(2))));

        assertTrue(calendarizacion.obtenerReservasDeLaSemana(LUNES).isEmpty());
    }
}
