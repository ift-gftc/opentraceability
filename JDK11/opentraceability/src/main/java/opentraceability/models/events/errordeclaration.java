package opentraceability.models.events;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.ArrayList;

public class ErrorDeclaration {
    @OpenTraceabilityJsonAttribute(name="reason")
    @OpenTraceabilityAttribute("", "@type")
    public URI Reason = null;

    @OpenTraceabilityAttribute("", "declarationTime")
    public OffsetDateTime DeclarationTime = null;

    @OpenTraceabilityArrayAttribute("correctiveEventID")
    @OpenTraceabilityAttribute("", "correctiveEventIDs")
    public List<String> CorrectingEventIDs = null;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IEventKDE> ExtensionKDEs = new ArrayList<IEventKDE>();
}