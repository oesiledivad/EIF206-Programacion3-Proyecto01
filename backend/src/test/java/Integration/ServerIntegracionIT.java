package Integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import una.proyecto.datos.Database;
import una.proyecto.model.Reserva;
import una.proyecto.protocolo.Protocol;
import una.proyecto.server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Prueba las peticiones del backend como las haria cualquier frontend:
// un cliente que habla el protocolo directamente por un socket.
public class ServerIntegracionIT {

    private static final String FUNCIONARIO_PRUEBA = "IT-FUNC-SRV";
    private static final int OPERACION_INEXISTENTE = 12345;

    private static int puerto;

    private ClientePrueba cliente;

    // ================= Preparacion =================

    // Levanta un servidor real una sola vez, en un hilo aparte y en un puerto libre.
    @BeforeAll
    static void iniciarServidor() throws Exception {
        final Server server = new Server(0);
        puerto = server.getPuerto();
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                server.iniciar();
            }
        }, "Servidor-Pruebas");
        hilo.setDaemon(true);
        hilo.start();
    }

    @BeforeEach
    void setUp() throws Exception {
        limpiarDatosDePrueba();
        ejecutarSql("insert into usuarios (id_usuario, nombre, clave_hash, rol) values (?, ?, ?, ?)",
                FUNCIONARIO_PRUEBA, "Funcionario de prueba", "sin-clave", "FUNCIONARIO");
        ejecutarSql("insert into funcionarios (id_usuario, telefono) values (?, ?)",
                FUNCIONARIO_PRUEBA, "00000000");
        cliente = new ClientePrueba(puerto);
    }

    @AfterEach
    void tearDown() throws Exception {
        cliente.cerrar();
        limpiarDatosDePrueba();
    }

    // ================= Peticiones =================

    @Test
    void listar_reservas_responde_sin_error_y_con_una_lista() throws Exception {
        Mensaje respuesta = cliente.enviar(Protocol.RESERVA_LISTAR);

        assertEquals(Protocol.STATUS_NO_ERROR, respuesta.codigo, "Mensaje: " + respuesta.objeto);
        assertTrue(respuesta.objeto instanceof List, "Se recibio: " + respuesta.objeto);
    }

    @Test
    void crear_reserva_valida_devuelve_la_reserva_con_su_id() throws Exception {
        Mensaje respuesta = cliente.enviar(Protocol.RESERVA_CREAR, reservaDePrueba());

        assertEquals(Protocol.STATUS_NO_ERROR, respuesta.codigo, "Mensaje: " + respuesta.objeto);
        Reserva creada = (Reserva) respuesta.objeto;
        assertNotNull(creada.getId());
        assertTrue(creada.getId().startsWith("RES-"), "Id recibido: " + creada.getId());
    }

    @Test
    void crear_reserva_invalida_devuelve_error_con_el_mensaje_real() throws Exception {
        Reserva reserva = reservaDePrueba();
        reserva.setIdFuncionario("IT-NO-EXISTE");

        Mensaje respuesta = cliente.enviar(Protocol.RESERVA_CREAR, reserva);

        assertEquals(Protocol.STATUS_ERROR, respuesta.codigo);
        assertTrue(((String) respuesta.objeto).contains("no existe"), "Mensaje: " + respuesta.objeto);
    }

    @Test
    void operacion_desconocida_devuelve_error_y_no_bloquea_al_cliente() throws Exception {
        Mensaje respuesta = cliente.enviar(OPERACION_INEXISTENTE);
        assertEquals(Protocol.STATUS_ERROR, respuesta.codigo);

        // El mismo cliente puede seguir trabajando despues del error.
        Mensaje siguiente = cliente.enviar(Protocol.RESERVA_LISTAR);
        assertEquals(Protocol.STATUS_NO_ERROR, siguiente.codigo);
    }

    // ================= Auxiliares de la prueba =================

    private Reserva reservaDePrueba() {
        Reserva reserva = new Reserva();
        reserva.setIdFuncionario(FUNCIONARIO_PRUEBA);
        reserva.setActividad("Prueba de servidor");
        reserva.setFecha(LocalDate.now().plusDays(7));
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        return reserva;
    }

    // Borra en orden inverso a las llaves foraneas: reservas, funcionario, usuario.
    private void limpiarDatosDePrueba() throws Exception {
        ejecutarSql("delete from reserva_recurso where id_reserva in "
                + "(select id_reserva from reservas where id_funcionario = ?)", FUNCIONARIO_PRUEBA);
        ejecutarSql("delete from reservas where id_funcionario = ?", FUNCIONARIO_PRUEBA);
        ejecutarSql("delete from funcionarios where id_usuario = ?", FUNCIONARIO_PRUEBA);
        ejecutarSql("delete from usuarios where id_usuario = ?", FUNCIONARIO_PRUEBA);
    }

    private void ejecutarSql(String sql, String... parametros) throws Exception {
        try (PreparedStatement stm = Database.instance().prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) {
                stm.setString(i + 1, parametros[i]);
            }
            Database.instance().executeUpdate(stm);
        }
    }

    // ================= Cliente falso =================

    // Lo que responde el servidor: un estado y un objeto (resultado o mensaje de error).
    private static class Mensaje {
        int codigo;
        Object objeto;
    }

    // Un "frontend" minimo: se conecta, saluda con SYNC y envia peticiones.
    private static class ClientePrueba {
        private final Socket socket;
        private final ObjectOutputStream os;
        private final ObjectInputStream is;

        ClientePrueba(int puerto) throws Exception {
            socket = new Socket("localhost", puerto);
            socket.setSoTimeout(5000); // Si el servidor no responde, la prueba falla en vez de colgarse.
            os = new ObjectOutputStream(socket.getOutputStream());
            os.flush();
            is = new ObjectInputStream(socket.getInputStream());

            os.writeInt(Protocol.SYNC);
            os.flush();
            is.readObject(); // sid: esta prueba no lo usa
        }

        Mensaje enviar(int operacion, Object... parametros) throws Exception {
            os.writeInt(operacion);
            for (Object parametro : parametros) {
                os.writeObject(parametro);
            }
            os.flush();

            Mensaje respuesta = new Mensaje();
            respuesta.codigo = is.readInt();
            respuesta.objeto = is.readObject();
            return respuesta;
        }

        void cerrar() {
            try {
                os.writeInt(Protocol.DISCONNECT);
                os.flush();
                socket.close();
            } catch (IOException e) {
                // Ya estaba cerrado.
            }
        }
    }
}