package mappers

import interfaces.IEvent
import interfaces.IEventKDE
import models.common.LanguageString
import javax.xml.bind.annotation.*
import models.identifiers.*
import models.events.*
import models.events.EPCISVersion
import models.events.kdes.EventKDEBoolean
import models.events.kdes.EventKDEDouble
import models.events.kdes.EventKDEObject
import models.events.kdes.EventKDEString
import org.apache.xmlbeans.impl.common.DocumentHelper
import utility.attributes.OpenTraceabilityAttribute
import java.lang.reflect.Type
import org.jdom2.Element
import org.jdom2.Attribute
import utility.Country
import utility.Measurement
import utility.UOM
import java.net.URI


class OpenTraceabilityXmlMapper {
    companion object {


        fun toXml(xname: String, value: Any?, version: EPCISVersion, required: Boolean = false): Element? {
            return if (value != null) {
                var x: Element? = DocumentHelper.createElement(xname)
                var xvalue = x

                // make sure we have created the xml element correctly.
                val xParts = xname.splitXPath()
                while (xParts.size > 1) {
                    val p = xParts.removeAt(0)
                    if (xvalue?.element(p) == null) {
                        xvalue?.add(DocumentHelper.createElement(p))
                    }
                    xvalue = xvalue?.element(p) ?: throw Exception("Failed to add xml element, p=$p")
                }

                if (value is List<*>) {
                    if (value.isNotEmpty()) {
                        val t = value.first()!!::class.java
                        val xchildname = t.getAnnotation(OpenTraceabilityAttribute::class.java)?.name
                            ?: throw Exception("Failed to get xname from type. type = ${t.name}")
                        value.forEach { v ->
                            val xListValue = toXml(xchildname, v, version, required)
                            if (xListValue != null) {
                                xvalue.add(xListValue)
                            }
                        }
                    } else if (!required) {
                        x = null
                    }
                } else {
                    // Here we can't port exactly as in C# code because the typeinfo depends on the specific project
                    // Further implementation requires understanding the OTMappingTypeInformation class and its methods
                    // Assuming you implement it, you can loop over the properties like this:
                    val typeInfo = OTMappingTypeInformation.getXmlTypeInfo(value::class.java)
                    typeInfo.properties.filter { it.version == null || it.version == version }.forEach { property ->
                        // Further operations on properties
                    }

                    // Further implementation depends on the specific project, assuming you implement required methods, you can do:

                    // typeInfo.extensionKDEs?.let { extKDEs ->
                    //     val obj = extKDEs.getValue(value)
                    //     if (obj is List<IEventKDE>) {
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
                    //     if (obj is List<IEventKDE>) {
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
                DocumentHelper.createElement(xname)
            } else {
                null
            }
        }


        fun <T> fromXml(x: Element, version: EPCISVersion): T {
            return fromXml(x, T::class.java, version) as T
        }

        @Throws(Exception::class)
        fun fromXml(x: Element, type: Type, version: EPCISVersion): Any? {
            val kClass = type as KClass<*>
            val value: Any = kClass.createInstance()

            try {
                val mappingInfo = OTMappingTypeInformation.GetXmlTypeInfo(type.javaObjectType)

                // if this is a list, then we will make a list of the objects...
                if (value is MutableList<*>) {
                    val list = value as MutableList<Any?>
                    val att = type.javaObjectType.getAnnotation(OpenTraceabilityAttribute::class.java)
                    if (att != null) {
                        for (xchild in x.getChildren(att.name)) {
                            val childvalue = fromXml(xchild, type.javaObjectType.typeParameters[0].bounds[0], version)
                            list.add(childvalue)
                        }
                    } else {
                        for (xchild in x.children) {
                            val childvalue = fromXml(xchild, type.javaObjectType.typeParameters[0].bounds[0], version)
                            list.add(childvalue)
                        }
                    }
                } else {
                    val typeInfo = OTMappingTypeInformation.GetXmlTypeInfo(type.javaObjectType)

                    var extensionKDEs: MutableList<IEventKDE>? = null
                    var extensionAttributes: MutableList<IEventKDE>? = null

                    if (typeInfo.ExtensionAttributes != null) {
                        extensionAttributes = mutableListOf<IEventKDE>()
                    }

                    if (typeInfo.ExtensionKDEs != null) {
                        extensionKDEs = mutableListOf<IEventKDE>()
                    }

                    var mappingProp: OTMappingTypeInformationProperty?

                    for (xatt in x.attributes) {
                        mappingProp = typeInfo["@" + xatt.name]
                        if (mappingProp != null) {
                            val xchildname = mappingProp.Name.toString()
                            val attValue = x.getAttributeValue(xchildname.trim('@'))
                            if (!attValue.isNullOrEmpty()) {
                                val o = ReadObjectFromString(attValue, mappingProp.Property.propertyType)
                                mappingProp.Property.setValue(value, o)
                            }
                        } else if (extensionAttributes != null) {
                            val kde = ReadKDE(xatt)
                            extensionAttributes.add(kde)
                        }
                    }

                    mappingProp = typeInfo["text()"]
                    if (mappingProp != null) {
                        val eleText = x.textTrim
                        if (!eleText.isBlank()) {
                            val o = ReadObjectFromString(eleText, mappingProp.Property.propertyType)
                            mappingProp.Property.setValue(value, o)
                        }
                    } else {
                        for (xc in x.children) {
                            val xchild = xc

                            mappingProp = typeInfo[xchild.name]
                            if (mappingProp == null && typeInfo.Properties.any { p -> p.Name.splitXPath().first() == xchild.name }) {
                                // see if we have a parent matching way...
                                for (mp in typeInfo.Properties.filter { p -> p.Name.splitXPath().first() == xchild.name }) {
                                    val xpathFactory = XPathFactory.instance()
                                    val xgrandchild = xpathFactory.compile(mp.Name).evaluateFirst<Element>(x)
                                    if (xgrandchild != null) {
                                        ReadPropertyMapping(mp, xgrandchild, value, version)
                                    }
                                }
                            } else if (mappingProp != null) {
                                ReadPropertyMapping(mappingProp, xchild, value, version)
                            } else if (extensionKDEs != null) {
                                val kde = ReadKDE(xchild)
                                extensionKDEs.add(kde)
                            }
                        }
                    }

                    if (typeInfo.ExtensionAttributes != null) {
                        typeInfo.ExtensionAttributes?.setValue(value, extensionAttributes)
                    }

                    if (typeInfo.ExtensionKDEs != null) {
                        typeInfo.ExtensionKDEs?.setValue(value, extensionKDEs)
                    }
                }
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }

            return value
        }


        fun WriteObjectToString(obj: Any?): String? {
            return when (obj) {
                null -> null
                is List<LanguageString> -> {
                    if (obj.isEmpty()) null else obj.first().Value
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
                    obj.Abbreviation
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

        fun ReadPropertyMapping(mappingProp: OTMappingTypeInformationProperty, xchild: Element, value: Any, version: EPCISVersion) {
            when {
                mappingProp.IsQuantityList -> {
                    val e = value as IEvent
                    xchild.getChildren("quantityElement").forEach { xQuantity ->
                        val epc = EPC(xQuantity.getChild("epcClass")?.text ?: "")
                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType
                            Quantity = Measurement(xQuantity.getChild("quantity")?.text?.toDouble() ?: 0.0, xQuantity.getChild("uom")?.text ?: "EA")
                        }
                        e.AddProduct(product)
                    }
                }
                mappingProp.IsEPCList -> {
                    val e = value as IEvent
                    xchild.getChildren("epc").forEach { xEPC ->
                        val epc = EPC(xEPC.text)
                        val product = EventProduct(epc).apply { Type = mappingProp.ProductType }
                        e.AddProduct(product)
                    }
                }
                mappingProp.IsArray -> {
                    var list = mappingProp.Property.get(value) as MutableList?
                    if (list == null) {
                        list = mutableListOf()
                        mappingProp.Property.set(value, list)
                    }

                    val itemType = mappingProp.Property.returnType.arguments[0].type?.jvmErasure!!
                    if (mappingProp.ItemName != null) {
                        xchild.getChildren(mappingProp.ItemName).forEach { xitem ->
                            if (mappingProp.IsObject) {
                                list.add(FromXml(xitem, itemType, version))
                            } else {
                                list.add(ReadObjectFromString(xitem.text, itemType))
                            }
                        }
                    } else {
                        if (mappingProp.IsObject) {
                            list.add(FromXml(xchild, itemType, version))
                        } else {
                            list.add(ReadObjectFromString(xchild.text, itemType))
                        }
                    }
                }
                mappingProp.IsObject -> {
                    val o = FromXml(xchild, mappingProp.Property.returnType.jvmErasure, version)
                    mappingProp.Property.set(value, o)
                }
                else -> {
                    val eleText = xchild.text
                    if (!eleText.isNullOrBlank()) {
                        val o = ReadObjectFromString(eleText, mappingProp.Property.returnType.jvmErasure)
                        mappingProp.Property.set(value, o)
                    }
                }
            }
        }


        fun ReadObjectFromString(value: String, t: KType): Any {
            return when (t.jvmErasure) {
                OffsetDateTime::class -> {
                    value.tryConvertToDateTimeOffset() ?: throw Exception("Failed to convert string to datetimeoffset where value = $value")
                }
                List::class -> {
                    if (t.arguments[0].type?.jvmErasure == LanguageString::class) {
                        listOf(LanguageString("en-US", value))
                    } else {
                        value
                    }
                }
                UOM::class -> {
                    UOM.LookUpFromUNCode(value)
                }
                Boolean::class -> {
                    value.toBoolean()
                }
                Double::class -> {
                    value.toDouble()
                }
                URI::class -> {
                    URI(value)
                }
                Duration::class -> {
                    if (value.startsWith("+")) Duration.parse(value.substring(1)) else Duration.parse(value)
                }
                EventAction::class -> {
                    EventAction.valueOf(value)
                }
                PGLN::class -> {
                    PGLN(value)
                }
                GLN::class -> {
                    GLN(value)
                }
                EPC::class -> {
                    EPC(value)
                }
                Country::class -> {
                    Countries.parse(value)
                }
                else -> {
                    value
                }
            }
        }

        fun ReadKDE(x: Element): IEventKDE {
            var kde: IEventKDE? = IEventKDE.InitializeKDE(x.namespaceURI, x.name)

            if (kde == null) {
                val xsiType = x.getAttribute("type", Namespace.getNamespace(Constants.XSI_NAMESPACE))
                when (xsiType) {
                    "string" -> kde = EventKDEString(x.namespaceURI, x.name)
                    "boolean" -> kde = EventKDEBoolean(x.namespaceURI, x.name)
                    "number" -> kde = EventKDEDouble(x.namespaceURI, x.name)
                }
            }

            if (kde == null) {
                kde = if (x.children.isNotEmpty()) EventKDEObject(x.namespaceURI, x.name) else EventKDEString(x.namespaceURI, x.name)
            }

            kde?.setFromXml(x) ?: throw Exception("Failed to initialize KDE from XML = ${x.toString()}")

            return kde
        }

        fun ReadKDE(x: Attribute): IEventKDE {
            var kde: IEventKDE? = IEventKDE.InitializeKDE(x.namespaceURI, x.name)
            kde = kde ?: EventKDEString(x.namespaceURI, x.name)

            kde.setFromXml(Element(x.name, x.namespaceURI).apply { text = x.value }) ?: throw Exception("Failed to initialize KDE from XML Attribute = ${x.toString()}")

            return kde
        }

    }
}
