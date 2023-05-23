package utility
class Measurement {
    var Value: Double = Double()
    var UoM: UOM = UOM()
    companion object{
    }

    fun Parse(strValue: String): Measurement {
        // Method body goes here
        return Measurement()
    }

    fun TryParse(strValue: String): Measurement {
        // Method body goes here
        return Measurement()
    }
}
