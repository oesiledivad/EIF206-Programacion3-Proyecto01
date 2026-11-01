package una.proyecto.proxy;

import una.proyecto.model.DashboardData;
import una.proyecto.model.LoginService;
import una.proyecto.model.Usuario;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import una.proyecto.model.Funcionario;
import java.time.LocalDate;
import java.time.LocalTime;
import una.proyecto.protocolo.Protocol;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import una.proyecto.model.Categoria;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import static una.proyecto.protocolo.Protocol.RECURSO_ACTUALIZAR;
import static una.proyecto.protocolo.Protocol.RECURSO_LISTAR;

public class ServiceProxy implements LoginService, AutoCloseable {
    private static final ServiceProxy INSTANCE = new ServiceProxy();
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

     //NOTIFICACIONES
    private volatile String sid;
    private ReceptorNotificaciones receptor;
    private final Object lockAlertas = new Object();

    public ServiceProxy() {
        this(System.getenv().getOrDefault("SERVER_HOST", Protocol.SERVER),
                Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", String.valueOf(Protocol.PORT))));
    }

    public ServiceProxy(String host, int port) { this.host = host; this.port = port; }
    public static ServiceProxy instance() { return INSTANCE; }

    @Override
    public Usuario authenticate(String id, String password) {
        try { return (Usuario) request(Protocol.AUTH_LOGIN, id, password); }
        catch (RemoteException e) {
            if ("Credenciales incorrectas.".equals(e.getMessage())) return null;
            throw e;
        }
    }

    public DashboardData dashboard() { return (DashboardData) request(Protocol.DASHBOARD_RESUMEN); }
    public void changePassword(String actual, String nueva) { request(Protocol.AUTH_CAMBIAR_CLAVE, actual, nueva); }

    // ================= Categorias =================

    @SuppressWarnings("unchecked")
    public List<Categoria> categoriasObtenerTodas() {
        return (List<Categoria>) request(Protocol.CATEGORIA_LISTAR);
    }

    @SuppressWarnings("unchecked")
    public List<Categoria> categoriasBuscarPorDescripcion(String descripcion) {
        return (List<Categoria>) request(Protocol.CATEGORIA_BUSCAR_DESCRIPCION, descripcion);
    }

    // Devuelve la categoría ya guardada, con el id que asignó MySQL.
    public Categoria categoriaCrear(Categoria categoria) {
        return (Categoria) request(Protocol.CATEGORIA_CREAR, categoria);
    }

    public void categoriaActualizar(Categoria categoria) {
        request(Protocol.CATEGORIA_ACTUALIZAR, categoria);
    }

    public void categoriaEliminar(String id) {
        request(Protocol.CATEGORIA_ELIMINAR, id);
    }

    @SuppressWarnings("unchecked")
    public List<Categoria> categoriasSinRecursos(List<Categoria> escogidas) {
        return (List<Categoria>) request(Protocol.CATEGORIA_SIN_RECURSOS, new ArrayList<>(escogidas));
    }

    @SuppressWarnings("unchecked")
    public List<Categoria> categoriasConRecursos(List<Categoria> escogidas) {
        return (List<Categoria>) request(Protocol.CATEGORIA_CON_RECURSOS, new ArrayList<>(escogidas));
    }

    // ================= Calendarizacion =================

    @SuppressWarnings("unchecked")
    public List<Recurso> calendarizacionRecursosPorCategoria(String idCategoria) {
        return (List<Recurso>) request(Protocol.CALENDARIZACION_RECURSOS_CATEGORIA, idCategoria);
    }

    // Reservas activas de ese día que usan recursos de la categoría, con su encargado.
    @SuppressWarnings("unchecked")
    public List<Reserva> calendarizacionReservasDelDia(LocalDate fecha, String idCategoria) {
        return (List<Reserva>) request(Protocol.CALENDARIZACION_RESERVAS_DIA, fecha, idCategoria);
    }

    // Reservas activas de la semana (lunes a domingo) de esa fecha, con su encargado.
    @SuppressWarnings("unchecked")
    public List<Reserva> calendarizacionReservasDeLaSemana(LocalDate fecha) {
        return (List<Reserva>) request(Protocol.CALENDARIZACION_RESERVAS_SEMANA, fecha);
    }

    // ================= Funcionarios =================

    @SuppressWarnings("unchecked")
    public List<Funcionario> funcionariosObtenerTodos() {
        return (List<Funcionario>) request(Protocol.FUNCIONARIO_LISTAR);
    }

    @SuppressWarnings("unchecked")
    public List<Funcionario> funcionariosBuscarPorId(String id) {
        return (List<Funcionario>) request(Protocol.FUNCIONARIO_BUSCAR_ID, id);
    }

    @SuppressWarnings("unchecked")
    public List<Funcionario> funcionariosBuscarPorNombre(String nombre) {
        return (List<Funcionario>) request(Protocol.FUNCIONARIO_BUSCAR_NOMBRE, nombre);
    }

    // La clave inicial (= cédula) la asigna el servidor; la clave nunca viaja (es transient).
    public void funcionarioCrear(Funcionario funcionario) {
        request(Protocol.FUNCIONARIO_CREAR, funcionario);
    }

    public void funcionarioActualizar(Funcionario funcionario) {
        request(Protocol.FUNCIONARIO_ACTUALIZAR, funcionario);
    }

    public void funcionarioEliminar(String id) {
        request(Protocol.FUNCIONARIO_ELIMINAR, id);
    }

    // ================= Reservas =================

    // Funcionario: sus propias reservas. Administrador: todas.
    // Llegan con sus categorias ya cargadas.
    @SuppressWarnings("unchecked")
    public List<Reserva> reservasObtener() {
        return (List<Reserva>) request(Protocol.RESERVA_LISTAR);
    }

    public Reserva reservaBuscarPorId(String id) {
        return (Reserva) request(Protocol.RESERVA_BUSCAR, id);
    }

    // Envia solo lo que lleno el usuario. El servidor valida las horas,
    // asigna los recursos, toma el funcionario de la sesion y devuelve
    // la reserva guardada, con su id y sus recursos.
    public Reserva reservaCrear(String actividad, LocalDate fecha, LocalTime horaInicio,
                                LocalTime horaFin, List<Categoria> categorias) {
        Reserva datos = new Reserva();
        datos.setActividad(actividad);
        datos.setFecha(fecha);
        datos.setHoraInicio(horaInicio);
        datos.setHoraFin(horaFin);
        datos.setCategoriasDeRecursos(new ArrayList<>(categorias));
        return (Reserva) request(Protocol.RESERVA_CREAR, datos);
    }

    public void reservaCancelar(String id) {
        request(Protocol.RESERVA_CANCELAR, id);
    }

    // Interpreta una frase con IA en el servidor y devuelve una reserva sugerida
    // (actividad, fecha, horas y categorias). No guarda nada.
    public Reserva reservaExtraerConIA(String frase) {
        return (Reserva) request(Protocol.RESERVA_EXTRAER_IA, frase);
    }

    // ================= Recursos =================

    @SuppressWarnings("unchecked")
    public List<Recurso> recursosObtenerTodos() {
        return (List<Recurso>) request(Protocol.RECURSO_LISTAR);
    }

    public Recurso recursoBuscarPorId(String id) {
        return (Recurso) request(Protocol.RECURSO_BUSCAR, id);
    }

    // La categoria puede ser null: en ese caso filtra solo por descripcion.
    @SuppressWarnings("unchecked")
    public List<Recurso> recursosBuscarPorFiltros(Categoria categoria, String descripcion) {
        return (List<Recurso>) request(Protocol.RECURSO_FILTRAR, categoria, descripcion);
    }

    // Devuelve el recurso ya guardado.
    public Recurso recursoCrear(Recurso recurso) {
        return (Recurso) request(Protocol.RECURSO_CREAR, recurso);
    }

    public void recursoActualizar(Recurso recurso) {
        request(Protocol.RECURSO_ACTUALIZAR, recurso);
    }

    public void recursoEliminar(String id) {
        request(Protocol.RECURSO_ELIMINAR, id);
    }

    @SuppressWarnings("unchecked")
    public List<Recurso> recursosPorCategoria(String idCategoria) {
        return (List<Recurso>) request(Protocol.RECURSO_POR_CATEGORIA, idCategoria);
    }

    public String recursosTextoPorCategorias(List<Categoria> categorias) {
        return (String) request(Protocol.RECURSO_TEXTO_POR_CATEGORIAS, new ArrayList<>(categorias));
    }


    // ================= Alertas =================

    // Abre el canal asincrono. Debe llamarse DESPUES del login: el servidor
    // rechaza el canal si la conexion no tiene sesion.
    public void iniciarAlertas(Consumer<Reserva> alAlertar) {
        String sidActual = sid;
        if (sidActual == null) {
            throw new IllegalStateException("Debe iniciar sesión antes de abrir el canal de alertas.");
        }
        ReceptorNotificaciones nuevo = new ReceptorNotificaciones(host, port, sidActual, alAlertar);
        try {
            nuevo.iniciar();
        } catch (IOException e) {
            nuevo.close();
            throw new IllegalStateException("No se pudo abrir el canal de alertas: " + e.getMessage(), e);
        }
        synchronized (lockAlertas) {
            if (receptor != null) receptor.close();
            receptor = nuevo;
        }
    }

    public void detenerAlertas() {
        synchronized (lockAlertas) {
            if (receptor != null) {
                receptor.close();
                receptor = null;
            }
        }
    }














    public synchronized void logout() {
        if (socket == null) return;
        try { request(Protocol.AUTH_LOGOUT); }
        catch (IllegalStateException ignored) {
            // Cerrar el socket también revoca la sesión del servidor.
        } finally { close(); }
    }




















    private synchronized Object request(int operation, Object... parameters) {
        try {
            connect();
            output.writeInt(operation);
            for (Object parameter : parameters) output.writeObject(parameter);
            output.flush();
            output.reset();
            int status = input.readInt();
            Object result = input.readObject();
            if (status == Protocol.STATUS_ERROR) throw new RemoteException(String.valueOf(result));
            if (status != Protocol.STATUS_NO_ERROR) throw new IOException("Respuesta desconocida");
            return result;
        } catch (IOException | ClassNotFoundException e) {
            close();
            throw new IllegalStateException("No se pudo comunicar con el servidor en " + host + ":" + port
                    + ". Compruebe que el backend esté iniciado.", e);
        }
    }

    private void connect() throws IOException, ClassNotFoundException {
        if (socket != null) return;
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);
        socket.setSoTimeout(30_000);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        output.writeInt(Protocol.SYNC);
        output.flush();
        if (!(input.readObject() instanceof String saludo)) throw new IOException("Saludo inválido");sid = saludo;
    }

    @Override
    public synchronized void close() {
        detenerAlertas();
        if (socket != null) {
            try { socket.close(); } catch (IOException ignored) { }
        }
        socket = null;
        input = null;
        output = null;
    }

    public static class RemoteException extends IllegalStateException {
        public RemoteException(String message) { super(message); }
    }
}
