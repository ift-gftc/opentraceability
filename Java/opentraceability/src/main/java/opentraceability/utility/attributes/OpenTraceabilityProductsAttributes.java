package opentraceability.utility.attributes;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface OpenTraceabilityProductsAttributes {
    OpenTraceabilityProductsAttribute[] value();
}
