package opentraceability.utility;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(CBVAttributes.class)
public @interface CBVAttribute {
    String value();
}