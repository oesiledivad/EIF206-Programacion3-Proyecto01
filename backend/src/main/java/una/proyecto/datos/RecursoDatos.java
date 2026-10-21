package una.proyecto.datos;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import una.proyecto.datos.wrapper.RecursosWrapper;
import una.proyecto.model.Recurso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecursoDatos {
    private   String filePath;
    private final JAXBContext ctx;
    private List<Recurso>cache;

    public RecursoDatos(String filePath) throws Exception {
        this.filePath=filePath;
        try {
            this.ctx = JAXBContext.newInstance(RecursosWrapper.class, Recurso.class);
        }catch(JAXBException e){
            throw new Exception("Error al inicializar JAXBContext: " + e.getMessage(), e);
        }
        this.cache=cargarTodo();
        }
    public void guardarTodo(List<Recurso> lista){
    try {
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        RecursosWrapper wrapper = new RecursosWrapper();
        wrapper.setRecursos(lista);
        marshaller.marshal(wrapper, new File(filePath));
    }catch(JAXBException e){
        throw  new RuntimeException("Error al guardar los recursos en XML: " + e.getMessage(), e);
    }
    }
    public List<Recurso> cargarTodo(){
        File file= new File (filePath);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try{
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            RecursosWrapper wrapper = (RecursosWrapper) unmarshaller.unmarshal(file);
            List<Recurso> lista = wrapper.getRecursos();
            return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        } catch (JAXBException e) {
            throw new RuntimeException("Error de persistencia: Falló el unmarshal de JAXB", e);
        }
    }

    public void crear(Recurso nuevoDto) {
        cache.add(nuevoDto);
        guardarTodo(cache);
    }


    public Recurso leerPorId(String id) {

        return cache.stream()
                .filter(u -> id.equals(u.getId()))
                .findFirst()
                .orElse(null);
    }
    public void actualizar(Recurso dtoActualizado) {

        for (int i = 0; i < cache.size(); i++) {
            if (Objects.equals(cache.get(i).getId(), dtoActualizado.getId())) {
                cache.set(i, dtoActualizado);
                guardarTodo(cache);
                return;
            }
        }

    }
    public void eliminar(String id) {
        cache.removeIf(recurso -> Objects.equals(recurso.getId(), id));
        guardarTodo(cache);
    }
    public List<Recurso> obtenerTodos() {
        return new ArrayList<>(cache);
    }
}
