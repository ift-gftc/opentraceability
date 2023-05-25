package utility

import java.lang.reflect.Type
import java.util.*

class Countries {

    companion object {
        internal var _dirCountries: MutableMap<String, Country>  = mutableMapOf()
        internal var _dirAlpha3Countries: MutableMap<String, Country>  = mutableMapOf()
        internal var _dirNameCountries: MutableMap<String, Country>  = mutableMapOf()


        var CountryList: ArrayList<Country> = ArrayList<Country>()

        fun FromAbbreviation(code: String): Country {
            TODO("Not yet implemented")
        }

        fun FromAlpha3(code: String): Country {
            TODO("Not yet implemented")
        }

        fun FromCountryName(name: String): Country {
            TODO("Not yet implemented")
        }

        fun FromCountryIso(iso: Int): Country {
            TODO("Not yet implemented")
        }

        fun Parse(strValue: String): Country {
            TODO("Not yet implemented")
        }
    }
}
