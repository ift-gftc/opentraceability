package opentraceability.models.events;

import opentraceability.models.identifiers.EPC;
import opentraceability.utility.Measurement;

public class EventProduct {

    public EPC EPC = null;
    public Measurement Quantity = null;

    public EventProductType Type;

    public EventProduct()
    {

    }

    public EventProduct(EPC epc) {
        this.EPC = epc;
    }
}