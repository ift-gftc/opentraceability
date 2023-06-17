package opentraceability.utility.attributes;

import opentraceability.models.events.EPCISVersion;

import javax.xml.namespace.QName;
import java.lang.annotation.*;

@Repeatable(OpenTraceabilityAttributes.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenTraceabilityAttribute {
    String ns();

    String name();

    int sequenceOrder() default -1;

    EPCISVersion version() default EPCISVersion.Any;
}

