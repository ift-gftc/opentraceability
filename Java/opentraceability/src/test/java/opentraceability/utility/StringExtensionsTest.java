package opentraceability.utility;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StringExtensionsTest {

    @Test
    void isOnlyDigits() {
        assertTrue(StringExtensions.isOnlyDigits("123"));
        assertFalse(StringExtensions.isOnlyDigits("123a"));
        assertFalse(StringExtensions.isOnlyDigits("123!"));
    }

    @Test
    void last() {
    }

    @Test
    void first() {
    }

    @Test
    void lastOrDefault() {
    }

    @Test
    void isURI() {
        assertTrue(StringExtensions.isURI("https://www.google.com"));
        assertTrue(StringExtensions.isURI("urn:epc:global:123.123"));
        assertFalse(StringExtensions.isURI("^"));
    }

    @Test
    void tryConvertToDateTimeOffset() {

    }

    @Test
    void toDuration_fromDuration() {
        String[] tests = new String[] { "+12:00", "-06:00", "-12:32", "+11:45" };

        for (var durationStr: tests)
        {
            Duration duration = StringExtensions.toDuration(durationStr);
            String durationStrAfter = StringExtensions.fromDuration(duration);
            assertEquals(durationStr, durationStrAfter);
        }
    }

    @Test
    void splitXPath() {

    }
}