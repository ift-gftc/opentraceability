package opentraceability.models.events;

import opentraceability.interfaces.IEventKDE;
import opentraceability.utility.UOM;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionAttributesAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class SensorReport {
    @OpenTraceabilityJsonAttribute("time")
    @OpenTraceabilityAttribute("", "@time")
    public OffsetDateTime timeStamp = null;

    @OpenTraceabilityJsonAttribute("type")
    @OpenTraceabilityAttribute("", "@type")
    public URI type = null;

    @OpenTraceabilityJsonAttribute("value")
    @OpenTraceabilityAttribute("", "@value")
    public Double value = null;

    @OpenTraceabilityJsonAttribute("stringValue")
    @OpenTraceabilityAttribute("", "@stringValue")
    public String stringValue = null;

    @OpenTraceabilityJsonAttribute("booleanValue")
    @OpenTraceabilityAttribute("", "@booleanValue")
    public Boolean booleanValue = null;

    @OpenTraceabilityJsonAttribute("hexBinaryValue")
    @OpenTraceabilityAttribute("", "@hexBinaryValue")
    public String hexBinaryValue = null;

    @OpenTraceabilityJsonAttribute("uriValue")
    @OpenTraceabilityAttribute("", "@uriValue")
    public URI uriValue = null;

    @OpenTraceabilityJsonAttribute("uom")
    @OpenTraceabilityAttribute("", "@uom")
    public UOM uom = new UOM();

    @OpenTraceabilityJsonAttribute("component")
    @OpenTraceabilityAttribute("", "@component")
    public URI component = null;

    @OpenTraceabilityJsonAttribute("minValue")
    @OpenTraceabilityAttribute("", "@minValue")
    public Double minValue = null;

    @OpenTraceabilityJsonAttribute("maxValue")
    @OpenTraceabilityAttribute("", "@maxValue")
    public Double maxValue = null;

    @OpenTraceabilityJsonAttribute("sDev")
    @OpenTraceabilityAttribute("", "@sDev")
    public Double sDev = null;

    @OpenTraceabilityJsonAttribute("chemicalSubstance")
    @OpenTraceabilityAttribute("", "@chemicalSubstance")
    public URI chemicalSubstance = null;

    @OpenTraceabilityJsonAttribute("microorganism")
    @OpenTraceabilityAttribute("", "@microorganism")
    public URI microOrganism = null;

    @OpenTraceabilityJsonAttribute("deviceID")
    @OpenTraceabilityAttribute("", "@deviceID")
    public URI deviceID = null;

    @OpenTraceabilityJsonAttribute("rawData")
    @OpenTraceabilityAttribute("", "@rawData")
    public URI rawData = null;

    @OpenTraceabilityJsonAttribute("meanValue")
    @OpenTraceabilityAttribute("", "@meanValue")
    public Double meanValue = null;

    @OpenTraceabilityJsonAttribute("percRank")
    @OpenTraceabilityAttribute("", "@percRank")
    public Double percentageRank = null;

    @OpenTraceabilityJsonAttribute("dataProcessingMethod")
    @OpenTraceabilityAttribute("", "@dataProcessingMethod")
    public URI dataProcessingMethod = null;

    @OpenTraceabilityJsonAttribute("coordinateReferenceSystem")
    @OpenTraceabilityAttribute("", "@coordinateReferenceSystem")
    public URI coordinateReferenceSystem = null;

    @OpenTraceabilityJsonAttribute("exception")
    @OpenTraceabilityAttribute("", "@exception")
    public URI exception = null;

    @OpenTraceabilityJsonAttribute("percValue")
    @OpenTraceabilityAttribute("", "@percValue")
    public Double percentageValue = null;

    @OpenTraceabilityJsonAttribute("deviceMetadata")
    @OpenTraceabilityAttribute("", "@deviceMetadata")
    public URI deviceMetadata = null;

    @OpenTraceabilityExtensionAttributesAttribute
    public List<IEventKDE> extensionAttributes = new ArrayList<>();
}