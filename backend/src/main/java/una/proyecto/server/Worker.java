package una.proyecto.server;

import una.proyecto.model.Reserva;
import una.proyecto.model.Usuario;
import una.proyecto.model.DashboardData;
import una.proyecto.model.Funcionario;
import java.time.LocalDate;
import java.time.LocalTime;
import una.proyecto.protocolo.Protocol;
import una.proyecto.service.ReservaService;
import una.proyecto.service.Servicios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

// Atiende a un cliente en su propio hilo.
// Recibe sus peticiones por el canal sincrono y le envia alertas por el asincrono.
public class Worker {

    private final Server server;
    private final Servicios servicios;
    private final String sid;
    private volatile Usuario usuario;

    // Canal sincrono: peticiones y respuestas
    private final Socket socket;
    private final ObjectOutputStream os;
    private final ObjectInputStream is;

    // Canal asincrono: alertas. Es null hasta que el cliente lo abre.
    private Socket socketAsync;
    private ObjectOutputStream osAsync;

    public Worker(Server server, Servicios servicios, Socket socket,
                  ObjectOutputStream os, ObjectInputStream is, String sid) {
        this.server = server;
        this.servicios = servicios;
        this.socket = socket;
        this.os = os;
        this.is = is;
        this.sid = sid;
    }

    // ================= Ciclo de vida =================

