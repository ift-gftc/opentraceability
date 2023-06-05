package opentraceability.mappers

import opentraceability.interfaces.*
import opentraceability.models.common.LanguageString
import opentraceability.models.identifiers.*
import opentraceability.models.events.*
import opentraceability.models.events.kdes.*
import org.w3c.dom.Element
import opentraceability.utility.attributes.OpenTraceabilityAttribute
import opentraceability.utility.*
import opentraceability.utility.StringExtensions.splitXPath
import opentraceability.utility.StringExtensions.tryConvertToDateTimeOffset
import org.w3c.dom.Node
import java.net.URI
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

class OpenTraceabilityXmlMapper {
    companion object {

        fun toXml(xname: String, value: Any?, version: EPCISVersion, required: Boolean = false): Element? {
            return if (value != null) {
                var x: Element? = createXmlElement(xname)
                var xvalue = x

                // make sure we have created the xml element correctly.
                val xParts = xname.splitXPath()
                while (xParts.size > 1) {
                    val p = xParts.removeAt(0);
                    if (xvalue?.getFirstElementByXPath(p) == null) {
                        xvalue?.addElement(p)
                    }
                    xvalue = xvalue?.getFirstElementByXPath(p) ?: throw Exception("Failed to add xml element, p=$p")
                }

                if (value is MutableList<*>) {
                    if (value.isNotEmpty()) {
                        val t = value.first()!!::class.java
                        val xchildname = t.getAnnotation(OpenTraceabilityAttribute::class.java)?.name
                            ?: throw Exception("Failed to get xname from type. type = ${t.name}")
                        value.forEach { v ->
                            val xListValue = toXml(xchildname, v, version, required)
                            if (xListValue != null) {
                                xvalue?.addElement(xListValue)
                            }
                        }
                    } else if (!required) {
                        x = null
                    }
                } else {
                    // Here we can't port exactly as in C# code because the typeinfo depends on the specific project
                    // Further implementation requires understanding the OTMappingTypeInformation class and its methods
                    // Assuming you implement it, you can loop over the properties like this:
                    val typeInfo = OTMappingTypeInformation.getXmlTypeInfo(value::class.starProjectedType as KClass<*>)
                    typeInfo.properties.filter { it.Version == null || it.Version == version }.forEach { property ->
                        // Further operations on properties
                    }

                    // Further implementation depends on the specific project, assuming you implement required methods, you can do:

                    // typeInfo.extensionKDEs?.let { extKDEs ->
                    //     val obj = extKDEs.getValue(value)
                    //     if (obj is MutableList<IEventKDE>) {
                    //         obj.forEach { kde ->
                    //             val xchild = kde.getXml()
                    //             if (xchild != null) {
                    //                 xvalue.add(xchild)
                    //             }
                    //         }
                    //     }
                    // }

                    // typeInfo.extensionAttributes?.let { extAttrs ->
                    //     val obj = extAttrs.getValue(value)
                    //     if (obj is MutableList<IEventKDE>) {
                    //         obj.forEach { kde ->
                    //             val xKDE = kde.getXml()
                    //             if (xKDE != null) {
                    //                 xvalue.add(Attribute(xKDE.name, xKDE.value))
                    //             }
                    //         }
                    //     }
                    // }
                }
                x
            } else if (required) {
                createXmlElement(xname)
            } else {
                null
            }
        }

        inline fun <reified T> fromXml(x: Element, version: EPCISVersion): T {
            return fromXml(x, T::class.createType() as KClass<*>, version) as T
        }

        @Throws(Exception::class)
        fun fromXml(x: Element, type: KClass<*>, version: EPCISVersion): Any? {
            val value: Any = type.createInstance()

            try {
                val mappingInfo = OTMappingTypeInformation.getXmlTypeInfo(type as KClass<*>)

                // if this is a list, then we will make a list of the objects...
                if (value is MutableList<*>) {
                    val list = value as MutableList<Any?>
                    val att = type.javaObjectType.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (att != null)
                    {
                        x.getElementsByXPath(att.name)?.forEachIndex { xChild, _ ->
                            val childValue = fromXml(xChild, type.typeParameters[0].starProjectedType as KClass<*>, version)
                            list.add(childValue)
                        }
                    }
                    else
                    {
                        x.childNodes.forEachIndex { xChild, _ ->
                            val childValue = fromXml(xChild, type.typeParameters[0].starProjectedType as KClass<*>, version)
                            list.add(childValue)
                        }
                    }
                } else {
                    val typeInfo = OTMappingTypeInformation.getXmlTypeInfo(type)

                    var extensionKDEs: MutableList<IEventKDE>? = null
                    var extensionAttributes: MutableList<IEventKDE>? = null

                    if (typeInfo.extensionAttributes != null) {
                        extensionAttributes = mutableListOf<IEventKDE>()
                    }

                    if (typeInfo.extensionKDEs != null) {
                        extensionKDEs = mutableListOf<IEventKDE>()
                    }

                    var mappingProp: OTMappingTypeInformationProperty?

                    for (i in 0 until x.attributes.length) {
                        val att = x.attributes.item(i)
                        val attName = att.nodeName
                        val attValue = att.nodeValue

                        mappingProp = typeInfo["@" + attName]
                        if (mappingProp != null)
                        {
                            if (!attValue.isNullOrEmpty())
                            {
                                val o = readObjectFromString(attValue, mappingProp.Property.returnType)
                                mappingProp.Property?.setter?.call(value, o)
                            }
                        }
                        else if (extensionAttributes != null)
                        {
                            val kde = readAttributeKDE(att, attName, attValue)
                            extensionAttributes.add(kde)
                        }
                    }

                    mappingProp = typeInfo["text()"]
                    if (mappingProp != null) {
                        val eleText = x.nodeValue
                        if (eleText.isNotBlank())
                        {
                            val o = readObjectFromString(eleText, mappingProp.Property.returnType)
                            mappingProp.Property?.setter?.call(value, o)
                        }
                    } else {
                        x.childNodes.forEachIndex { xc, _ ->
                            val xchild = xc

                            mappingProp = typeInfo[xchild.nodeName]
                            if (mappingProp == null && typeInfo.properties.any { p -> p.Name.splitXPath().first() == xchild.nodeName }) {

                                // see if we have a parent matching way...
                                for (mp in typeInfo.properties.filter { p -> p.Name.splitXPath().first() == xchild.nodeName }) {
                                    val xgrandchild = x.getFirstElementByXPath(mp.Name)
                                    if (xgrandchild != null) {
                                        readPropertyMapping(mp, xgrandchild, value, version)
                                    }
                                }
                            } else if (mappingProp != null)
                            {
                                readPropertyMapping(mappingProp!!, xchild, value, version)
                            }
                            else if (extensionKDEs != null)
                            {
                                val kde = readKDE(xchild)
                                extensionKDEs.add(kde)
                            }
                        }
                    }

                    if (typeInfo.extensionAttributes != null) {
                        typeInfo.extensionAttributes!!.setter.call(value, extensionAttributes)
                    }

                    if (typeInfo.extensionKDEs != null) {
                        typeInfo.extensionKDEs!!.setter.call(value, extensionKDEs)
                    }
                }
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }

            return value
        }

        fun writeObjectToString(obj: Any?): String? {
            return when (obj) {
                null -> null
                (obj::class.createType() == typeOf<MutableList<LanguageString>>()) -> {
                    var list = obj as? MutableList<LanguageString>
                    if (list != null)
                    {
                        if (list.isEmpty()) null else list.first().value
                    }
                    else
                    {
                        null
                    }
                }
                is OffsetDateTime -> {
                    obj.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                }
                is UOM -> {
                    obj.UNCode
                }
                is Boolean -> {
                    obj.toString().lowercase()
                }
                is Country -> {
                    obj.abbreviation
                }
                is Duration -> {
                    if (obj.isNegative) "-${obj.abs().toHours().toString().padStart(2, '0')}:${obj.abs().toMinutesPart().toString().padStart(2, '0')}"
                    else "+${obj.toHours().toString().padStart(2, '0')}:${obj.toMinutesPart().toString().padStart(2, '0')}"
                }
                else -> {
                    obj.toString()
                }
            }
        }

        fun readPropertyMapping(mappingProp: OTMappingTypeInformationProperty, xchild: Element, value: Any, version: EPCISVersion) {
            when {
                mappingProp.IsQuantityList -> {
                    val e = value as IEvent
                    xchild.getElementsByXPath("quantityElement")?.forEachIndex { xQuantity, _ ->
                        val epc = EPC(xQuantity.getFirstElementByXPath("epcClass")?.nodeValue ?: "")
                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType
                            Quantity = Measurement(xQuantity.getFirstElementByXPath("quantity")?.nodeValue?.toDouble() ?: 0.0, xQuantity.getFirstElementByXPath("uom")?.nodeValue ?: "EA")
                        }
                        e.addProduct(product)
                    }
                }
                mappingProp.IsEPCList -> {
                    val e = value as IEvent
                    xchild.getElementsByXPath("epc")?.forEachIndex { xEPC, _ ->
                        val epc = EPC(xEPC.nodeValue)
                        val product = EventProduct(epc).apply { Type = mappingProp.ProductType }
                        e.addProduct(product)
                    }
                }
                mappingProp.IsArray -> {
                    val itemType = mappingProp.Property.returnType.arguments[0].type
                        ?: throw Exception("Cannot determine item type of list type.")

                    var list = mappingProp.Property.getter.call(value) as MutableList<Any?>

                    if (list == null)
                    {
                        list = mutableListOf()
                        mappingProp.Property.setter.call(value, list)
                    }

                    if (mappingProp.ItemName != null)
                    {
                        xchild.getElementsByXPath(mappingProp.ItemName!!)?.forEachIndex { xItem, _ ->
                            if (mappingProp.IsObject) {
                                list.add(fromXml(xItem, itemType as KClass<*>, version))
                            } else {
                                list.add(readObjectFromString(xItem.nodeValue, itemType))
                            }
                        }
                    } else {
                        if (mappingProp.IsObject) {
                            list.add(fromXml(xchild, itemType as KClass<*>, version))
                        } else {
                            list.add(readObjectFromString(xchild.nodeValue, itemType))
                        }
                    }
                }
                mappingProp.IsObject -> {
                    val o = fromXml(xchild, mappingProp.Property.returnType as KClass<*>, version)
                    mappingProp.Property.setter.call(value, o)
                }
                else -> {
                    val eleText = xchild.nodeValue
                    if (!eleText.isNullOrBlank()) {
                        val o = readObjectFromString(eleText, mappingProp.Property.returnType)
                        mappingProp.Property.setter.call(value, o)
                    }
                }
            }
        }

