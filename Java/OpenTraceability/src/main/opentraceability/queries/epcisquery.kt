package opentraceability.queries

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
        return eventTypes.count() > 0;
    }
    fun ShouldSerializeMATCH_epc(): Boolean{
        return MATCH_epc.count() > 0;
    }
    fun ShouldSerializeMATCH_epcClass(): Boolean{
        return MATCH_epcClass.count() > 0;
    }
    fun ShouldSerializeMATCH_anyEPC(): Boolean{
        return MATCH_anyEPC.count() > 0;
    }
    fun ShouldSerializeMATCH_anyEPCClass(): Boolean{
        return MATCH_anyEPCClass.count() > 0;
    }
    fun ShouldSerializeEQ_bizStep(): Boolean{
        return EQ_bizStep.count() > 0;
    }
    fun ShouldSerializeEQ_bizLocation(): Boolean{
        return EQ_bizLocation.count() > 0;
    }
    fun ShouldSerializeEQ_action(): Boolean{
        return EQ_action.count() > 0;
    }

}
