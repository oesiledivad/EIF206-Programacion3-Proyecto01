package una.proyecto.server;

import una.proyecto.protocolo.Protocol;

import java.io.IOException;

// Punto de arranque del backend: crea el servidor y lo pone a escuchar.
public class BackendApp {

    public static void main(String[] args) {
        Server server;
        try {
            server = new Server(Protocol.PORT);
        } catch (IOException e) {
            System.err.println("No se pudo abrir el puerto " + Protocol.PORT
                    + ". Puede que otro servidor ya este corriendo. Detalle: " + e.getMessage());
            return;
        } catch (RuntimeException e) {
            System.err.println("No se pudieron iniciar los servicios del backend: " + e.getMessage());
            return;
        }
        server.iniciar();
    }
}