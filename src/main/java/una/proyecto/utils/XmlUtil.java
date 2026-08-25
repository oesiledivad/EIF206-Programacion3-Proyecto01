package una.proyecto.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class XmlUtil {

    /**
     * Lee un objeto desde un archivo XML.
     *
     * @param filePath Ruta del archivo XML.
     * @param objectClass Clase del objeto que se desea deserializar.
     * @param <T> Tipo del objeto.
     * @return Objeto obtenido del archivo XML o null si el archivo no existe.
     */
    public static <T> T readXml(
            String filePath,
            Class<T> objectClass) {

        try {
            File file = new File(filePath);

            if (!file.exists() || file.length() == 0) {
                return null;
            }

            JAXBContext context = JAXBContext.newInstance(objectClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            return objectClass.cast(unmarshaller.unmarshal(file));

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Escribe un objeto en un archivo XML.
     *
     * @param filePath Ruta donde se guardará el archivo XML.
     * @param object Objeto que se desea escribir.
     * @param objectClass Clase del objeto.
     * @param <T> Tipo del objeto.
     */
    public static <T> void writeXml(
            String filePath,
            T object,
            Class<T> objectClass) {

        try {
            File file = new File(filePath);

            createParentDirectory(file);

            JAXBContext context = JAXBContext.newInstance(objectClass);
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            marshaller.marshal(object, file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee una lista desde un archivo XML utilizando una clase Wrapper.
     *
     * @param filePath Ruta del archivo XML.
     * @param wrapperClass Clase Wrapper utilizada para deserializar el XML.
     * @param extractor Función que extrae la lista de elementos del Wrapper.
     * @param <T> Tipo de los elementos de la lista.
     * @param <W> Tipo de la clase Wrapper.
     * @return Lista de elementos obtenida del archivo XML.
     */
    public static <T, W> List<T> readListXml(
            String filePath,
            Class<W> wrapperClass,
            Function<W, List<T>> extractor) {

        try {
            File file = new File(filePath);

            if (!file.exists() || file.length() == 0) {
                return new ArrayList<>();
            }

            JAXBContext context = JAXBContext.newInstance(wrapperClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            W wrapper = wrapperClass.cast(
                    unmarshaller.unmarshal(file)
            );

            List<T> list = extractor.apply(wrapper);

            return list != null ? list : new ArrayList<>();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Escribe una lista en un archivo XML utilizando una clase Wrapper.
     *
     * @param filePath Ruta donde se guardará el archivo XML.
     * @param wrapperClass Clase Wrapper utilizada para serializar los elementos.
     * @param elements Lista de elementos que se escribirán en el XML.
     * @param setter Función que asigna la lista de elementos al Wrapper.
     * @param <T> Tipo de los elementos de la lista.
     * @param <W> Tipo de la clase Wrapper.
     */
    public static <T, W> void writeListXml(
            String filePath,
            Class<W> wrapperClass,
            List<T> elements,
            BiConsumer<W, List<T>> setter) {

        try {
            File file = new File(filePath);

            createParentDirectory(file);

            W wrapper = wrapperClass.getDeclaredConstructor().newInstance();

            setter.accept(wrapper, elements);

            JAXBContext context = JAXBContext.newInstance(wrapperClass);
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            marshaller.marshal(wrapper, file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Crea el directorio padre del archivo si todavía no existe.
     *
     * @param file Archivo cuyo directorio padre se desea crear.
     */
    private static void createParentDirectory(File file) {

        File parentDirectory = file.getParentFile();

        if (parentDirectory != null && !parentDirectory.exists()) {
            parentDirectory.mkdirs();
        }
    }
}
