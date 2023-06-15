package opentraceability.models.events;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import opentraceability.interfaces.IEvent;
import opentraceability.interfaces.IEventKDE;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.EventAction;
import opentraceability.models.identifiers.EventBusinessTransaction;
import opentraceability.models.identifiers.EventDestination;
import opentraceability.models.identifiers.EventLocation;
import opentraceability.models.identifiers.EventProduct;
import opentraceability.models.identifiers.EventProductType;
import opentraceability.models.identifiers.EventReadPoint;
import opentraceability.models.identifiers.EventSource;
import opentraceability.models.identifiers.EventType;
import opentraceability.models.identifiers.PersistentDisposition;
import opentraceability.models.identifiers.SensorElement;
import opentraceability.utility.attributes.EPCISVersion;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttributeType;
import opentraceability.utility.attributes.OpenTraceabilityAttributes;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import opentraceability.utility.attributes.OpenTraceabilityProductsAttribute;
import opentraceability.utility.attributes.OpenTraceabilityProductsListType;
import opentraceability.utility.attributes.OpenTraceabilityXmlIgnoreAttribute;

public class AssociationEvent extends EventBase implements IEvent {
    @OpenTraceabilityAttribute(productOwner = "", name = "parentID", order = 7)
    public EPC parentID;

    @OpenTraceabilityProductsAttribute(productOwner = "", epcis = org.ePCIS.V2.EPCIS.class, productType = EventProductType.Child, order = 9, listType = OpenTraceabilityProductsListType.QuantityList )
    @OpenTraceabilityProductsAttribute(productOwner = "", epcis = org.ePCIS.V2.EPCIS.class, productType = EventProductType.Child, order = 8, listType = OpenTraceabilityProductsListType.EPCList)
    public List<EventProduct> children = new ArrayList<>();

    @OpenTraceabilityAttribute(productOwner = "", name = "action", order = 10)
    public EventAction action;

    @OpenTraceabilityAttribute(productOwner = "", name = "bizStep", order = 11)
    public URI businessStep;

    @OpenTraceabilityAttribute(productOwner = "", name = "disposition", order = 12)
    public URI disposition;

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityAttribute(productOwner = "", name = "readPoint", order = 13)
    public EventReadPoint readPoint = new EventReadPoint();

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityAttribute(productOwner = "", name = "bizLocation", order = 14)
    public EventLocation location;

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityArrayAttribute(name = "bizTransaction")
    @OpenTraceabilityAttribute(productOwner = "", name = "bizTransactionList", order = 15, epcis = org.ePCIS.V2.EPCIS.class)
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityArrayAttribute(name = "source")
    @OpenTraceabilityAttribute(productOwner = "", name = "sourceList", order = 16, epcis = org.ePCIS.V2.EPCIS.class)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityArrayAttribute(name = "destination")
    @OpenTraceabilityAttribute(productOwner = "", name = "destinationList", order = 17, epcis = org.ePCIS.V2.EPCIS.class)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityArrayAttribute(name = "sensorElement")
    @OpenTraceabilityAttribute(productOwner = "", name = "sensorElementList", order = 18, epcis = org.ePCIS.V2.EPCIS.class)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute()
    @OpenTraceabilityAttribute(productOwner = "", name = "persistentDisposition", order = 19)
    public PersistentDisposition persistentDisposition = new PersistentDisposition();

    public EventILMD ilmd = new EventILMD();

    @OpenTraceabilityXmlIgnoreAttribute()
    @OpenTraceabilityAttribute(productOwner = "", name = "type", order = 0, type = OpenTraceabilityAttributeType.STRING)
    public EventType eventType;

    public List<EventProduct> getProducts() {
        List<EventProduct> prods = new ArrayList<>();
        if (parentID != null) {
            prods.add(new EventProduct(parentID) {{ Type = EventProductType.Parent;}});
        }
        prods.addAll(this.children);
        return prods;
    }

    public void setProducts(List<EventProduct> products) {
        for(EventProduct product: products) {
            addProduct(product);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends IEventKDE> T getKDE(Class<T> clazz) { 
        return super.getKDE(clazz); 
    }

    public <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name) { 
        return super.getKDE(clazz, ns, name); 
    }

    public void addProduct(EventProduct product) {
        switch (product.Type) {
            case Parent: {
                if (product.Quantity != null) {
                    throw new RuntimeException("Parents do not support quantity.");
                }
                parentID = product.EPC;
                break;
            }
            case Child: {
                children.add(product);
                break;
            }
            default: {
                throw new RuntimeException("Association event only supports children and parents.");
            }
        }
    }

    public EventILMD grabILMD() {
        return ilmd;
    }
}