package utility

import java.util.*

class UOMS {

    companion object {
        var uomsAbbrevDict: MutableMap<String, UOM> = mutableMapOf()
        var uomsUNCodeDict: MutableMap<String, UOM> = mutableMapOf()
        var _locker: Object = Object()

        fun Load() {
            TODO("Not yet implemented")
        }

        fun GetBase(uom: UOM): UOM {
            TODO("Not yet implemented")
        }

        fun GetBase(dimension: String): UOM {
            TODO("Not yet implemented")
        }

        fun GetUOMFromName(Name: String): UOM {
            TODO("Not yet implemented")
        }

        fun GetUOMFromUNCode(Name: String): UOM {
            TODO("Not yet implemented")
        }

        var List: List<UOM> = ArrayList<UOM>()
    }

    constructor(){
        _locker = Object()
        uomsAbbrevDict = mutableMapOf()
        uomsUNCodeDict = mutableMapOf()
        Load()
    }

}
