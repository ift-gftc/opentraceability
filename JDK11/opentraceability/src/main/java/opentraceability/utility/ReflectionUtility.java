package opentraceability.utility;

import opentraceability.models.events.EventAction;
import opentraceability.models.events.EventProduct;
import opentraceability.models.identifiers.EPC;
import opentraceability.models.identifiers.GLN;
import opentraceability.models.identifiers.GTIN;
import opentraceability.models.identifiers.PGLN;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtility
{
    public static Object constructType(Type type) throws Exception
    {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType.equals(ArrayList.class) && typeArguments.length == 1) {
                Type genericType = typeArguments[0];

                if (genericType.equals(String.class)) {
                    // Construct and return an ArrayList<String> object
                    return new ArrayList<String>();
                } else if (genericType.equals(EventProduct.class)) {
                    // Construct and return an ArrayList<EventProduct> object
                    return new ArrayList<EventProduct>();
                }
                else {
                    throw new IllegalArgumentException("Error constructing ArrayList with generic type: " + genericType.getTypeName());
                }
            }
        }

        try {
            // Construct and return the type using reflection
            Class<?> clazz = Class.forName(type.getTypeName());
            return clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            throw new IllegalArgumentException("Error constructing object of type: " + type.getTypeName(), ex);
        }
    }

    public static boolean isTypeInherited(Type childType, Type baseType) {
        Class<?> childClass = (Class<?>) childType;
        Class<?> parentClass = (Class<?>) baseType;

        return parentClass.isAssignableFrom(childClass);
    }

    public static List<Annotation> getFieldAnnotations(Field field) {
        Annotation[] annotations = field.getDeclaredAnnotations();
        List<Annotation> annotationList = new ArrayList<>();

        for (Annotation annotation : annotations) {
            annotationList.add(annotation);
        }

        return annotationList;
    }

    public static <T extends Annotation> List<T> getFieldAnnotations(Field field, Class<T> annotationClass) {
        List<T> matchingAnnotations = new ArrayList<>();

        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                matchingAnnotations.add(annotationClass.cast(annotation));
            }
        }

        return matchingAnnotations;
    }

    public static <T extends Annotation> T getFieldAnnotation(Field field, Class<T> annotationClass) {
        List<T> matchingAnnotations = new ArrayList<>();

        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationClass)) {
                matchingAnnotations.add(annotationClass.cast(annotation));
            }
        }

        if (matchingAnnotations.isEmpty())
        {
            return null;
        }
        else {
            return matchingAnnotations.get(0);
        }
    }

    public static Type getItemType(Type t) {
        if (t instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType.equals(ArrayList.class) && typeArguments.length == 1) {
                Type genericType = typeArguments[0];
                return genericType;
            }
        }

        return null;
    }

    public static Boolean isListOf(Type type, Class<?> itemType)
    {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (rawType.equals(ArrayList.class) && typeArguments.length == 1) {
                Type genericType = typeArguments[0];
                if (genericType.getClass().equals(itemType))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static Object parseFromString(Type t, String value)
    {
        if (t == OffsetDateTime.class || t == OffsetDateTime.class)
        {
            OffsetDateTime o = OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            return o;
        }
        else if (t == UOM.class)
        {
            UOM uom = UOM.lookUpFromUNCode(value);
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
            if (value.startsWith("+"))
            {
                value = value.substring(1);
            }
            Duration ts = Duration.parse(value);
            return ts;
        }
        else if (t == EventAction.class)
        {
            EventAction action = Enum.valueOf(EventAction.class, value);
            return action;
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
}
