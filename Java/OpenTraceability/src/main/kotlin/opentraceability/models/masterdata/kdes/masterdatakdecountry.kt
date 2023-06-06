package opentraceability.models.masterdata.kdes

import opentraceability.interfaces.IMasterDataKDE
import org.json.*
import org.w3c.dom.*
import opentraceability.utility.Countries
import opentraceability.utility.Country
import opentraceability.utility.createXmlElement
import opentraceability.utility.createXmlElementNS
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

class MasterDataKDECountry() : MasterDataKDEBase(), IMasterDataKDE {
    var value: Country? = null

    override var valueType: KType = typeOf<Country>()


    override fun getEPCISXml(): Element? {
        return value?.let { country ->
            val x = createXmlElement("attribute")
            x.setAttribute("id", name)
            x.nodeValue = country.alpha3
            x
        }
    }

    override fun getGS1WebVocabJson(): JSONObject? {
        TODO("Not Implemented Exception")
    }

    fun getXml(): Element? {
        if (value == null)
        {
            return null
        }
        else {
            if (this.namespace != null && this.namespace.isNotEmpty())
            {
                var x = createXmlElementNS(this.namespace, this.name)
                x.nodeValue = this.value!!.alpha3
                return x
            }
            else {
                var x = createXmlElement(this.name)
                x.nodeValue = this.value!!.alpha3
                return x
            }
        }
    }

    override fun setFromEPCISXml(xml: Element) {
        val country = Countries.parse(xml.nodeValue)
        value = country
        name = xml.getAttribute("id") ?: ""
    }

    override fun setFromGS1WebVocabJson(json: JSONObject) {
        TODO("Not Implemented Exception")
    }

    override fun toString(): String {
        return value?.name ?: ""
    }
}
