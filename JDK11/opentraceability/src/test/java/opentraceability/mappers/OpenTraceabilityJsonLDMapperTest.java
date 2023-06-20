package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.utility.DataCompare;
import opentraceability.utility.EmbeddedResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenTraceabilityJsonLDMapperTest {

    @Test
    public void epcisDocument() throws Exception
    {
        Setup.Initialize();

        // load an XML file
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String jsonStr = loader.readString(Setup.class, "/tests/gdst_with_masterdata.jsonld");

        var doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(jsonStr);
        String jsonStrAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc);

        DataCompare.CompareJSON(jsonStr, jsonStrAfter);
    }
}