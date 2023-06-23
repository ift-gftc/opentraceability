package opentraceability.models.events;

import java.util.List;
import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute;

public class SensorElement {
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name ="sensorMetadata", sequenceOrder = 0)
    public SensorMetaData metaData;

    @OpenTraceabilityArrayAttribute(itemType = SensorReport.class)
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "sensorReport", sequenceOrder = 1)
    public List<SensorReport> reports;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IEventKDE> extensionKDEs;
}