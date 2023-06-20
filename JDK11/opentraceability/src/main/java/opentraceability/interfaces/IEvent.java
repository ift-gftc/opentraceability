package opentraceability.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import opentraceability.Constants;
import opentraceability.models.events.*;
import opentraceability.models.events.kdes.CertificationList;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.*;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class IEvent
{
    @OpenTraceabilityAttribute(ns = "", name = "eventTime")
    public OffsetDateTime eventTime;

    @OpenTraceabilityAttribute(ns = "", name = "recordTime")
    public OffsetDateTime recordTime;

    @OpenTraceabilityAttribute(ns = "", name = "eventTimeZoneOffset")
    public Duration eventTimeZoneOffset;

    @OpenTraceabilityAttribute(ns = "", name = "eventID")
    public URI eventID;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "errorDeclaration")
    public ErrorDeclaration errorDeclaration;

    @OpenTraceabilityAttribute(ns = "", name = "certificationInfo", version = EPCISVersion.V2)
    public String certificationInfo;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "certificationList")
    @OpenTraceabilityJsonAttribute(name="cbvmda:certificationList")
    public CertificationList certificationList;

    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name = "informationProvider")
    @OpenTraceabilityJsonAttribute(name="cbvmda:informationProvider")
    public PGLN informationProvider;

    @OpenTraceabilityExtensionElementsAttribute
    public ArrayList<IEventKDE> kdes = new ArrayList<>();

    @JsonProperty("@type")
    public EventType eventType;

    @OpenTraceabilityAttribute(ns = "", name = "action")
    public EventAction action = null;

    @OpenTraceabilityAttribute(ns = "", name = "bizStep")
    public URI businessStep = null;

    @OpenTraceabilityAttribute(ns = "", name = "disposition")
    public URI disposition = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "readPoint")
    public EventReadPoint readPoint = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "bizLocation")
    public EventLocation location = null;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(itemName = "bizTransaction", itemType = EventBusinessTransaction.class)
    @OpenTraceabilityAttribute(ns = "", name = "bizTransactionList")
    public List<EventBusinessTransaction> bizTransactionList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(itemName = "source", itemType = EventSource.class)
    @OpenTraceabilityAttribute(ns = "", name = "sourceList", version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/sourceList", version = EPCISVersion.V1)
    public List<EventSource> sourceList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(itemName = "destination", itemType = EventDestination.class)
    @OpenTraceabilityAttribute(ns = "", name = "destinationList", version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/destinationList", version = EPCISVersion.V1)
    public List<EventDestination> destinationList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityArrayAttribute(itemName = "sensorElement", itemType = SensorElement.class)
    @OpenTraceabilityAttribute(ns = "",  name = "sensorElementList", version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/sensorElementList", version = EPCISVersion.V1)
    public List<SensorElement> sensorElementList = new ArrayList<>();

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = "", name = "persistentDisposition", version = EPCISVersion.V2)
    @OpenTraceabilityAttribute(ns = "", name = "extension/persistentDisposition", version = EPCISVersion.V1)
    public PersistentDisposition persistentDisposition = null;

    public abstract List<EventProduct> getProducts();

    public <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name) {
        Optional<IEventKDE> kde = kdes.stream().filter(k -> k.namespace == ns && k.name == name).findFirst();
        if (kde.isPresent()) {
            if (clazz.isInstance(kde)) {
                @SuppressWarnings("unchecked")
                T result = (T)kde.get();
                return result;
            }
        }
        return null;
    }

    public <T extends IEventKDE> T getKDE(Class<T> clazz) {
        Optional<IEventKDE> kde = kdes.stream().filter(k -> clazz.isInstance(k)).findFirst();
        if (kde.isPresent()) {
            @SuppressWarnings("unchecked")
            T result = (T)kde.get();
            return result;
        }
        return null;
    }
    public abstract void addProduct(EventProduct product) throws Exception;
    public abstract EventILMD grabILMD();
}