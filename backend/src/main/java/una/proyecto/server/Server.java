package una.proyecto.server;

import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;
import una.proyecto.service.Servicios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Espera conexiones de los frontends, les asigna un Worker
// y reparte las notificaciones entre los clientes conectados.
public class Server {

    private final ServerSocket serverSocket;
    private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
    private final Servicios servicios;
    private int contadorSesiones = 0;

    public Server(int puerto) throws IOException {
        this.serverSocket = new ServerSocket(puerto);
        this.servicios = new Servicios();
    }

    // ================= Ciclo principal =================

    public void iniciar() {
        System.out.println("Servidor escuchando en el puerto " + serverSocket.getLocalPort());
        while (true) {
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Error al aceptar un cliente: " + e.getMessage());
                continue;
            }
            try {
                atenderConexionNueva(socket);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Conexion rechazada (" + e.getClass().getSimpleName() + ")");
                cerrar(socket);
            }
        }
    }
    public int getPuerto() {
        return serverSocket.getLocalPort();
    }
    // ================= Saludo inicial =================

    // El cliente indica que tipo de conexion abre: SYNC (peticiones) o ASYNC (alertas).
    private void atenderConexionNueva(Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
        os.flush();
        ObjectInputStream is = new ObjectInputStream(socket.getInputStream());

        int tipo = is.readInt();
        switch (tipo) {
            case Protocol.SYNC -> registrarSync(socket, os, is);
            case Protocol.ASYNC -> registrarAsync(socket, os, is);
            default -> {
                System.err.println("Tipo de conexion desconocido: " + tipo);
                cerrar(socket);
            }
        }
    }

    // Conexion de peticiones: crea un Worker nuevo para este cliente.
    private void registrarSync(Socket socket, ObjectOutputStream os, ObjectInputStream is)
            throws IOException {
        String sid = generarSid();
        os.writeObject(sid);
        os.flush();

        Worker worker = new Worker(this, servicios, socket, os, is, sid);
        workers.add(worker);
        worker.start();
        System.out.println("Cliente conectado: " + sid + " (activos: " + workers.size() + ")");
    }

    // Conexion de alertas: el cliente presenta su sid para unirla a su Worker.
    private void registrarAsync(Socket socket, ObjectOutputStream os, ObjectInputStream is)
            throws IOException, ClassNotFoundException {
        String sid = (String) is.readObject();
        Worker worker = buscarWorker(sid);
        if (worker == null) {
            System.err.println("Canal de alertas rechazado: no existe la sesion " + sid);
            cerrar(socket);
            return;
        }

        worker.setCanalAsync(socket, os, is);
        System.out.println("Canal de alertas listo: " + sid);
    }

    // ================= Lista de clientes =================

    private Worker buscarWorker(String sid) {
        synchronized (workers) {
            for (Worker worker : workers) {
                if (worker.getSid().equals(sid)) {
                    return worker;
                }
            }
        }
        return null;
    }

    // Lo llama el Worker cuando su cliente se desconecta.
    void remover(Worker worker) {
        workers.remove(worker);
        System.out.println("Cliente desconectado: " + worker.getSid()
                + " (activos: " + workers.size() + ")");
    }

    // ================= Notificaciones =================

    // Envia la reserva nueva a todos los clientes conectados, excepto al que la creo.
    void notificarReservaCreada(Worker origen, Reserva reserva) {
        List<Worker> destinos;
        synchronized (workers) {
            destinos = new ArrayList<>(workers);
        }
        for (Worker worker : destinos) {
            if (worker != origen) {
                worker.enviarNotificacion(Protocol.NOTIFICACION_RESERVA_CREADA, reserva);
            }
        }
    }

    // ================= Auxiliares =================

    private synchronized String generarSid() {
        contadorSesiones++;
        return "S-" + contadorSesiones;
    }

    private void cerrar(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            // Ya estaba cerrado.
        }
    }
}