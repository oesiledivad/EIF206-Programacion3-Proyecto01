package Unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.service.RecursoService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservasTest {

    @Mock
    private ReservaDatos datos;

    @Mock
    private RecursoService recursoService;

    private ReservaLogic logica;

    @BeforeEach
    void setUp() {
        logica = new ReservaLogic(datos, recursoService);
    }

    private Reserva reservaValida() {
        return new Reserva(
                "Reunión de equipo",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                "F001",
                new ArrayList<>(),
                EstadoReserva.ACTIVA,
                new Funcionario("F001", "FUNCIONARIO", "Juan Perez", "88887777")
        );
    }

    @Test
    void eliminar_Id_Inexistente_Debe_Lanzar_Excepcion() {
        when(datos.leerPorId("RESS-000")).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> logica.eliminar("RESS-000"));
    }

    @Test
    void crear_id_existente_debe_lanzar_Excepcion() {
        Reserva nueva = reservaValida();
        nueva.setId("RES-000001");

        Reserva existente = reservaValida();
        existente.setId("RES-000001");

        when(datos.leerPorId("RES-000001")).thenReturn(existente);

        assertThrows(IllegalArgumentException.class, () -> logica.crear(nueva));
    }

    @Test
    void verificarHoras_horaInicioDespuesDeHoraFin_deberiaLanzarExcepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                logica.verificarHoras(LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(9, 0))
        );
    }

    @Test
    void verifica_que_no_se_hagan_reservas_al_Pasado_devuelve_excepcion() {
        assertThrows(IllegalArgumentException.class, () ->
                logica.verificarHoras(LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(9, 0))
        );
    }

    @Test
    void obtener_reservas_por_funcionario_lista_vacia_sin_reservas_null() {
        List<Reserva> resultado = logica.obtenerReservasPorFuncionario(null);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtener_reservas_por_funcionario_lista_vacia_sin_reservas_vacio() {
        List<Reserva> resultado = logica.obtenerReservasPorFuncionario("");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void noHayRecursosDisponibles_deberiaLanzarExcepcion() {
        Categoria cat = new Categoria("CAT-01", "Sala");
        when(recursoService.obtenerTodosRecursos()).thenReturn(new ArrayList<>());
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        assertThrows(IllegalArgumentException.class, () ->
                logica.asignarRecursosDisponibles(
                        LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0),
                        List.of(cat)
                )
        );
    }

    @Test
    void recursoOcupado_sinRecursoAsignado_debeDevolverFalse() {
        Reserva re = new Reserva("", LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), LocalTime.of(10, 0),
                "", new ArrayList<>(), EstadoReserva.ACTIVA, new Funcionario());

        assertFalse(logica.recursoOcupado("0000",
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                List.of(re)
        ));
    }

    @Test
    void actualizarCategoriasDeReserva_nuevasCategoriasNull_deberiaLanzarExcepcion() {
        Reserva reserva = reservaValida();
        assertThrows(IllegalArgumentException.class, () ->
                logica.actualizarCategoriasDeReserva(reserva, null)
        );
    }

    @Test
    void borarrCategoria_debeDevolverUnaExcepcion() {
        Reserva reserva = reservaValida();
        assertThrows(IllegalArgumentException.class, () ->
                logica.borrarCategoriasDeReserva(reserva, null)
        );
    }

    @Test
    void crear_sinIdYSinReservasExistentes_generaPrimerId() {
        Reserva nueva = reservaValida();
        nueva.setId(null);
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        when(datos.leerPorId(anyString())).thenReturn(null);
        logica.crear(nueva);
        assertEquals("RES-000001", nueva.getId());
    }

    @Test
    void filtrarReserva_sinCoincidencias_retornaListaVacia() {
        when(datos.obtenerTodos()).thenReturn(new ArrayList<>());
        List<Reserva> resultado = logica.filtrarReserva(LocalDate.now().plusDays(1), "");
        assertTrue(resultado.isEmpty());
    }
}