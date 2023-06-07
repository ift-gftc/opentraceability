package opentraceability

import org.junit.jupiter.api.Assertions.*
import kotlinx.serialization.json.*
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.*
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.*
import org.w3c.dom.*
import kotlin.test.assertFails
import opentraceability.utility.*
import org.json.JSONObject
import java.io.File

class OpenTraceabilityTests {

    constructor() {
        init()
    }


    fun init() {
        opentraceability.Setup.Initialize()
        opentraceability.gdst.Setup.initialize()
    }


    companion object {

        internal fun compareXML(xml: String, xmlAfter: String) {
            val x1 = opentraceability.utility.createXmlElement(xml)
            val x2 = opentraceability.utility.createXmlElement(xmlAfter)
            xmlCompare(x1, x2)
        }

        internal fun xmlCompare(primary: Element, secondary: Element, noAssertions: Boolean = false) {
            if (primary.tagName != secondary.tagName) {
                assert(false) { "The XML element name does not match where name=${primary.tagName}.\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
            }

            if (primary.hasAttributes()) {
                if (primary.getAttributes().getLength() != secondary.getAttributes().getLength()) {
                    assert(false) { "The XML attribute counts to not match.\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
                }


                val attributes = primary.attributes
                for (i in 0 until attributes.length) {
                    val attr = attributes.item(i) as Attr

                    if (secondary.getAttribute(attr.name) == null) {
                        assert(false) { "The XML attribute ${attr.name} was not found on the secondary xml.\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
                    }

                    val val1 = attr.value
                    val val2 = secondary.getAttribute(attr.name)
                    if (val1?.toLowerCase() != val2?.toLowerCase()) {
                        if (!tryAdvancedValueCompare(val1, val2)) {
                            assert(false) { "The XML attribute ${attr.name} value does not match where the original is $val1 and the after is $val2.\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
                        }
                    }
                }


            }

            if (primary.hasChildNodes() || secondary.hasChildNodes()) {
                if (primary.elements().count() != secondary.elements().count()) {
                    val eles1 = primary.elements().map { it.nodeName.toString() }.toList()
                    val eles2 = secondary.elements().map { it.nodeName.toString() }.toList()

                    val missing1 = eles1.filter { !eles2.contains(it) }
                    val missing2 = eles2.filter { !eles1.contains(it) }

                    assert(false) {
                        "The XML child elements count does not match.\nElements only in primary xml: ${
                            missing1.joinToString(
                                ", "
                            )
                        }\nElements only in secondary xml: ${missing2.joinToString(", ")}\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}"
                    }
                }
                for (i in 0 until primary.elements().count()) {
                    val child1 = primary.elements().elementAt(i)

                    // we will try and find the matching node...
                    val xchild2 = findMatchingNode(child1, secondary, i)
                    if (xchild2 == null) {
                        assert(false) { "Failed to find matching node for comparison in the secondary xml.\nchild1=$child1\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
                    } else {
                        xmlCompare(child1, xchild2)
                    }
                }
            } else if (primary.textContent.toLowerCase() != secondary.textContent.toLowerCase()) {
                if (!tryAdvancedValueCompare(primary.textContent, secondary.textContent)) {
                    assert(false) { "The XML element value does not match where name=${primary.textContent} and value=${primary.textContent} with the after value=${secondary.textContent}.\nprimary xml:\n${primary.toString()}\n\nsecondary xml:\n${secondary.toString()}" }
                }
            }
        }

        internal fun findMatchingNode(xchild1: Element, x2: Element, i: Int): Element? {
            // lets see if there is more than one node with the same element name...
            if (x2.elements(xchild1.nodeName).count() == 0) {
                return null
            } else if (x2.elements(xchild1.nodeName).count() == 1) {
                return x2.element(xchild1.tagName)
            } else {
                var xchild2: Element? = null
                if (x2.tagName == "EventList") {
                    val eventxpaths =
                        listOf("eventID", "baseExtension/eventID", "TransformationEvent/baseExtension/eventID")
                    for (xp in eventxpaths) {
                        if (xchild1.getFirstElementByXPath(xp) != null) {
                            val eventid = xchild1.getFirstElementByXPath(xp)
                            xchild2 = x2.elements().firstOrNull { it.getFirstElementByXPath(xp) == eventid }
                            return xchild2
                        }
                    }
                }

                val id = xchild1.getAttribute("id") ?: ""
                if (!id.isEmpty()) {
                    xchild2 = x2.elements().firstOrNull { it.getAttribute("id") == id }
                }
                if (xchild2 == null) {
                    // try and find by internal value...
                    val value = xchild1.textContent
                    if (!value.isEmpty() && x2.elements()
                            .count { it.tagName == xchild1.tagName && it.textContent == value } == 1
                    ) {
                        xchild2 = x2.elements().firstOrNull { it.tagName == xchild1.tagName && it.textContent == value }
                    }
                    if (xchild2 == null) {
                        xchild2 = x2.elements().elementAt(i)
                        return xchild2
                    } else {
                        return xchild2
                    }
                } else {
                    return xchild2
                }
            }
        }

        internal fun tryAdvancedValueCompare(str1: String?, str2: String?): Boolean {
            return tryCompareDouble(str1, str2) || tryCompareXMLDateTime(str1, str2)
        }

        internal fun tryCompareXMLDateTime(str1: String?, str2: String?): Boolean {
            if (str1 != null && str2 != null) {
                val dt1 = str1.tryConvertToDateTimeOffset()
                val dt2 = str2.tryConvertToDateTimeOffset()
                if (dt1 != null && dt2 != null) {
                    return dt1 == dt2
                }
            }
            return false
        }

        internal fun tryCompareDouble(str1: String?, str2: String?): Boolean {
            if (str1 != null && str2 != null) {
                val d1 = str1.toDoubleOrNull()
                val d2 = str2.toDoubleOrNull()
                if (d1 != null && d2 != null) {
                    return d1 == d2
                }
            }
            return false
        }

        internal fun String.tryConvertToDateTimeOffset(): OffsetDateTime? {
            return try {
                OffsetDateTime.parse(this)
            } catch (e: DateTimeParseException) {
                null
            }
        }

        internal fun compareJSON(json: String, jsonAfter: String) {
            val j1 = JSONObject(json) as? JSONObject
                ?: throw Exception("Failed to parse json from string. $json")
            val j2 = JSONObject(jsonAfter) as? JSONObject
                ?: throw Exception("Failed to parse json from string. $jsonAfter")

            jsonCompare(j1, j2)
        }

        internal fun jsonCompare(j1: JSONObject, j2: JSONObject) {
            val j1props = j1.keys()
            val j2props = j2.keys()

            // go through each property
            while (j1props.hasNext()) {
                val name = j1props.next()
                val value = j1.get(name)

                val j2Value = if (j2.has(name)) j2[name] else null

                if (j2Value == null) {
                    assert(false) { "j1 has property $name, but it was not found on j2." }
                }

                if (value::class != j2Value!!::class) {
                    assert(false) {  "j1 property value type for $name is ${value::class}, but on j2 it is ${j2Value::class}." }
                }

                when (value) {
                    is JsonArray -> {
                        val jarr2 = j2Value as? JsonArray

                        if (jarr2 == null) {
                            fail("j1 property $name is JsonArray, but not on j2.")
                        } else if (value.size != jarr2.size) {
                            fail("j1 property value type for $name is an array with ${value.size} items, but the same property on j2 has only ${jarr2.size} items.")
                        } else {
                            for (i in value.indices) {
                                val jt1 = value[i]
                                val jt2 = jarr2[i]

                                if (jt1::class != jt2::class) {
                                    assert(false) { "j1 property array $name has item[$i] with type ${jt1::class}, but on j2 it is ${jt2::class}." }
                                }

                                when {
                                    jt1 is JSONObject && jt2 is JSONObject -> jsonCompare(jt1, jt2)
                                    else -> {
                                        val str1 = jt1.toString()
                                        val str2 = jt2.toString()
                                        if (!tryAdvancedValueCompare(str1, str2)) {
                                            if (str1.lowercase() != str2.lowercase()) {
                                                assert(false) { "j1 property array $name has item[$i] with value $str1, but on j2 it the value is $str2." }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is JSONObject -> {
                        val jobj2 = j2Value as? JSONObject
                        if (jobj2 != null) {
                            jsonCompare(value, jobj2)
                        } else {
                            assert(false) { "j1 property $name is JSONObject, but not on j2." }
                        }
                    }

                    else -> {
                        val str1 = value.toString()
                        val str2 = j2Value.toString()
                        if (!tryAdvancedValueCompare(str1, str2)) {
                            if (str1.lowercase() != str2.lowercase()) {
                                assert(false) { "j1 property $name has string value $str1, but on j2 it the value is $str2." }
                            }
                        }
                    }
                }
            }
        }


        fun readTestData(v: String): String {
            //val loader = EmbeddedResourceLoader()
            //val str = loader.readString("OpenTraceability.Tests", "opentraceability.test.data.$v")

            val str = loadFile("src/test/kotlin/opentraceability/data/$v")
            return str
        }



        fun loadFile(filePath: String) : String {
            val file = File(filePath)
            val jsonString = file.readText()
            return jsonString;
        }


        fun getConfiguration(appsettingsName: String): Properties {
            // get the current working directory
            val currentDirectory = Paths.get("").toAbsolutePath().toString()

            // delete any appsettings files in the directory
            Files.list(Paths.get(currentDirectory))
                .filter { path: Path -> path.fileName.toString().startsWith("appsettings") }
                .forEach { path: Path -> Files.deleteIfExists(path) }

            val config = Properties()

            //val loader = EmbeddedResourceLoader()
            //val jsonString = loader.readString("OpenTraceability.Tests","OpenTraceability.Tests.Configurations.$appsettingsName.json")

            val jsonString = loadFile("src/test/kotlin/opentraceability/configurations/$appsettingsName.json")
            val appsettings = JSONObject(jsonString)

            ByteArrayInputStream(appsettings.toString().toByteArray(Charsets.UTF_8)).use { stream -> config.load(stream) }

            try {

                //val machineJsonStr = loader.readString("OpenTraceability.Tests","OpenTraceability.Tests.Configurations.AppSettings.$appsettingsName.${System.getenv("COMPUTERNAME")}.json")

                val machineJsonStr = loadFile("src/test/kotlin/opentraceability/configurations/$appsettingsName.${System.getenv("COMPUTERNAME")}.json")
                val machineAppSettings = Json.parseToJsonElement(machineJsonStr) as JSONObject

                ByteArrayInputStream(machineAppSettings.toString().toByteArray(Charsets.UTF_8)).use { stream ->
                    config.load(stream)
                }
            } catch (ex: IOException) {
                if (!ex.message!!.contains("(The system cannot find the file specified)")) {
                    throw ex
                }
            }

            return config
        }

    }




}