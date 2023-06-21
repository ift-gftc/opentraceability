package opentraceability.queries;

import opentraceability.mappers.EPCISDataFormat;
import opentraceability.models.events.EPCISVersion;
import opentraceability.models.identifiers.*;
import java.net.URI;

public class EPCISQueryInterfaceOptions {
    public URI url = null;

    public EPCISVersion version = EPCISVersion.V2;

    public EPCISDataFormat format = EPCISDataFormat.JSON;

    public boolean enableStackTrace = true;
}