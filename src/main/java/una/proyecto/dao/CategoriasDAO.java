package una.proyecto.dao;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import una.proyecto.model.Categorias;
import una.proyecto.model.ListaCategorias;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;

public class CategoriasDAO {

    private static final String ARCHIVO = "categorias.xml";

    public void guardar(ListaCategorias lista) {

        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.newDocument();

            // Elemento raíz
            Element raiz = document.createElement("categorias");
            document.appendChild(raiz);

            // Recorrer las categorías
            for (Categorias categoria : lista.getCategorias()) {

                Element categoriaElement = document.createElement("categoria");

                Element id = document.createElement("id");
                id.setTextContent(String.valueOf(categoria.getId()));

                Element descripcion = document.createElement("descripcion");
                descripcion.setTextContent(categoria.getDescripcion());

                categoriaElement.appendChild(id);
                categoriaElement.appendChild(descripcion);

                raiz.appendChild(categoriaElement);
            }

            // Crear el archivo XML
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(ARCHIVO));

            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListaCategorias cargar() {

        ListaCategorias lista = new ListaCategorias();

        try {

            File archivo = new File(ARCHIVO);

            // Si todavía no existe el XML
            if (!archivo.exists()) {
                return lista;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(archivo);

            document.getDocumentElement().normalize();

            NodeList nodos = document.getElementsByTagName("categoria");

            for (int i = 0; i < nodos.getLength(); i++) {

                Node nodo = nodos.item(i);

                if (nodo.getNodeType() == Node.ELEMENT_NODE) {

                    Element elemento = (Element) nodo;

                    int id = Integer.parseInt(
                            elemento
                                    .getElementsByTagName("id")
                                    .item(0)
                                    .getTextContent()
                    );

                    String descripcion =
                            elemento
                                    .getElementsByTagName("descripcion")
                                    .item(0)
                                    .getTextContent();

                    Categorias categoria =
                            new Categorias(id, descripcion);

                    lista.getCategorias().add(categoria);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}