        fun readObjectFromString(value: String, t: KType): Any? {
            return when (t) {
                typeOf<OffsetDateTime>() -> {
                    value.tryConvertToDateTimeOffset() ?: throw Exception("Failed to convert string to datetimeoffset where value = $value")
                }
                typeOf<MutableList<LanguageString>>() -> {
                    mutableListOf(LanguageString("en-US", value))
                }
                typeOf<UOM>() -> {
                    UOM.lookUpFromUNCode(value)
                }
                typeOf<Boolean>() -> {
                    value.toBoolean()
                }
                typeOf<Double>() -> {
                    value.toDouble()
                }
                typeOf<URI>() -> {
                    URI(value)
                }
                typeOf<Duration>() -> {
                    if (value.startsWith("+")) Duration.parse(value.substring(1)) else Duration.parse(value)
                }
                typeOf<EventAction>() -> {
                    EventAction.valueOf(value)
                }
                typeOf<PGLN>() -> {
                    PGLN(value)
                }
                typeOf<GLN>() -> {
                    GLN(value)
                }
                typeOf<EPC>() -> {
                    EPC(value)
                }
                typeOf<Country>() -> {
                    Countries.parse(value)
                }
                else -> {
                    value
                }
            }
        }

        fun readKDE(x: Element): IEventKDE {
            var kde: IEventKDE? = IEventKDE.initializeKDE(x.namespaceURI, x.tagName)

            if (kde == null)
            {
                when (x.getAttributeNS(opentraceability.Constants.XSI_NAMESPACE,"type")) {
                    "string" -> kde = EventKDEString(x.namespaceURI, x.tagName)
                    "boolean" -> kde = EventKDEBoolean(x.namespaceURI, x.tagName)
                    "number" -> kde = EventKDEDouble(x.namespaceURI, x.tagName)
                }
            }

            if (kde == null)
            {
                if (x.hasChildNodes())
                {
                    kde = EventKDEObject(x.namespaceURI, x.tagName)
                }
                else
                {
                    kde = EventKDEString(x.namespaceURI, x.tagName);
                }
            }

            kde?.setFromXml(x) ?: throw Exception("Failed to initialize KDE from XML = ${x.toString()}")

            return kde
        }



        fun readAttributeKDE(x: Node, attName: String, attValue: String): IEventKDE {
            var kde: IEventKDE? = IEventKDE.initializeKDE(x.namespaceURI, x.nodeName)
            kde = kde ?: EventKDEString(x.namespaceURI, x.nodeName)

            kde.setFromXml(x as Element) ?: throw Exception("Failed to initialize KDE from XML Attribute = ${x.toString()}")

            return kde
        }
    }
}
