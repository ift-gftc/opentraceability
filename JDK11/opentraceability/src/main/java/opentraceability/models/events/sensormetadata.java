package opentraceability.models.events;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionAttributesAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

public class SensorMetaData {

    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("", "@time")
    public OffsetDateTime timeStamp;

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("", "@deviceID")
    public URI deviceID;

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("", "@deviceMetadata")
    public URI deviceMetaData;

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("", "@rawData")
    public URI rawData;

    @OpenTraceabilityJsonAttribute("startTime")
    @OpenTraceabilityAttribute("", "@startTime")
    public OffsetDateTime startTime;

    @OpenTraceabilityJsonAttribute("bizRules")
    @OpenTraceabilityAttribute("", "@bizRules")
    public URI bizRules;

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("", "@dataProcessingMethod")
    public URI dataProcessingMethod;

    @OpenTraceabilityJsonAttribute("endTime")
    @OpenTraceabilityAttribute("", "@endTime")
    public OffsetDateTime endTime;

    @OpenTraceabilityExtensionAttributesAttribute
    public List<IEventKDE> extensionAttributes = new ArrayList<>();
}