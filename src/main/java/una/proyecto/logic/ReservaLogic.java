package una.proyecto.logic;

import una.proyecto.datos.ReservaDatos;
import una.proyecto.model.*;
import una.proyecto.service.RecursoService;
import una.proyecto.utils.AppFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReservaLogic {
    private final ReservaDatos reservaDatos;
    private final RecursoService recursoService;

    public ReservaLogic(ReservaDatos reservaDatos) {
        this.reservaDatos = reservaDatos;
        this.recursoService = AppFactory.createRecursoDatos();
    }

    public ReservaLogic(ReservaDatos reservaDatos, RecursoService recursoService) {
        this.reservaDatos = reservaDatos;
        this.recursoService = recursoService;
    }
    public void crear(Reserva nuevaReserva) {
        if (nuevaReserva == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula");
        }
        if (nuevaReserva.getId() == null || nuevaReserva.getId().trim().isEmpty()) {
            nuevaReserva.setId(generarSiguienteIdReserva());
        }
        Reserva existente = reservaDatos.leerPorId(nuevaReserva.getId());
        if (existente != null) {
            throw new IllegalArgumentException("La reserva con id " + nuevaReserva.getId() + " ya existe");
        }
        reservaDatos.crear(nuevaReserva);
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

    // VALIDACIONES

    /**
     * Verifica que las horas sean válidas.
     * La disponibilidad real por recurso se valida en asignarRecursosDisponibles().
     */
    public void verificarHoras(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        // 1. Validar horas
        if (horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin)) {
            throw new IllegalArgumentException("La hora de inicio debe ser anterior a la hora de fin");
        }

        // 2. Validar fecha pasada
        if (fecha.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
        }

        // 3. Validar hora pasada (si es hoy)
        if (fecha.isEqual(LocalDate.now()) && horaInicio.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("No se puede reservar en un horario que ya pasó");
        }
    }

    /**
     * Obtiene solo las reservas de un funcionario específico
     */
    public List<Reserva> obtenerReservasPorFuncionario(String idFuncionario) {
        if (idFuncionario == null || idFuncionario.isEmpty()) {
            return new ArrayList<>();
        }

        return reservaDatos.obtenerTodos().stream()
                .filter(reserva -> idFuncionario.equals(reserva.getIdFuncionario()))
                .collect(Collectors.toList());
    }

    // OPERACIONES CON CATEGORÍAS Y RECURSOS

    /**
     * Busca, para cada categoría solicitada, el primer recurso disponible
     * (sin traslape de horario) en la fecha/hora dadas.
     * Si alguna categoría no tiene disponibilidad, lanza excepción indicando cuáles.
     */
    public List<String> asignarRecursosDisponibles(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                                   List<Categoria> categorias) {
        List<String> asignados = new ArrayList<>();
        List<Categoria> sinDisponibilidad = new ArrayList<>();
        List<Recurso> todosRecursos = recursoService.obtenerTodosRecursos();
        List<Reserva> reservasExistentes = reservaDatos.obtenerTodos();

        for (Categoria cat : categorias) {
            List<Recurso> recursosDeLaCategoria = todosRecursos.stream()
                    .filter(r -> r.getIdCategoria().equals(cat.getId()))
                    .collect(Collectors.toList());

            Optional<Recurso> disponible = recursosDeLaCategoria.stream()
                    .filter(r -> !recursoOcupado(r.getId(), fecha, horaInicio, horaFin, reservasExistentes))
                    .findFirst();

            if (disponible.isPresent()) {
                asignados.add(disponible.get().getId());
            } else {
                sinDisponibilidad.add(cat);
            }
        }

        if (!sinDisponibilidad.isEmpty()) {
            String nombres = sinDisponibilidad.stream()
                    .map(Categoria::getDescripcion)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("No hay recursos disponibles para: " + nombres);
        }

        return asignados;
    }

    /**
     * Verifica si un recurso específico ya está ocupado en la fecha/hora dadas,
     * considerando solo reservas activas.
     */
    public boolean recursoOcupado(String recursoId, LocalDate fecha, LocalTime inicio, LocalTime fin,
                                   List<Reserva> reservas) {
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.CANCELADA) continue;
            if (!fecha.isEqual(r.getFecha())) continue;
            if (r.getRecursosAsignadosIds() == null || !r.getRecursosAsignadosIds().contains(recursoId)) continue;

            boolean seSolapan = inicio.isBefore(r.getHoraFin()) && fin.isAfter(r.getHoraInicio());
            if (seSolapan) return true;
        }
        return false;
    }

    public Reserva crearReserva(String actividad, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String idFuncionario, List<Categoria> categoriasSeleccionadas,
                                EstadoReserva estado, Funcionario funcionario) {
        if (categoriasSeleccionadas == null || categoriasSeleccionadas.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar al menos una categoría");
        }

        Reserva reserva = new Reserva(actividad, fecha, horaInicio, horaFin, idFuncionario, categoriasSeleccionadas, estado, funcionario);

        List<String> ids = categoriasSeleccionadas.stream()
                .map(Categoria::getId)
                .collect(Collectors.toList());
        reserva.setCategoriasDeRecursosIds(ids);

        // Resolver y asignar el primer recurso disponible de cada categoría
        List<String> recursosAsignados = asignarRecursosDisponibles(fecha, horaInicio, horaFin, categoriasSeleccionadas);
        reserva.setRecursosAsignadosIds(recursosAsignados);

        return reserva;
    }

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

    public void actualizarCategoriasLogic(List<Categoria> categorias, String id) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException("El id de la reserva no puede estar vacío");
        }
        if (categorias == null) {
            throw new IllegalArgumentException("La lista de categorías no puede ser nula");
        }
        Reserva reserva = reservaDatos.leerPorId(id);
        reserva.setCategoriasDeRecursos(categorias);
    }
    private String generarSiguienteIdReserva() {
        int maximo = obtenerTodos().stream()
                .map(Reserva::getId)
                .filter(id -> id != null && id.startsWith("RES-"))
                .mapToInt(id -> Integer.parseInt(id.substring(4)))
                .max()
                .orElse(0);

        return String.format("RES-%06d", maximo + 1);
    }
    //aca voy a filtrar las reservas por id de categoria
   public List<Reserva> filtrarReserva(LocalDate fecha, String idCategoria){
           List<Reserva> reservas = obtenerTodos();
           List<Reserva> filtradas = new ArrayList<>();

           for (Reserva reserva : reservas) {
               if (reserva.getFecha().equals(fecha)
                       && reserva.getCategoriasDeRecursosIds().contains(idCategoria)) {

                        filtradas.add(reserva);
               }
           }
           return filtradas;
       }

}