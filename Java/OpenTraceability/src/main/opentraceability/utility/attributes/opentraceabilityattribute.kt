package utility.attributes

import models.identifiers.*
import models.events.*
import models.events.EPCISVersion
import java.lang.reflect.Type
import javax.xml.namespace.QName

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenTraceabilityAttribute(
    val ns: String,
    val name: String,
    val sequenceOrder: Int = -1,
    val version: EPCISVersion = EPCISVersion.V1
)

fun getXNamespace(ns: String): String {
    return ns // Replace this with the appropriate logic to convert the namespace to XNamespace.
}

fun buildQualifiedName(ns: String, name: String): String {
    val namespace = getXNamespace(ns)
    return QName(namespace, name).toString()
}