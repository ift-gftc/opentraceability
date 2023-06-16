package opentraceability.utility;

import opentraceability.utility.attributes.OpenTraceabilityAttribute;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CBVAttributes {
    public CBVAttribute[] value();
}
