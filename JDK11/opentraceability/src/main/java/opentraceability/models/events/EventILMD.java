package opentraceability.models.events;

import opentraceability.interfaces.IEventKDE;
import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityExtensionElementsAttribute;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;
import opentraceability.utility.attributes.OpenTraceabilityRepeatingAttribute;
import opentraceability.utility.Country;

import java.time.OffsetDateTime;
import java.util.List;

import opentraceability.models.events.kdes.CertificationList;

public class EventILMD {
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="productionMethodForFishAndSeafoodCode")
    @OpenTraceabilityJsonAttribute(name="cbvmda:productionMethodForFishAndSeafoodCode")
    public String productionMethodForFishAndSeafoodCode;

    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="itemExpirationDate")
    @OpenTraceabilityJsonAttribute(name="cbvmda:itemExpirationDate")
    public OffsetDateTime itemExpirationDate;

    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="productionDate")
    @OpenTraceabilityJsonAttribute(name="cbvmda:productionDate")
    public OffsetDateTime productionDate;

    @OpenTraceabilityArrayAttribute(itemType = Country.class)
    @OpenTraceabilityRepeatingAttribute
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="countryOfOrigin")
    @OpenTraceabilityJsonAttribute(name="cbvmda:countryOfOrigin")
    public List<Country> countryOfOrigin;

    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="lotNumber")
    @OpenTraceabilityJsonAttribute(name="cbvmda:lotNumber")
    public String lotNumber;

    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns = Constants.CBVMDA_NAMESPACE, name="certificationList")
    @OpenTraceabilityJsonAttribute(name="cbvmda:certificationList")
    public CertificationList certificationList;

    @OpenTraceabilityExtensionElementsAttribute
    public List<IEventKDE> extensionKDEs;
}