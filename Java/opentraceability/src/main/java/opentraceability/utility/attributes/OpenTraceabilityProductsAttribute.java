package opentraceability.utility.attributes;

import opentraceability.models.events.EPCISVersion;
import opentraceability.models.events.EventProductType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(OpenTraceabilityProductsAttributes.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenTraceabilityProductsAttribute {
    String name();

    EPCISVersion version() default EPCISVersion.Any;
    EventProductType productType();
    OpenTraceabilityProductsListType listType();

    int sequenceOrder() default -1;

    boolean required() default false;
}

