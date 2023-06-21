package opentraceability.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class CountriesTest {

    @Test
    void load() throws Exception {
        Countries.load();
    }

    @Test
    void fromAbbreviation() throws Exception {
        Countries.load();
        Country c = Countries.fromAbbreviation("US");
        assertNotNull(c);
        assertTrue(c instanceof Country);
    }

    @Test
    void fromAlpha3() throws Exception {
        Countries.load();
        Country c = Countries.fromAlpha3("USA");
        assertNotNull(c);
        assertTrue(c instanceof Country);
    }

    @Test
    void fromCountryName() throws Exception {
        Countries.load();
        Country c = Countries.fromCountryName("United States");
        assertNotNull(c);
        assertTrue(c instanceof Country);
    }

    @Test
    void fromCountryIso() throws Exception {
        Countries.load();
        Country c = Countries.fromCountryIso(840);
        assertNotNull(c);
        assertTrue(c instanceof Country);
    }

    @Test
    void parse() throws Exception {
        Countries.load();
    }
}