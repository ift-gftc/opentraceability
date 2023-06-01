package utility

import java.util.*

object EnumUtil {
    inline fun <reified T : Enum<T>> GetEnumDescription(value: T): String? {
        try {
            val field = value::class.java.getField(value.name)
            val displayAnnotation = field.getAnnotationsByType(opentraceability.utility.attributes.Description::class.java)
            return displayAnnotation.firstOrNull()?.Description
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> GetEnumDisplayName(value: T): String? {
        try {
            val field = value::class.java.getField(value.name)
            val displayAnnotation = field.getAnnotationsByType(opentraceability.utility.attributes.Display::class.java)
            return displayAnnotation.firstOrNull()?.Name
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <T : Enum<T>, reified A : Annotation> GetEnumAttributes(value: T): List<A> {
        try {
            val field = value::class.java.getField(value.name)
            val annotations = field.getAnnotationsByType(A::class.java)
            return annotations.toList()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> GetValues(): List<T> {
        return enumValues<T>().toList()
    }



}
