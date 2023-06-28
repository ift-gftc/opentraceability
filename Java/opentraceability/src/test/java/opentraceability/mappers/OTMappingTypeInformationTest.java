package opentraceability.mappers;

import opentraceability.Setup;
import opentraceability.models.events.ObjectEvent;
import opentraceability.models.masterdata.TradeItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTMappingTypeInformationTest {

    @Test
    void getXmlTypeInfo() throws Exception {
        Setup.Initialize();
        var mapping = OTMappingTypeInformation.getXmlTypeInfo(ObjectEvent.class);
        assertNotNull(mapping);

        mapping = OTMappingTypeInformation.getXmlTypeInfo(ObjectEvent.class);
        assertNotNull(mapping);
    }

    @Test
    void getJsonTypeInfo() throws Exception {
        Setup.Initialize();
        var mapping = OTMappingTypeInformation.getXmlTypeInfo(ObjectEvent.class);
        assertNotNull(mapping);

        mapping = OTMappingTypeInformation.getXmlTypeInfo(ObjectEvent.class);
        assertNotNull(mapping);
    }

    @Test
    void getMasterDataXmlTypeInfo() throws Exception {
        Setup.Initialize();
        var mapping = OTMappingTypeInformation.getXmlTypeInfo(TradeItem.class);
        assertNotNull(mapping);

        mapping = OTMappingTypeInformation.getXmlTypeInfo(TradeItem.class);
        assertNotNull(mapping);
    }

    @Test
    void getMasterDataJsonTypeInfo() throws Exception {
        Setup.Initialize();
        var mapping = OTMappingTypeInformation.getMasterDataJsonTypeInfo(TradeItem.class);
        assertNotNull(mapping);

        mapping = OTMappingTypeInformation.getMasterDataJsonTypeInfo(TradeItem.class);
        assertNotNull(mapping);
    }
}