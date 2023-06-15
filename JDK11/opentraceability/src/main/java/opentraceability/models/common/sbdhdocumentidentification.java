package opentraceability.models.common;

import java.time.OffsetDateTime;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import static opentraceability.Constants.SBDH_NAMESPACE;

public class SBDHDocumentIdentification {
    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "standard", 1)
    public String Standard = "";

    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "TypeVersion", 2)
    public String TypeVersion = "";

    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "InstanceIdentifier", 3)
    public String InstanceIdentifier = null;

    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "Type", 4)
    public String Type = "";

    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "MultipleType", 5)
    public String MultipleType = "";

    @OpenTraceabilityAttribute(SBDH_NAMESPACE, "CreationDateAndTime", 6)
    public OffsetDateTime CreationDateAndTime = null;

}