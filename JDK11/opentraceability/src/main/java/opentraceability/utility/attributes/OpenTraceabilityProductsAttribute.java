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
    public String name();
    public EPCISVersion version();
    public EventProductType productType();
    public OpenTraceabilityProductsListType listType();
    public boolean required() default false;
}

