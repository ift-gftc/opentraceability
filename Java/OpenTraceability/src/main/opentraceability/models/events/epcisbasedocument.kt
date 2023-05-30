package models.events

import interfaces.IEvent
import interfaces.IVocabularyElement
import models.common.StandardBusinessDocumentHeader
import java.util.*
import models.identifiers.*
import models.events.*
import queries.EPCISQueryParameters
import java.net.URL
import java.time.OffsetDateTime

open class EPCISBaseDocument {
    var EPCISVersion: EPCISVersion? = null
    var CreationDate: OffsetDateTime? = null
    var Header: StandardBusinessDocumentHeader? = null
    var Events: ArrayList<IEvent> = ArrayList<IEvent>()
    var MasterData: ArrayList<IVocabularyElement> = ArrayList<IVocabularyElement>()
    var Namespaces: MutableMap<String, String> = mutableMapOf()
    var Contexts: ArrayList<String> = ArrayList<String>()
    var Attributes: MutableMap<String, String> = mutableMapOf()


    fun <T : IVocabularyElement> GetMasterData(): List<T> {
        return this.MasterData.filterIsInstance<T>()
    }



    open fun Merge(data: EPCISBaseDocument) {
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
                if (evt.Location?.GLN == null || !parameters.query.EQ_bizLocation.map { it.toString().toLowerCase() }.contains(evt.Location.GLN.toString().toLowerCase())) {
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







    private fun HasMatch(evt: IEvent, epcs: List<String>, vararg allowedTypes: EventProductType): Boolean {
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

    private fun HasUriMatch(uri: Uri?, filter: MutableList<String>, prefix: String, replacePrefix: String): Boolean {
        // make sure all of the EQ_bizStep are converted into URI format before comparing
        for (i in filter.indices) {
            val bizStep = filter[i]
            val u: Uri? = Uri.parse(bizStep)
            if (u == null) {
                filter[i] = "$replacePrefix$bizStep"
            } else if (bizStep.startsWith(prefix)) {
                filter[i] = replacePrefix + bizStep.split("-").last()
            }
        }

        // we need to handle the various formats that the bizStep can occur in
        if (uri != null) {
            var bizStep = Uri.parse(uri.toString())
            if (bizStep.toString().startsWith(prefix)) {
                bizStep = Uri.parse("$replacePrefix${uri.toString().split("-").last()}")
            }

            val filterUris = filter.map { Uri.parse(it) }
            if (!filterUris.map { it.toString().toLowerCase() }.contains(bizStep.toString().toLowerCase())) {
                return false
            }
        } else {
            return false
        }

        return true
    }

}
