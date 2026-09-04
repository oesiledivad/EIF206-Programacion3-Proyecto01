package una.proyecto.datos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import una.proyecto.datos.wrapper.Reservas;
import una.proyecto.model.Reserva;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReservaDatos {
    private final String filepath;
    private final JAXBContext ctx;
    private List<Reserva> cache;

    public ReservaDatos(String filePath) throws Exception {
        this.filepath = filePath;
        try {
            this.ctx = JAXBContext.newInstance(Reservas.class, Reserva.class);
        } catch (JAXBException e) {
            throw new Exception("Error al inicializar JAXBContext: " + e.getMessage(), e);
        }
        this.cache = cargarTodo();
    }

    public void guardarReservas(List<Reserva> reservas) {

        try {
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            Reservas wrapper = new Reservas();
            wrapper.setReservas(reservas);
            marshaller.marshal(wrapper, new File(filepath));
        } catch (JAXBException e) {
            throw new RuntimeException("Error al guardar los reservas XML: " + e.getMessage(), e);
        }
    }
    public List<Reserva> cargarTodo(){
        File file= new File (filepath);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try{
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            Reservas wrapper = (Reservas) unmarshaller.unmarshal(file);
            List<Reserva> lista = wrapper.getReservas();
            return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        } catch (JAXBException e) {
            throw new RuntimeException("Error de persistencia: Falló el unmarshal de JAXB", e);
        }
    }
    public void crear(Reserva nuevoDto) {
        cache.add(nuevoDto);
        guardarReservas(cache);
    }
    public Reserva leerPorId(String id) {

        return cache.stream()
                .filter(u -> id.equals(u.getId()))
                .findFirst()
                .orElse(null);
    }
    public void eliminar(String id) {
        cache.removeIf(recurso -> Objects.equals(recurso.getId(), id));
        guardarReservas(cache);
    }
    public List<Reserva> obtenerTodos() {
        return new ArrayList<>(cache);
    }


}
