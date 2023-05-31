package utility

import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import java.io.StringReader
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

class Countries {

    constructor() {
        _dirCountries = mutableMapOf()
        _dirAlpha3Countries = mutableMapOf()
        _dirNameCountries = mutableMapOf()
        load()
    }

    companion object {
        internal var _dirCountries: MutableMap<String, Country> = mutableMapOf()
        internal var _dirAlpha3Countries: MutableMap<String, Country> = mutableMapOf()
        internal var _dirNameCountries: MutableMap<String, Country> = mutableMapOf()

        fun load() {
            var data: String? = StaticData.readData("Countries.xml")

            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val xmlInput = InputSource(StringReader(data))
            val xmlCountries: Document = dBuilder.parse(xmlInput)

            val root = xmlCountries.documentElement // get root element

            for (i in 0 until root.childNodes.length) {
                val node = root.childNodes.item(i)
                if (node.nodeType == Node.ELEMENT_NODE) {
                    val x = node as Element
                    val country = Country(x)
                    _dirCountries[country.Abbreviation.toUpperCase()] = country
                    _dirNameCountries[country.Name.toUpperCase()] = country
                    if (!country.Alpha3.isNullOrEmpty()) {
                        _dirAlpha3Countries[country.Alpha3.toUpperCase()] = country
                    }
                }
            }
        }


        var CountryList: ArrayList<Country> = ArrayList<Country>()
            get() {
                var list: ArrayList<Country> = ArrayList()

                _dirNameCountries.forEach { element ->
                    list.add(element.value)
                }
                return list
            }


        fun fromAbbreviation(code: String): Country? {
            var country: Country? = null

            if (!code.isNullOrEmpty()){
                if (_dirCountries != null){
                    if (_dirCountries.containsKey((code.toUpperCase()))){
                        country = _dirCountries.get(code.toUpperCase())!!;
                    }
                }
            }

            return country
        }

        fun fromAlpha3(code: String): Country? {
            var country: Country? = null

            if (!code.isNullOrEmpty()){
                if (_dirCountries != null){
                    if (_dirAlpha3Countries.containsKey((code.toUpperCase()))){
                        country = _dirCountries.get(code.toUpperCase())!!;
                    }
                }
            }

            return country
        }

        fun fromCountryName(name: String): Country? {
            var country: Country? = null

            if (_dirCountries != null && !name.isNullOrEmpty()){

                if (_dirNameCountries.containsKey((name.toUpperCase()))){
                    country = _dirCountries.get(name.toUpperCase())!!;
                }
                else{
                    run loop@{
                        _dirNameCountries.forEach { element ->
                            if (element.value.AlternativeName != null) {
                                if (element.value.AlternativeName.toUpperCase() == name.toUpperCase()) {
                                    country = element.value;
                                    return@loop
                                }
                            }
                        }
                    }
                }
            }

            return country
        }

        fun fromCountryIso(iso: Int): Country? {
            var country: Country? = null

            if (_dirCountries != null){

                run loop@{
                    _dirCountries.forEach { element ->
                        if (element.value.ISO == iso) {
                            country = element.value;
                            return@loop
                        }
                    }
                }

                return country
            }

            return country
        }

        fun parse(strValue: String): Country? {

            val parsedInt = strValue.toIntOrNull()
            if (parsedInt != null) {
                return fromCountryIso(parsedInt);
            } else {
                return fromAbbreviation(strValue) ?: fromAlpha3(strValue) ?: fromCountryName(strValue)
            }
        }
    }
}
