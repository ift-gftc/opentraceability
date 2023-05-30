package utility

import java.util.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

object EnumUtil {
    inline fun <reified T : Enum<T>> getEnumDescription(value: T): String {
        try {
            val field = value::class.java.getField(value.name)
            val descriptionAnnotation = field.getAnnotation(DescriptionAttribute::class.java)
            return descriptionAnnotation?.description ?: ""
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> getEnumDisplayName(value: T): String? {
        try {
            val field = value::class.java.getField(value.name)
            val displayAnnotation = field.getAnnotation(DisplayAttribute::class.java)
            return displayAnnotation?.name ?: value.name
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>, reified A : Annotation> getEnumAttributes(value: T): List<A> {
        try {
            val field = value::class.java.getField(value.name)
            val annotation = field.getAnnotation(A::class.java)
            return if (annotation != null) listOf(annotation) else emptyList()
        } catch (ex: Exception) {
            OTLogger.error(ex)
            throw ex
        }
    }

    inline fun <reified T : Enum<T>> getValues(): List<T> {
        return enumValues<T>().toList()
    }
}
