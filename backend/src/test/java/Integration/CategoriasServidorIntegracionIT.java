package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.RecursoDatos;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Administrador;
import una.proyecto.model.Categoria;
import una.proyecto.model.Funcionario;
import una.proyecto.model.Recurso;
import una.proyecto.protocolo.Protocol;
import una.proyecto.server.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración de Categorías por sockets:
 * Cliente → Server → Worker → CategoriaService → MySQL.
 * Usa usuarios, categorías (descripción "IT-SRV...") y recursos ("IT-SRV-...") propios.
 */
class CategoriasServidorIntegracionIT {

    private static final String ADMIN = "IT-ADMIN-CAT";
    private static final String FUNCIONARIO = "IT-FUNC-CAT";
    private static final String CLAVE = "clavePrueba";
    private static final String PREFIJO = "IT-SRV";

    private static Server server;
    private static Thread serverThread;
    private Cliente cliente;

    @BeforeAll
    static void iniciarServidor() throws Exception {
        server = new Server(0); // 0 = el sistema operativo escoge un puerto libre
        serverThread = new Thread(server::iniciar, "Servidor-Pruebas-Categorias");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @AfterAll
    static void cerrarServidor() throws Exception {
        server.close();
        serverThread.join(5000);
    }

    @BeforeEach
    void setUp() throws Exception {
        limpiar();
        UsuarioDatos datos = new UsuarioDatos();
        Administrador admin = new Administrador(ADMIN, "ADMIN", "Administrador de prueba");
        admin.setPassword(CLAVE);
        assertTrue(datos.addUser(admin));
        Funcionario funcionario = new Funcionario(FUNCIONARIO, "FUNCIONARIO", "Funcionario de prueba", "00000000");
        funcionario.setPassword(CLAVE);
        assertTrue(datos.addUser(funcionario));
        cliente = new Cliente();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (cliente != null) cliente.close();
        limpiar();
    }

    // Hijos primero: recursos -> categorias; funcionarios -> usuarios.
    private void limpiar() throws Exception {
        try (Connection cnx = Database.instance().openConnection()) {
            ejecutar(cnx, "DELETE FROM recursos WHERE id_recurso LIKE ?", PREFIJO + "-%");
            ejecutar(cnx, "DELETE FROM categorias WHERE descripcion LIKE ?", PREFIJO + "%");
            ejecutar(cnx, "DELETE FROM funcionarios WHERE id_usuario IN (?, ?)", ADMIN, FUNCIONARIO);
            ejecutar(cnx, "DELETE FROM usuarios WHERE id_usuario IN (?, ?)", ADMIN, FUNCIONARIO);
        }
    }

    private void ejecutar(Connection cnx, String sql, String... parametros) throws Exception {
        try (PreparedStatement stm = cnx.prepareStatement(sql)) {
            for (int i = 0; i < parametros.length; i++) stm.setString(i + 1, parametros[i]);
            stm.executeUpdate();
        }
    }

    private Categoria crearComoAdmin(String descripcion) throws Exception {
        Mensaje respuesta = cliente.enviar(Protocol.CATEGORIA_CREAR, new Categoria(null, descripcion));
        assertEquals(Protocol.STATUS_NO_ERROR, respuesta.codigo(), String.valueOf(respuesta.objeto()));
        return (Categoria) respuesta.objeto();
    }

    @SuppressWarnings("unchecked")
    private List<Categoria> listar() throws Exception {
        return (List<Categoria>) cliente.enviar(Protocol.CATEGORIA_LISTAR).objeto();
    }

    // ---------- crear / listar / buscar ----------

    @Test
    void admin_crea_categoria_y_recibe_el_id_generado_por_mysql() throws Exception {
        cliente.login(ADMIN, CLAVE);

        Categoria creada = crearComoAdmin(PREFIJO + " Sala");

        assertNotNull(creada.getId());
        assertTrue(creada.getId().matches("CAT-\\d{6}"), "Id recibido: " + creada.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    void funcionario_puede_listar_y_buscar_por_descripcion() throws Exception {
        cliente.login(ADMIN, CLAVE);
        Categoria creada = crearComoAdmin(PREFIJO + " Proyector");
        cliente.login(FUNCIONARIO, CLAVE);

        assertTrue(listar().stream().anyMatch(c -> c.getId().equals(creada.getId())));
        List<Categoria> encontradas = (List<Categoria>) cliente
                .enviar(Protocol.CATEGORIA_BUSCAR_DESCRIPCION, "it-srv proy").objeto();
        assertEquals(1, encontradas.size());
    }

    // ---------- actualizar / eliminar ----------

    @Test
    void admin_actualiza_la_descripcion() throws Exception {
        cliente.login(ADMIN, CLAVE);
        Categoria creada = crearComoAdmin(PREFIJO + " Sala");

        Mensaje respuesta = cliente.enviar(Protocol.CATEGORIA_ACTUALIZAR,
                new Categoria(creada.getId(), PREFIJO + " Sala grande"));

        assertEquals(Protocol.STATUS_NO_ERROR, respuesta.codigo(), String.valueOf(respuesta.objeto()));
        assertTrue(listar().stream().anyMatch(c -> c.getId().equals(creada.getId())
                && c.getDescripcion().equals(PREFIJO + " Sala grande")));
    }

    @Test
    void admin_elimina_y_ya_no_aparece_en_la_lista() throws Exception {
        cliente.login(ADMIN, CLAVE);
        Categoria creada = crearComoAdmin(PREFIJO + " Sala");

        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.CATEGORIA_ELIMINAR, creada.getId()).codigo());

        assertTrue(listar().stream().noneMatch(c -> c.getId().equals(creada.getId())));
    }

    // ---------- permisos y errores ----------

    @Test
    void funcionario_no_puede_crear_actualizar_ni_eliminar() throws Exception {
        cliente.login(ADMIN, CLAVE);
        Categoria creada = crearComoAdmin(PREFIJO + " Sala");
        cliente.login(FUNCIONARIO, CLAVE);

        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CATEGORIA_CREAR, new Categoria(null, PREFIJO + " Otra")).codigo());
        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CATEGORIA_ACTUALIZAR, new Categoria(creada.getId(), PREFIJO + " X")).codigo());
        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CATEGORIA_ELIMINAR, creada.getId()).codigo());
        assertTrue(listar().stream().anyMatch(c -> c.getId().equals(creada.getId())));
    }

    @Test
    void sin_login_no_lista_pero_la_conexion_sigue_sirviendo() throws Exception {
        assertEquals(Protocol.STATUS_ERROR,
                cliente.enviar(Protocol.CATEGORIA_CREAR, new Categoria(null, PREFIJO + " Sala")).codigo());
        assertEquals(Protocol.STATUS_ERROR, cliente.enviar(Protocol.CATEGORIA_LISTAR).codigo());

        // Si el Worker no hubiera leído la Categoria antes de validar la sesión, este login fallaría.
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.login(ADMIN, CLAVE).codigo());
        assertEquals(Protocol.STATUS_NO_ERROR, cliente.enviar(Protocol.CATEGORIA_LISTAR).codigo());
    }

    @Test
    void error_de_negocio_llega_como_mensaje_al_cliente() throws Exception {
        cliente.login(ADMIN, CLAVE);
        crearComoAdmin(PREFIJO + " Sala");

        Mensaje respuesta = cliente.enviar(Protocol.CATEGORIA_CREAR, new Categoria(null, PREFIJO + " sala"));

        assertEquals(Protocol.STATUS_ERROR, respuesta.codigo());
        assertTrue(String.valueOf(respuesta.objeto()).contains("Ya existe"), "Mensaje: " + respuesta.objeto());
    }

    // ---------- consultas con recursos ----------

    @Test
    @SuppressWarnings("unchecked")
    void separa_categorias_con_y_sin_recursos() throws Exception {
        cliente.login(ADMIN, CLAVE);
        Categoria conRecurso = crearComoAdmin(PREFIJO + " Laptop");
        Categoria sinRecurso = crearComoAdmin(PREFIJO + " Pizarra");
        new RecursoDatos().crear(new Recurso(PREFIJO + "-001", conRecurso.getId(), "Laptop de prueba"));
        List<Categoria> escogidas = new ArrayList<>(List.of(conRecurso, sinRecurso));

        List<Categoria> sin = (List<Categoria>) cliente.enviar(Protocol.CATEGORIA_SIN_RECURSOS, escogidas).objeto();
        List<Categoria> con = (List<Categoria>) cliente.enviar(Protocol.CATEGORIA_CON_RECURSOS, escogidas).objeto();

        assertEquals(List.of(sinRecurso.getId()), sin.stream().map(Categoria::getId).toList());
        assertEquals(List.of(conRecurso.getId()), con.stream().map(Categoria::getId).toList());
    }

    // ================= Cliente mínimo del protocolo =================

    private record Mensaje(int codigo, Object objeto) { }

    private static class Cliente implements AutoCloseable {
        final Socket socket;
        final ObjectOutputStream output;
        final ObjectInputStream input;

        Cliente() throws Exception {
            socket = new Socket("localhost", server.getPuerto());
            socket.setSoTimeout(5000);
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            output.writeInt(Protocol.SYNC);
            output.flush();
            input.readObject(); // saludo: el sid de la sesión
        }

        Mensaje login(String id, String clave) throws Exception {
            return enviar(Protocol.AUTH_LOGIN, id, clave);
        }

        Mensaje enviar(int operacion, Object... parametros) throws Exception {
            output.writeInt(operacion);
            for (Object p : parametros) output.writeObject(p);
            output.flush();
            output.reset();
            return new Mensaje(input.readInt(), input.readObject());
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}