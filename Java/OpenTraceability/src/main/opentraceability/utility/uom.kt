package opentraceability.utility

import com.intellij.json.psi.JsonObject
import opentraceability.OTLogger

//[System.Xml.Serialization.XmlTypeAttribute(AnonymousType = true)
//[DataContract]
class UOM {

    companion object {


        var _locker: Object = Object()

        fun LookUpFromUNCode(unCode: String): UOM? {
            try {
                //TODO: _locker not yet implemented
                //lock(_locker) {

                var uom: UOM? = UOMS.List.filter { s -> s.UNCode == unCode }.single()
                if (uom == null) {
                    uom = UOM()
                }

                return uom
                //}
            } catch (ex: Exception) {
                OTLogger.Error(ex)
                throw ex
            }
        }


        fun IsNullOrEmpty(uom: UOM): Boolean {
            if (uom == null) {
                return true
            } else if (!uom.Abbreviation.isNullOrEmpty()) {
                return true
            } else {
                return false
            }
        }

        fun ParseFromName(name: String): UOM? {
            var u: UOM? = null

            try {

                if (name.isNullOrEmpty()) {
                    throw Exception("argument is null");
                }

                var uom: UOM? = UOMS.GetUOMFromName(name);
                if (uom != null) {
                    u = UOM();
                    u.Name = uom.Name;
                    u.Abbreviation = uom.Abbreviation;
                    u.UnitDimension = uom.UnitDimension;
                    u.UNCode = uom.UNCode;
                    u.Offset = uom.Offset;
                    u.SubGroup = uom.SubGroup;
                    u.A = uom.A;
                    u.B = uom.B;
                    u.C = uom.C;
                    u.D = uom.D;
                } else {
                    uom = UOMS.GetUOMFromUNCode(name.toUpperCase());
                    if (uom != null) {
                        u = UOM();
                        u.Name = uom.Name;
                        u.Abbreviation = uom.Abbreviation;
                        u.UnitDimension = uom.UnitDimension;
                        u.SubGroup = uom.SubGroup;
                        u.UNCode = uom.UNCode;
                        u.Offset = uom.Offset;
                        u.A = uom.A;
                        u.B = uom.B;
                        u.C = uom.C;
                        u.D = uom.D;
                    } else {
                        throw Exception("Failed to parse UOM");
                    }
                }

                return u
            } catch (ex: Exception) {
                OTLogger.Error(ex)
                throw ex
            }
        }
    }

    var Key: String = String()
        get() {
            return (Abbreviation);
        }


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

    constructor() {
        this.A = 0.0
        this.B = 1.0
        this.C = 1.0
        this.D = 0.0
    }

    constructor(uom: UOM) {
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

    fun CopyFrom(uom: UOM) {
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

    fun IsBase(): Boolean {
        if (A == 0.0 && B == 1.0 && C == 1.0 && D == 0.0) {
            return (true);
        } else {
            return (false);
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj != null) {
            if (obj is UOM) {
                var other: UOM = obj;
                if (UNCode == other.UNCode) {
                    return (true);
                }
            }
        }
        return (false);
    }

    override fun hashCode(): Int {
        if (UNCode != null) {
            return (UNCode.hashCode());
        } else {
            //TODO: implement base class
            //return (base.hashCode());
            return 1
        }
    }


    constructor(juom: JsonObject) {

        this.A = 0.0
        this.B = 1.0
        this.C = 1.0
        this.D = 0.0

        //TODO: implement Json
        /*
        this.Abbreviation = uom.Abbreviation;
        this.Name = uom.Name;
        this.UnitDimension = uom.UnitDimension;
        this.UNCode = uom.UNCode;
        this.SubGroup = uom.SubGroup;
        this.Offset = uom.Offset;
        */
    }


    fun Convert(value: Double, to: UOM): Double {
        var valueBase: Double = this.ToBase(value);
        var valueNew: Double = to.FromBase(valueBase);
        return (valueNew);
    }

    fun Convert(value: Double, from: UOM, to: UOM): Double {
        var valueBase: Double = from.ToBase(value);
        var valueNew: Double = to.FromBase(valueBase);
        return (valueNew);
    }

    fun ToBase(value: Double): Double {
        var baseValue: Double = ((A + B * value) / (C + D * value)) - Offset;
        return baseValue;
    }

    fun FromBase(baseValue: Double): Double {
        var value: Double  = ((A - C * baseValue) / (D * baseValue - B)) + Offset;
        return value;
    }


    fun ToString(): String {
        return this.Name + " [" + this.Abbreviation + "]"
    }
}
