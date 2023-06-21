package opentraceability.queries;

import opentraceability.mappers.EPCISDataFormat;
import opentraceability.models.events.EPCISVersion;

import java.net.URI;

public class DigitalLinkQueryOptions {
    public URI url = null;

    public EPCISVersion version = EPCISVersion.V2;

    public EPCISDataFormat format = EPCISDataFormat.JSON;
    public boolean EnableStackTrace = false;
}