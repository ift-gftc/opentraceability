package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.ObjectEvent;
import opentraceability.utility.EmbeddedResourceLoader;
import opentraceability.utility.XElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenTraceabilityXmlMapperTest {

    @Test
    void objectEvent_to_from_xml() throws Exception {

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");
        XElement xml = XElement.Parse(str);

        // find each Object Event
        for (var xObject: xml.Elements("//ObjectEvent"))
        {
            ObjectEvent objEvent = (ObjectEvent)OpenTraceabilityXmlMapper.FromXml(xObject, EPCISVersion.V2, ObjectEvent.class);
            assertNotNull(objEvent);
        }

        // convert into Object Event using XML Mapper

        // convert back into XML using mapper

        // compare the XMLs
    }
}