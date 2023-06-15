package opentraceability.models.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import opentraceability.interfaces.EPCISVersion;
import opentraceability.interfaces.EventAction;
import opentraceability.interfaces.EventBase;
import opentraceability.interfaces.EventBusinessTransaction;
import opentraceability.interfaces.EventDestination;
import opentraceability.interfaces.EventILMD;
import opentraceability.interfaces.EventLocation;
import opentraceability.interfaces.EventProduct;
import opentraceability.interfaces.EventReadPoint;
import opentraceability.interfaces.EventSource;
import opentraceability.interfaces.IAggregationEvent;
import opentraceability.interfaces.IEventKDE;
import opentraceability.interfaces.IILMDEvent;
import opentraceability.interfaces.OpenTraceabilityArrayAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityAttribute;
import opentraceability.interfaces.OpenTraceabilityObjectAttribute;
import opentraceability.interfaces.OpenTraceabilityProductsAttribute;
import opentraceability.interfaces.OpenTraceabilityProductsAttribute;
import opentraceability.interfaces.OpenTraceabilityProductsAttribute;
import opentraceability.interfaces.OpenTraceabilityProductsListType;
import opentraceability.models.identifiers.EPC;
import opentraceability.utility.attributes.PersistentDisposition;
import opentraceability.utility.attributes.SensorElement;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused", "FieldCanBeLocal", "InnerClassMayBeStatic"})
public class AggregationEvent<T extends EventILMD> extends EventBase implements IAggregationEvent, IILMDEvent<T> {
    @JsonProperty("@type")
    public EventType type = EventType.TransformationEvent;

    @OpenTraceabilityAttribute("", "parentID", 7)
    public EPC parentID = null;

    @OpenTraceabilityProductsAttribute(value = "", version = EPCISVersion.V1, type = EventProductType.Child, order = 21, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(value = "childQuantityList", version = EPCISVersion.V2, type = EventProductType.Child, order = 15, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(value = "childEPCs", version = EPCISVersion.V2, type = EventProductType.Child, order = 8, listType = OpenTraceabilityProductsListType.EPCList, havingAttributes = true)
    public List<EventProduct> children = new ArrayList<>();

    @OpenTraceabilityAttribute("", "action", 9)
    public EventAction action = null;

    @OpenTraceabilityAttribute("", "bizStep", 10)
    public URI businessStep = null;

    @OpenTraceabilityAttribute("", "disposition", 11)
    public URI disposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 12)
    public EventReadPoint readPoint = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 13)
    public EventLocation location = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 14)
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute("", "sourceList", 16, EPCISVersion.V2)
    @OpenTraceabilityAttribute(value = "", v1Order = 22, version = EPCISVersion.V1)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("", "destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute(value = "", v1Order = 23, version = EPCISVersion.V1)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute("", "sensorElementList", 18, EPCISVersion.V2)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 19, EPCISVersion.V2)
    public PersistentDisposition persistentDisposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "ilmd", 20, EPCISVersion.V2)
    public T ilmd = null;

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    public EventType eventType = EventType.AggregationEvent;

    public List<EventProduct> products = new ArrayList<>();

    public AggregationEvent() { }

    public AggregationEvent(T ilmd) {
        this.ilmd = ilmd;
    }

    public AggregationEvent(EventReadPoint readPoint, EventAction action) {
        this.readPoint = readPoint;
        this.action = action;
    }

    @SuppressWarnings("unchecked")
    public <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name) {
        return super.getKDE(clazz, ns, name);
    }

    @SuppressWarnings("unchecked")
    public <T extends IEventKDE> T getKDE(Class<T> clazz) {
        return super.getKDE(clazz);
    }

    public void addProduct(EventProduct product) {
        switch (product.Type) {
            case Parent:
                if (product.Quantity != null) throw new Exception("Parents do not support quantity.");
                this.parentID = product.EPC;
                break;

            case Child:
                this.children.add(product);
                break;
            default:
                throw new Exception("Aggregation event only supports children and parents.");
        }
    }

    public EventILMD grabILMD() {
        return ilmd;
    }
}