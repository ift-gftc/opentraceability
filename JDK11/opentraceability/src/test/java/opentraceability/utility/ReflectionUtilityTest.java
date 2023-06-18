package opentraceability.utility;

import opentraceability.models.events.EventILMD;
import opentraceability.models.events.ObjectEvent;
import opentraceability.queries.EPCISQueryParameters;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReflectionUtilityTest {

    @org.junit.jupiter.api.Test
    public void constructType() throws Exception {

        // construct instance of ObjectEvent
        var objectEvent = ReflectionUtility.constructType(ObjectEvent.class);
        assertNotNull(objectEvent);

        // construct instance of EventILMD
        var ilmd = ReflectionUtility.constructType(EventILMD.class);
        assertNotNull(ilmd);

        // construct ArrayList<String>
        var stringList = ReflectionUtility.constructType((new ArrayList<String>()).getClass());
        assertNotNull(stringList);

        // construct EPCISQueryParameters
        var epcisQueryParams = ReflectionUtility.constructType(EPCISQueryParameters.class);
        assertNotNull(epcisQueryParams);
    }
}
