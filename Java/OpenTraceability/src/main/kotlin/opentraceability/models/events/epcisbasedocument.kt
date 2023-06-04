package opentraceability.models.events

import opentraceability.interfaces.*
import opentraceability.models.common.StandardBusinessDocumentHeader
import opentraceability.models.identifiers.*
import opentraceability.queries.EPCISQueryParameters
import java.net.*
import java.time.OffsetDateTime

@Suppress("LocalVariableName", "PropertyName", "FunctionName")
open class EPCISBaseDocument {
    var epcisVersion: EPCISVersion? = null
    var creationDate: OffsetDateTime? = null
    var header: StandardBusinessDocumentHeader? = null
    var events: MutableList<IEvent> = mutableListOf()
    var masterData: MutableList<IVocabularyElement> =  mutableListOf()
    var namespaces: MutableMap<String, String> = mutableMapOf()
    var contexts: MutableList<String> = mutableListOf()
    var attributes: MutableMap<String, String> = mutableMapOf()

    inline fun <reified T : IVocabularyElement> searchMasterData(): MutableList<T> {
        return this.masterData.filterIsInstance<T>().toMutableList()
    }
    inline fun <reified T : IVocabularyElement> searchMasterData(id: String?): T? {
        return this.masterData.filterIsInstance<T>().firstOrNull { it.id == id }
    }

    open fun merge(data: EPCISBaseDocument) {
        data.events.forEach { e ->
            var found: Boolean = false;

            this.events.forEach { e2 ->
                if (e.eventID == e2.eventID) {
                    if (e.errorDeclaration == null && e2.errorDeclaration != null) {
                        this.events.remove(e);
                        this.events.add(e2);
                    }
                    found = true;
                }
            }

            if (!found) {
                this.events.add(e);
            }

            data.masterData.forEach { element ->
                var single = this.masterData.filter { x -> x.id == element.id }.single()

                if (single == null) {
                    this.masterData.add(element)
                }
            }
        }
    }

    fun filterEvents(parameters: EPCISQueryParameters): MutableList<IEvent> {
        val events = mutableListOf<IEvent>()

        for (evt in this.events) {
            // filter: GE_eventTime
            if (parameters.query.GE_eventTime != null) {
                if (evt.eventTime == null || evt.eventTime!! < parameters.query.GE_eventTime) {
                    continue
                }
            }

            // filter: LE_eventTime
            if (parameters.query.LE_eventTime != null) {
                if (evt.eventTime == null || evt.eventTime!! > parameters.query.LE_eventTime) {
                    continue
                }
            }

            // filter: GE_recordTime
            if (parameters.query.GE_recordTime != null) {
                if (evt.recordTime == null || evt.recordTime!! < parameters.query.GE_recordTime) {
                    continue
                }
            }

            // filter: LE_recordTime
            if (parameters.query.LE_recordTime != null) {
                if (evt.recordTime == null || evt.recordTime!! > parameters.query.LE_recordTime) {
                    continue
                }
            }

            // filter: EQ_bizStep
            if (parameters.query.EQ_bizStep != null && parameters.query.EQ_bizStep.isNotEmpty()) {
                if (!HasUriMatch(evt.businessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:")) {
                    continue
                }
            }

            // filter: EQ_bizLocation
            if (parameters.query.EQ_bizLocation != null && parameters.query.EQ_bizLocation.isNotEmpty()) {
                if (evt.location?.gln == null || !parameters.query.EQ_bizLocation.map { it.toString().toLowerCase() }.contains(
                        evt.location!!.gln.toString().toLowerCase())) {
                    continue
                }
            }

            // filter: MATCH_anyEPC
            if (parameters.query.MATCH_anyEPC != null && parameters.query.MATCH_anyEPC.isNotEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPC)) {
                    continue
                }
            }

            // filter: MATCH_anyEPCClass
            if (parameters.query.MATCH_anyEPCClass != null && parameters.query.MATCH_anyEPCClass.isNotEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPCClass)) {
                    continue
                }
            }

            // filter: MATCH_epc
            if (parameters.query.MATCH_epc != null && parameters.query.MATCH_epc.isNotEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_epc, EventProductType.Reference, EventProductType.Child)) {
                    continue
                }
            }

            // filter: MATCH_epcClass
            if (parameters.query.MATCH_epcClass != null && parameters.query.MATCH_epcClass.isNotEmpty()) {
                if (!HasMatch(evt, parameters.query.MATCH_epcClass, EventProductType.Reference, EventProductType.Child)) {
                    continue
                }
            }

            events.add(evt)
        }

        return events
    }

    fun HasMatch(evt: IEvent, epcs: MutableList<String>, vararg allowedTypes: EventProductType): Boolean {
        for (epcMatchStr in epcs) {
            val epcMatch = EPC(epcMatchStr)
            for (product in evt.products) {
                if (allowedTypes.isEmpty() || allowedTypes.contains(product.Type)) {
                    if (epcMatch.matches(product.EPC)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun HasUriMatch(uri: URI?, filter: MutableList<String>, prefix: String, replacePrefix: String): Boolean {
        // make sure all of the EQ_bizStep are converted into URI format before comparing
        for (i in filter.indices) {
            val bizStep = filter[i]
            val u: URI? = URI.create(bizStep)
            if (u == null) {
                filter[i] = "$replacePrefix$bizStep"
            } else if (bizStep.startsWith(prefix)) {
                filter[i] = replacePrefix + bizStep.split("-").last()
            }
        }

        // we need to handle the various formats that the bizStep can occur in
        if (uri != null) {
            var bizStep = URI.create(uri.toString())
            if (bizStep.toString().startsWith(prefix)) {
                bizStep = URI.create("$replacePrefix${uri.toString().split("-").last()}")
            }

            val filterUris = filter.map { URI.create(it) }
            if (!filterUris.map { it.toString().toLowerCase() }.contains(bizStep.toString().toLowerCase())) {
                return false
            }
        } else {
            return false
        }

        return true
    }
}
