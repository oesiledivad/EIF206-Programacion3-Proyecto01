package una.proyecto.server;

import una.proyecto.model.Reserva;
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
                reservaService.save(reserva);
                server.notificarReservaCreada(this, reserva);
                return reserva;
            }
            case Protocol.RESERVA_LISTAR: {
                return new ArrayList<Reserva>(reservaService.obtenerTodasReservas());
            }
            case Protocol.RESERVA_BUSCAR: {
                String id = (String) is.readObject();
                return reservaService.buscarPorId(id);
            }
            case Protocol.RESERVA_CANCELAR: {
                String id = (String) is.readObject();
                reservaService.delete(id);
                return null;
            }
            default:
                throw new IllegalArgumentException("Operacion de reserva desconocida: " + operacion);
        }
    }

    // ================= Alertas =================

    // Lo llama el Server cuando el cliente abre su canal de alertas.
    public synchronized void setCanalAsync(Socket socketAsync, ObjectOutputStream osAsync,
                                           ObjectInputStream isAsync) {
        this.socketAsync = socketAsync;
        this.osAsync = osAsync;
    }

    // Lo llaman los hilos de OTROS Workers (via Server); por eso es synchronized.
    public synchronized void enviarNotificacion(int tipo, Object dato) {
        if (osAsync == null) {
            return; // Este cliente todavia no abrio su canal de alertas.
        }
        try {
            osAsync.writeInt(tipo);
            osAsync.writeObject(dato);
            osAsync.flush();
            osAsync.reset();
        } catch (IOException e) {
            System.err.println("[" + sid + "] No se pudo enviar la alerta: " + e.getMessage());
            cerrarCanalAsync();
        }
    }

    // ================= Cierre =================

    private void cerrar() {
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