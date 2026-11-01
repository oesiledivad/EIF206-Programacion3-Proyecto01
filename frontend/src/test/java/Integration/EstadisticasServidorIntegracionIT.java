package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.Administrador;
import una.proyecto.model.EstadisticaItem;
import una.proyecto.model.Funcionario;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

class EstadisticasServidorIntegracionIT {
    private static final String FUNCIONARIO = "IT-FUNC-EST";
    private static final String ADMIN = "IT-ADMIN-EST";
    private static final String CLAVE = "clavePrueba";
    private static final String RECURSO = "IT-RECURSO-EST";
    private static final String CATEGORIA = "IT Estadisticas remotas";
    private static final LocalDate FECHA = LocalDate.of(2040, 1, 10);
    private static Server server;
    private static Thread servidor;
    private ServiceProxy proxy;
    private int categoriaId;

    @BeforeAll static void iniciar() throws Exception {
        server = new Server(0);
        servidor = new Thread(server::iniciar, "Servidor-Estadisticas-IT");
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
        Funcionario funcionario = new Funcionario(FUNCIONARIO, "FUNCIONARIO", "Ana Estadisticas", "00000000");
        funcionario.setPassword(CLAVE);
        assertTrue(usuarios.addUser(funcionario));
        Administrador admin = new Administrador(ADMIN, "ADMIN", "Administrador Estadisticas");
        admin.setPassword(CLAVE);
        assertTrue(usuarios.addUser(admin));
        try (Connection cnx = Database.instance().openConnection();
             PreparedStatement categoria = cnx.prepareStatement(
                     "INSERT INTO categorias (descripcion) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            categoria.setString(1, CATEGORIA);
            categoria.executeUpdate();
            try (var keys = categoria.getGeneratedKeys()) {
                assertTrue(keys.next());
                categoriaId = keys.getInt(1);
            }
            try (PreparedStatement recurso = cnx.prepareStatement(
                    "INSERT INTO recursos (id_recurso, id_categoria, descripcion) VALUES (?, ?, ?)")) {
                recurso.setString(1, RECURSO);
                recurso.setInt(2, categoriaId);
                recurso.setString(3, "Recurso para estadisticas");
                recurso.executeUpdate();
            }
        }
        crearReserva(FECHA, "ACTIVA");
        crearReserva(FECHA.plusDays(7), "ACTIVA");
        crearReserva(FECHA, "CANCELADA");
        proxy = new ServiceProxy("127.0.0.1", server.getPuerto());
    }

    private void crearReserva(LocalDate fecha, String estado) throws Exception {
        try (Connection cnx = Database.instance().openConnection();
             PreparedStatement reserva = cnx.prepareStatement(
                     "INSERT INTO reservas (id_funcionario, actividad, fecha, hora_inicio, hora_fin, estado) "
                             + "VALUES (?, 'Estadisticas IT', ?, '09:00:00', '10:00:00', ?)", Statement.RETURN_GENERATED_KEYS)) {
            reserva.setString(1, FUNCIONARIO);
            reserva.setObject(2, fecha);
            reserva.setString(3, estado);
            reserva.executeUpdate();
            try (var keys = reserva.getGeneratedKeys();
                 PreparedStatement asignacion = cnx.prepareStatement(
                         "INSERT INTO reserva_recurso (id_reserva, id_recurso) VALUES (?, ?)")) {
                assertTrue(keys.next());
                asignacion.setInt(1, keys.getInt(1));
                asignacion.setString(2, RECURSO);
                asignacion.executeUpdate();
            }
        }
    }

    @AfterEach void limpiar() throws Exception {
        if (proxy != null) proxy.close();
        try (Connection cnx = Database.instance().openConnection()) {
            for (String sql : new String[]{
                    "DELETE FROM reserva_recurso WHERE id_reserva IN (SELECT id_reserva FROM reservas WHERE id_funcionario = ?)",
                    "DELETE FROM reservas WHERE id_funcionario = ?"}) {
                try (PreparedStatement statement = cnx.prepareStatement(sql)) {
                    statement.setString(1, FUNCIONARIO);
                    statement.executeUpdate();
                }
            }
            try (PreparedStatement recurso = cnx.prepareStatement("DELETE FROM recursos WHERE id_recurso = ?")) {
                recurso.setString(1, RECURSO);
                recurso.executeUpdate();
            }
            try (PreparedStatement categoria = cnx.prepareStatement("DELETE FROM categorias WHERE id_categoria = ?")) {
                categoria.setInt(1, categoriaId);
                categoria.executeUpdate();
            }
            for (String tabla : List.of("funcionarios", "usuarios")) {
                try (PreparedStatement statement = cnx.prepareStatement("DELETE FROM " + tabla + " WHERE id_usuario IN (?, ?)")) {
                    statement.setString(1, FUNCIONARIO);
                    statement.setString(2, ADMIN);
                    statement.executeUpdate();
                }
            }
        }
        categoriaId = 0;
    }

    @Test void funcionarioConsultaRecursosPorPeriodoSinContarCancelaciones() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        List<EstadisticaItem> items = proxy.estadisticasRecursos(FECHA, FECHA);
        assertEquals(1, items.stream().filter(i -> CATEGORIA.equals(i.getNombre())).findFirst().orElseThrow().getCantidad());
        assertEquals(2, proxy.estadisticasRecursos(FECHA, FECHA.plusDays(7)).stream()
                .filter(i -> CATEGORIA.equals(i.getNombre())).findFirst().orElseThrow().getCantidad());
        assertTrue(proxy.estadisticasRecursos(FECHA.minusDays(1), FECHA.minusDays(1)).stream()
                .noneMatch(i -> CATEGORIA.equals(i.getNombre())));
    }

