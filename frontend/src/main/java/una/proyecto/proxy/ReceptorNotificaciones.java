package una.proyecto.proxy;

import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicBoolean;


public class ReceptorNotificaciones implements AutoCloseable {

    private final String host;
    private final int port;
    private final String sid;
    private final Consumer<Reserva> alAlertar;
    private final Consumer<Exception> alDesconectar;
    private final int esperaSaludoMs;

    private volatile boolean activo;
    private volatile boolean cierreSolicitado;
    private final AtomicBoolean desconexionNotificada = new AtomicBoolean();
    private final Socket socket = new Socket();
    private ObjectInputStream input;

    public ReceptorNotificaciones(String host, int port, String sid, Consumer<Reserva> alAlertar) {
        this(host, port, sid, alAlertar, error -> { });
    }

    public ReceptorNotificaciones(String host, int port, String sid, Consumer<Reserva> alAlertar,
                                  Consumer<Exception> alDesconectar) {
        this(host, port, sid, alAlertar, alDesconectar, 5000);
    }

    ReceptorNotificaciones(String host, int port, String sid, Consumer<Reserva> alAlertar,
                           Consumer<Exception> alDesconectar, int esperaSaludoMs) {
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.alAlertar = alAlertar;
        this.alDesconectar = alDesconectar;
        this.esperaSaludoMs = esperaSaludoMs;
    }

    // Abre el canal, confirma con el servidor y deja un hilo escuchando.
    public void iniciar() throws IOException {
        try {
            socket.connect(new InetSocketAddress(host, port), 3000);
            socket.setSoTimeout(esperaSaludoMs);

            // Mismo orden que el canal de peticiones: primero output, flush, luego input.
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());

            output.writeInt(Protocol.ASYNC);
            output.writeObject(sid);
            output.flush();

            // Solo el saludo tiene limite de espera. Despues el hilo espera sin limite.
            if (input.readInt() != Protocol.STATUS_NO_ERROR) {
                throw new IOException("El servidor rechazó el canal de alertas.");
            }
            socket.setSoTimeout(0);

            activo = true;
            Thread hilo = new Thread(this::escuchar, "Receptor-Alertas");
            hilo.setDaemon(true);
            hilo.start();
        } catch (IOException | RuntimeException e) {
            close();
            throw e;
        }
    }

    private void escuchar() {
        try {
            while (activo) {
                int tipo = input.readInt();
                Object dato = input.readObject();
                if (tipo == Protocol.NOTIFICACION_RESERVA_CREADA && dato instanceof Reserva reserva) {
                    alAlertar.accept(reserva);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            notificarDesconexion(e);
        } finally {
            activo = false;
            cerrarSocket();
        }
    }

    @Override
    public void close() {
        cierreSolicitado = true;
        activo = false;
        cerrarSocket();
    }

    void interrumpir(Exception error) {
        try {
            if (activo) notificarDesconexion(error);
        } finally {
            close();
        }
    }

    private void notificarDesconexion(Exception error) {
        if (!cierreSolicitado && desconexionNotificada.compareAndSet(false, true)) {
            alDesconectar.accept(error);
        }
    }

    private void cerrarSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
            // Ya estaba cerrado.
        }
    }
}
