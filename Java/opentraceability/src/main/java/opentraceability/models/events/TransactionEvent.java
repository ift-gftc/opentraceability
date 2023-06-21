package opentraceability.models.events;

import opentraceability.interfaces.IEvent;
import opentraceability.models.identifiers.EPC;
import opentraceability.utility.attributes.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class TransactionEvent extends IEvent {
    public TransactionEvent() {
        this.eventType = opentraceability.models.events.EventType.TransactionEvent;
    }

    @OpenTraceabilityProductsAttribute(name = "extension/quantityList", version = EPCISVersion.V1, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "quantityList", version = EPCISVersion.V2, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "epcList", version = EPCISVersion.V2, productType = EventProductType.Reference, listType = OpenTraceabilityProductsListType.EPCList, required = true)
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
}