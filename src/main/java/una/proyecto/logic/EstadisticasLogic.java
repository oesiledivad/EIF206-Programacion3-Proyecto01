package una.proyecto.logic;

import una.proyecto.datos.CategoriaDatos;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.model.Categoria;
import una.proyecto.model.EstadisticaItem;
import una.proyecto.model.Reserva;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class EstadisticasLogic {

    private final ReservaDatos reservaDatos;
    private final CategoriaDatos categoriaDatos;

    public EstadisticasLogic(ReservaDatos reservaDatos, CategoriaDatos categoriaDatos) {
        this.reservaDatos = reservaDatos;
        this.categoriaDatos = categoriaDatos;
    }

    public List<EstadisticaItem> obtenerEstadisticasRecursos(LocalDate desde, LocalDate hasta) {
        List<Reserva> reservas = reservaDatos.obtenerTodos();
        Map<String, Integer> conteo = new LinkedHashMap<>();

        for (Reserva reserva : reservas) {
            if (reserva.getFecha() != null && !reserva.getFecha().isBefore(desde) && !reserva.getFecha().isAfter(hasta)) {

                // 1. Si los objetos Categoria están cargados en memoria
                if (reserva.getCategoriasDeRecursos() != null && !reserva.getCategoriasDeRecursos().isEmpty()) {
                    for (Categoria categoria : reserva.getCategoriasDeRecursos()) {
                        String nombre = categoria.getDescripcion();
                        conteo.put(nombre, conteo.getOrDefault(nombre, 0) + 1);
                    }
                }
                // 2. Si vienen los IDs deserializados del XML (<categoriasDeRecursosIds>)
                else if (reserva.getCategoriasDeRecursosIds() != null) {
                    for (String idCat : reserva.getCategoriasDeRecursosIds()) {
                        Categoria categoria = categoriaDatos.leerPorId(idCat);
                        String nombre = (categoria != null && categoria.getDescripcion() != null) ? categoria.getDescripcion() : "Categoría (" + idCat + ")";

                        conteo.put(nombre, conteo.getOrDefault(nombre, 0) + 1);
                    }
                }
            }
        }

        return conteo.entrySet().stream()
                .map(entry -> new EstadisticaItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<EstadisticaItem> obtenerEstadisticasActividades(LocalDate desde, LocalDate hasta) {
        List<Reserva> reservas = reservaDatos.obtenerTodos();
        Map<String, Integer> conteoPorSemana = new LinkedHashMap<>();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        for (Reserva reserva : reservas) {
            if (reserva.getFecha() != null && !reserva.getFecha().isBefore(desde) && !reserva.getFecha().isAfter(hasta)) {

                LocalDate inicioSemana = reserva.getFecha().with(weekFields.dayOfWeek(), 1);
                String etiquetaSemana = inicioSemana.toString();

                conteoPorSemana.put(
                        etiquetaSemana,
                        conteoPorSemana.getOrDefault(etiquetaSemana, 0) + 1
                );
            }
        }

        return conteoPorSemana.entrySet().stream()
                .map(entry -> new EstadisticaItem(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}