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
    @OpenTraceabilityJsonAttribute(name="time")
    @OpenTraceabilityAttribute(ns="", name="@time")
    public OffsetDateTime timeStamp = null;

    @OpenTraceabilityJsonAttribute(name="type")
    @OpenTraceabilityAttribute(ns="", name="@type")
    public URI type = null;

    @OpenTraceabilityJsonAttribute(name="value")
    @OpenTraceabilityAttribute(ns="", name="@value")
    public Double value = null;

    @OpenTraceabilityJsonAttribute(name="stringValue")
    @OpenTraceabilityAttribute(ns="", name="@stringValue")
    public String stringValue = null;

    @OpenTraceabilityJsonAttribute(name="booleanValue")
    @OpenTraceabilityAttribute(ns="", name="@booleanValue")
    public Boolean booleanValue = null;

    @OpenTraceabilityJsonAttribute(name="hexBinaryValue")
    @OpenTraceabilityAttribute(ns="", name="@hexBinaryValue")
    public String hexBinaryValue = null;

    @OpenTraceabilityJsonAttribute(name="uriValue")
    @OpenTraceabilityAttribute(ns="", name="@uriValue")
    public URI uriValue = null;

    @OpenTraceabilityJsonAttribute(name="uom")
    @OpenTraceabilityAttribute(ns="", name="@uom")
    public UOM uom = new UOM();

    @OpenTraceabilityJsonAttribute(name="component")
    @OpenTraceabilityAttribute(ns="", name="@component")
    public URI component = null;

    @OpenTraceabilityJsonAttribute(name="minValue")
    @OpenTraceabilityAttribute(ns="", name="@minValue")
    public Double minValue = null;

    @OpenTraceabilityJsonAttribute(name="maxValue")
    @OpenTraceabilityAttribute(ns="", name="@maxValue")
    public Double maxValue = null;

    @OpenTraceabilityJsonAttribute(name="sDev")
    @OpenTraceabilityAttribute(ns="", name="@sDev")
    public Double sDev = null;

    @OpenTraceabilityJsonAttribute(name="chemicalSubstance")
    @OpenTraceabilityAttribute(ns="", name="@chemicalSubstance")
    public URI chemicalSubstance = null;

    @OpenTraceabilityJsonAttribute(name="microorganism")
    @OpenTraceabilityAttribute(ns="", name="@microorganism")
    public URI microOrganism = null;

    @OpenTraceabilityJsonAttribute(name="deviceID")
    @OpenTraceabilityAttribute(ns="", name="@deviceID")
    public URI deviceID = null;

    @OpenTraceabilityJsonAttribute(name="rawData")
    @OpenTraceabilityAttribute(ns="", name="@rawData")
    public URI rawData = null;

    @OpenTraceabilityJsonAttribute(name="meanValue")
    @OpenTraceabilityAttribute(ns="", name="@meanValue")
    public Double meanValue = null;

    @OpenTraceabilityJsonAttribute(name="percRank")
    @OpenTraceabilityAttribute(ns="", name="@percRank")
    public Double percentageRank = null;

    @OpenTraceabilityJsonAttribute(name="dataProcessingMethod")
    @OpenTraceabilityAttribute(ns="", name="@dataProcessingMethod")
    public URI dataProcessingMethod = null;

    @OpenTraceabilityJsonAttribute(name="coordinateReferenceSystem")
    @OpenTraceabilityAttribute(ns="", name="@coordinateReferenceSystem")
    public URI coordinateReferenceSystem = null;

    @OpenTraceabilityJsonAttribute(name="exception")
    @OpenTraceabilityAttribute(ns="", name="@exception")
    public URI exception = null;

    @OpenTraceabilityJsonAttribute(name="percValue")
    @OpenTraceabilityAttribute(ns="", name="@percValue")
    public Double percentageValue = null;

    @OpenTraceabilityJsonAttribute(name="deviceMetadata")
    @OpenTraceabilityAttribute(ns="", name="@deviceMetadata")
    public URI deviceMetadata = null;

    @OpenTraceabilityExtensionAttributesAttribute
    public List<IEventKDE> extensionAttributes = new ArrayList<>();
}