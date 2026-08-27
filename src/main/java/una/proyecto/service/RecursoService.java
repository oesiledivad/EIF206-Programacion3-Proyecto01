package una.proyecto.service;

import una.proyecto.model.Recurso;
import una.proyecto.model.ListaRecursos;
import una.proyecto.utils.XmlUtil;

import java.util.List;

public class RecursoService {
    private final String RUTA_XML = "data/xml/recursos.xml";

    public List<Recurso> obtenerTodosRecursos(){
        return XmlUtil.readListXml(RUTA_XML, ListaRecursos.class, ListaRecursos::getRecursos);
    }
    public void delete(String id){
        List<Recurso> recursos = obtenerTodosRecursos();
        recursos.removeIf(recurso -> recurso.getId().equals(id));
        XmlUtil.writeListXml(RUTA_XML, ListaRecursos.class,recursos,ListaRecursos::setRecursos);
    }
    public void save(Recurso recurso){
        List<Recurso> recursos = obtenerTodosRecursos();
        boolean encontrado=false;
        for (int i=0; i<recursos.size(); i++){
            if (recursos.get(i).getId().equals(recurso.getId())){
                recursos.set(i, recurso);

                encontrado=true;
                break;
            }
            }
        if(!encontrado){
            recursos.add(recurso);
        }
        XmlUtil.writeListXml(RUTA_XML, ListaRecursos.class, recursos, ListaRecursos::setRecursos);
    }
}



