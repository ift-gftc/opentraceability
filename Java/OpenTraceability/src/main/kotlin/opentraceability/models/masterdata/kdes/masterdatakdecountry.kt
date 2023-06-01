package models.masterdata.kdes

import interfaces.IMasterDataKDE
import org.json.*
import org.w3c.dom.*
import utility.Countries
import utility.Country

class MasterDataKDECountry : MasterDataKDEBase(), IMasterDataKDE {
    var value: Country? = null

     val valueType: Class<*> = Country::class.java


    override fun getEPCISXml(): Element? {
        return value?.let { country ->
            val x = Element("attribute")
            x.addAttribute(XAttribute("id", Name))
            x.value = country.Alpha3
            x
        }
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        TODO("Not Implemented Exception")
    }

    fun getXml(): Element? {
        return if (value == null) null else Element((Namespace as XNamespace) + name, value.ISO)
    }

    override fun setFromEPCISXml(xml: Element) {
        val country = Countries.parse(xml.value)
        value = country
        Name = xml.getAttributeValue("id") ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        TODO("Not Implemented Exception")
    }

    override fun toString(): String {
        return value?.Name ?: ""
    }
}
