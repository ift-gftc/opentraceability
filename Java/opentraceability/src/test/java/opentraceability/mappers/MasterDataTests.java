package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.interfaces.IVocabularyElement;
import opentraceability.models.events.EPCISDocument;
import opentraceability.models.masterdata.Location;
import opentraceability.models.masterdata.TradeItem;
import opentraceability.models.masterdata.TradingParty;
import opentraceability.utility.DataCompare;
import opentraceability.utility.EmbeddedResourceLoader;
import org.junit.jupiter.api.Test;

public class MasterDataTests {

    public MasterDataTests() throws Exception {
        Setup.Initialize();
    }

    @Test
    public void GS1WebVocab_Products() throws Exception {
        String file = "gs1-vocab-products01.json";

        // read object events from test data specified in the file argument
        String json = ReadTestData(file);

        // map into a trade item
        TradeItem tradeitem = (TradeItem)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(TradeItem.class, json);

        // map back into json
        String jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tradeitem);

        // compare the JSON
        DataCompare.CompareJSON(json, jsonAfter);
    }

    @Test
    public void GS1WebVocab_Location() throws Exception {
        String file = "gs1-vocab-locations01.json";

        // read object events from test data specified in the file argument
        String json = ReadTestData(file);

        // map into a trade item
        Location loc = (Location)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(Location.class, json);

        // map back into json
        String jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(loc);

        // compare the JSON
        DataCompare.CompareJSON(json, jsonAfter);
    }

    @Test
    public void GS1WebVocab_TradingParty() throws Exception {
        String file = "gs1-vocab-tradingparties01.json";

        // read object events from test data specified in the file argument
        String json = ReadTestData(file);

        // map into a trade item
        TradingParty tp = (TradingParty)OpenTraceabilityMappers.MasterData.GS1WebVocab.map(TradingParty.class, json);

        // map back into json
        String jsonAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(tp);

        // compare the JSON
        DataCompare.CompareJSON(json, jsonAfter);
    }

    @Test
    public void GS1WebVocab_EPCISDocument() throws Exception {

        String file = "testserver_advancedfilters.jsonld";

        // read object events from test data specified in the file argument
        String json = ReadTestData(file);

        // map into a trade item
        EPCISDocument doc = OpenTraceabilityMappers.EPCISDocument.JSON.map(json, true);

        // foreach master data item
        for (var md: doc.masterData)
        {
            String jsonMD = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(md);

            IVocabularyElement mdAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(md.getClass(), jsonMD);

            // map back into json
            String jsonMDAfter = OpenTraceabilityMappers.MasterData.GS1WebVocab.map(mdAfter);

            // compare the JSON
            DataCompare.CompareJSON(jsonMD, jsonMDAfter);
        }
    }

    String ReadTestData(String file)
    {
        EmbeddedResourceLoader loader = new EmbeddedResourceLoader();
        String str = loader.readString(Setup.class, "/tests/" + file);
        return str;
    }
}
