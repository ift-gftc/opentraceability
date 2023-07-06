package opentraceability.models.events;

import opentraceability.interfaces.ITransformationEvent;
import opentraceability.utility.attributes.*;

import java.util.ArrayList;
import java.util.List;

public class TransformationEventBase<T extends EventILMD> extends ITransformationEvent<T> {

    public TransformationEventBase()
    {
        this.eventType = EventType.TransformationEvent;
    }

    @OpenTraceabilityProductsAttribute(name = "inputQuantityList", version = EPCISVersion.Any, sequenceOrder = 11, productType = EventProductType.Input, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "inputEPCList", version = EPCISVersion.Any, sequenceOrder = 10, productType = EventProductType.Input, listType = OpenTraceabilityProductsListType.EPCList)
    public List<EventProduct> inputs = new ArrayList<>();
    @OpenTraceabilityProductsAttribute(name = "outputQuantityList", version = EPCISVersion.Any, sequenceOrder = 13, productType = EventProductType.Output, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(name = "outputEPCList", version = EPCISVersion.Any, sequenceOrder = 12, productType = EventProductType.Output, listType = OpenTraceabilityProductsListType.EPCList)
    public List<EventProduct> outputs = new ArrayList<>();

    @OpenTraceabilityAttribute(ns = "", name = "transformationID", sequenceOrder = 14)
    public String transformationID = null;


    @Override
    public List<EventProduct> getProducts() {
        ArrayList<EventProduct> products = new ArrayList<>();
        for (var p: inputs)
        {
            products.add(p);
        }
        for (var p: outputs)
        {
            products.add(p);
        }
        return products;
    }

    public void addProduct(EventProduct product) throws Exception {
        switch (product.Type) {
            case Output:
                outputs.add(product);
                break;
            case Input:
                inputs.add(product);
                break;
            default:
                throw new Exception("Transformation event only supports inputs and outputs.");
        }
    }

    @Override
    public EventILMD grabILMD() {
        return this.ilmd;
    }
}