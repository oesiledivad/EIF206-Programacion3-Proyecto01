package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.ReservaDatos;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.*;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class RecursosReservasServidorIntegracionIT {
    private static final String ADMIN = "IT-ADMIN-RR";
    private static final String FUNCIONARIO = "IT-FUNC-RR";
    private static final String OTRO = "IT-OTRO-RR";
    private static final String CLAVE = "clavePrueba";
    private static final String RECURSO = "IT-RR-RECURSO";
    private static Server server;
    private static Thread servidor;
    private ServiceProxy proxy;
    private Categoria categoria;

    @BeforeAll static void iniciar() throws Exception {
        server = new Server(0);
        servidor = new Thread(server::iniciar, "Servidor-Recursos-Reservas-IT");
        servidor.setDaemon(true);
        servidor.start();
    }

    @AfterAll static void cerrar() throws Exception {
        server.close();
        servidor.join(5000);
        assertFalse(servidor.isAlive());
    }

    @BeforeEach void preparar() throws Exception {
        limpiar();
        UsuarioDatos usuarios = new UsuarioDatos();
        Administrador admin = new Administrador(ADMIN, "ADMIN", "Administrador RR");
        admin.setPassword(CLAVE);
        assertTrue(usuarios.addUser(admin));
        for (String id : List.of(FUNCIONARIO, OTRO)) {
            Funcionario funcionario = new Funcionario(id, "FUNCIONARIO", "Funcionario RR", "00000000");
            funcionario.setPassword(CLAVE);
            assertTrue(usuarios.addUser(funcionario));
        }
        proxy = new ServiceProxy("127.0.0.1", server.getPuerto());
        assertNotNull(proxy.authenticate(ADMIN, CLAVE));
        categoria = proxy.categoriaCrear(new Categoria(null, "IT-RR-Sala"));
        proxy.recursoCrear(new Recurso(RECURSO, categoria.getId(), "Sala de prueba RR"));
    }

    @AfterEach void limpiar() throws Exception {
        if (proxy != null) proxy.close();
        try (Connection cnx = Database.instance().openConnection()) {
            for (String sql : List.of(
                    "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario IN (?, ?, ?))",
                    "DELETE FROM reservas WHERE id_funcionario IN (?, ?, ?)",
                    "DELETE FROM funcionarios WHERE id_usuario IN (?, ?, ?)",
                    "DELETE FROM usuarios WHERE id_usuario IN (?, ?, ?)")) {
                try (PreparedStatement statement = cnx.prepareStatement(sql)) {
                    statement.setString(1, ADMIN);
                    statement.setString(2, FUNCIONARIO);
                    statement.setString(3, OTRO);
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement recursos = cnx.prepareStatement("DELETE FROM recursos WHERE id_recurso LIKE 'IT-RR-%'")) {
                recursos.executeUpdate();
            }
            try (PreparedStatement categorias = cnx.prepareStatement("DELETE FROM categorias WHERE descripcion LIKE 'IT-RR-%'")) {
                categorias.executeUpdate();
            }
        }
    }

    @Test void administradorConsultaActualizaFiltraYEliminaRecursosPorProxy() {
        assertEquals(categoria.getId(), proxy.recursoBuscarPorId(RECURSO).getIdCategoria());
        proxy.recursoActualizar(new Recurso(RECURSO, categoria.getId(), "Sala actualizada RR"));
        assertEquals("Sala actualizada RR", proxy.recursoBuscarPorId(RECURSO).getDescripcion());
        assertEquals(List.of(RECURSO), proxy.recursosBuscarPorFiltros(categoria, "actualizada RR").stream().map(Recurso::getId).toList());
        proxy.recursoEliminar(RECURSO);
        assertNull(proxy.recursoBuscarPorId(RECURSO));
    }

    @Test void funcionarioConsultaRecursosPeroNoPuedeModificarlos() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        assertTrue(proxy.recursosPorCategoria(categoria.getId()).stream().anyMatch(r -> RECURSO.equals(r.getId())));
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.recursoCrear(new Recurso("IT-RR-OTRO", categoria.getId(), "Otro recurso")));
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.recursoActualizar(new Recurso(RECURSO, categoria.getId(), "Cambio no autorizado")));
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.recursoEliminar(RECURSO));
        assertEquals("Sala de prueba RR", proxy.recursoBuscarPorId(RECURSO).getDescripcion());
    }

    @Test void reservaAsignaRecursoImpideTraslapeYCancelacionLoLibera() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        Reserva reserva = reservar(proxy);
        assertEquals(List.of(RECURSO), reserva.getRecursosAsignadosIds());
        assertEquals(FUNCIONARIO, reserva.getIdFuncionario());
        assertEquals(reserva.getId(), proxy.reservaBuscarPorId(reserva.getId()).getId());
        assertTrue(proxy.reservasObtener().stream().anyMatch(r -> reserva.getId().equals(r.getId())));
        try (ServiceProxy otro = new ServiceProxy("127.0.0.1", server.getPuerto())) {
            assertNotNull(otro.authenticate(OTRO, CLAVE));
            assertThrows(ServiceProxy.RemoteException.class, () -> reservar(otro));
            assertThrows(ServiceProxy.RemoteException.class, () -> otro.reservaCancelar(reserva.getId()));
            assertThrows(ServiceProxy.RemoteException.class, () -> otro.reservaBuscarPorId(reserva.getId()));
            assertTrue(otro.reservasObtener().isEmpty());
            proxy.reservaCancelar(reserva.getId());
            assertNull(proxy.reservaBuscarPorId(reserva.getId()));
            assertEquals(List.of(RECURSO), reservar(otro).getRecursosAsignadosIds());
        }
    }

    private Reserva reservar(ServiceProxy cliente) {
        return cliente.reservaCrear("Reserva RR", LocalDate.now().plusDays(30),
                LocalTime.of(9, 0), LocalTime.of(10, 0), List.of(categoria));
    }

    @RepeatedTest(5)
    void reservasSimultaneasDelMismoRecursoGuardanYNotificanSoloUna() throws Exception {
        comprobarReservasSimultaneas(false);
    }

    @Test void reservasSimultaneasConDosRecursosAsignanUnoDistintoACadaFuncionario() throws Exception {
        proxy.recursoCrear(new Recurso("IT-RR-SEGUNDO", categoria.getId(), "Segunda sala RR"));
        comprobarReservasSimultaneas(true);
    }

    private void comprobarReservasSimultaneas(boolean hayDosRecursos) throws Exception {
        BlockingQueue<Reserva> avisosAdmin = new LinkedBlockingQueue<>();
        BlockingQueue<Reserva> avisosPrimero = new LinkedBlockingQueue<>();
        BlockingQueue<Reserva> avisosSegundo = new LinkedBlockingQueue<>();
        BlockingQueue<Exception> erroresCanal = new LinkedBlockingQueue<>();
        proxy.iniciarAlertas(avisosAdmin::add, erroresCanal::add);
        try (ServiceProxy primero = new ServiceProxy("127.0.0.1", server.getPuerto());
             ServiceProxy segundo = new ServiceProxy("127.0.0.1", server.getPuerto())) {
            assertNotNull(primero.authenticate(FUNCIONARIO, CLAVE));
            assertNotNull(segundo.authenticate(OTRO, CLAVE));
            primero.iniciarAlertas(avisosPrimero::add, erroresCanal::add);
            segundo.iniciarAlertas(avisosSegundo::add, erroresCanal::add);

            ExecutorService hilos = Executors.newFixedThreadPool(2);
            CountDownLatch listos = new CountDownLatch(2);
            CountDownLatch inicio = new CountDownLatch(1);
            Resultado primeroResultado;
            Resultado segundoResultado;
            try {
                Future<Resultado> uno = hilos.submit(() -> intentarReserva(primero, listos, inicio));
                Future<Resultado> dos = hilos.submit(() -> intentarReserva(segundo, listos, inicio));
                assertTrue(listos.await(5, TimeUnit.SECONDS), "Ambos clientes deben estar listos");
                inicio.countDown();
                primeroResultado = uno.get(10, TimeUnit.SECONDS);
                segundoResultado = dos.get(10, TimeUnit.SECONDS);
            } finally {
                inicio.countDown();
                hilos.shutdownNow();
                assertTrue(hilos.awaitTermination(10, TimeUnit.SECONDS));
            }

            List<Resultado> resultados = List.of(primeroResultado, segundoResultado);
            List<Reserva> guardadas = resultados.stream().map(Resultado::reserva).filter(java.util.Objects::nonNull).toList();
            assertEquals(hayDosRecursos ? 2 : 1, guardadas.size());
            if (hayDosRecursos) {
                assertEquals(2, guardadas.stream().flatMap(r -> r.getRecursosAsignadosIds().stream()).distinct().count());
            } else {
                Resultado rechazado = resultados.stream().filter(r -> r.error() != null).findFirst().orElseThrow();
                assertTrue(rechazado.error().getMessage().contains("No hay recursos disponibles"), rechazado.error().getMessage());
            }

            List<String> ids = guardadas.stream().map(Reserva::getId).sorted().toList();
            assertEquals(ids, proxy.reservasObtener().stream()
                    .filter(r -> List.of(FUNCIONARIO, OTRO).contains(r.getIdFuncionario()))
                    .map(Reserva::getId).sorted().toList());
            assertEquals(guardadas.size(), contarFilasDePrueba("reservas"));
            assertEquals(guardadas.size(), contarFilasDePrueba("reserva_recurso"));
            comprobarAvisos(avisosAdmin, guardadas);
            comprobarAvisos(avisosPrimero, guardadas.stream().filter(r -> !FUNCIONARIO.equals(r.getIdFuncionario())).toList());
            comprobarAvisos(avisosSegundo, guardadas.stream().filter(r -> !OTRO.equals(r.getIdFuncionario())).toList());
            assertTrue(erroresCanal.isEmpty(), "Los canales deben permanecer conectados");
        }
    }

    private Resultado intentarReserva(ServiceProxy cliente, CountDownLatch listos, CountDownLatch inicio) throws Exception {
        listos.countDown();
        assertTrue(inicio.await(5, TimeUnit.SECONDS));
        try {
            return new Resultado(reservar(cliente), null);
        } catch (ServiceProxy.RemoteException error) {
            return new Resultado(null, error);
        }
    }

    private void comprobarAvisos(BlockingQueue<Reserva> avisos, List<Reserva> esperadas) throws Exception {
        java.util.ArrayList<String> recibidas = new java.util.ArrayList<>();
        for (Reserva ignorada : esperadas) {
            Reserva aviso = avisos.poll(3, TimeUnit.SECONDS);
            assertNotNull(aviso, "Debe llegar una alerta por cada reserva ajena guardada");
            Reserva persistida = proxy.reservaBuscarPorId(aviso.getId());
            assertNotNull(persistida, "La alerta debe referirse a una reserva ya persistida");
            assertEquals(persistida.getRecursosAsignadosIds(), aviso.getRecursosAsignadosIds());
            recibidas.add(aviso.getId());
        }
        assertEquals(esperadas.stream().map(Reserva::getId).sorted().toList(), recibidas.stream().sorted().toList());
        assertNull(avisos.poll(300, TimeUnit.MILLISECONDS), "No debe haber alertas propias, duplicadas ni de reservas rechazadas");
    }

    @Test void falloAlGuardarAsignacionesRevierteReservaYRecursosInsertados() throws Exception {
        Reserva reserva = new Reserva();
        reserva.setIdFuncionario(FUNCIONARIO);
        reserva.setActividad("Reserva que debe revertirse");
        reserva.setFecha(LocalDate.now().plusDays(30));
        reserva.setHoraInicio(LocalTime.of(9, 0));
        reserva.setHoraFin(LocalTime.of(10, 0));
        // La primera asignación es válida; la segunda fuerza el fallo de la transacción.
        reserva.setRecursosAsignadosIds(List.of(RECURSO, "IT-RR-INEXISTENTE"));

        RuntimeException error = assertThrows(RuntimeException.class, () -> new ReservaDatos().crear(reserva));

        assertTrue(error.getMessage().contains("recurso"), error.getMessage());
        assertNull(reserva.getId());
        assertEquals(0, contarFilasDePrueba("reservas"));
        assertEquals(0, contarFilasDePrueba("reserva_recurso"));
        assertTrue(proxy.reservasObtener().stream().noneMatch(r -> FUNCIONARIO.equals(r.getIdFuncionario())));
    }

    private int contarFilasDePrueba(String tabla) throws Exception {
        String sql = switch (tabla) {
            case "reservas" -> "SELECT COUNT(*) FROM reservas WHERE id_funcionario IN (?, ?)";
            case "reserva_recurso" -> "SELECT COUNT(*) FROM reserva_recurso rr JOIN reservas r ON r.id_reserva = rr.id_reserva WHERE r.id_funcionario IN (?, ?)";
            default -> throw new IllegalArgumentException("Tabla no contemplada en la prueba");
        };
        try (Connection cnx = Database.instance().openConnection();
             PreparedStatement consulta = cnx.prepareStatement(sql)) {
            consulta.setString(1, FUNCIONARIO);
            consulta.setString(2, OTRO);
            try (var filas = consulta.executeQuery()) {
                assertTrue(filas.next());
                return filas.getInt(1);
            }
        }
    }

    private record Resultado(Reserva reserva, ServiceProxy.RemoteException error) { }
}
