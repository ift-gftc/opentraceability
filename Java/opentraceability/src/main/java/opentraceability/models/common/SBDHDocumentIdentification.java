package opentraceability.models.common;

import java.time.OffsetDateTime;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import static opentraceability.Constants.SBDH_NAMESPACE;

public class SBDHDocumentIdentification {
    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="Standard", sequenceOrder = 1)
    public String Standard = "";

    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="TypeVersion", sequenceOrder = 2)
    public String TypeVersion = "";

    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="InstanceIdentifier", sequenceOrder = 3)
    public String InstanceIdentifier = null;

    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="Type", sequenceOrder = 4)
    public String Type = "";

    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="MultipleType", sequenceOrder = 5)
    public String MultipleType = "";

    @OpenTraceabilityAttribute(ns = SBDH_NAMESPACE, name="CreationDateAndTime", sequenceOrder = 6)
    public OffsetDateTime CreationDateAndTime = null;

}