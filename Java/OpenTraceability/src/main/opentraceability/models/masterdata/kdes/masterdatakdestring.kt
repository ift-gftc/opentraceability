package models.masterdata.kdes

import interfaces.IMasterDataKDE


class MasterDataKDEString : MasterDataKDEBase, IMasterDataKDE {
    override var ValueType: Class<*>
        get() = String::class.java

    var value: String? = null
    var type: String? = null
    val attributes: MutableMap<String, String> = mutableMapOf()

    constructor(ValueType: Class<*>) {
        // Default constructor
        this.ValueType = ValueType
    }

    constructor(ns: String, name: String) {
        super.namespace = ns
        super.name = name
    }

    fun getJson(): JToken? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            JToken.fromObject(value)
        }
    }

    fun getXml(): XElement? {
        return if (value.isNullOrBlank()) {
            null
        } else {
            val xname = (XNamespace)namespace + super.name
            val x = XElement(xname, value)

            // set the xsi type...
            attributes.forEach { (key, value) ->
                x.add(XAttribute(key, value))
            }

            x
        }
    }

    fun setFromJson(json: JToken) {
        value = json.toString()
    }

    fun setFromXml(xml: XElement) {
        value = xml.value

        xml.attributes().forEach { xatt ->
            attributes[xatt.name.toString()] = xatt.value
        }
    }

    override fun toString(): String {
        return value ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JToken) {
        value = json.toString()
    }

    override fun getGS1WebVocabJson(): JToken? {
        return if (value != null) {
            JToken.fromObject(value)
        } else {
            null
        }
    }

    override fun setFromEPCISXml(xml: XElement) {
        name = xml.attribute("id")?.value ?: ""
        value = xml.value
    }

    override fun getEPCISXml(): XElement? {
        return if (value != null) {
            val x = XElement("attribute")
            x.add(XAttribute("id", super.name))
            x.value = value
            x
        } else {
            null
        }
    }
}
