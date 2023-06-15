package opentraceability.models.events;

import opentraceability.interfaces.IILMDEvent;
import opentraceability.utility.attributes.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ObjectEvent<T> extends IILMDEvent<T> {

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute(ns = "", name = "type", sequenceOrder = 0)
    public EventType eventType = opentraceability.models.events.EventType.ObjectEvent;

    @OpenTraceabilityProductsAttribute(name = "extension/quantityList", version = EPCISVersion.V1, type = EventProductType.Reference, order = 20, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "quantityList", version = EPCISVersion.V2, type = EventProductType.Reference, order = 14, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "epcList", version = EPCISVersion.V2, type = EventProductType.Reference, order = 7, listType = OpenTraceabilityProductsListType.EPCList, isNamespaceRequired = true)
    public List<EventProduct> referenceProducts = new ArrayList<>();

    @OpenTraceabilityAttribute(ns = "", name = "action", sequenceOrder = 8)
    public EventAction action = null;

    @OpenTraceabilityAttribute(ns = "", name = "bizStep", sequenceOrder = 9)
    public URI businessStep = null;

    @OpenTraceabilityAttribute(ns = "", name = "disposition", sequenceOrder = 10)
    public URI disposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "readPoint", sequenceOrder = 11)
    public EventReadPoint readPoint = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "bizLocation", sequenceOrder = 12)
    public EventLocation location = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "bizTransaction")
    @OpenTraceabilityAttribute(ns = "", name = "bizTransactionList", sequenceOrder = 13)
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "source")
    @OpenTraceabilityAttribute(ns = "", name = "sourceList", sequenceOrder = 15, version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/sourceList", sequenceOrder = 21, version = EPCISVersion.V1)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute(value = "", ns = "", name = "destinationList", sequenceOrder = 16, version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(name = "extension/destinationList", sequenceOrder = 22, version = EPCISVersion.V1)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute(value = "", namespace = "", name = "sensorElementList", order = 17, version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(value = "extension/sensorElementList", namespace = "", name = "", order = 17, version = EPCISVersion.V1)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "persistentDisposition", 18)
    @OpenTraceabilityAttribute(value = "extension/persistentDisposition", namespace = "", name = "", order = 18, version = EPCISVersion.V1)
    public PersistentDisposition persistentDisposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(value = "", namespace = "", name = "ilmd", order = 19, version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(value = "extension/ilmd", namespace = "", name = "", order = 23, version = EPCISVersion.V1)
    public T ilmd = null;

    public List<EventProduct> getProducts() {
        List<EventProduct> products = new ArrayList<>();
        products.addAll(referenceProducts);
        return products;
    }

    public void addProduct(EventProduct product) throws Exception {
        if (product.Type == EventProductType.Reference) {
            referenceProducts.add(product);
        } else {
            throw new Exception("Object event only supports references.");
        }
    }

    @Override
    public EventILMD grabILMD() {
        return (EventILMD) ilmd;
    }
}