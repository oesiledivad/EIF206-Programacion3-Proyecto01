package una.proyecto.logic;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadoReserva;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // Arma una Reserva nueva a partir de las categorías elegidas en la UI,
    // seteando tanto categoriasDeRecursos (memoria) como categoriasDeRecursosIds (persistencia).
    public Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String idFuncionario, List<Categoria> categoriasSeleccionadas,
                                EstadoReserva estado) {
        if (categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría");
        }

        Reserva reserva = new Reserva(actividad, fecha, horaInicio, horaFin, idFuncionario, categoriasSeleccionadas, estado);

        List<String> ids = categoriasSeleccionadas.stream()
                .map(Categoria::getId)
                .collect(Collectors.toList());
        reserva.setCategoriasDeRecursosIds(ids);

        return reserva;
    }

    // Repuebla categoriasDeRecursos en reservas cargadas desde el XML,
    // cruzando categoriasDeRecursosIds contra el catálogo completo de categorías.
    public void repoblarCategorias(List<Reserva> reservas, List<Categoria> catalogoCategorias) {
        if (reservas == null || catalogoCategorias == null) {
            return;
        }
        for (Reserva r : reservas) {
            if (r.getCategoriasDeRecursos() == null && r.getCategoriasDeRecursosIds() != null) {
                List<Categoria> categorias = catalogoCategorias.stream()
                        .filter(c -> r.getCategoriasDeRecursosIds().contains(c.getId()))
                        .collect(Collectors.toList());
                r.setCategoriasDeRecursos(categorias);
            }
        }
    }

    public boolean existe(String id) {
        return reservaDatos.leerPorId(id) != null;
    }

    public void actualizarCategoriasDeReserva(Reserva reserva, List<Categoria> nuevasCategorias) {
        if (nuevasCategorias == null) {
            throw new IllegalArgumentException("La lista de nuevas categorías no puede ser nula");
        }
        if (reserva.getCategoriasDeRecursos() == null) {
            reserva.setCategoriasDeRecursos(new ArrayList<>());
        }
        reserva.getCategoriasDeRecursos().addAll(nuevasCategorias);
    }

    public void borrarCategoriasDeReserva(Reserva reserva, List<Categoria> categoriasABorrar) {
        if (categoriasABorrar == null) {
            throw new IllegalArgumentException("La lista de categorías a borrar no puede ser nula");
        }
        if (reserva.getCategoriasDeRecursos() == null) {
            reserva.setCategoriasDeRecursos(new ArrayList<>());
        }
        reserva.getCategoriasDeRecursos().removeAll(categoriasABorrar);
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

    public void actualizarCategoriasLogic(List<Categoria> categorias, String id) {
        if(id.isEmpty()) {
            throw new IllegalArgumentException("El id de la reserva no puede estar vacío");
        }
        if (categorias == null) {
            throw new IllegalArgumentException("La lista de categorías no puede ser nula");
        }
        Reserva reserva = reservaDatos.leerPorId(id);
        reserva.setCategoriasDeRecursos(categorias);
    }
    public List<Reserva> obtenerTodos() {
        return reservaDatos.obtenerTodos();
    }
    public void verificarHoras(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        if (fecha.isEqual(LocalDate.now()) && horaInicio.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("No se puede reservar en un horario que ya pasó");
        }

        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
        }

        List<Reserva> reservas = reservaDatos.obtenerTodos();
        for (Reserva reserva : reservas) {
            boolean mismaFecha = fecha.isEqual(reserva.getFecha());
            boolean seSolapan = horaInicio.isBefore(reserva.getHoraFin())
                    && horaFin.isAfter(reserva.getHoraInicio());

            if (mismaFecha && seSolapan) {
                throw new IllegalArgumentException("La reserva se superpone con otra reserva existente");
            }
        }
    }

}