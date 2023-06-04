package opentraceability.queries

import java.util.*
import java.lang.reflect.Type
import java.net.URI
import java.time.OffsetDateTime

class EPCISQuery {
    var GE_recordTime: OffsetDateTime? = null
    var LE_recordTime: OffsetDateTime? = null
    var GE_eventTime: OffsetDateTime? = null
    var LE_eventTime: OffsetDateTime? = null
    var eventTypes: MutableList<String> = mutableListOf()
    var MATCH_epc: MutableList<String> = mutableListOf()
    var MATCH_epcClass: MutableList<String> = mutableListOf()
    var MATCH_anyEPC: MutableList<String> = mutableListOf()
    var MATCH_anyEPCClass: MutableList<String> = mutableListOf()
    var EQ_bizStep: MutableList<String> = mutableListOf()
    var EQ_bizLocation: MutableList<URI> = mutableListOf()
    var EQ_action: MutableList<String> = mutableListOf()
}
