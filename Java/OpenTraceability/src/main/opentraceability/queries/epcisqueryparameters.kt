package queries

import models.identifiers.*
import models.identifiers.EPC
import models.identifiers.EPCType
import org.intellij.markdown.lexer.push
import java.lang.reflect.Type
import java.net.URL

import org.apache.http.client.utils.URIBuilder
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.reflect.KMutableProperty

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
            if (epc.type == EPCType.Class) {
                query.MATCH_anyEPCClass?.add(epc.toString())
                    ?: run { query.MATCH_anyEPCClass = mutableListOf(epc.toString()) }
            } else {
                query.MATCH_anyEPC?.add(epc.toString())
                    ?: run { query.MATCH_anyEPC = mutableListOf(epc.toString()) }
            }
        }
    }

    constructor(uri: URI) {
        val queryParameters = URIBuilder(uri).parameters
        for (param in queryParameters) {
            val key = param.name
            val value = URLDecoder.decode(param.value, "UTF-8")

            val prop = propMapping[key]
            if (prop != null) {
                when (prop.returnType) {
                    KType.Companion.typeOf<DateTimeOffset?>() -> {
                        val dt = DateTimeOffset.parse(value)
                        prop.setter.call(query, dt)
                    }
                    KType.Companion.typeOf<MutableList<String>?>() -> {
                        val values = value.split("|")
                        prop.setter.call(query, values.toMutableList())
                    }
                    KType.Companion.typeOf<MutableList<URI>?>() -> {
                        val values = value.split("|").map { URI(it) }
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
                    is DateTimeOffset? -> {
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
                    is MutableList<URI> -> {
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
                    is DateTimeOffset? -> {
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
                    is MutableList<URI> -> {
                        if (otherValue.isNotEmpty()) {
                            val list = prop.getter.call(this.query) as? MutableList<URI>
                            if (list == null) {
                                prop.setter.call(this.query, otherValue.toMutableList())
                            } else {
                                list.addAll(otherValue as Collection<URI>)
                            }
                        }
                    }
                }
            }
        }
    }
}
