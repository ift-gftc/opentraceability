package opentraceability.utility;

import opentraceability.Setup;
import org.junit.jupiter.api.Test;
import tangible.OutObject;

import static org.junit.jupiter.api.Assertions.*;

class XmlSchemaCheckerTest {

    @Test
    void validate() throws Exception {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");
        XElement xml = XElement.Parse(xmlStr);
        OutObject<String> outArgs = new OutObject<>();
        Boolean isValid = XmlSchemaChecker.validate(xml, "https://ref.gs1.org/standards/epcis/epcglobal-epcis-2_0.xsd", outArgs);
        assertTrue(isValid);
    }
}