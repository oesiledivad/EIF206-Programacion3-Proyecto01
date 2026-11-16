package una.proyecto.server;

import una.proyecto.protocolo.Protocol;

import java.io.IOException;

// Punto de arranque del backend: crea el servidor y lo pone a escuchar.
public class BackendApp {

    public static void main(String[] args) {
        String configuredPort = System.getenv("SERVER_PORT");
        int port = configuredPort == null ? Protocol.PORT : Integer.parseInt(configuredPort);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("SERVER_PORT debe estar entre 1 y 65535");
        }
        Server server;
        try {
            server = new Server(port);
        } catch (IOException e) {
            System.err.println("No se pudo abrir el puerto " + port
                    + ". Puede que otro servidor ya este corriendo. Detalle: " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            System.err.println("No se pudieron iniciar los servicios del backend: " + e.getMessage());
            return;
        }
        server.iniciar();
    }
}
