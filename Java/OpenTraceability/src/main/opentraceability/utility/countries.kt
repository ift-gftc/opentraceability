package utility

import java.util.*

class Countries {

    constructor() {
        _dirCountries = mutableMapOf()
        _dirAlpha3Countries = mutableMapOf()
        _dirNameCountries = mutableMapOf()
        Load()
    }

    companion object {
        internal var _dirCountries: MutableMap<String, Country> = mutableMapOf()
        internal var _dirAlpha3Countries: MutableMap<String, Country> = mutableMapOf()
        internal var _dirNameCountries: MutableMap<String, Country> = mutableMapOf()
        var _locker: Object = Object()

        fun load() {
            var data: String? = null
            data = StaticData.readData("Countries.xml")
            val xmlCountries = XDocument.parse(data)
            for (x in xmlCountries.root.elements()) {
                val country = Country(x)
                _dirCountries[country.abbreviation.toUpperCase()] = country
                _dirNameCountries[country.name.toUpperCase()] = country
                if (!country.alpha3.isNullOrEmpty()) {
                    _dirAlpha3Countries[country.alpha3.toUpperCase()] = country
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


        fun FromAbbreviation(code: String): Country? {
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

        fun FromAlpha3(code: String): Country? {
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

        fun FromCountryName(name: String): Country? {
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

        fun FromCountryIso(iso: Int): Country? {
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

        fun Parse(strValue: String): Country? {

            val parsedInt = strValue.toIntOrNull()
            if (parsedInt != null) {
                return FromCountryIso(parsedInt);
            } else {
                return FromAbbreviation(strValue) ?: FromAlpha3(strValue) ?: FromCountryName(strValue)
            }
        }
    }
}
