package Integration;

import org.junit.jupiter.api.*;
import una.proyecto.datos.Database;
import una.proyecto.datos.UsuarioDatos;
import una.proyecto.model.*;
import una.proyecto.proxy.ServiceProxy;
import una.proyecto.server.Server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
}
