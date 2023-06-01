package utility

import java.util.*

object EnumUtil {
    inline fun <reified T : Enum<T>> GetEnumDescription(value: T): String {
        try {
            val field = value::class.java.getField(value.name)
            val descriptionAnnotation = field.GetAnnotation(DescriptionAttribute::class.java)
            return descriptionAnnotation?.description ?: ""
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> GetEnumDisplayName(value: T): String? {
        try {
            val field = value::class.java.getField(value.name)
            val displayAnnotation = field.GetAnnotation(DisplayAttribute::class.java)
            return displayAnnotation?.name ?: value.name
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>, reified A : Annotation> GetEnumAttributes(value: T): List<A> {
        try {
            val field = value::class.java.getField(value.name)
            val annotation = field.getAnnotation(A::class.java)
            return if (annotation != null) listOf(annotation) else emptyList()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> GetValues(): List<T> {
        return enumValues<T>().toList()
    }



}
