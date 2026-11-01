package una.proyecto.proxy;

import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;


public class ReceptorNotificaciones implements AutoCloseable {

    private final String host;
    private final int port;
    private final String sid;
    private final Consumer<Reserva> alAlertar;

    private volatile boolean activo;
    private Socket socket;
    private ObjectInputStream input;

    public ReceptorNotificaciones(String host, int port, String sid, Consumer<Reserva> alAlertar) {
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.alAlertar = alAlertar;
    }

    // Abre el canal, confirma con el servidor y deja un hilo escuchando.
    public void iniciar() throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);

        // Mismo orden que el canal de peticiones: primero output, flush, luego input.
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

        output.writeInt(Protocol.ASYNC);
        output.writeObject(sid);
        output.flush();

        // Solo el saludo tiene limite de espera. Despues el hilo espera sin limite.
        socket.setSoTimeout(5000);
        if (input.readInt() != Protocol.STATUS_NO_ERROR) {
            throw new IOException("El servidor rechazó el canal de alertas.");
        }
        socket.setSoTimeout(0);

        activo = true;
        Thread hilo = new Thread(this::escuchar, "Receptor-Alertas");
        hilo.setDaemon(true);
        hilo.start();
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
        } finally {
            cerrarSocket();
        }
    }

    @Override
    public void close() {
        activo = false;
        cerrarSocket();
    }

    private void cerrarSocket() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
            // Ya estaba cerrado.
        }
    }
}