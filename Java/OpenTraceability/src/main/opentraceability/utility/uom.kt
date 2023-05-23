package utility
class UOM {
    var Key: String = String()
    var Name: String = String()
    var Abbreviation: String = String()
    var UnitDimension: String = String()
    var SubGroup: String = String()
    var UNCode: String = String()
    var A: Double = Double()
    var B: Double = Double()
    var C: Double = Double()
    var D: Double = Double()
    var Offset: Double = Double()
    companion object{
    }


    fun LookUpFromUNCode(unCode: String): UOM {
        TODO("Not yet implemented")
    }


    fun IsNullOrEmpty(uom: UOM): Boolean {
        TODO("Not yet implemented")
    }


    fun ParseFromName(name: String): UOM {
        TODO("Not yet implemented")
    }


    fun Convert(value: Double, from: UOM, to: UOM): Double {
        TODO("Not yet implemented")
    }
}
