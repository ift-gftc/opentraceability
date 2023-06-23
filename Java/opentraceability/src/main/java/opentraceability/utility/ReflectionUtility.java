package opentraceability.utility;

import opentraceability.models.events.EventAction;
import opentraceability.models.events.EventProduct;
import opentraceability.models.events.EventType;
import opentraceability.models.events.ObjectEvent;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.GLN;
import opentraceability.models.identifiers.GTIN;
import opentraceability.models.identifiers.PGLN;
import opentraceability.utility.attributes.OpenTraceabilityAttribute;
import opentraceability.utility.attributes.OpenTraceabilityAttributes;
import opentraceability.utility.attributes.OpenTraceabilityProductsAttribute;
import opentraceability.utility.attributes.OpenTraceabilityProductsAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtility
{
    public static Object constructType(Class type) throws Exception
    {
        if (type == null)
        {
            return null;
        }

        if (List.class.isAssignableFrom(type)) {
            return (ArrayList.class).getDeclaredConstructor().newInstance();
        }

        try {
            // Construct and return the type using reflection
            Class<?> clazz = Class.forName(type.getTypeName());
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException("Error constructing object of type: " + type.getTypeName(), ex);
        }
    }

    public static boolean isTypeInherited(Class childType, Class baseType) {
        Class<?> childClass = (Class<?>) childType;
        Class<?> parentClass = (Class<?>) baseType;

        return parentClass.isAssignableFrom(childClass);
    }

    public static List<Annotation> getFieldAnnotations(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        List<Annotation> annotationList = new ArrayList<>();

        Collections.addAll(annotationList, annotations);

        return annotationList;
    }

    public static <T extends Annotation> List<T> getFieldAnnotations(Field field, Class<T> annotationClass) {
        List<T> matchingAnnotations = new ArrayList<>();
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation))
            {
                matchingAnnotations.add(annotationClass.cast(annotation));
            }
            else
            {
                if (annotationClass.equals(OpenTraceabilityAttribute.class))
                {
                    if (annotation.annotationType().equals(OpenTraceabilityAttributes.class))
                    {
                        OpenTraceabilityAttributes att = (OpenTraceabilityAttributes)annotation;
                        for (var a: att.value())
                        {
                            matchingAnnotations.add((T)a);
                        }
                    }
                }
                else if (annotationClass.equals(OpenTraceabilityProductsAttribute.class))
                {
                    if (annotation.annotationType().equals(OpenTraceabilityProductsAttributes.class))
                    {
                        OpenTraceabilityProductsAttributes att = (OpenTraceabilityProductsAttributes)annotation;
                        for (var a: att.value())
                        {
                            matchingAnnotations.add((T)a);
                        }
                    }
                }
            }
        }

        return matchingAnnotations;
    }

    public static <T extends Annotation> T getFieldAnnotation(Field field, Class<T> annotationClass) {
        List<T> matchingAnnotations = getFieldAnnotations(field, annotationClass);

        if (matchingAnnotations.isEmpty())
        {
            return null;
        }
        else {
            return matchingAnnotations.get(0);
        }
    }

    public static Class getItemType(Class t) {
        if (t.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)t.getGenericSuperclass();
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType.equals(ArrayList.class) && typeArguments.length == 1) {
                Class genericType = (Class)typeArguments[0];
                return genericType;
            }
        }

        return null;
    }

    public static Boolean isListOf(Class type, Class<?> itemType)
    {
        if (type.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)type.getGenericSuperclass();
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType.equals(ArrayList.class) && typeArguments.length == 1) {
                Type genericType = typeArguments[0];
                return genericType.getClass().equals(itemType);
            }
        }

        return false;
    }

    public static Object parseFromString(Class t, String value) throws Exception {
        if (t == OffsetDateTime.class || t == OffsetDateTime.class)
        {
            OffsetDateTime o = OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            return o;
        }
        else if (t == UOM.class)
        {
            UOM uom = UOMS.getUOMFromUNCode(value);
            return uom;
        }
        else if (t == Boolean.class || t == Boolean.class)
        {
            boolean v = Boolean.parseBoolean(value);
            return v;
        }
        else if (t == Double.class || t == Double.class)
        {
            double v = Double.parseDouble(value);
            return v;
        }
        else if (t == URI.class)
        {
            URI v = URI.create(value);
            return v;
        }
        else if (t == Duration.class)
        {
            Duration ts = StringExtensions.toDuration(value);
            return ts;
        }
        else if (t == EventAction.class)
        {
            EventAction action = Enum.valueOf(EventAction.class, value);
            return action;
        }
        else if (t == EventType.class)
        {
            EventType type = Enum.valueOf(EventType.class, value);
            return type;
        }
        else if (t == PGLN.class)
        {
            PGLN pgln = new PGLN(value);
            return pgln;
        }
        else if (t == GLN.class)
        {
            GLN gln = new GLN(value);
            return gln;
        }
        else if (t == GTIN.class)
        {
            GTIN gtin = new GTIN(value);
            return gtin;
        }
        else if (t == EPC.class)
        {
            EPC epc = new EPC(value);
            return epc;
        }
        else if (t == Country.class)
        {
            Country c = Countries.parse(value);
            return c;
        }
        else
        {
            return value;
        }
    }

    public static <T extends Annotation> T getAnnotation(Class type, Class<T> annotationClass) {
        Annotation[] annotations = type.getClass().getDeclaredAnnotations();
        List<Annotation> annotationList = new ArrayList<>();

        for (Annotation annotation : annotations) {
            return (T)annotation;
        }

        return null;
    }
}
