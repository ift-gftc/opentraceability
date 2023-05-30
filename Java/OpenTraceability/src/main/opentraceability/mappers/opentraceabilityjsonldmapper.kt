package mappers

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEvent
import interfaces.IEventKDE
import models.common.LanguageString
import models.events.EPCISVersion
import models.events.EventProduct
import models.events.kdes.EventKDEObject
import models.events.kdes.EventKDEString
import models.identifiers.EPC
import models.identifiers.GLN
import models.identifiers.GTIN
import models.identifiers.PGLN
import utility.Country
import utility.Measurement
import utility.UOM
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime
import kotlin.reflect.KClass

class OpenTraceabilityJsonLDMapper {
    companion object {

        /**
         * Converts an object into JSON.
         */
        fun ToJson(value: Any?, namespacesReversed: Map<String, String>, required: Boolean = false): JToken? {
            try {
                value?.let {
                    var json: JToken? = JObject()
                    var jpointer: JToken = json

                    val t: Type = value::class.java
                    val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.GetJsonTypeInfo(t)
                    typeInfo.Properties.filter { it.Version == null || it.Version == EPCISVersion.V2 }.forEach { property ->
                        property.Property.getValue(value)?.let { obj ->
                            var jvaluepointer: JToken = jpointer
                            val xchildname: String = property.Name

                            when {
                                property.IsQuantityList -> {
                                    var products: List<EventProduct> = obj as List<EventProduct>
                                    products = products.filter { it.Quantity != null && it.Type == property.ProductType }
                                    if (products.isNotEmpty()) {
                                        val xQuantityList: JArray = JArray()
                                        for (product in products) {
                                            if (product.EPC != null && product.Quantity != null) {
                                                val xQuantity: JObject = JObject()
                                                xQuantity["epcClass"] = product.EPC.toString()
                                                xQuantity["quantity"] = product.Quantity.Value
                                                if (product.Quantity.UoM.UNCode != "EA") {
                                                    xQuantity["uom"] = product.Quantity.UoM.UNCode
                                                }
                                                xQuantityList.add(xQuantity)
                                            }
                                        }
                                        jvaluepointer[xchildname] = xQuantityList
                                    }
                                }
                                property.IsEPCList -> {
                                    var products: List<EventProduct> = obj as List<EventProduct>
                                    products = products.filter { it.Quantity == null && it.Type == property.ProductType }
                                    if (products.isNotEmpty() || property.Required) {
                                        val xEPCList: JArray = JArray()
                                        for (product in products) {
                                            product.EPC?.let {
                                                xEPCList.add(it.toString())
                                            }
                                        }
                                        jvaluepointer[xchildname] = xEPCList
                                    }
                                }
                                property.IsArray -> {
                                    val list: MutableList<Any> = obj as MutableList<Any>
                                    val xlist: JArray = JArray()

                                    if (list.isNotEmpty() || property.Required) {
                                        if (property.IsRepeating && list.size == 1) {
                                            val jt: JToken? = WriteObjectToJToken(list[0])
                                            if (jt != null) {
                                                jvaluepointer[xchildname] = jt
                                            }
                                        } else {
                                            for (o in list) {
                                                if (property.IsObject) {
                                                    val xchild: JToken? = ToJson(o, namespacesReversed, property.Required)
                                                    if (xchild != null) {
                                                        xlist.add(xchild)
                                                    }
                                                } else {
                                                    val jt: JToken? = WriteObjectToJToken(o)
                                                    if (jt != null) {
                                                        xlist.add(jt)
                                                    }
                                                }
                                            }

                                            jvaluepointer[xchildname] = xlist
                                        }
                                    }
                                }
                                property.IsObject -> {
                                    val xchild: JToken? = ToJson(obj, namespacesReversed, property.Required)
                                    if (xchild != null) {
                                        jvaluepointer[xchildname] = xchild
                                    }
                                }
                                else -> {
                                    val jt: JToken? = WriteObjectToJToken(obj)
                                    if (jt != null) {
                                        jvaluepointer[xchildname] = jt
                                    }
                                }
                            }
                        }
                    }

                    typeInfo.ExtensionKDEs?.getValue(value)?.let { obj ->
                        if (obj is MutableList<IEventKDE>) {
                            val kdes: MutableList<IEventKDE> = obj
                            for (kde in kdes) {
                                kde.GetJson()?.let { xchild ->
                                    var name: String = kde.Name
                                    if (kde.Namespace != null) {
                                        if (!namespacesReversed.containsKey(kde.Namespace)) {
                                            throw Exception("The namespace ${kde.Namespace} is not recognized in the EPCIS Document / EPCIS Query Document.")
                                        }
                                        name = namespacesReversed[kde.Namespace] + ":" + name
                                    }

                                    jpointer[name] = xchild
                                }
                            }
                        }
                    }

                    typeInfo.ExtensionAttributes?.getValue(value)?.let { obj ->
                        if (obj is MutableList<IEventKDE>) {
                            val kdes: MutableList<IEventKDE> = obj
                            for (kde in kdes) {
                                kde.GetJson()?.let { xchild ->
                                    var name: String = kde.Name
                                    if (kde.Namespace != null) {
                                        if (!namespacesReversed.containsKey(kde.Namespace)) {
                                            throw Exception("The namespace ${kde.Namespace} is not recognized in the EPCIS Document / EPCIS Query Document.")
                                        }
                                        name = namespacesReversed[kde.Namespace] + ":" + name
                                    }

                                    jpointer[name] = xchild
                                }
                            }
                        }
                    }

                    return json
                }

                return null
            } catch (ex: Exception) {
                val e = Exception("Failed to parse json. value=$value", ex)
                OTLogger.error(e)
                throw e
            }
        }


        /**
         * Converts a JSON object into the generic type specified.
         */
        inline fun <reified T> FromJson(json: JToken, namespaces: Map<String, String>): T {
            val o: T = FromJson(json, T::class.java, namespaces) as T
            return o
        }


        /**
         * Converts a JSON object into the type specified.
         */
        fun FromJson(json: JToken, type: Type, namespaces: Map<String, String>): Any {
            val value: Any = type.javaClass.kotlin.objectInstance ?: throw Exception("Failed to create instance of type ${type.typeName}")

            try {
                val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.GetJsonTypeInfo(type)

                var extensionKDEs: MutableList<IEventKDE>? = null
                var extensionAttributes: MutableList<IEventKDE>? = null

                if (typeInfo.ExtensionAttributes != null) {
                    extensionAttributes = mutableListOf()
                }

                if (typeInfo.ExtensionKDEs != null) {
                    extensionKDEs = mutableListOf()
                }

                var mappingProp: OTMappingTypeInformationProperty? = null

                val jobj: JObject? = json as? JObject
                if (jobj != null) {
                    for (jprop in jobj.properties()) {
                        mappingProp = typeInfo[jprop.Name]

                        if (mappingProp != null && mappingProp.Property.setMethod == null) {
                            continue
                        }

                        val jchild: JToken? = jobj[jprop.Name]
                        if (jchild != null) {
                            when {
                                mappingProp != null -> {
                                    ReadPropertyMapping(mappingProp, jchild, value, namespaces)
                                }
                                extensionKDEs != null -> {
                                    val kde: IEventKDE = ReadKDE(jprop.Name, jchild, namespaces)
                                    extensionKDEs.add(kde)
                                }
                                extensionAttributes != null -> {
                                    val kde: IEventKDE = ReadKDE(jprop.Name, jchild, namespaces)
                                    extensionAttributes.add(kde)
                                }
                            }
                        }
                    }
                }

                typeInfo.ExtensionAttributes?.setValue(value, extensionAttributes)
                typeInfo.ExtensionKDEs?.setValue(value, extensionKDEs)
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }

            return value
        }


        fun WriteObjectToJToken(obj: Any?): JToken? {
            return when(obj) {
                null -> null
                is List<LanguageString> -> {
                    val json = obj.toJson()
                    JArray.parse(json)
                }
                is OffsetDateTime -> {
                    obj.toString("O")
                }
                is UOM -> {
                    obj.UNCode
                }
                is Double, is Boolean -> {
                    JToken.fromObject(obj)
                }
                is Country -> {
                    obj.Abbreviation
                }
                is TimeSpan -> {
                    if (obj.ticks < 0)
                        "-${obj.negate().totalHours.toString("#00")}:${obj.minutes.toString("00")}"
                    else
                        "+${obj.TotalHours.toString("#00")}:${obj.minutes.toString("00")}"
                }
                else -> obj.toString() ?: ""
            }
        }

        fun ReadPropertyMapping(mappingProp: OTMappingTypeInformationProperty, json: JToken, value: Any, namespaces: Map<String, String>) {
            when {
                mappingProp.IsQuantityList -> {
                    val e = value as IEvent
                    val jQuantityList = json as? JArray
                    jQuantityList?.forEach {
                        val jQuantity = it as JObject
                        val epc = EPC(jQuantity["epcClass"]?.value<String>() ?: "")

                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType
                            Quantity = Measurement(jQuantity.value<Double>("quantity"), jQuantity.value<String>("uom") ?: "EA")
                        }

                        e.addProduct(product)
                    }
                }
                mappingProp.IsEPCList -> {
                    val e = value as IEvent
                    val jEPCList = json as? JArray
                    jEPCList?.forEach {
                        val epc = EPC(it.toString())
                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType
                        }
                        e.addProduct(product)
                    }
                }
                mappingProp.IsArray -> {
                    var list = mappingProp.Property.getValue(value) as? MutableList<*>
                    if (list == null) {
                        list = (mappingProp.Property.PropertyType.kotlin.objectInstance as? MutableList<*>)
                            ?: throw Exception("Failed to create instance of ${mappingProp.Property.PropertyType.typeName}")

                        mappingProp.Property.setValue(value, list)
                    }

                    val itemType = list::class.java.genericSuperclass as? Class<*>

                    if (mappingProp.IsRepeating && json !is JArray) {
                        val v = json.toString()
                        if (v.isNotBlank()) {
                            val o = ReadObjectFromString(v, itemType)
                            list.add(o)
                        }
                    } else {
                        val jArr = json as? JArray
                        jArr?.forEach {
                            val o = if (mappingProp.IsObject) {
                                FromJson(it, itemType, namespaces)
                            } else {
                                ReadObjectFromString(it.toString(), itemType)
                            }
                            list.add(o)
                        }
                    }
                }
                mappingProp.IsObject -> {
                    val o = FromJson(json, mappingProp.Property.PropertyType, namespaces)
                    mappingProp.Property.setValue(value, o)
                }
                mappingProp.Property.PropertyType == List::class.java -> {
                    val languageStrings: List<LanguageString>? = json.toString().fromJson()
                    if (languageStrings != null) {
                        mappingProp.Property.setValue(value, languageStrings)
                    }
                }
                else -> {
                    val v = json.toString()
                    if (v.isNotBlank()) {
                        val o = ReadObjectFromString(v, mappingProp.Property.PropertyType)
                        mappingProp.Property.setValue(value, o)
                    }
                }
            }
        }



        fun ReadObjectFromString(value: String, t: KClass<*>): Any {
            return try {
                when(t) {
                    OffsetDateTime::class -> {
                        val dt = value.TryConvertToDateTimeOffset() ?: throw Exception("Failed to convert string to datetimeoffset where value = $value")
                        dt
                    }
                    UOM::class -> {
                        val uom = UOM.LookUpFromUNCode(value)
                        uom
                    }
                    Boolean::class -> {
                        val v = value.toBoolean()
                        v
                    }
                    Double::class -> {
                        val v = value.toDouble()
                        v
                    }
                    URI::class -> {
                        val v = URI(value)
                        v
                    }
                    TimeSpan::class -> {
                        val ts = if (value.startsWith("+")) TimeSpan.parse(value.drop(1)) else TimeSpan.parse(value)
                        ts
                    }
                    EventAction::class -> {
                        val action = EventAction.valueOf(value)
                        action
                    }
                    PGLN::class -> {
                        val pgln = PGLN(value)
                        pgln
                    }
                    GLN::class -> {
                        val gln = GLN(value)
                        gln
                    }
                    GTIN::class -> {
                        val gtin = GTIN(value)
                        gtin
                    }
                    EPC::class -> {
                        val epc = EPC(value)
                        epc
                    }
                    Country::class -> {
                        val c = Countries.parse(value)
                        c
                    }
                    else -> value
                }
            } catch (ex: Exception) {
                val e = Exception("Failed to convert string into object. value=$value and t=$t", ex)
                OTLogger.error(e)
                throw e
            }
        }

        fun ReadKDE(name: String, json: JToken, namespaces: Map<String, String>): IEventKDE {
            var kde: IEventKDE? = null
            var ns = ""
            var realName = name

            if (name.contains(":")) {
                val split = name.split(":")
                ns = split.first()
                realName = split.last()

                if (!namespaces.containsKey(ns)) {
                    throw Exception("The KDE has a namespace prefix, but there is no such namespace in the dictionary. $ns")
                }
                ns = namespaces[ns] ?: ""
            }

            if (json is JObject || json is JArray) {
                kde = EventKDEObject(ns, realName)
            } else {
                kde = EventKDEString(ns, realName)
            }

            kde?.setFromJson(json) ?: throw Exception("Failed to initialize KDE from JSON = ${json.toString()}")

            return kde
        }


    }
}
