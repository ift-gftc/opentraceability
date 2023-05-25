package queries

import java.util.*
import models.identifiers.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class EPCISQuery {
    var GE_recordTime: OffsetDateTime? = null
    var LE_recordTime: OffsetDateTime? = null
    var GE_eventTime: OffsetDateTime? = null
    var LE_eventTime: OffsetDateTime? = null
    var eventTypes: ArrayList<String> = ArrayList<String>()
    var MATCH_epc: ArrayList<String> = ArrayList<String>()
    var MATCH_epcClass: ArrayList<String> = ArrayList<String>()
    var MATCH_anyEPC: ArrayList<String> = ArrayList<String>()
    var MATCH_anyEPCClass: ArrayList<String> = ArrayList<String>()
    var EQ_bizStep: ArrayList<String> = ArrayList<String>()
    var EQ_bizLocation: ArrayList<URI> = ArrayList<URI>()
    var EQ_action: ArrayList<String> = ArrayList<String>()

    fun ShouldSerializeeventTypes(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeMATCH_epc(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeMATCH_epcClass(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeMATCH_anyEPC(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeMATCH_anyEPCClass(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeEQ_bizStep(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeEQ_bizLocation(): Boolean{
        TODO("Not yet implemented")
    }
    fun ShouldSerializeEQ_action(): Boolean{
        TODO("Not yet implemented")
    }

}
