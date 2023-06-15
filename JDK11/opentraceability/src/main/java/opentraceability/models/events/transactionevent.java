package opentraceability.models.events;

import opentraceability.interfaces.IEvent;
import opentraceability.models.attributes.*;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.product.EventProduct;
import opentraceability.models.product.EventProductType;
import opentraceability.utility.EPCISVersion;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class TransactionEvent extends EventBase implements IEvent {
    @OpenTraceabilityAttribute("", "parentID", 8)
    public EPC parentID;

    @OpenTraceabilityProductsAttribute(value = {"extension/quantityList"}, versionV1 = EPCISVersion.V1, productTypeV1 = EventProductType.Reference, priorityV1 = 20, listTypeV1 = OpenTraceabilityProductsListType.QuantityList, valueV2 = "quantityList", productTypeV2 = EventProductType.Reference, priorityV2 = 15, listTypeV2 = OpenTraceabilityProductsListType.QuantityList, valueV3 = "epcList", productTypeV3 = EventProductType.Reference, priorityV3 = 9, listTypeV3 = OpenTraceabilityProductsListType.EPCList)
    public List<EventProduct> referenceProducts = new ArrayList<>();

    @OpenTraceabilityAttribute("", "action", 10)
    public EventAction action = null;

    @OpenTraceabilityAttribute("", "bizStep", 11)
    public URI businessStep = null;

    @OpenTraceabilityAttribute("", "disposition", 12)
    public URI disposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "readPoint", 13)
    public EventReadPoint readPoint = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute("", "bizLocation", 14)
    public EventLocation location = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("bizTransaction")
    @OpenTraceabilityAttribute("", "bizTransactionList", 7)
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("source")
    @OpenTraceabilityAttribute(value = {"sourceList", "extension/sourceList"}, versionV2 = EPCISVersion.V2, priorityV2 = 16, productTypeV2 = EventProductType.Reference, listTypeV2 = OpenTraceabilityProductsListType.SensorElementList, valueV1 = "bizTransactionList", productTypeV1 = EventProductType.Reference, priorityV1 = 21, listTypeV1 = OpenTraceabilityProductsListType.SensorElementList)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("destination")
    @OpenTraceabilityAttribute("","destinationList", 17, EPCISVersion.V2)
    @OpenTraceabilityAttribute("","extension/destinationList", 22, EPCISVersion.V1)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute("sensorElement")
    @OpenTraceabilityAttribute(value = {"sensorElementList", "extension/sensorElementList"}, versionV2 = EPCISVersion.V2, priorityV2 = 18, productTypeV2 = EventProductType.Reference, listTypeV2 = OpenTraceabilityProductsListType.SensorElementList, valueV1 = "bizTransactionList", productTypeV1 = EventProductType.Reference, priorityV1 = 18, listTypeV1 = OpenTraceabilityProductsListType.SensorElementList)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(value = {"persistentDisposition", "extension/persistentDisposition"}, versionV2 = EPCISVersion.V2, priorityV2 = 19, productTypeV2 = EventProductType.Extension, listTypeV2 = OpenTraceabilityProductsListType.QuantityList, valueV1 = "bizTransactionList", productTypeV1 = EventProductType.Extension, priorityV1 = 19, listTypeV1 = OpenTraceabilityProductsListType.EPCList)
    public PersistentDisposition persistentDisposition = null;

    public EventILMD ILMD = null;

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute("", "type", 0)
    public EventType eventType;

    public List<EventProduct> products = new ArrayList<>();

    @Override
    public void addProduct(EventProduct product) throws Exception {
        if (product.Type == EventProductType.Parent) {
            if (product.Quantity != null) {
                throw new Exception("Parents do not support quantity.");
            }
            this.parentID = product.EPC;
        } else if (product.Type == EventProductType.Reference) {
            this.referenceProducts.add(product);
        } else {
            throw new Exception("Transaction event only supports references and parents.");
        }
    }
}