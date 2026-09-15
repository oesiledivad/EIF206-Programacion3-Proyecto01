package una.proyecto.logic.ia;

import org.json.JSONArray;
import org.json.JSONObject;


import una.proyecto.model.Categoria;
import una.proyecto.model.Reserva;
import una.proyecto.service.CategoriaService;
import una.proyecto.utils.AppFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class aiGenerator {
    //Este es el api key de OpenAI que usaremos en nuestro proyecto.
    private static final String API_KEY = "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"; // Reemplaza con tu clave de API de OpenAI
    private static String URL="https://api.groq.com/openai/v1/chat/completions";
    public static Reserva extraeInformacion(String frase, List<Categoria> categoriasDisponibles)throws Exception{
    //Vamos a extraer la fecha del dia actual.
    String hoy = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String prompt = "Hoy es " + hoy + ". A partir de la siguiente frase de un funcionario que quiere "
                + "hacer una reserva de recursos, extrae los datos. "
            +"El campo \"actividad\" debe ser un titulo corto y descriptivo de la actividad"
                + "Responde SOLO con un JSON válido, sin texto adicional, con esta forma exacta: "
                + "{\"actividad\": \"...\", \"fecha\": \"YYYY-MM-DD\", "
                + "\"horaInicio\": \"HH:mm\", \"horaFin\": \"HH:mm\", "
                + "\"categorias\": [\"...\"]}. "
                + "El campo \"categorias\" debe contener SOLO valores tomados literalmente de esta lista "
                + "de categorías disponibles (elige las que apliquen, puede ser una o varias): "
                + categoriasDisponibles + ". "
                + "Frase del funcionario: \"" + frase + "\"";

    JSONObject body = new JSONObject();
        body.put("model", "qwen/qwen3.6-27b");
        body.put("response_format", new JSONObject().put("type", "json_object"));
        body.put("reasoning_effort", "none");
        body.put("reasoning_format", "hidden");
        body.put("max_tokens", 300);
        body.put("messages", new Object[]{
                new JSONObject().put("role", "user").put("content", prompt)
        });
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new Exception("Error IA (" + response.statusCode() + "): " + response.body());
        }
        /*
        La respuesta completa (response.body()) es un JSON, pero es "metadata de la API"
       Cuando llamas a la API de IA, ella te devuelve un JSON con información sobre la llamada en sí:
        qué modelo respondió, cuántos tokens usó, si hubo error, etc. Algo así
        */
        JSONObject respuesta = new JSONObject(response.body());
        String contenido = respuesta.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        JSONObject datos = new JSONObject(contenido);
        Reserva reservaGeneradaPorelAI= new Reserva();
        reservaGeneradaPorelAI.setActividad(datos.optString("actividad", ""));
        String fechaTxt = datos.optString("fecha", null);
        if (fechaTxt != null && !fechaTxt.isBlank()) {
            reservaGeneradaPorelAI.setFecha(LocalDate.parse(fechaTxt));
        }

        String horaInicioTxt = datos.optString("horaInicio", null);
        if (horaInicioTxt != null && !horaInicioTxt.isBlank()) {
            reservaGeneradaPorelAI.setHoraInicio(LocalTime.parse(normalizarHora(horaInicioTxt)));
        }

        String horaFinTxt = datos.optString("horaFin", null);
        if (horaFinTxt != null && !horaFinTxt.isBlank()) {
            reservaGeneradaPorelAI.setHoraFin(LocalTime.parse(normalizarHora(horaFinTxt)));
        }
        CategoriaService categoriaService= AppFactory.createCategoriaService();
        List<Categoria> categorias = new ArrayList<>();
        List<Categoria> todasCategorias= categoriaService.obtenerTodas();
        JSONArray arr = datos.optJSONArray("categorias");
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                String descripcionCategoria = arr.getString(i);
                for (Categoria cate : categoriasDisponibles) {
                    if (cate.getDescripcion().equals(descripcionCategoria)) {
                        categorias.add(cate);
                        break;
                    }
                }
            }
        }
        reservaGeneradaPorelAI.setCategoriasDeRecursos(categorias);

        return reservaGeneradaPorelAI;

}
    private static String normalizarHora(String hora) {
        String[] partes = hora.split(":");
        String hh = partes[0].length() == 1 ? "0" + partes[0] : partes[0];
        String mm = partes.length > 1 ? partes[1] : "00";
        return hh + ":" + mm;
    }
}
