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
    @OpenTraceabilityAttribute(ns = "", name = "@time")
    public OffsetDateTime timeStamp;

    @OpenTraceabilityJsonAttribute(name="deviceID")
    @OpenTraceabilityAttribute(ns = "", name = "@deviceID")
    public URI deviceID;

    @OpenTraceabilityJsonAttribute(name="deviceMetadata")
    @OpenTraceabilityAttribute(ns = "", name = "@deviceMetadata")
    public URI deviceMetaData;

    @OpenTraceabilityJsonAttribute(name="rawData")
    @OpenTraceabilityAttribute(ns = "", name = "@rawData")
    public URI rawData;

    @OpenTraceabilityJsonAttribute(name="startTime")
    @OpenTraceabilityAttribute(ns = "", name = "@startTime")
    public OffsetDateTime startTime;

    @OpenTraceabilityJsonAttribute(name="bizRules")
    @OpenTraceabilityAttribute(ns = "", name = "@bizRules")
    public URI bizRules;

    @OpenTraceabilityJsonAttribute(name="dataProcessingMethod")
    @OpenTraceabilityAttribute(ns = "", name = "@dataProcessingMethod")
    public URI dataProcessingMethod;

    @OpenTraceabilityJsonAttribute(name="endTime")
    @OpenTraceabilityAttribute(ns = "", name = "@endTime")
    public OffsetDateTime endTime;

    @OpenTraceabilityExtensionAttributesAttribute
    public List<IEventKDE> extensionAttributes = new ArrayList<>();
}