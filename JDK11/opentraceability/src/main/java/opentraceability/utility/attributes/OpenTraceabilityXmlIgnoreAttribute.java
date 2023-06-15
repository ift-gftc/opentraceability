package opentraceability.utility.attributes;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface OpenTraceabilityXmlIgnoreAttribute {
}