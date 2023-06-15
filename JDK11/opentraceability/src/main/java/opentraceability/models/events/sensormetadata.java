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

    @OpenTraceabilityJsonAttribute(name="time")
    @OpenTraceabilityAttribute("", "@time")
    public OffsetDateTime timeStamp;

    @OpenTraceabilityJsonAttribute(name="deviceID")
    @OpenTraceabilityAttribute("", "@deviceID")
    public URI deviceID;

    @OpenTraceabilityJsonAttribute(name="deviceMetadata")
    @OpenTraceabilityAttribute("", "@deviceMetadata")
    public URI deviceMetaData;

    @OpenTraceabilityJsonAttribute(name="rawData")
    @OpenTraceabilityAttribute("", "@rawData")
    public URI rawData;

    @OpenTraceabilityJsonAttribute(name="startTime")
    @OpenTraceabilityAttribute("", "@startTime")
    public OffsetDateTime startTime;

    @OpenTraceabilityJsonAttribute(name="bizRules")
    @OpenTraceabilityAttribute("", "@bizRules")
    public URI bizRules;

    @OpenTraceabilityJsonAttribute(name="dataProcessingMethod")
    @OpenTraceabilityAttribute("", "@dataProcessingMethod")
    public URI dataProcessingMethod;

    @OpenTraceabilityJsonAttribute(name="endTime")
    @OpenTraceabilityAttribute("", "@endTime")
    public OffsetDateTime endTime;

    @OpenTraceabilityExtensionAttributesAttribute
    public List<IEventKDE> extensionAttributes = new ArrayList<>();
}