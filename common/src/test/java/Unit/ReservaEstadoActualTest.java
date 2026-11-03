package Unit;

import org.junit.jupiter.api.Test;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** Estado que muestran las pantallas: ACTIVA, FINALIZADA (ya terminó) o CANCELADA. */
class ReservaEstadoActualTest {

    private Reserva reserva(LocalDateTime inicio, LocalDateTime fin, EstadoReserva estado) {
        Reserva r = new Reserva();
        r.setFecha(inicio.toLocalDate());
        r.setHoraInicio(inicio.toLocalTime());
        r.setHoraFin(fin.toLocalTime());
        r.setEstado(estado);
        return r;
    }

    @Test
    void activaQueYaTerminoSeMuestraComoFinalizada() {
        LocalDateTime ayer = LocalDate.now().minusDays(1).atTime(9, 0);
        assertEquals("FINALIZADA", reserva(ayer, ayer.plusHours(1), EstadoReserva.ACTIVA).getEstadoActual());
    }

    @Test
    void activaFuturaSigueActiva() {
        LocalDateTime manana = LocalDate.now().plusDays(1).atTime(9, 0);
        assertEquals("ACTIVA", reserva(manana, manana.plusHours(1), EstadoReserva.ACTIVA).getEstadoActual());
    }

    @Test
    void canceladaSigueCanceladaAunqueYaHayaPasado() {
        LocalDateTime ayer = LocalDate.now().minusDays(1).atTime(9, 0);
        assertEquals("CANCELADA", reserva(ayer, ayer.plusHours(1), EstadoReserva.CANCELADA).getEstadoActual());
    }

    @Test
    void elEstadoGuardadoNoCambia() {
        LocalDateTime ayer = LocalDate.now().minusDays(1).atTime(9, 0);
        Reserva r = reserva(ayer, ayer.plusHours(1), EstadoReserva.ACTIVA);
        r.getEstadoActual();
        assertEquals(EstadoReserva.ACTIVA, r.getEstado());
    }

    @Test
    void sinEstadoDevuelveTextoVacio() {
        Reserva r = new Reserva();
        r.setFecha(LocalDate.now());
        r.setHoraFin(LocalTime.NOON);
        assertEquals("", r.getEstadoActual());
    }
}
