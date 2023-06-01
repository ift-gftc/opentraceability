package models.events

import interfaces.*
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import queries.EPCISQueryParameters
import java.net.*
import java.time.OffsetDateTime

open class EPCISBaseDocument {
    var EPCISVersion: EPCISVersion? = null
    var CreationDate: OffsetDateTime? = null
    var Header: StandardBusinessDocumentHeader? = null
    var Events: MutableList<IEvent> = mutableListOf()
    var MasterData: MutableList<IVocabularyElement> =  mutableListOf()
    var Namespaces: MutableMap<String, String> = mutableMapOf()
    var Contexts: MutableList<String> = mutableListOf()
    var Attributes: MutableMap<String, String> = mutableMapOf()

    inline fun <reified T : IVocabularyElement> getMasterData(): List<T> {
        return this.MasterData.filterIsInstance<T>()
    }

    open fun merge(data: EPCISBaseDocument) {
        data.Events.forEach { e ->
            var found: Boolean = false;

            this.Events.forEach { e2 ->
                if (e.EventID == e2.EventID) {
                    if (e.ErrorDeclaration == null && e2.ErrorDeclaration != null) {
                        this.Events.remove(e);
                        this.Events.add(e2);
                    }
                    found = true;
                }
            }

            if (!found) {
                this.Events.add(e);
            }

            data.MasterData.forEach { element ->
                var single = this.MasterData.filter { x -> x.ID == element.ID }.single()

                if (single == null) {
                    this.MasterData.add(element)
                }
            }
        }
    }

    fun filterEvents(parameters: EPCISQueryParameters): List<IEvent> {
        val events = mutableListOf<IEvent>()

        for (evt in this.Events) {
            // filter: GE_eventTime
            if (parameters.query.GE_eventTime != null) {
                if (evt.EventTime == null || evt.EventTime!! < parameters.query.GE_eventTime) {
                    continue
                }
            }

            // filter: LE_eventTime
            if (parameters.query.LE_eventTime != null) {
                if (evt.EventTime == null || evt.EventTime!! > parameters.query.LE_eventTime) {
                    continue
                }
            }

            // filter: GE_recordTime
            if (parameters.query.GE_recordTime != null) {
                if (evt.RecordTime == null || evt.RecordTime!! < parameters.query.GE_recordTime) {
                    continue
                }
            }

            // filter: LE_recordTime
            if (parameters.query.LE_recordTime != null) {
                if (evt.RecordTime == null || evt.RecordTime!! > parameters.query.LE_recordTime) {
                    continue
                }
            }

            // filter: EQ_bizStep
            if (parameters.query.EQ_bizStep != null && parameters.query.EQ_bizStep.isNotEmpty()) {
                if (!HasUriMatch(evt.BusinessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:")) {
                    continue
                }
            }

            // filter: EQ_bizLocation
            if (parameters.query.EQ_bizLocation != null && parameters.query.EQ_bizLocation.isNotEmpty()) {
                if (evt.Location?.GLN == null || !parameters.query.EQ_bizLocation.map { it.toString().toLowerCase() }.contains(
                        evt.Location!!.GLN.toString().toLowerCase())) {
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







    fun HasMatch(evt: IEvent, epcs: List<String>, vararg allowedTypes: EventProductType): Boolean {
        for (epcMatchStr in epcs) {
            val epcMatch = EPC(epcMatchStr)
            for (product in evt.Products) {
                if (allowedTypes.isEmpty() || allowedTypes.contains(product.Type)) {
                    if (epcMatch.Matches(product.EPC)) {
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
