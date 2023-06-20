package opentraceability.models.events.kdes;

import java.util.*;
import opentraceability.models.common.Certificate;
import opentraceability.utility.attributes.OpenTraceabilityArrayAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityObjectAttribute;

public class CertificationList {
    @OpenTraceabilityArrayAttribute(itemType = Certificate.class)
    @OpenTraceabilityObjectAttribute
    @OpenTraceabilityAttribute(ns="", name="certification")
    public List<Certificate> certificates = new ArrayList<Certificate>();
}