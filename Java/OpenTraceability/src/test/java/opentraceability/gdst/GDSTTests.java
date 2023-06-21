package opentraceability.gdst;

import opentraceability.gdst.events.*;
import opentraceability.gdst.masterdata.GDSTLocation;
import opentraceability.mappers.OpenTraceabilityMappers;
import opentraceability.models.events.EPCISDocument;
import opentraceability.utility.DataCompare;
import opentraceability.utility.EmbeddedResourceLoader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GDSTTests {
    public GDSTTests() throws Exception {
        Setup.Initialize();
    }

    @Test
    void epcisDocument() throws Exception {
        opentraceability.Setup.Initialize();

        Boolean foundFishing = false;
        Boolean foundTransshipment = false;
        Boolean foundLanding = false;
        Boolean foundReceiving = false;
        Boolean foundFeedmill = false;
        Boolean foundHatching = false;
        Boolean foundFarmHarvest = false;
        Boolean foundProcessing = false;
        Boolean foundShipping = false;

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
            String str = loader.readString(opentraceability.Setup.class, "/tests/" + file);

            try
            {
                EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(str);

                for (var e: doc.events)
                {
                    if (e instanceof GDSTFishingEvent) foundFishing = true;
                    if (e instanceof GDSTTransshipmentEvent) foundTransshipment = true;
                    if (e instanceof GDSTLandingEvent) foundLanding = true;
                    if (e instanceof GDSTFeedmillObjectEvent || e instanceof GDSTFeedmillTransformationEvent) foundFeedmill = true;
                    if (e instanceof GDSTHatchingEvent) foundHatching = true;
                    if (e instanceof GDSTFarmHarvestEvent) foundFarmHarvest = true;
                    if (e instanceof GDSTShippingEvent) foundShipping = true;
                    if (e instanceof GDSTReceiveEvent) foundReceiving = true;
                    if (e instanceof GDSTProcessingEvent) foundProcessing = true;
                }

                String strAfter = OpenTraceabilityMappers.EPCISDocument.JSON.map(doc);

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

        assertTrue(foundFishing, "Failed to find GDSTFishingEvent.");
        assertTrue(foundTransshipment, "Failed to find GDSTTransshipmentEvent.");
        assertTrue(foundLanding, "Failed to find GDSTLandingEvent.");
        assertTrue(foundFeedmill, "Failed to find GDSTFeedmillObjectEvent or GDSTFeedmillTransfomrationEvent.");
        assertTrue(foundHatching, "Failed to find GDSTHatchingEvent.");
        assertTrue(foundFarmHarvest, "Failed to find GDSTFarmHarvestEvent.");
        assertTrue(foundShipping, "Failed to find GDSTShippingEvent.");
        assertTrue(foundReceiving, "Failed to find GDSTReceiveEvent.");
        assertTrue(foundProcessing, "Failed to find GDSTProcessingEvent.");
    }
}
