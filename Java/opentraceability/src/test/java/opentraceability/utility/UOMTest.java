package opentraceability.utility;

import static org.junit.jupiter.api.Assertions.*;

class UOMTest {

    @org.junit.jupiter.api.Test
    void lookUpFromUNCode() throws Exception {
        UOMS.load();
        UOM uom = UOMS.getUOMFromUNCode("KGM");
        assertNotNull(uom);
    }

    @org.junit.jupiter.api.Test
    void isNullOrEmpty() throws Exception {
        UOMS.load();
        UOM uom = UOMS.getUOMFromUNCode("KGM");
        var isNull = UOM.isNullOrEmpty(uom);
        assertFalse(isNull);
    }

    @org.junit.jupiter.api.Test
    void parseFromName() throws Exception {
        UOMS.load();
        UOM uom = UOMS.getUOMFromName("kilogram");
        var isNull = UOM.isNullOrEmpty(uom);
        assertFalse(isNull);
    }
}