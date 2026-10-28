package una.proyecto.proxy;

import una.proyecto.model.DashboardData;
import una.proyecto.model.LoginService;
import una.proyecto.model.Usuario;
import una.proyecto.model.Recurso;
import una.proyecto.model.Reserva;
import java.time.LocalDate;
import una.proyecto.protocolo.Protocol;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import una.proyecto.model.Categoria;
import java.util.ArrayList;
import java.util.List;

public class ServiceProxy implements LoginService, AutoCloseable {
    private static final ServiceProxy INSTANCE = new ServiceProxy();
    private final String host;
    private final int port;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

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
        socket.setSoTimeout(10_000);
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());
        output.writeInt(Protocol.SYNC);
        output.flush();
        if (!(input.readObject() instanceof String)) throw new IOException("Saludo inválido");
    }

    @Override
    public synchronized void close() {
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
