package models.masterdata.kdes

import com.fasterxml.jackson.core.JsonToken
import interfaces.IEventKDE
import interfaces.IMasterDataKDE
import models.events.kdes.EventKDEBase
import utility.Country
import java.lang.reflect.Type
import javax.xml.bind.annotation.XmlElement

class MasterDataKDECountry : MasterDataKDEBase, IMasterDataKDE {
    var value: Country? = null

    override val valueType: Class<*>
        get() = Country::class.java

    fun getEPCISXml(): XElement? {
        return value?.let { country ->
            val x = XElement("attribute")
            x.addAttribute(XAttribute("id", name))
            x.value = country.alpha3
            x
        }
    }

    override fun getGS1WebVocabJson(): JToken? {
        throw NotImplementedException()
    }

    fun getXml(): XElement? {
        return if (value == null) null else XElement((Namespace as XNamespace) + name, value.iso)
    }

    fun setFromEPCISXml(xml: XElement) {
        val country = Countries.parse(xml.value)
        value = country
        name = xml.getAttributeValue("id") ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JToken) {
        throw NotImplementedException()
    }

    override fun toString(): String {
        return value?.name ?: ""
    }
}
