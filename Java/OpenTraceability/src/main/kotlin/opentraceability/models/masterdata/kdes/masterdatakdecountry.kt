package models.masterdata.kdes


import interfaces.IMasterDataKDE
import utility.Country
import org.jdom2.Element

class MasterDataKDECountry : MasterDataKDEBase, IMasterDataKDE {
    var value: Country? = null

     val valueType: Class<*>
        get() = Country::class.java

    fun getEPCISXml(): Element? {
        return value?.let { country ->
            val x = Element("attribute")
            x.addAttribute(XAttribute("id", name))
            x.value = country.alpha3
            x
        }
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        TODO("Not Implemented Exception")
    }

    fun getXml(): Element? {
        return if (value == null) null else XmlElement((Namespace as XNamespace) + name, value.iso)
    }

    fun setFromEPCISXml(xml: Element) {
        val country = Countries.parse(xml.value)
        value = country
        name = xml.getAttributeValue("id") ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        TODO("Not Implemented Exception")
    }

    override fun toString(): String {
        return value?.Name ?: ""
    }
}
