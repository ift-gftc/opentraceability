package opentraceability.models.events;

import opentraceability.interfaces.IILMDEvent;
import opentraceability.utility.attributes.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@OpenTraceabilityEventKDEOrder(order = { "" })
public class ObjectEventBase<T extends EventILMD> extends IILMDEvent<T> {

    public ObjectEventBase() {
        this.eventType = opentraceability.models.events.EventType.ObjectEvent;
    }

    @OpenTraceabilityProductsAttribute(name = "extension/quantityList", version = EPCISVersion.V1, sequenceOrder = 30, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "quantityList", version = EPCISVersion.V2, sequenceOrder = 30, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "epcList", version = EPCISVersion.Any, sequenceOrder = 10, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.EPCList, required = true)
    public List<EventProduct> referenceProducts = new ArrayList<>();

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
        return ilmd;
    }
}