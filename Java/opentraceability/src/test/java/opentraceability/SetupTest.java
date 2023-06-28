package opentraceability;

import opentraceability.models.masterdata.TradeItem;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.*;

class SetupTest {

    @Test
    void initialize() throws Exception {
        Setup.Initialize();
    }

    @Test
    void registerMasterDataType() throws Exception {
        Setup.registerMasterDataType(TradeItem.class, TradeItem.class);
    }

    @Test
    void getMasterDataTypeDefault() throws Exception {
        Setup.registerMasterDataType(TradeItem.class, TradeItem.class);
        Class clazz = Setup.getMasterDataTypeDefault(TradeItem.class);
        assertTrue(clazz == TradeItem.class);
    }
}