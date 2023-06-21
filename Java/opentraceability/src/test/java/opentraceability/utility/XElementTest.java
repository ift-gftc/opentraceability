package opentraceability.utility;

import opentraceability.Setup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XElementTest {

    @Test
    public void XPath() throws Exception {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String xmlStr = loader.readString(Setup.class, "/tests/all_events_query_doc_1_2.xml");
        XElement xe = XElement.Parse(xmlStr);

        XElement xEventList = xe.Element("EPCISBody/epcisq:QueryResults/resultsBody/EventList");
        assertFalse(xEventList.IsNull);

        // manual creation
        XElement xList = new XElement("epcisq:EPCISQueryDocument");
        xList.Add("EPCISBody").Add("epcisq:QueryResults").Add("resultsBody").Add("EventList");
        xList.AddNamespace("epcisq", "urn:test:1");
        xList.FixPrefixesAndNamespacing();
        xEventList = xList.Element("EPCISBody/epcisq:QueryResults/resultsBody/EventList");
        assertFalse(xEventList.IsNull);
    }
}