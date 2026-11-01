package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.*;
import una.proyecto.logic.EstadisticasLogic;
import una.proyecto.model.*;
import una.proyecto.service.EstadisticasService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EstadisticasIntegracionIT {
    private static final String FUNCIONARIO = "IT-FUNC-EST-DB";
    private static final String RECURSO = "IT-EST-DB-1";
    private static final String DESCRIPCION = "IT-EST-DB-Sala";
    private static final LocalDate FECHA = LocalDate.of(2042, 1, 15);
    private Categoria categoria;
    private ReservaDatos reservas;
    private EstadisticasService service;

    @BeforeEach void preparar() throws Exception {
        limpiar();
        Funcionario funcionario = new Funcionario(FUNCIONARIO, "FUNCIONARIO", "Estadisticas MySQL", "00000000");
        funcionario.setPassword(FUNCIONARIO);
        assertTrue(new UsuarioDatos().addUser(funcionario));
        CategoriaDatos categorias = new CategoriaDatos();
        categoria = new Categoria(null, DESCRIPCION);
        categorias.crear(categoria);
        new RecursoDatos().crear(new Recurso(RECURSO, categoria.getId(), "Sala para estadisticas"));
        reservas = new ReservaDatos();
        service = new EstadisticasService(new EstadisticasLogic(reservas, categorias));
    }

    @AfterEach void limpiar() throws Exception {
        try (Connection cnx = Database.instance().openConnection()) {
            for (String sql : List.of(
                    "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario = ?)",
                    "DELETE FROM reservas WHERE id_funcionario = ?",
                    "DELETE FROM recursos WHERE id_recurso = ?",
                    "DELETE FROM categorias WHERE descripcion = ?",
                    "DELETE FROM funcionarios WHERE id_usuario = ?",
                    "DELETE FROM usuarios WHERE id_usuario = ?")) {
                try (PreparedStatement statement = cnx.prepareStatement(sql)) {
                    statement.setString(1, sql.contains("FROM recursos") ? RECURSO :
                            sql.contains("FROM categorias") ? DESCRIPCION : FUNCIONARIO);
                    statement.executeUpdate();
                }
            }
        }
    }

    private Reserva reservar(LocalDate fecha) {
        Reserva reserva = new Reserva("Estadisticas MySQL", fecha, LocalTime.of(8, 0), LocalTime.of(10, 0),
                FUNCIONARIO, List.of(categoria), EstadoReserva.ACTIVA, null);
        reserva.setRecursosAsignadosIds(List.of(RECURSO));
        reservas.crear(reserva);
        return reserva;
    }

    @Test void recursosPersistidosEnMySQLSeCuentanPorCategoria() {
        reservar(FECHA);
        reservar(FECHA.plusDays(1));
        var result = service.obtenerEstadisticasRecursos(FECHA, FECHA.plusDays(1));
        assertEquals(1, result.size());
        assertEquals(DESCRIPCION, result.get(0).getNombre());
        assertEquals(2, result.get(0).getCantidad());
    }

    @Test void reservasCanceladasNoSeCuentan() {
        Reserva cancelada = reservar(FECHA);
        reservas.eliminar(cancelada.getId());
        assertTrue(service.obtenerEstadisticasRecursos(FECHA, FECHA).isEmpty());
    }

    @Test void reservasFueraDelPeriodoSeExcluyen() {
        reservar(FECHA.minusDays(1));
        assertTrue(service.obtenerEstadisticasRecursos(FECHA, FECHA).isEmpty());
    }

    @Test void actividadesSeAgrupanPorSemanaIncluyendoCeros() {
        LocalDate lunes = FECHA.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        reservar(lunes);
        reservar(lunes.plusDays(2));
        var result = service.obtenerEstadisticasActividades(lunes.minusWeeks(1), lunes.plusWeeks(1));
        assertEquals(List.of(lunes.minusWeeks(1).toString(), lunes.toString(), lunes.plusWeeks(1).toString()),
                result.stream().map(EstadisticaItem::getNombre).toList());
        assertEquals(List.of(0, 2, 0), result.stream().map(EstadisticaItem::getCantidad).toList());
    }

    @Test void sinReservasDevuelveCategoriasVaciasYSemanaConCero() {
        assertTrue(service.obtenerEstadisticasRecursos(FECHA, FECHA).isEmpty());
        var actividades = service.obtenerEstadisticasActividades(FECHA, FECHA);
        assertEquals(1, actividades.size());
        assertEquals(0, actividades.get(0).getCantidad());
    }
}
