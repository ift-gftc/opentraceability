package utility

import com.intellij.json.psi.JsonObject

class UOM {

    companion object{
    }

    var Key: String = String()
    var Name: String = String()
    var Abbreviation: String = String()
    var UnitDimension: String = String()
    var SubGroup: String = String()
    var UNCode: String = String()
    var A: Double = 0.0
    var B: Double = 0.0
    var C: Double = 0.0
    var D: Double = 0.0
    var Offset: Double = 0.0

    constructor(){
        this.A = 0.0
        this.B = 1.0
        this.C = 1.0
        this.D = 0.0
    }
    constructor(uom: UOM){
        this.Abbreviation = uom.Abbreviation;
        this.Name = uom.Name;
        this.UnitDimension = uom.UnitDimension;
        this.UNCode = uom.UNCode;
        this.SubGroup = uom.SubGroup;
        this.Offset = uom.Offset;
        this.A = uom.A;
        this.B = uom.B;
        this.C = uom.C;
        this.D = uom.D;
    }

    constructor(juom: JsonObject){

        this.A = 0.0
        this.B = 1.0
        this.C = 1.0
        this.D = 0.0

        /*
        this.Abbreviation = uom.Abbreviation;
        this.Name = uom.Name;
        this.UnitDimension = uom.UnitDimension;
        this.UNCode = uom.UNCode;
        this.SubGroup = uom.SubGroup;
        this.Offset = uom.Offset;
        */
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

    fun Convert(value: Double, to: UOM): Double {
        TODO("Not yet implemented")
    }

    fun Convert(value: Double, from: UOM, to: UOM): Double {
        TODO("Not yet implemented")
    }

    fun ToBase(value: Double): Double {
        TODO("Not yet implemented")
    }

    fun FromBase(baseValue: Double): Double {
        TODO("Not yet implemented")
    }


    fun ToString(): String {
        return this.Name + " [" + this.Abbreviation + "]"
    }
}