    @Test void administradorConsultaActividadesPorSemana() {
        assertNotNull(proxy.authenticate(ADMIN, CLAVE));
        List<EstadisticaItem> items = proxy.estadisticasActividades(FECHA, FECHA.plusDays(7));
        assertEquals(2, items.size());
        assertEquals(2, items.stream().mapToInt(EstadisticaItem::getCantidad).sum());
        assertTrue(items.stream().allMatch(i -> LocalDate.parse(i.getNombre()).isBefore(FECHA.plusDays(8))));
    }

    @Test void actividadesIncluyenSemanasVaciasEnOrdenConInicioLunes() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        LocalDate lunes = FECHA.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        List<EstadisticaItem> items = proxy.estadisticasActividades(lunes.minusWeeks(1), lunes.plusWeeks(3));
        assertEquals(List.of(lunes.minusWeeks(1).toString(), lunes.toString(), lunes.plusWeeks(1).toString(),
                        lunes.plusWeeks(2).toString(), lunes.plusWeeks(3).toString()),
                items.stream().map(EstadisticaItem::getNombre).toList());
        assertEquals(List.of(0, 1, 1, 0, 0), items.stream().map(EstadisticaItem::getCantidad).toList());
    }

    @Test void periodoSinActividadesDevuelveSuSemanaConCero() {
        assertNotNull(proxy.authenticate(ADMIN, CLAVE));
        LocalDate sinReservas = FECHA.plusYears(1);
        var items = proxy.estadisticasActividades(sinReservas, sinReservas);
        assertEquals(1, items.size());
        assertEquals(sinReservas.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString(),
                items.get(0).getNombre());
        assertEquals(0, items.get(0).getCantidad());
    }

    @Test void sinSesionRechazaAmbasConsultasYPermiteLoginPosterior() {
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.estadisticasRecursos(FECHA, FECHA));
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.estadisticasActividades(FECHA, FECHA));
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        assertFalse(proxy.estadisticasRecursos(FECHA, FECHA).isEmpty());
    }

    @Test void validaFechasEnBackendSinDesincronizarElCanal() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        List<BiFunction<LocalDate, LocalDate, List<EstadisticaItem>>> consultas =
                List.of(proxy::estadisticasRecursos, proxy::estadisticasActividades);
        for (var consulta : consultas) {
            assertThrows(ServiceProxy.RemoteException.class, () -> consulta.apply(null, FECHA));
            assertThrows(ServiceProxy.RemoteException.class, () -> consulta.apply(FECHA, null));
            assertThrows(ServiceProxy.RemoteException.class, () -> consulta.apply(FECHA.plusDays(1), FECHA));
            assertFalse(consulta.apply(FECHA, FECHA).isEmpty());
        }
    }

    @Test void logoutYBajaLogicaRevocanLasConsultas() {
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        proxy.logout();
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.estadisticasRecursos(FECHA, FECHA));
        assertNotNull(proxy.authenticate(FUNCIONARIO, CLAVE));
        new UsuarioDatos().deleteUser(FUNCIONARIO);
        assertThrows(ServiceProxy.RemoteException.class, () -> proxy.estadisticasActividades(FECHA, FECHA));
    }
}
