package opentraceability.interfaces;

import opentraceability.models.events.*;
import opentraceability.models.events.kdes.CertificationList;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.List;

public abstract class IEvent {
    public URI eventID = null;
    public String certificationInfo = null;
    public OffsetDateTime eventTime = null;
    public Duration eventTimeZoneOffset = null;
    public OffsetDateTime recordTime = null;
    public EventType eventType = null;
    public EventAction action = null;
    public URI businessStep = null;
    public URI disposition = null;
    public PersistentDisposition persistentDisposition = null;
    public ErrorDeclaration errorDeclaration = null;
    public EventLocation location = null;
    public EventReadPoint readPoint = null;
    public List<EventBusinessTransaction> bizTransactionList = null;
    public List<EventSource> sourceList = null;
    public List<EventDestination> destinationList = null;
    public List<IEventKDE> kdes = null;
    public List<SensorElement> sensorElementList = null;
    public List<EventProduct> products = null;
    public CertificationList certificationList = null;

    abstract <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name);
    abstract <T extends IEventKDE> T getKDE(Class<T> clazz);
    public abstract void addProduct(EventProduct product);
    abstract EventILMD grabILMD();
}