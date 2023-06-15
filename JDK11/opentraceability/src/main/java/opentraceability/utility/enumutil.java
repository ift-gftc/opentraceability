package opentraceability.utility;

import opentraceability.OTLogger;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EnumUtil {
    public static <T extends Enum<T>> String GetEnumDescription(T value) {
        try {
            Field field = value.getClass().getField(value.name());
            opentraceability.utility.attributes.Description[] displayAnnotation = field.getAnnotationsByType(opentraceability.utility.attributes.Description.class);
            return displayAnnotation.length > 0 ? displayAnnotation[0].Description() : null;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static <T extends Enum<T>> String GetEnumDisplayName(T value) {
        try {
            Field field = value.getClass().getField(value.name());
            opentraceability.utility.attributes.Display[] displayAnnotation = field.getAnnotationsByType(opentraceability.utility.attributes.Display.class);
            return displayAnnotation.length > 0 ? displayAnnotation[0].Name() : null;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static <T extends Enum<T>, A extends Annotation> List<A> GetEnumAttributes(T value) {
        try {
            Field field = value.getClass().getField(value.name());
            A[] annotations = field.getAnnotationsByType((Class<A>) Annotation.class);
            List<A> annotationList = new ArrayList<>();
            for (A annotation : annotations) {
                annotationList.add(annotation);
            }
            return annotationList;
        } catch (Exception ex) {
            OTLogger.error(ex);
            throw ex;
        }
    }

    public static <T extends Enum<T>> List<T> GetValues() {
        return List.of(Enum.<T>values());
    }
}