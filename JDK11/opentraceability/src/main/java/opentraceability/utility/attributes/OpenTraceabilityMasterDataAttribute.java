package opentraceability.utility.attributes;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface OpenTraceabilityMasterDataAttribute {
    String ns() default "";
    String name() default "";
}