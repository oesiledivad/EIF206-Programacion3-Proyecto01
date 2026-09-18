package Integration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;
import una.proyecto.service.ReservaService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ReservasIntegracionIT {
    private static final String XML_TEST = "target/test-it-reservas.xml";
    private ReservaService service;

    @BeforeEach
    void setUp() throws Exception {
        new File("target").mkdirs();
        new File(XML_TEST).delete();
        service = new ReservaService(new ReservaLogic(new ReservaDatos(XML_TEST)));
    }

    @AfterEach
    void tearDown() {
        new File(XML_TEST).delete();
    }

    private Reserva reservaValida() {
        return new Reserva("Baile", LocalDate.now().plusDays(1), LocalTime.now().plusHours(1), LocalTime.now().plusHours(3), "703200781",
                new ArrayList<Categoria>(), EstadoReserva.ACTIVA,
                new Funcionario("703200781", "FUNCIONARIO", "Maria", "62284139"));
    }

    @Test
    void crear_Reservas() {
        Reserva nueva = reservaValida();
        service.crearReserva(nueva);
        Reserva recuperada = service.buscarPorId(nueva.getId());
        assertNotNull(recuperada);
        assertEquals(nueva.getId(), recuperada.getId());
    }

    @Test
    void eliminar_Reservas_porId() {
        Reserva r = reservaValida();
        service.crearReserva(r);
        service.delete(r.getId());
        Reserva recuperada = service.buscarPorId(r.getId());
        assertNull(recuperada, "La reserva debería haber sido eliminada");
    }
    @Test
    void actualizar_categoria_reservas() {
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(new Categoria("CAT-001", "nuevoBaile"));
        categorias.add(new Categoria("CAT-002", "nuevoCanto"));

        Reserva valida = reservaValida();
        service.crearReserva(valida);

        service.actualizarCategoriasDeReservaService(valida, categorias);

        Reserva actualizada = service.buscarPorId(valida.getId());
        assertNotNull(actualizada);
        assertEquals(2, actualizada.getCategoriasDeRecursos().size());
    }
    // public void borrarCategoriasDeReservaService(Reserva reserva, List<Categoria> categoriasABorrar) {
    //        reservaLogica.borrarCategoriasDeReserva(reserva, categoriasABorrar);
    //    }

    @Test
    void eliminarCategoriasDeReservas() {
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(new Categoria("CAT-001", "nuevoBaile"));
        categorias.add(new Categoria("CAT-002", "nuevoCanto"));
        Reserva valida = reservaValida();
        service.crearReserva(valida);
        service.actualizarCategoriasDeReservaService(valida, categorias);
        Reserva encontrada = service.buscarPorId(valida.getId());
        assertNotNull(encontrada);
        assertEquals(2, encontrada.getCategoriasDeRecursos().size());
        service.borrarCategoriasDeReservaService(encontrada, categorias);
        Reserva despuesDeBorrar = service.buscarPorId(valida.getId());
        assertNotNull(despuesDeBorrar);
        assertEquals(0, despuesDeBorrar.getCategoriasDeRecursos().size());
    }

    @Test
    void obtener_todas_reservas() {
        Reserva r1 = new Reserva("Baile", LocalDate.now().plusDays(1), LocalTime.now().plusHours(1), LocalTime.now().plusHours(3),
                "9999", new ArrayList<Categoria>(), EstadoReserva.ACTIVA,
                new Funcionario("9999", "FUNCIONARIO", "marcos", "64658298"));

        Reserva r2 = new Reserva("Baile", LocalDate.now().plusDays(1), LocalTime.now().plusHours(3), LocalTime.now().plusHours(6),
                "999", new ArrayList<Categoria>(), EstadoReserva.ACTIVA,
                new Funcionario("999", "FUNCIONARIO", "marcos2", "64658298"));

        service.crearReserva(r1);
        service.crearReserva(r2);

        assertEquals(2, service.obtenerTodasReservas().size());
    }

}