package opentraceability.queries

import opentraceability.models.identifiers.*
import org.apache.http.client.utils.URIBuilder
import java.net.*
import java.time.OffsetDateTime
import kotlin.reflect.KMutableProperty
import java.net.URLEncoder
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.*
import kotlin.reflect.typeOf


class EPCISQueryParameters {
    private val propMapping = mutableMapOf<String, KMutableProperty<*>>()

    init {
        for (prop in EPCISQuery::class.members) {
            if (prop is KMutableProperty<*>) {
                propMapping[prop.name] = prop
            }
        }
    }

    var queryType: EPCISQueryType = EPCISQueryType.events
    val query: EPCISQuery = EPCISQuery()

    constructor()

    constructor(vararg epcs: EPC) {
        for (epc in epcs) {
            if (epc.Type == EPCType.Class) {
                query.MATCH_anyEPCClass?.add(epc.toString())
            } else {
                query.MATCH_anyEPC?.add(epc.toString())
            }
        }
    }

    constructor(uri: URI) {
        val uriBuilder = URIBuilder(uri)
        val queryParameters = uriBuilder.queryParams
        for (param in queryParameters) {
            val key = param.name
            val value = URLDecoder.decode(param.value, "UTF-8")

            val prop = propMapping[key]
            if (prop != null) {
                when (prop.returnType) {
                    typeOf<OffsetDateTime>() -> {
                        val dt = OffsetDateTime.parse(value)
                        prop.setter.call(query, dt)
                    }
                    typeOf<MutableList<String>>() -> {
                        val values = value.split("|")
                        prop.setter.call(query, values.toMutableList())
                    }
                    typeOf<MutableList<URI>>() -> {
                        val values = value.split("|").map { URI.create(it) }
                        prop.setter.call(query, values.toMutableList())
                    }
                }
            }
        }
    }

    fun isValid(): Boolean {
        return true
    }

    fun toUri(): URI {
        val queryParameters = mutableListOf<String>()

        for (prop in EPCISQuery::class.members) {
            if (prop is KMutableProperty<*>) {
                when (val value = prop.getter.call(query)) {
                    is OffsetDateTime? -> {
                        value?.let { queryParameters.add("${prop.name}=${URLEncoder.encode(it.toString(), "UTF-8")}") }
                    }
                    is MutableList<*> -> {
                        value.let {
                            if (it.isNotEmpty()) {
                                val encodedValues = it.map { URLEncoder.encode(it.toString(), "UTF-8") }
                                queryParameters.add("${prop.name}=${encodedValues.joinToString("|")}")
                            }
                        }
                    }
                }
            }
        }

        val queryString = queryParameters.joinToString("&")
        return URI.create("?${queryString}")
    }

    fun merge(queryParameters: EPCISQueryParameters) {
        for (prop in EPCISQuery::class.members) {
            if (prop is KMutableProperty<*>) {
                when (val otherValue = prop.getter.call(queryParameters.query)) {
                    is OffsetDateTime? -> {
                        if (otherValue != null) {
                            prop.setter.call(this.query, otherValue)
                        }
                    }
                    is MutableList<*> -> {
                        if (otherValue.isNotEmpty()) {
                            val list = prop.getter.call(this.query) as? MutableList<Any>
                            if (list == null) {
                                prop.setter.call(this.query, otherValue.toMutableList())
                            } else {
                                list.addAll(otherValue as Collection<Any>)
                            }
                        }
                    }
                }
            }
        }
    }

    fun toQueryParameters(): String {
        val queryParameters = mutableListOf<String>()

        // Go through each property on the query
        for (prop in EPCISQueryParameters::class.memberProperties) {
            when (val value = prop.get(this)) {
                is String -> {
                    if (value.isNotBlank()) {
                        val encodedValue = URLEncoder.encode(value.toString(), "UTF-8")
                        val queryParam = "${prop.name}=${encodedValue}"
                        queryParameters.add(queryParam)
                    }
                }
                is MutableList<*> -> {
                    if (value.isNotEmpty()) {
                        val encodedValues = value.filterIsInstance<String>()
                            .map { URLEncoder.encode(it, "UTF-8") }
                            .joinToString("|")
                        val queryParam = "${prop.name}=$encodedValues"
                        queryParameters.add(queryParam)
                    }
                }
                is URI -> {
                    val queryParam = "${prop.name}=${URLEncoder.encode(value.toString(), "UTF-8")}"
                    queryParameters.add(queryParam)
                }
                is OffsetDateTime? -> {
                    if (value != null) {
                        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
                        val isoString = value.format(formatter)

                        val queryParam = "${prop.name}=${URLEncoder.encode(isoString, "UTF-8")}"
                        queryParameters.add(queryParam)
                    }
                }
            }
        }

        val queryString = queryParameters.joinToString("&")
        return "?$queryString"
    }



}
