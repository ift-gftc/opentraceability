package opentraceability.utility;

import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlToJsonConverter {
    public static JSONObject toJSON(XElement xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xml.element), new StreamResult(writer));
            String xmlStr = writer.getBuffer().toString();
            return XML.toJSONObject(xmlStr); // convert the XML into a JSON object
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static XElement toXML(JSONObject json) {
        try {
            String xmlStr = XML.toString(json); // convert the json into an XML String
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
            return new XElement(doc.getDocumentElement());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
