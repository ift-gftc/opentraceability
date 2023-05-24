package utility
import javax.xml.bind.annotation.XmlElement

class Measurement {
    var Value: Double = 0.0
    var UoM: UOM = UOM()


    constructor(){
        Value = 0.0
        UoM =  UOM()
    }

    constructor(xmlElement: XmlElement){
        Value = 0.0
        UoM =  UOM()
    }

    constructor(copyFrom: Measurement){
        TODO("Not yet implemented")
    }

    constructor(value: Double, unitCode: UOM){
        TODO("Not yet implemented")
    }

    constructor(value: Double, unitCode: String){
        TODO("Not yet implemented")
    }

    fun Add(measurement: Measurement) {
        TODO("Not yet implemented")
    }

    fun ToBase() : Measurement {
        TODO("Not yet implemented")
    }

    fun ConvertTo(uomStr: String) : Measurement {
        TODO("Not yet implemented")
    }

    fun ConvertTo(uom: UOM) : Measurement {
        TODO("Not yet implemented")
    }

    fun ToString() : String {
        TODO("Not yet implemented")
    }

    fun ToStringEx() : String {
        TODO("Not yet implemented")
    }

    fun GetUniquenessKey(iVersion: Int) : String {
        TODO("Not yet implemented")
    }


    fun Parse(strValue: String): Measurement {
        TODO("Not yet implemented")
    }

    fun TryParse(strValue: String): Measurement {
        TODO("Not yet implemented")
    }
}
