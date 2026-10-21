package una.proyecto.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class HoraRow {

    private final String hora;
    private final Map<String, String> reservasPorRecurso = new LinkedHashMap<>();

    public HoraRow(String hora) {
        this.hora = hora;
    }

    public String getHora() {
        return hora;
    }

    // Esto es lo que preguntas
    public String getReserva(String idRecurso) {
        return reservasPorRecurso.getOrDefault(idRecurso, "");
    }

    public void setReserva(String idRecurso, String texto) {
        reservasPorRecurso.put(idRecurso, texto);
    }
}