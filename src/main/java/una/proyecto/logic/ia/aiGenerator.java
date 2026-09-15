package una.proyecto.logic.ia;

import io.github.cdimascio.dotenv.Dotenv;
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

    // Cargar las variables de entorno desde el archivo .env
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String API_KEY = dotenv.get("GROQ_API_KEY");
    private static final String URL = dotenv.get("GROQ_URL", "https://api.groq.com/openai/v1/chat/completions");
    private static final String MODEL = dotenv.get("GROQ_MODEL", "qwen/qwen3.8-27b"); // Valor por defecto si no existe

    public static Reserva extraeInformacion(String frase, List<Categoria> categoriasDisponibles) throws Exception {

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new Exception("La API Key de Groq no está configurada en el archivo .env");
        }

        String hoy = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String prompt = "Hoy es " + hoy + ". A partir de la siguiente frase de un funcionario que quiere "
                + "hacer una reserva de recursos, extrae los datos. "
                + "El campo \"actividad\" debe ser un titulo corto y descriptivo de la actividad. "
                + "Responde SOLO con un JSON válido, sin texto adicional, con esta forma exacta: "
                + "{\"actividad\": \"...\", \"fecha\": \"YYYY-MM-DD\", "
                + "\"horaInicio\": \"HH:mm\", \"horaFin\": \"HH:mm\", "
                + "\"categorias\": [\"...\"]}. "
                + "El campo \"categorias\" debe contener SOLO valores tomados literalmente de esta lista "
                + "de categorías disponibles (elige las que apliquen, puede ser una o varias): "
                + categoriasDisponibles + ". "
                + "Frase del funcionario: \"" + frase + "\"";

        JSONObject body = new JSONObject();
        body.put("model", MODEL);
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

        JSONObject respuesta = new JSONObject(response.body());
        String contenido = respuesta.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");

        JSONObject datos = new JSONObject(contenido);
        Reserva reservaGeneradaPorelAI = new Reserva();
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

        CategoriaService categoriaService = AppFactory.createCategoriaService();
        List<Categoria> categorias = new ArrayList<>();
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