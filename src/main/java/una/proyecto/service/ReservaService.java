package una.proyecto.service;

import una.proyecto.logic.ReservaLogic;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservaService {
    private final ReservaLogic reservaLogica;
    public ReservaService(ReservaLogic reservaLogic) {
        this.reservaLogica = reservaLogic;
    }
    public List<Reserva> obtenerTodasReservas() {
        return reservaLogica.obtenerTodos();
    }

    public List<Reserva> obtenerReservasPorFuncionario(String idFuncionario) {
        return reservaLogica.obtenerReservasPorFuncionario(idFuncionario);
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

    // VALIDACIONES

    public void verificarHorasService(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        reservaLogica.verificarHoras(fecha, horaInicio, horaFin);
    }
    public void actualizarCategoriasDeReservaService(Reserva reserva, List<Categoria> nuevasCategorias) {
        reservaLogica.actualizarCategoriasDeReserva(reserva, nuevasCategorias);
    }

    // OPERACIONES CON CATEGORÍAS


    public void borrarCategoriasDeReservaService(Reserva reserva, List<Categoria> categoriasABorrar) {
        reservaLogica.borrarCategoriasDeReserva(reserva, categoriasABorrar);
    }

    public void actualizarCategoriasService(List<Categoria> categorias, String id) {
        reservaLogica.actualizarCategoriasLogic(categorias, id);
    }

    public Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String idFuncionario, List<Categoria> categoriasSeleccionadas,
                                EstadoReserva estado, Funcionario funcionario) {
        return reservaLogica.crearReserva(actividad, fecha, horaInicio, horaFin, idFuncionario, categoriasSeleccionadas, estado, funcionario);
    }

    public void repoblarCategoriasService(List<Reserva> reservas, List<Categoria> catalogoCategorias) {
        reservaLogica.repoblarCategorias(reservas, catalogoCategorias);
    }
    public List <Reserva>filtrarReservasService(LocalDate fecha, String id){
        return reservaLogica.filtrarReserva(fecha, id);
    }
    public void crearReserva(Reserva nueva){
        reservaLogica.crear(nueva);
    }
}