package opentraceability.mappers

import opentraceability.interfaces.*
import opentraceability.models.common.LanguageString
import opentraceability.models.events.*
import opentraceability.models.events.kdes.*
import opentraceability.models.identifiers.*
import org.json.*
import opentraceability.utility.*
import opentraceability.utility.StringExtensions.toDuration
import opentraceability.utility.StringExtensions.tryConvertToDateTimeOffset
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf
import kotlin.time.Duration

class OpenTraceabilityJsonLDMapper {
    companion object {

        /**
         * Converts an object into JSON.
         */
        fun toJson(value: Any?, namespacesReversed: MutableMap<String, String>, required: Boolean = false): JSONObject? {
            try {
                value?.let {
                    var json: JSONObject? = JSONObject()
                    var jpointer: JSONObject = json!!

                    val t: KType = value::class.starProjectedType
                    //val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.getJsonTypeInfo(t as KClass<*>)
                    val typeInfo: OTMappingTypeInformation = OTMappingTypeInformation.getJsonTypeInfo(t::class)
                    typeInfo.properties.filter { it.Version == null || it.Version == EPCISVersion.V2 }.forEach { property ->
                        property.Property?.getter?.call(value)?.let { obj ->
                            var jvaluepointer: JSONObject = jpointer
                            val xchildname: String = property.Name

                            when {
                                property.IsQuantityList -> {
                                    var products: MutableList<EventProduct> = obj as MutableList<EventProduct>
                                    products = products.filter { (it.Quantity != null) && (it.Type == property.ProductType) }.toMutableList()
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
                                    var products: MutableList<EventProduct> = obj as MutableList<EventProduct>
                                    products = products.filter { (it.Quantity == null) && (it.Type == property.ProductType) }.toMutableList()
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
                                            val jt: String? = writeObjectToJToken(obj)
                                            if (jt != null) {
                                                jvaluepointer.put(xchildname, jt)
                                            }
                                        } else {
                                            for (o in list) {
                                                if (property.IsObject) {
                                                    val xchild: JSONObject? = toJson(o, namespacesReversed, property.Required)
                                                    if (xchild != null) {
                                                        xlist.put(xchild)
                                                    }
                                                } else {
                                                    val jt: String? = writeObjectToJToken(obj)
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
                                    val jt: String? = writeObjectToJToken(obj)
                                    if (jt != null) {
                                        jvaluepointer.put(xchildname, jt)
                                    }
                                }
                            }
                        }
                    }

                    typeInfo.extensionKDEs?.getter?.call(value)?.let { obj ->
                        if (obj is MutableList<*>) {
                            val kdes: MutableList<IEventKDE> = obj as MutableList<IEventKDE>
                            for (kde in kdes) {
                                kde.getJson()?.let { xchild ->
                                    var name: String = kde.name
                                    if (kde.namespace != null) {
                                        if (!namespacesReversed.containsKey(kde.namespace)) {
                                            throw Exception("The namespace ${kde.namespace} is not recognized in the EPCIS Document / EPCIS Query Document.")
                                        }
                                        name = namespacesReversed[kde.namespace] + ":" + name
                                    }

                                    jpointer.put(name,xchild)
                                }
                            }
                        }
                    }

                    typeInfo.extensionAttributes?.getter?.call(value)?.let { obj ->
                        if (obj is MutableList<*>) {
                            val kdes: MutableList<IEventKDE> = obj as MutableList<IEventKDE>
                            for (kde in kdes) {
                                kde.getJson()?.let { xchild ->
                                    var name: String = kde.name
                                    if (kde.namespace != null) {
                                        if (!namespacesReversed.containsKey(kde.namespace)) {
                                            throw Exception("The namespace ${kde.namespace} is not recognized in the EPCIS Document / EPCIS Query Document.")
                                        }
                                        name = namespacesReversed[kde.namespace] + ":" + name
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
                opentraceability.OTLogger.error(e)
                throw e
            }
        }

        /**
         * Converts a JSON object into the generic type specified.
         */
        inline fun <reified T> fromJson(json: JSONObject, namespaces: MutableMap<String, String>): T {
            val o: T = fromJson(json, T::class.java as KClass<*>, namespaces) as T
            return o
        }

        /**
         * Converts a JSON object into the type specified.
         */
        fun fromJson(json: JSONObject, type: KClass<*>, namespaces: MutableMap<String, String>): Any {
            //val value: Any = type::class.createInstance() ?: throw Exception("Failed to create instance of type ${type.qualifiedName}")

            val noArgsConstructor = type.primaryConstructor
            val value: Any = if (noArgsConstructor != null && noArgsConstructor.parameters.isEmpty()) {
                 noArgsConstructor.call()
            } else {
                throw Exception("Failed to create instance of type ${type.qualifiedName}")
            }


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
                    for (jprop in jobj.keys()) {
                        mappingProp = typeInfo[jprop]

                        if (mappingProp != null && mappingProp.Property is KMutableProperty<*>) {
                            continue
                        }

                        val jChild = jobj.get(jprop) as? JSONObject
                        if (jChild != null)
                        {
                            when {
                                mappingProp != null -> {
                                    readPropertyMapping(mappingProp, jChild, value, namespaces)
                                }
                                extensionKDEs != null -> {
                                    val kde: IEventKDE = readKDE(jprop, jChild, namespaces)
                                    extensionKDEs.add(kde)
                                }
                                extensionAttributes != null -> {
                                    val kde: IEventKDE = readKDE(jprop, jChild, namespaces)
                                    extensionAttributes.add(kde)
                                }
                            }
                        }
                    }
                }

                typeInfo.extensionAttributes?.setter?.call(value, extensionAttributes)
                typeInfo.extensionKDEs?.setter?.call(value, extensionKDEs)
            } catch (ex: Exception) {
                opentraceability.OTLogger.error(ex)
                throw ex
            }

            return value
        }

        fun writeObjectToJToken(obj: Any?): String? {
            return when(obj) {
                null -> null
                is OffsetDateTime -> {
                    obj.format(ISO_OFFSET_DATE_TIME)
                }
                is UOM -> {
                    obj.UNCode
                }
                is Double, is Boolean -> {
                    obj.toString()
                }
                is Country -> {
                    obj.abbreviation
                }
                is Duration -> {
                    if (obj.isNegative())
                        "-${obj.inWholeHours}:${obj.inWholeMinutes}"
                    else
                        "+${obj.inWholeHours}:${obj.inWholeMinutes}"
                }
                else -> obj.toString() ?: ""
            }
        }

        fun readPropertyMapping(mappingProp: OTMappingTypeInformationProperty, json: JSONObject, value: Any, namespaces: MutableMap<String, String>) {
            when {
                mappingProp.IsQuantityList -> {
                    val e = value as IEvent
                    val jQuantityList = json as? JSONArray
                    jQuantityList?.forEach {
                        val jQuantity = it as JSONObject
                        val epc = EPC(jQuantity.getString("epcClass"))

                        val product = EventProduct(epc).apply {
                            Type = mappingProp.ProductType!!
                            Quantity = Measurement(jQuantity.getDouble("quantity"), jQuantity.getString("uom") ?: "EA")
                        }

                        e.addProduct(product)
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
                        e.addProduct(product)
                    }
                }
                mappingProp.IsArray -> {
                    var list = mappingProp.Property?.getter?.call(value) as? MutableList<Any?>

                    if (list == null)
                    {
                        list = (mappingProp.Property.returnType as KClass<*>).createInstance() as MutableList<Any?>
                        mappingProp.Property?.setter?.call(value, list)
                    }

                    val itemType = (list::class.starProjectedType as KClass<*>).typeParameters[0] as KClass<*>

                    if (mappingProp.IsRepeating && json !is JSONArray) {
                        val v = json.toString()
                        if (v.isNotBlank()) {
                            val o = readObjectFromString(v, itemType)
                            list.add(o)
                        }
                    } else {
                        val jArr = json as? JSONArray
                        jArr?.forEach { it: Any? ->
                            val o = if (mappingProp.IsObject)
                            {
                                fromJson(it as JSONObject, itemType, namespaces)
                            } else {
                                readObjectFromString(it.toString(), itemType)
                            }
                            list.add(o)
                        }
                    }
                }
                mappingProp.IsObject -> {
                    val o = fromJson(json, mappingProp.Property?.returnType as KClass<*>, namespaces)
                    mappingProp.Property?.setter?.call(value, o)
                }
                mappingProp.Property?.returnType == List::class.java -> {
                    val languageStrings: MutableList<LanguageString>? = fromJson(json, typeOf<MutableList<LanguageString>>() as KClass<*>, namespaces) as MutableList<LanguageString>
                    if (languageStrings != null) {
                        mappingProp.Property?.setter?.call(value, languageStrings)
                    }
                }
                else -> {
                    val v = json.toString()
                    if (v.isNotBlank()) {
                        val o = readObjectFromString(v, mappingProp.Property?.returnType as KClass<*>)
                        mappingProp.Property?.setter?.call(value, o)
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
                opentraceability.OTLogger.error(e)
                throw e
            }
        }

        fun readKDE(name: String, json: JSONObject, namespaces: MutableMap<String, String>): IEventKDE {
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

            kde?.setFromJson(json) ?: throw Exception("Failed to initialize KDE from JSON = ${json.toString()}")

            return kde
        }

    }
}
