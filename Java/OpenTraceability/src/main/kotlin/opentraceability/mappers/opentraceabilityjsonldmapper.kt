package mappers

import interfaces.*
import models.common.LanguageString
import models.events.*
import models.events.kdes.*
import models.identifiers.*
import org.json.*
import utility.*
import utility.StringExtensions.tryConvertToDateTimeOffset
import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.time.Duration

class OpenTraceabilityJsonLDMapper {
    companion object {

        /**
         * Converts an object into JSON.
         */
        fun toJson(value: Any?, namespacesReversed: Map<String, String>, required: Boolean = false): JSONObject? {
            try {
                value?.let {
                    var json: JSONObject? = JSONObject()
                    var jpointer: JSONObject = json!!

                    val t: Type = value::class.java
                    val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.getJsonTypeInfo(t)
                    typeInfo.Properties.filter { it.Version == null || it.Version == EPCISVersion.V2 }.forEach { property ->
                        property.Property.getValue(value)?.let { obj ->
                            var jvaluepointer: JSONObject = jpointer
                            val xchildname: String = property.Name

                            when {
                                property.IsQuantityList -> {
                                    var products: List<EventProduct> = obj as List<EventProduct>
                                    products = products.filter { (it.Quantity != null) && (it.Type == property.ProductType) }
                                    if (products.isNotEmpty()) {
                                        val xQuantityList: JSONArray = JSONArray()
                                        for (product in products) {
                                            if (product.EPC != null && product.Quantity != null) {
                                                val xQuantity: JSONObject = JSONObject()
                                                xQuantity.put("epcClass", product.EPC.toString())
                                                xQuantity.put("quantity",product?.Quantity?.value)
                                                if (product.Quantity?.uom?.UNCode != "EA") {
                                                    xQuantity.put("uom", product.Quantity?.uom?.UNCode)
                                                }
                                                xQuantityList.put(xQuantity)
                                            }
                                        }
                                        jvaluepointer.put(xchildname,xQuantityList)
                                    }
                                }
                                property.IsEPCList -> {
                                    var products: List<EventProduct> = obj as List<EventProduct>
                                    products = products.filter { (it.Quantity == null) && (it.Type == property.ProductType) }
                                    if (products.isNotEmpty() || property.Required) {
                                        val xEPCList: JSONArray = JSONArray()
                                        for (product in products) {
                                            product.EPC?.let {
                                                xEPCList.put(it.toString())
                                            }
                                        }
                                        jvaluepointer.put(xchildname,xEPCList)
                                    }
                                }
                                property.IsArray -> {
                                    val list: MutableList<Any> = obj as MutableList<Any>
                                    val xlist: JSONArray = JSONArray()

                                    if (list.isNotEmpty() || property.Required) {
                                        if (property.IsRepeating && list.size == 1) {
                                            val jt: JSONObject? = writeObjectToJToken(list[0])
                                            if (jt != null) {
                                                jvaluepointer.put(xchildname,jt)
                                            }
                                        } else {
                                            for (o in list) {
                                                if (property.IsObject) {
                                                    val xchild: JSONObject? = toJson(o, namespacesReversed, property.Required)
                                                    if (xchild != null) {
                                                        xlist.put(xchild)
                                                    }
                                                } else {
                                                    val jt: JSONObject? = writeObjectToJToken(o)
                                                    if (jt != null) {
                                                        xlist.put(jt)
                                                    }
                                                }
                                            }

                                            jvaluepointer.put(xchildname,xlist)
                                        }
                                    }
                                }
                                property.IsObject -> {
                                    val xchild: JSONObject? = toJson(obj, namespacesReversed, property.Required)
                                    if (xchild != null) {
                                        jvaluepointer.put(xchildname,xchild)
                                    }
                                }
                                else -> {
                                    val jt: JSONObject? = writeObjectToJToken(obj)
                                    if (jt != null) {
                                        jvaluepointer.put(xchildname,jt)
                                    }
                                }
                            }
                        }
                    }

                    typeInfo.extensionKDEs?.getValue(value)?.let { obj ->
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

                                    jpointer.put(name,xchild)
                                }
                            }
                        }
                    }

                    typeInfo.extensionAttributes?.getValue(value)?.let { obj ->
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

                                    jpointer.put(name,xchild)
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
        inline fun <reified T> fromJson(json: JSONObject, namespaces: Map<String, String>): T {
            val o: T = fromJson(json, T::class.java, namespaces) as T
            return o
        }


        /**
         * Converts a JSON object into the type specified.
         */
        fun fromJson(json: JSONObject, type: Type, namespaces: Map<String, String>): Any {
            val value: Any = type.javaClass.kotlin.objectInstance ?: throw Exception("Failed to create instance of type ${type.typeName}")

            try {
                val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.getJsonTypeInfo(type)

                var extensionKDEs: MutableList<IEventKDE>? = null
                var extensionAttributes: MutableList<IEventKDE>? = null

                if (typeInfo.extensionAttributes != null) {
                    extensionAttributes = mutableListOf()
                }

                if (typeInfo.extensionKDEs != null) {
                    extensionKDEs = mutableListOf()
                }

                var mappingProp: OTMappingTypeInformationProperty? = null

                val jobj: JSONObject? = json as? JSONObject
                if (jobj != null) {
                    for (jprop in jobj.Properties()) {
                        mappingProp = typeInfo[jprop.Name]

                        if (mappingProp != null && mappingProp.Property.setMethod == null) {
                            continue
                        }

                        val jchild: JSONObject? = jobj[jprop.Name]
                        if (jchild != null) {
                            when {
                                mappingProp != null -> {
                                    readPropertyMapping(mappingProp, jchild, value, namespaces)
                                }
                                extensionKDEs != null -> {
                                    val kde: IEventKDE = readKDE(jprop.Name, jchild, namespaces)
                                    extensionKDEs.add(kde)
                                }
                                extensionAttributes != null -> {
                                    val kde: IEventKDE = readKDE(jprop.Name, jchild, namespaces)
                                    extensionAttributes.add(kde)
                                }
                            }
                        }
                    }
                }

                typeInfo.extensionAttributes?.setValue(value, extensionAttributes)
                typeInfo.extensionKDEs?.setValue(value, extensionKDEs)
            } catch (ex: Exception) {
                OTLogger.error(ex)
                throw ex
            }

            return value
        }


        fun writeObjectToJToken(obj: Any?): JSONObject? {
            return when(obj) {
                null -> null
                is List<LanguageString> -> {
                    val json = obj.toJson()
                    JSONArray.parse(json)
                }
                is OffsetDateTime -> {
                    obj.toString("O")
                }
                is UOM -> {
                    obj.UNCode
                }
                is Double, is Boolean -> {
                    JSONObject.fromObject(obj)
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

        fun readPropertyMapping(mappingProp: OTMappingTypeInformationProperty, json: JSONObject, value: Any, namespaces: Map<String, String>) {
            when {
                mappingProp.IsQuantityList -> {
                    val e = value as IEvent
                    val jQuantityList = json as? JSONArray
                    jQuantityList?.forEach {
                        val jQuantity = it as JSONObject
                        val epc = EPC(jQuantity["epcClass"]?.value<String>() ?: "")

                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType!!
                            Quantity = Measurement(jQuantity.value<Double>("quantity"), jQuantity.value<String>("uom") ?: "EA")
                        }

                        e.AddProduct(product)
                    }
                }
                mappingProp.IsEPCList -> {
                    val e = value as IEvent
                    val jEPCList = json as? JSONArray
                    jEPCList?.forEach {
                        val epc = EPC(it.toString())
                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType!!
                        }
                        e.AddProduct(product)
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

                    if (mappingProp.IsRepeating && json !is JSONArray) {
                        val v = json.toString()
                        if (v.isNotBlank()) {
                            val o = readObjectFromString(v, itemType)
                            list.add(o)
                        }
                    } else {
                        val jArr = json as? JSONArray
                        jArr?.forEach {
                            val o = if (mappingProp.IsObject) {
                                fromJson(it, itemType, namespaces)
                            } else {
                                readObjectFromString(it.toString(), itemType)
                            }
                            list.add(o)
                        }
                    }
                }
                mappingProp.IsObject -> {
                    val o = fromJson(json, mappingProp.Property.PropertyType, namespaces)
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
                        val o = readObjectFromString(v, mappingProp.Property.PropertyType)
                        mappingProp.Property.setValue(value, o)
                    }
                }
            }
        }



        fun readObjectFromString(value: String, t: KClass<*>): Any? {
            return try {
                when (t) {
                    OffsetDateTime::class -> {
                        val dt = value.tryConvertToDateTimeOffset() ?: throw Exception("Failed to convert string to datetimeoffset where value = $value")
                        dt
                    }
                    UOM::class -> {
                        val uom = UOM.lookUpFromUNCode(value)
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
                    Duration::class -> {
                        val ts = if (value.startsWith("+")) value.drop(1).toDuration() else value.toDuration()
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


        fun readKDE(name: String, json: JSONObject, namespaces: Map<String, String>): IEventKDE {
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

            if (json is JSONObject || json is JSONArray) {
                kde = EventKDEObject(ns, realName)
            } else {
                kde = EventKDEString(ns, realName)
            }

            kde?.SetFromJson(json) ?: throw Exception("Failed to initialize KDE from JSON = ${json.toString()}")

            return kde
        }


    }
}
