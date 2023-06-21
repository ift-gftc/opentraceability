package opentraceability.models.common;

import opentraceability.Constants;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityJsonAttribute;
import java.time.OffsetDateTime;

public class Certificate {
    @OpenTraceabilityJsonAttribute(name="gdst:certificationType")
    @OpenTraceabilityAttribute(ns = Constants.GDST_NAMESPACE, name="certificationType")
    public String certificateType = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationAgency")
    public String agency = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationStandard")
    public String standard = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationValue")
    public String value = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationIdentification")
    public String identification = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationStartDate")
    public OffsetDateTime startDate = null;

    @OpenTraceabilityAttribute(ns = "", name="certificationEndDate")
    public OffsetDateTime endDate = null;
}