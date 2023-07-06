package opentraceability.models.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import opentraceability.interfaces.IILMDEvent;
import opentraceability.utility.attributes.*;
import opentraceability.models.identifiers.*;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@OpenTraceabilityEventKDEOrder(order = {
        "",
        "",
        "",
        "",
        ""
})
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused", "FieldCanBeLocal", "InnerClassMayBeStatic"})
public class AggregationEventBase<T extends EventILMD> extends IILMDEvent<T>
{
    @OpenTraceabilityAttribute(ns = "", name = "parentID", sequenceOrder = 10)
    public EPC parentID = null;

    @OpenTraceabilityProductsAttribute(name = "extension/childQuantityList", version = EPCISVersion.V1, sequenceOrder = 30, productType = EventProductType.Child, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "childQuantityList", version = EPCISVersion.V2, sequenceOrder = 30, productType = EventProductType.Child, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "childEPCs", version = EPCISVersion.Any, sequenceOrder = 11, productType = EventProductType.Child, listType = OpenTraceabilityProductsListType.EPCList, required = true)
    public List<EventProduct> children = new ArrayList<>();


    public AggregationEventBase() {
        this.eventType = opentraceability.models.events.EventType.AggregationEvent;
    }

    @Override
    public List<EventProduct> getProducts() {
        ArrayList<EventProduct> products = new ArrayList<>();
        if (this.parentID != null)
        {
            var parentProduct = new EventProduct();
            parentProduct.EPC = this.parentID;
            parentProduct.Type = EventProductType.Parent;
            products.add(parentProduct);
        }
        products.addAll(this.children);
        return products;
    }

    public void addProduct(EventProduct product) throws Exception {
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