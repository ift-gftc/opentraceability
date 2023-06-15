package opentraceability.models.events;

import opentraceability.Constants;
import opentraceability.interfaces.IEventKDE;
import opentraceability.models.events.kdes.CertificationList;
import opentraceability.utility.attributes.*;
import opentraceability.models.identifiers.PGLN;

import java.net.URI;
import java.time.*;

public class EventBase {
    @OpenTraceabilityAttribute(value = "", name = "eventTime", order = 1)
    public OffsetDateTime eventTime;

    @OpenTraceabilityAttribute(value = "", name = "recordTime", order = 2)
    public OffsetDateTime recordTime;

    @OpenTraceabilityAttribute(value = "", name = "eventTimeZoneOffset", order = 3)
    public Duration eventTimeZoneOffset;

    @OpenTraceabilityAttribute(value = "", name = "eventID", order = 4, versions = {EPCISVersion.V2, EPCISVersion.V1}, conditionalVersions = {})
    public URI eventID;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(value = "", name = "errorDeclaration", order = 5, versions = {EPCISVersion.V2, EPCISVersion.V1}, conditionalVersions = {})
    public ErrorDeclaration errorDeclaration;

    @OpenTraceabilityAttribute(value = "", name = "certificationInfo", order = 6, versions = {EPCISVersion.V2}, conditionalVersions = {})
    public String certificationInfo;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(value = Constants.CBVMDA_NAMESPACE, name = "certificationList", versions = {}, conditionalVersions = {})
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    public CertificationList certificationList;

    @OpenTraceabilityAttribute(value = Constants.CBVMDA_NAMESPACE, name = "informationProvider", versions = {}, conditionalVersions = {})
    @OpenTraceabilityJsonAttribute("cbvmda:informationProvider")
    public PGLN informationProvider;

    @OpenTraceabilityExtensionElementsAttribute
    public MutableList<IEventKDE> kdes = new MutableList<>();

    public <T extends IEventKDE> T getKDE(Class<T> clazz, String ns, String name) {
        IEventKDE kde = kdes.find(kde -> kde.namespace == ns && kde.name == name);
        if (kde != null) {
            if (clazz.isInstance(kde)) {
                @SuppressWarnings("unchecked")
                T result = (T) kde;
                return result;
            }
        }
        return null;
    }

    public <T extends IEventKDE> T getKDE(Class<T> clazz) {
        IEventKDE kde = kdes.find(kde -> clazz.isInstance(kde));
        if (kde != null) {
            @SuppressWarnings("unchecked")
            T result = (T) kde;
            return result;
        }
        return null;
    }
}