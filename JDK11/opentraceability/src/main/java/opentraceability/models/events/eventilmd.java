package opentraceability.models.events;

import opentraceability.interfaces.IEventKDE;
import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import opentraceability.utility.attributes.OpenTraceabilityRepeatingAttribute;
import opentraceability.utility.attributes.OpenTraceabilityUtility;
import opentraceability.utility.Country;

import java.time.OffsetDateTime;
import java.util.List;

import opentraceability.models.events.kdes.CertificationList;

public class EventILMD {
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionMethodForFishAndSeafoodCode")
    @OpenTraceabilityJsonAttribute("cbvmda:productionMethodForFishAndSeafoodCode")
    public String productionMethodForFishAndSeafoodCode;

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")
    @OpenTraceabilityJsonAttribute("cbvmda:itemExpirationDate")
    public OffsetDateTime itemExpirationDate;

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "productionDate")
    @OpenTraceabilityJsonAttribute("cbvmda:productionDate")
    public OffsetDateTime productionDate;

    @OpenTraceabilityArrayAttribute
    @OpenTraceabilityRepeatingAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")
    @OpenTraceabilityJsonAttribute("cbvmda:countryOfOrigin")
    public List<Country> countryOfOrigin;

    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "lotNumber")
    @OpenTraceabilityJsonAttribute("cbvmda:lotNumber")
    public String lotNumber;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(Constants.CBVMDA_NAMESPACE, "certificationList")
    @OpenTraceabilityJsonAttribute("cbvmda:certificationList")
    public CertificationList certificationList;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IEventKDE> extensionKDEs;
}