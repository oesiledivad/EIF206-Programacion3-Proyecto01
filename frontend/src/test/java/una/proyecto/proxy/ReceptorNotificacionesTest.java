package una.proyecto.proxy;

import org.junit.jupiter.api.Test;
import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ReceptorNotificacionesTest {
    @Test void limitaEsperaDeCabeceraYCierraElSocketFallido() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso(socket -> socket.getInputStream().readAllBytes());
             ReceptorNotificaciones receptor = receptor(servidor, new AtomicInteger(), 200)) {
            assertThrows(SocketTimeoutException.class, receptor::iniciar);
            servidor.esperarFin();
        }
    }

    @Test void limitaEsperaDeConfirmacionAsync() throws Exception {
        try (ServidorFalso servidor = new ServidorFalso(socket -> {
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); output.flush();
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            assertEquals(Protocol.ASYNC, input.readInt());
            assertEquals("sid-prueba", input.readObject());
            assertThrows(EOFException.class, input::readInt);
        }); ReceptorNotificaciones receptor = receptor(servidor, new AtomicInteger(), 200)) {
            assertThrows(SocketTimeoutException.class, receptor::iniciar);
            servidor.esperarFin();
        }
    }

    @Test void informaReservaYCorteInesperadoUnaSolaVez() throws Exception {
        BlockingQueue<Reserva> reservas = new LinkedBlockingQueue<>();
        CountDownLatch desconectado = new CountDownLatch(1);
        AtomicInteger avisos = new AtomicInteger();
        try (ServidorFalso servidor = new ServidorFalso(socket -> {
            ObjectOutputStream output = aceptar(socket);
            Reserva reserva = new Reserva(); reserva.setActividad("Reserva asincrona");
            output.writeInt(Protocol.NOTIFICACION_RESERVA_CREADA);
            output.writeObject(reserva); output.flush();
        }); ReceptorNotificaciones receptor = new ReceptorNotificaciones("127.0.0.1", servidor.puerto(),
                "sid-prueba", reservas::add, error -> {avisos.incrementAndGet();desconectado.countDown();})) {
            receptor.iniciar();
            Reserva reserva = reservas.poll(3, TimeUnit.SECONDS);
            assertNotNull(reserva);
            assertEquals("Reserva asincrona", reserva.getActividad());
            assertTrue(desconectado.await(3, TimeUnit.SECONDS));
            assertEquals(1, avisos.get());
            servidor.esperarFin();
        }
    }

    @Test void cierreIntencionalNoNotificaError() throws Exception {
        AtomicInteger avisos = new AtomicInteger();
        CountDownLatch error = new CountDownLatch(1);
        try (ServidorFalso servidor = new ServidorFalso(socket -> {
            aceptar(socket);
            assertEquals(-1, socket.getInputStream().read());
        }); ReceptorNotificaciones receptor = new ReceptorNotificaciones("127.0.0.1", servidor.puerto(),
                "sid-prueba", reserva -> {}, fallo -> {avisos.incrementAndGet();error.countDown();})) {
            receptor.iniciar(); receptor.close();
            servidor.esperarFin();
            assertFalse(error.await(300, TimeUnit.MILLISECONDS));
            assertEquals(0, avisos.get());
        }
    }

    @Test void errorDelCanalSincronoYDelAsyncNoDuplicanElAviso() throws Exception {
        AtomicInteger avisos = new AtomicInteger();
        try (ServidorFalso servidor = new ServidorFalso(socket -> {
            aceptar(socket);
            assertEquals(-1, socket.getInputStream().read());
        }); ReceptorNotificaciones receptor = receptor(servidor, avisos, 5000)) {
            receptor.iniciar();
            receptor.interrumpir(new IOException("Fallo del canal de peticiones"));
            receptor.interrumpir(new IOException("Segundo fallo"));
            servidor.esperarFin();
            assertEquals(1, avisos.get());
        }
    }

    @Test void cerrarDuranteElSaludoDesbloqueaLaInicializacion() throws Exception {
        CountDownLatch conectado = new CountDownLatch(1);
        ExecutorService cliente = Executors.newSingleThreadExecutor();
        AtomicInteger avisos = new AtomicInteger();
        try (ServidorFalso servidor = new ServidorFalso(socket -> {
            conectado.countDown(); socket.getInputStream().readAllBytes();
        }); ReceptorNotificaciones receptor = receptor(servidor, avisos, 5000)) {
            Future<?> inicio = cliente.submit(() -> {try {receptor.iniciar();}catch(IOException e){throw new UncheckedIOException(e);}});
            assertTrue(conectado.await(3, TimeUnit.SECONDS));
            receptor.close();
            assertThrows(ExecutionException.class, () -> inicio.get(3, TimeUnit.SECONDS));
            assertEquals(0, avisos.get());
            servidor.esperarFin();
        } finally {cliente.shutdownNow();}
    }

    private ReceptorNotificaciones receptor(ServidorFalso servidor, AtomicInteger avisos, int timeout) {
        return new ReceptorNotificaciones("127.0.0.1", servidor.puerto(), "sid-prueba",
                reserva -> {}, error -> avisos.incrementAndGet(), timeout);
    }

    private static ObjectOutputStream aceptar(Socket socket) throws Exception {
        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream()); output.flush();
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
        assertEquals(Protocol.ASYNC, input.readInt());
        assertEquals("sid-prueba", input.readObject());
        output.writeInt(Protocol.STATUS_NO_ERROR); output.flush();
        return output;
    }

    @FunctionalInterface private interface Accion {void ejecutar(Socket socket) throws Exception;}

    private static class ServidorFalso implements AutoCloseable {
        private final ServerSocket servidor;
        private final ExecutorService hilo = Executors.newSingleThreadExecutor();
        private final Future<?> resultado;
        private volatile Socket cliente;
        ServidorFalso(Accion accion) throws IOException {
            servidor = new ServerSocket(0);
            resultado = hilo.submit(() -> {
                try (Socket socket = servidor.accept()) {
                    cliente = socket; socket.setSoTimeout(5000); accion.ejecutar(socket);
                } catch(Exception e){throw new RuntimeException(e);}
            });
        }
        int puerto(){return servidor.getLocalPort();}
        void esperarFin() throws Exception {resultado.get(3, TimeUnit.SECONDS);}
        public void close() throws Exception {
            if(cliente!=null) cliente.close();
            servidor.close();hilo.shutdownNow();
            assertTrue(hilo.awaitTermination(3,TimeUnit.SECONDS));
        }
    }
}
