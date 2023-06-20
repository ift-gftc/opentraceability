package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.models.events.EPCISDocument;
import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.ObjectEvent;
import opentraceability.utility.EmbeddedResourceLoader;
import opentraceability.utility.XElement;
import opentraceability.utility.DataCompare;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenTraceabilityXmlMapperTest {

    @Test
    void objectEvent_to_from_xml() throws Exception {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");
        XElement xml = XElement.Parse(str);

        // find each Object Event
        for (var xObject: xml.Elements("//ObjectEvent"))
        {
            ObjectEvent objEvent = (ObjectEvent)OpenTraceabilityXmlMapper.FromXml(xObject, EPCISVersion.V2, ObjectEvent.class);
            assertNotNull(objEvent);

            // convert back to XML
            XElement xele = OpenTraceabilityXmlMapper.ToXml(null, "ObjectEvent", objEvent, EPCISVersion.V2, false);

            // copy in any of our namespaces...
            xml.copyNamespacesTo(xele);
            xml.copyNamespacesTo(xObject);

            // compare the XMLs
            DataCompare.CompareXML(xObject.toString(), xele.toString());
        }
    }

    @Test
    void objectEvent_to_from_xml_100Times() throws Exception
    {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");
        XElement xml = XElement.Parse(str);

        // find each Object Event
        for (int i = 0; i < 100; i++)
        {
            for (var xObject: xml.Elements("//ObjectEvent"))
            {
                ObjectEvent objEvent = (ObjectEvent)OpenTraceabilityXmlMapper.FromXml(xObject, EPCISVersion.V2, ObjectEvent.class);
                assertNotNull(objEvent);

                // convert back to XML
                XElement xele = OpenTraceabilityXmlMapper.ToXml(null, "ObjectEvent", objEvent, EPCISVersion.V2, false);

                // we don't check because this is pure stress testing
            }
        }
    }

    @Test
    void epcisQueryDocument_to_from_xml_1_2() throws Exception {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/all_events_query_doc_1_2.xml");

        EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlStr, true);
        String xmlStrAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(doc);

        // compare the XMLs
        DataCompare.CompareXML(xmlStr, xmlStrAfter);
    }

    @Test
    void epcisQueryDocument_to_from_xml_1_2_100Times() throws Exception {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/all_events_query_doc_1_2.xml");

        for (int i = 0; i < 100; i++)
        {
            EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(xmlStr, true);
            String xmlStrAfter = OpenTraceabilityMappers.EPCISQueryDocument.XML.map(doc);
        }
    }

    @Test
    void epcisDocument_to_from_xml() throws Exception
    {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");

        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.map(xmlStr);
        String xmlStrAfter = OpenTraceabilityMappers.EPCISDocument.XML.map(doc);

        // compare the XMLs
        DataCompare.CompareXML(xmlStr, xmlStrAfter);
    }

    @Test
    void epcisDocument_to_from_xml_100Times() throws Exception
    {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/object_event_all_possible_fields.xml");

        for (int i = 0; i < 1000; i++)
        {
            EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.XML.map(xmlStr);
            String xmlStrAfter = OpenTraceabilityMappers.EPCISDocument.XML.map(doc);
        }
    }
}