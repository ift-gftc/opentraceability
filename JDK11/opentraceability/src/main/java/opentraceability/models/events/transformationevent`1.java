package opentraceability.models.events;

import opentraceability.interfaces.IEventKDE;
import opentraceability.interfaces.ITransformationEvent;
import opentraceability.utility.attributes.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class TransformationEvent<T> extends EventBase implements ITransformationEvent {

    @OpenTraceabilityProductsAttribute(value = "inputQuantityList", version = EPCISVersion.V2, type = EventProductType.Input, order = 8, listType = OpenTraceabilityProductsListType.QuantityList)
    @OpenTraceabilityProductsAttribute(value = "inputEPCList", version = EPCISVersion.V2, type = EventProductType.Input, order = 7, listType = OpenTraceabilityProductsListType.EPCList)
    public List<EventProduct> inputs = new ArrayList<>();

    public List<EventProduct> outputs = new ArrayList<>();

    public EventAction action = null;

    @OpenTraceabilityAttribute(namespace = "", name = "transformationID", order = 11)
    public String transformationID = null;

    @OpenTraceabilityAttribute(namespace = "", name = "bizStep", order = 12)
    public URI businessStep = null;

    @OpenTraceabilityAttribute(namespace = "", name = "disposition", order = 13)
    public URI disposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(namespace = "", name = "readPoint", order = 14)
    public EventReadPoint readPoint = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(namespace = "", name = "bizLocation", order = 15)
    public EventLocation location = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "bizTransaction")
    @OpenTraceabilityAttribute(namespace = "", name = "bizTransactionList", order = 16)
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "source")
    @OpenTraceabilityAttribute(namespace = "", name = "sourceList", order = 17)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "destination")
    @OpenTraceabilityAttribute(namespace = "", name = "destinationList", order = 18)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(name = "sensorElement")
    @OpenTraceabilityAttribute(namespace = "", name = "sensorElementList", order = 19)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(namespace = "", name = "persistentDisposition", order = 20)
    public PersistentDisposition persistentDisposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(namespace = "", name = "ilmd", order = 21)
    public T ilmd = null;

    @OpenTraceabilityXmlIgnoreAttribute
    @OpenTraceabilityAttribute(namespace = "", name = "type", order = 0)
    public EventType eventType = EventType.TransformationEvent;

    public List<EventProduct> products = new ArrayList<>();
  

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
    public <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name) {
        return super.getKDE(clazz, ns, name);
    }

    @Override
    public <T extends IEventKDE> T getKDE(Class<T> clazz) {
        return super.getKDE(clazz);
    }

    @Override
    public EventILMD grabILMD() {
        return (EventILMD) ilmd;
    }
}