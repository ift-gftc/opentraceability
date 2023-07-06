package opentraceability.queries;

import com.squareup.okhttp.OkHttpClient;
import opentraceability.Setup;
import opentraceability.interfaces.IEvent;
import opentraceability.mappers.EPCISDataFormat;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.models.events.EPCISDocument;
import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventProduct;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.masterdata.Location;
import opentraceability.queries.*;
import opentraceability.utility.DataCompare;
import opentraceability.utility.EmbeddedResourceLoader;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryTests {
    public QueryTests() throws Exception {
        Setup.Initialize();
    }

    String testServerURL = "https://traceabilityserver01.azurewebsites.net/";

    @Test
    public void queryParameters() throws Exception {
        EPCISQueryParameters parameters = new EPCISQueryParameters();
        parameters.query.MATCH_epc = new ArrayList<>();
        parameters.query.MATCH_epc.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_anyEPC = new ArrayList<>();
        parameters.query.MATCH_anyEPC.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_anyEPC.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_epcClass = new ArrayList<>();
        parameters.query.MATCH_epcClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.MATCH_anyEPCClass = new ArrayList<>();
        parameters.query.MATCH_anyEPCClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.MATCH_anyEPCClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.GE_eventTime = OffsetDateTime.now();
        parameters.query.GE_recordTime = OffsetDateTime.now();
        parameters.query.LE_eventTime = OffsetDateTime.now();
        parameters.query.LE_recordTime = OffsetDateTime.now();
        parameters.query.EQ_bizLocation = new ArrayList<>();
        parameters.query.EQ_bizLocation.add("urn:epc:id:sgln:0614141.00888.0");
        parameters.query.EQ_bizLocation.add("urn:epc:id:sgln:0614141.00888.0");
        parameters.query.EQ_bizStep = new ArrayList<>();
        parameters.query.EQ_bizStep.add("https://ref.gs1.org/cbv/BizStep-shipping");
        parameters.query.EQ_bizStep.add("receiving");

        String queryParameters = parameters.toQueryParameters();
        URI uri = URI.create("https://example.org" + queryParameters);

        EPCISQueryParameters paramsAfter = new EPCISQueryParameters(uri);

        DataCompare.CompareJSON(parameters.toJSON(), paramsAfter.toJSON());
    }

    @Test
    public void queryParameters02() throws Exception {
        EPCISQueryParameters parameters = new EPCISQueryParameters();
        parameters.query.MATCH_epc = new ArrayList<>();
        parameters.query.MATCH_epc.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_anyEPC = new ArrayList<>();
        parameters.query.MATCH_anyEPC.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_anyEPC.add("https://id.gs1.org/01/00614141777778/10/987");
        parameters.query.MATCH_epcClass = new ArrayList<>();
        parameters.query.MATCH_epcClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.MATCH_anyEPCClass = new ArrayList<>();
        parameters.query.MATCH_anyEPCClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.MATCH_anyEPCClass.add("urn:epc:class:lgtin:4012345.012345.998877");
        parameters.query.GE_eventTime = OffsetDateTime.now();
        parameters.query.GE_recordTime = OffsetDateTime.now();
        parameters.query.LE_eventTime = OffsetDateTime.now();
        parameters.query.LE_recordTime = OffsetDateTime.now();
        parameters.query.EQ_bizStep = new ArrayList<>();
        parameters.query.EQ_bizStep.add("https://ref.gs1.org/cbv/BizStep-shipping");
        parameters.query.EQ_bizStep.add("receiving");

        String queryParameters = parameters.toQueryParameters();
        URI uri = URI.create("https://example.org" + queryParameters);

        EPCISQueryParameters paramsAfter = new EPCISQueryParameters(uri);

        DataCompare.CompareJSON(parameters.toJSON(), paramsAfter.toJSON());
    }

    @Test
    public void masterData() throws Exception {
        String filename = "testserver_advancedfilters.jsonld";
        EPCISTestServerClient client = new EPCISTestServerClient(testServerURL, EPCISDataFormat.JSON, EPCISVersion.V2);

        String data = ReadTestData(filename);
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data, true);
        String blob_id = client.postEPCISDocument(doc);

        boolean foundOneGDSTLocation = false;

        for (IEvent e : doc.events) {
            for (EventProduct p : e.getProducts()) {
                EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                EPCISQueryResults results = client.queryEvents(blob_id, parameters);
                assertNotNull(results.Document);
                assertEquals(results.Errors.size(), 0, "errors found in the query events");
                assertNotEquals(results.Document.events.size(), 0, "no events returned");

                client.resolveMasterData(blob_id, results.Document);
                assertNotEquals(results.Document.masterData.size(), 0, "no master data resolved");

                if (results.Document.masterData.stream().anyMatch(m -> m instanceof Location)) {
                    foundOneGDSTLocation = true;
                }
            }
        }

        assertTrue(foundOneGDSTLocation, "Did not find GDSTLocation.");
    }

    @Test
    public void getEPCISQueryInterfaceURL() throws Exception {
        String filename = "testserver_advancedfilters.jsonld";
        OkHttpClient httpClient = new OkHttpClient();
        EPCISTestServerClient client = new EPCISTestServerClient(testServerURL, EPCISDataFormat.JSON, EPCISVersion.V2);

        String data = ReadTestData(filename);
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data, true);
        String blob_id = client.postEPCISDocument(doc);

        DigitalLinkQueryOptions queryOptions = new DigitalLinkQueryOptions();
        try {
            queryOptions.url = new URI(testServerURL + "/digitallink/" + blob_id + "/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        for (IEvent e : doc.events) {
            for (EventProduct p : e.getProducts()) {
                URL epcisQueryInterfaceURL = EPCISTraceabilityResolver.getEPCISQueryInterfaceURL(queryOptions, p.EPC, httpClient);
                assertNotNull(epcisQueryInterfaceURL, "Failed to get EPCIS URL for " + p.EPC);
            }
        }
    }

    @Test
    public void queryEvents() throws Exception {
        String filename = "aggregation_event_all_possible_fields.jsonld";
        EPCISTestServerClient client = new EPCISTestServerClient(testServerURL, EPCISDataFormat.JSON, EPCISVersion.V2);

        String data = ReadTestData(filename);
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data, true);
        String blob_id = client.postEPCISDocument(doc);

        for (IEvent e : doc.events) {
            for (EventProduct p : e.getProducts()) {
                EPCISQueryParameters parameters = new EPCISQueryParameters(p.EPC);
                EPCISQueryResults results = client.queryEvents(blob_id, parameters);
                assertEquals(results.Errors.size(),0, "errors found in the query events");
                assertEquals(results.Document.events.size(), 1, "no events returned");
            }
        }


    }

    @Test
    public void advancedFilters() throws Exception {
        String filename = "testserver_advancedfilters.jsonld";
        EPCISTestServerClient client = new EPCISTestServerClient(testServerURL, EPCISDataFormat.JSON, EPCISVersion.V2);

        String data = ReadTestData(filename);
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data, true);
        String blob_id = client.postEPCISDocument(doc);

        String epc = "urn:epc:id:sscc:08600031303.0004";
        String bizStep = "urn:epcglobal:cbv:bizStep:receiving";
        String bizLocation = "urn:gdst:example.org:location:loc:importer.123u";

        EPCISQueryParameters parameters = new EPCISQueryParameters(new EPC(epc));
        parameters.query.EQ_bizStep = new ArrayList<>();
        parameters.query.EQ_bizStep.add(bizStep);
        parameters.query.EQ_bizLocation = new ArrayList<>();
        parameters.query.EQ_bizLocation.add(bizLocation);

        EPCISQueryResults results = client.queryEvents(blob_id, parameters);
        assertEquals(results.Errors.size(), 0, "errors found in the query events");
        assertEquals(results.Document.events.size(), 1, "no events returned");
    }

    @Test
    public void traceback() throws Exception {
        String filename = "traceback_tests.jsonld";
        EPCISTestServerClient client = new EPCISTestServerClient(testServerURL, EPCISDataFormat.JSON, EPCISVersion.V2);

        String data = ReadTestData(filename);
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(data, true);
        String blob_id = client.postEPCISDocument(doc);

        String epc = "urn:gdst:example.org:product:lot:class:processor.2u.v1-0122-2022";
        EPCISQueryResults results = client.traceback(blob_id, new EPC(epc));
        assertEquals(results.Errors.size(), 0, "errors found in the traceback events");
        assertNotNull(results.Document);
        assertEquals(16, results.Document.events.size(), "expected 16 events");
    }

    String ReadTestData(String file)
    {
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(Setup.class, "/tests/" + file);
        return str;
    }
}
