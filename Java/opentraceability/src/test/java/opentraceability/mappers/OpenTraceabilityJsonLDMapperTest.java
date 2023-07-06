package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.models.events.EPCISDocument;
import opentraceability.models.events.EPCISQueryDocument;
import opentraceability.utility.DataCompare;
import opentraceability.utility.EmbeddedResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenTraceabilityJsonLDMapperTest {

    @Test
    void epcisDocument() throws Exception {
        Setup.Initialize();

        String[] files = {
                "gdst_data_withmasterdata.jsonld",
                "aggregation_event_all_possible_fields.jsonld",
                "AssociationEvent-a.jsonld",
                "AssociationEvent-b.jsonld",
                "AssociationEvent-c.jsonld",
                "AssociationEvent-d.jsonld",
                "AssociationEvent-e.jsonld",
                "AssociationEvent-f.jsonld",
                "AssociationEvent-g.jsonld",
                "AssociationEvent-h.jsonld",
                "association_event_all_possible_fields.jsonld",
                "EPCISDocument_objectevents_complete.jsonld",
                "ErrorDeclarationAndCorrectiveEvent.jsonld",
                "Example-TransactionEvents-2020_07_03y.jsonld",
                "Example-Type-sourceOrDestination,measurement,bizTransaction.jsonld",
                "Example_9.6.1-ObjectEvent-with-error-declaration.jsonld",
                "Example_9.6.1-ObjectEvent-with-pseudo-SBDH-headers.jsonld",
                "Example_9.6.1-ObjectEvent.jsonld",
                "Example_9.6.1-ObjectEventWithDigitalLink.jsonld",
                "Example_9.6.1-with-comment.jsonld",
                "Example_9.6.2-ObjectEvent.jsonld",
                "Example_9.6.2-ObjectEventWithDigitalLink.jsonld",
                "Example_9.6.3-AggregationEvent.jsonld",
                "Example_9.6.3-AggregationEventWithDigitalLink.jsonld",
                "Example_9.6.4-TransformationEvent.jsonld",
                "Example_9.6.4-TransformationEventWithDigitalLink.jsonld",
                "object_event_all_possible_fields.jsonld",
                "PersistentDisposition-example.jsonld",
                "SensorDataExample1.jsonld",
                "SensorDataExample10.jsonld",
                "gdst_with_masterdata.jsonld",
                "SensorDataExample12.jsonld",
                "SensorDataExample13.jsonld",
                "SensorDataExample14.jsonld",
                "SensorDataExample15.jsonld",
                "SensorDataExample16.jsonld",
                "SensorDataExample17.jsonld",
                "SensorDataExample1b.jsonld",
                "SensorDataExample2.jsonld",
                "SensorDataExample3.jsonld",
                "SensorDataExample4.jsonld",
                "SensorDataExample5.jsonld",
                "SensorDataExample6.jsonld",
                "SensorDataExample7.jsonld",
                "SensorDataExample9.jsonld",
                "transaction_event_all_possible_fields.jsonld",
                "transformation_event_all_possible_fields.jsonld"
        };


        for (String file: files)
        {
            // load an XML file
            EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
            String str = loader.readString(Setup.class, "/tests/" + file);

            try
            {
                EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(str, true);
                String strAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc, true);

                // compare the XMLs
                DataCompare.CompareJSON(str, strAfter);
            }
            catch (AssertionError err)
            {
                throw new Exception("File Failed: " + file, err);
            }
            catch (Exception ex)
            {
                throw new Exception("File Failed: " + file, ex);
            }
        }
    }

    @Test
    void epcisQueryDocument() throws Exception {
        //Setup.Initialize();
        opentraceability.gdst.Setup.Initialize();

        String[] files = new String[] {
                "EPCISQueryDocument.jsonld"
        };

        for (String file: files)
        {
            try
            {
                // load an XML file
                EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
                String str = loader.readString(Setup.class, "/tests/" + file);

                EPCISQueryDocument doc = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(str, true);
                String strAfter = OpenTraceabilityMappers.EPCISQueryDocument.JSON.map(doc);

                // compare the XMLs
                DataCompare.CompareJSON(str, strAfter);
            }
            catch (Exception ex)
            {
                throw new Exception("File Failed: " + file, ex);
            }
        }
    }
}