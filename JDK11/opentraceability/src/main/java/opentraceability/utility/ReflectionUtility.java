package opentraceability.utility;

import opentraceability.models.events.EventProduct;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
}