    public void start() {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                escuchar();
            }
        }, "Worker-" + sid);
        hilo.start();
    }
    // Lo ejecuta el hilo creado en start().
    private void escuchar() {
        boolean continuar = true;
        while (continuar) {
            try {
                int operacion = is.readInt();
                continuar = atender(operacion);
            } catch (IOException e) {
                continuar = false; // El cliente se desconecto sin avisar.
            }
        }
        cerrar();
    }

    // Ejecuta la operacion y responde siempre con: estado + objeto.
    // Devuelve false cuando el cliente pide desconectarse.
    private boolean atender(int operacion) throws IOException {
        if (operacion == Protocol.DISCONNECT) {
            return false;
        }
        Object resultado;
        int estado;
        try {
            resultado = ejecutar(operacion);
            estado = Protocol.STATUS_NO_ERROR;
        } catch (IOException e) {
            throw e; // La conexion se rompio: no se puede responder.
        } catch (Exception e) {
            resultado = mensajeDe(e);
            estado = Protocol.STATUS_ERROR;
            System.err.println("[" + sid + "] Error en la operacion " + operacion + ": " + resultado);
        }
        os.writeInt(estado);
        os.writeObject(resultado);
        os.flush();
        os.reset();
        return true;
    }

    // Reparte cada operacion al metodo de su entidad.
    private Object ejecutar(int operacion) throws IOException, ClassNotFoundException {
        switch (operacion) {
            case Protocol.AUTH_LOGIN: {
                Object id = is.readObject();
                Object clave = is.readObject();
                logout();
                if (!(id instanceof String) || !(clave instanceof String)) {
                    throw new IllegalArgumentException("Credenciales inválidas.");
                }
                usuario = servicios.getAuthService().authenticate((String) id, (String) clave);
                if (usuario == null) throw new IllegalArgumentException("Credenciales incorrectas.");
                return usuario;
            }
            case Protocol.AUTH_LOGOUT:
                logout();
                return null;
            case Protocol.AUTH_CAMBIAR_CLAVE: {
                Object actual = is.readObject();
                Object nueva = is.readObject();
                Usuario sesion = requireSession();
                if (!(actual instanceof String) || !(nueva instanceof String)) {
                    throw new IllegalArgumentException("Contraseñas inválidas.");
                }
                servicios.getUsuarioLogic().cambiarContrasena(sesion.getId(), (String) actual, (String) nueva);
                return null;
            }
            case Protocol.DASHBOARD_RESUMEN: {
                Usuario sesion = requireSession();
                boolean admin = "ADMIN".equals(sesion.getRole());
                var reservas = admin ? servicios.getReservaService().obtenerTodasReservas()
                        : servicios.getReservaService().obtenerReservasPorFuncionario(sesion.getId());
                var funcionarios = admin ? servicios.getFuncionarioService().getAllEmployees()
                        : java.util.List.<Funcionario>of();
                for (Reserva reserva : reservas) {
                    funcionarios.stream().filter(f -> f.getId().equals(reserva.getIdFuncionario()))
                            .findFirst().ifPresent(reserva::setFuncionarioReserva);
                }
                return new DashboardData(reservas, servicios.getRecursoService().obtenerTodosRecursos().size(),
                        funcionarios.size());
            }
            case Protocol.RESERVA_CREAR:
            case Protocol.RESERVA_LISTAR:
            case Protocol.RESERVA_BUSCAR:
            case Protocol.RESERVA_CANCELAR:
                return ejecutarReserva(operacion);

            // Recursos:
            // case Protocol.RECURSO_CREAR:
            // case Protocol.RECURSO_LISTAR:
            //     return ejecutarRecurso(operacion);

            // Categoria:
            //            // case Protocol.CATEGORIA_CREAR:
            //            // case Protocol.CATEGORIA_LISTAR:
            //            //     return ejecutarCATEGORIA(operacion);
            default:
                throw new IllegalArgumentException("Operacion desconocida: " + operacion);
        }
    }

    // ================= Reservas =================

    private Object ejecutarReserva(int operacion) throws IOException, ClassNotFoundException {
        ReservaService reservaService = servicios.getReservaService();
        switch (operacion) {
            case Protocol.RESERVA_CREAR: {
                Reserva reserva = (Reserva) is.readObject();
                Usuario sesion = requireSession();
                requireFuncionario(sesion);
                if (reserva == null) throw new IllegalArgumentException("La reserva es requerida.");
                if (reserva.getIdFuncionario() != null
                        && !sesion.getId().equalsIgnoreCase(reserva.getIdFuncionario())) {
                    throw new IllegalArgumentException("No puede reservar para otro funcionario.");
                }
                reserva.setIdFuncionario(sesion.getId());
                reservaService.verificarHorasService(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin());
                reservaService.save(reserva);
                server.notificarReservaCreada(this, reserva);
                return reserva;
            }
            case Protocol.RESERVA_LISTAR: {
                Usuario sesion = requireSession();
                return new ArrayList<Reserva>("ADMIN".equals(sesion.getRole())
                        ? reservaService.obtenerTodasReservas()
                        : reservaService.obtenerReservasPorFuncionario(sesion.getId()));
            }
            case Protocol.RESERVA_BUSCAR: {
                String id = (String) is.readObject();
                Usuario sesion = requireSession();
                Reserva reserva = reservaService.buscarPorId(id);
                if (reserva != null && !"ADMIN".equals(sesion.getRole())) requirePropietario(sesion, reserva);
                return reserva;
            }
            case Protocol.RESERVA_CANCELAR: {
                String id = (String) is.readObject();
                Usuario sesion = requireSession();
                requireFuncionario(sesion);
                Reserva reserva = reservaService.buscarPorId(id);
                if (reserva == null) throw new IllegalArgumentException("La reserva no existe.");
                requirePropietario(sesion, reserva);
                boolean futura = reserva.getFecha().isAfter(LocalDate.now())
                        || (reserva.getFecha().equals(LocalDate.now()) && reserva.getHoraInicio().isAfter(LocalTime.now()));
                if (!futura) throw new IllegalArgumentException("Solo puede cancelar reservas futuras.");
                reservaService.delete(id);
                return null;
            }
            default:
                throw new IllegalArgumentException("Operacion de reserva desconocida: " + operacion);
        }
    }

    private synchronized Usuario requireSession() {
        Usuario sesion = usuario;
        Usuario vigente = sesion == null ? null : servicios.getAuthService().activeUser(sesion.getId());
        if (vigente == null) {
            logout();
            throw new IllegalStateException("Debe iniciar sesión con un usuario activo.");
        }
        usuario = vigente;
        return vigente;
    }

    private void requireFuncionario(Usuario sesion) {
        if (!"FUNCIONARIO".equals(sesion.getRole())) {
            throw new IllegalArgumentException("Esta operación requiere un funcionario.");
        }
    }

    private void requirePropietario(Usuario sesion, Reserva reserva) {
        if (!sesion.getId().equalsIgnoreCase(reserva.getIdFuncionario())) {
            throw new IllegalArgumentException("La reserva pertenece a otro funcionario.");
        }
    }

    private synchronized void logout() {
        usuario = null;
        cerrarCanalAsync();
    }

    String getUsuarioId() {
        Usuario sesion = usuario;
        return sesion == null ? null : sesion.getId();
    }

    // ================= Alertas =================

    // Lo llama el Server cuando el cliente abre su canal de alertas.
    public synchronized void setCanalAsync(Socket socketAsync, ObjectOutputStream osAsync,
                                           ObjectInputStream isAsync) throws IOException {
        try {
            requireSession();
        } catch (IllegalStateException e) {
            throw new IOException("El canal de alertas requiere una sesión activa.", e);
        }
        cerrarCanalAsync();
        osAsync.writeInt(Protocol.STATUS_NO_ERROR);
        osAsync.flush();
        this.socketAsync = socketAsync;
        this.osAsync = osAsync;
    }

    // Lo llaman los hilos de OTROS Workers (via Server); por eso es synchronized.
    public synchronized void enviarNotificacion(int tipo, Object dato) {
        if (osAsync == null || usuario == null) {
            return; // Este cliente todavia no abrio su canal de alertas.
        }
        try {
            requireSession();
            osAsync.writeInt(tipo);
            osAsync.writeObject(dato);
            osAsync.flush();
            osAsync.reset();
        } catch (IOException | IllegalStateException e) {
            System.err.println("[" + sid + "] No se pudo enviar la alerta: " + e.getMessage());
            cerrarCanalAsync();
        }
    }

    // ================= Cierre =================

    synchronized void cerrar() {
        usuario = null;
        server.remover(this);
        cerrarCanalAsync();
        try {
            socket.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
    }

    private synchronized void cerrarCanalAsync() {
        if (socketAsync != null) {
            try {
                socketAsync.close();
            } catch (IOException e) {
                // Ya estaba cerrado.
            }
        }
        socketAsync = null;
        osAsync = null;
    }

    // ================= Auxiliares =================

    private String mensajeDe(Exception e) {
        String mensaje = e.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return "Error inesperado en el servidor (" + e.getClass().getSimpleName() + ")";
        }
        return mensaje;
    }

    public String getSid() {
        return sid;
    }
}
