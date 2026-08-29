package una.proyecto.service;



import una.proyecto.model.Reservas;
import una.proyecto.model.Reserva;
import una.proyecto.utils.XmlUtil;

import java.util.List;

public class ReservaService {
    //return XmlUtil.readListXml(RUTA_XML, ListaCategoria.class, ListaCategoria::getCategorias);
    private final String RUTA_XML = "data/xml/reservas.xml";
    public List<Reserva> obtenerTodasReservas(){
        return XmlUtil.readListXml(RUTA_XML, una.proyecto.model.Reservas.class, una.proyecto.model.Reservas::getReservas);
    }
    public void save(Reserva reserva){
        List<Reserva> lista = obtenerTodasReservas();
        Boolean encontrado =false;
        for(int i=0; i<lista.size(); i++){
            if (lista.get(i).getId()==reserva.getId()){
                lista.set(i, reserva);
                encontrado=true;
                break ;
            }
        }
        if (!encontrado) {
            lista.add(reserva);
        }

        XmlUtil.writeListXml(RUTA_XML, Reservas.class, lista, Reservas::setReservas);
    }

    public void delete(String id){
        List<Reserva> lista =obtenerTodasReservas();
        lista.removeIf(reserva -> reserva.getId().equals(id));
        XmlUtil.writeListXml(RUTA_XML, Reservas.class,lista,Reservas::setReservas);
    }

}

