package una.proyecto.dao;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import una.proyecto.model.ListaCategorias;

import java.io.File;

public class CategoriasDAO {

    private static final String ARCHIVO = "categorias.xml";

    public void guardar(ListaCategorias listaCategorias) {

        try {

            JAXBContext context =
                    JAXBContext.newInstance(ListaCategorias.class);

            Marshaller marshaller =
                    context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            marshaller.marshal(
                    listaCategorias,
                    new File(ARCHIVO)
            );

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public ListaCategorias cargar() {

        try {

            File archivo = new File(ARCHIVO);

            if (!archivo.exists()) {
                return new ListaCategorias();
            }

            JAXBContext context =
                    JAXBContext.newInstance(ListaCategorias.class);

            Unmarshaller unmarshaller =
                    context.createUnmarshaller();

            return (ListaCategorias)
                    unmarshaller.unmarshal(archivo);

        } catch (JAXBException e) {

            e.printStackTrace();

            return new ListaCategorias();
        }
    }
}