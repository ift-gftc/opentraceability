package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import java.time.OffsetDateTime;

public class Certificate {
    @OpenTraceabilityJsonAttribute("gdst:certificationType")
    @OpenTraceabilityAttribute(Constants.GDST_NAMESPACE, "certificationType")
    public String certificateType = null;

    @OpenTraceabilityAttribute("", "certificationAgency")
    public String agency = null;

    @OpenTraceabilityAttribute("", "certificationStandard")
    public String standard = null;

    @OpenTraceabilityAttribute("", "certificationValue")
    public String value = null;

    @OpenTraceabilityAttribute("", "certificationIdentification")
    public String identification = null;

    @OpenTraceabilityAttribute("", "certificationStartDate")
    public OffsetDateTime startDate = null;

    @OpenTraceabilityAttribute("", "certificationEndDate")
    public OffsetDateTime endDate = null;
}