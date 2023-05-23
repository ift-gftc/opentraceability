package models.identifiers

import models.identifiers.*
import java.lang.reflect.Type

class EPC {
    var Type: EPCType = EPCType()
    var GTIN: GTIN = GTIN()
    var SerialLotNumber: String = String()

    companion object {
    }

    fun DetectEPCIssue(epcStr: String): String {
        TODO("Not yet implemented")
    }

    fun TryParse(epcStr: String, epc: EPC, error: String): Boolean {
        TODO("Not yet implemented")
    }
}
