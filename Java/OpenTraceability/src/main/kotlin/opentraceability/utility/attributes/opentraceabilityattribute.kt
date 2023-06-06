package opentraceability.utility.attributes

import opentraceability.models.events.EPCISVersion
import javax.xml.namespace.QName

@Repeatable
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenTraceabilityAttribute(
    val ns: String ,
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