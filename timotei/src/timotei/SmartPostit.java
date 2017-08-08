package timotei;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Sadmiral
 */
public class SmartPostit {
    String maattinimi, nimi, osoite, toimipaikka, aukioloaika, postinumero, longitude, latitude;
    public ArrayList<Posti> maatti_array;
    public ArrayList<Paketti> paketti_array;
    public ArrayList<Esine> esine_array;
    private static SmartPostit sp = null;
    
    private SmartPostit(String m) {
        maattinimi = m;
        maatti_array = new ArrayList<>();
        esine_array = new ArrayList<>();
        paketti_array = new ArrayList<>();
        
        try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        try {
            Document doc = builder.parse("http://iseteenindus.smartpost.ee/api/?request=destinations&country=FI&type=APT");
            NodeList nimiList = doc.getElementsByTagName("name");
            NodeList osoiteList = doc.getElementsByTagName("address");
            NodeList postinumeroList = doc.getElementsByTagName("postalcode");
            NodeList toimipisteList = doc.getElementsByTagName("city");
            NodeList aukioloajatList = doc.getElementsByTagName("availability");
            NodeList latitudeList = doc.getElementsByTagName("lat");
            NodeList longitudeList = doc.getElementsByTagName("lng");

            for (int i = 0; i<nimiList.getLength();i++) {
                Node n = nimiList.item(i);
                Node o = osoiteList.item(i);
                Node pn = postinumeroList.item(i);
                Node tp = toimipisteList.item(i);
                Node ao = aukioloajatList.item(i);
                Node lat = latitudeList.item(i);
                Node lon = longitudeList.item(i);

                if (n.getNodeType()==Node.ELEMENT_NODE) {
                    Element nimi = (Element) n;
                    Element osoite = (Element) o;
                    Element toimipiste = (Element) tp;
                    Element aukioloajat = (Element) ao;
                    Element postinumero = (Element) pn;
                    Element longitude = (Element) lon;
                    Element latitude = (Element) lat;

                    maatti_array.add(new Posti(nimi.getTextContent(), osoite.getTextContent(), toimipiste.getTextContent(), aukioloajat.getTextContent(), postinumero.getTextContent(), longitude.getTextContent(), latitude.getTextContent()));
                }
            }
        } catch (SAXException | IOException ex) {
            Logger.getLogger(timoteiDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        } catch (ParserConfigurationException ex) {
        Logger.getLogger(timoteiDB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void addPaketti(Integer pID, Integer id, Integer aID) {
        paketti_array.add(new Paketti(pID, id, aID));
    }
    public void addEsine(Integer eID, String n, Double k, Boolean b, Float m) {
        esine_array.add(new Esine(eID, n, k, b, m));
    }
    static public SmartPostit getInstance() {
        if (sp == null)
            sp = new SmartPostit("SmartPost");
        return sp;
    }
}

