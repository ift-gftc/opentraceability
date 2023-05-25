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

    fun <T> GetMasterData(): ArrayList<T> {
        TODO("Not yet implemented")
    }

    fun <T> GetMasterData(id: String?): T? {
        TODO("Not yet implemented")
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

    fun FilterEvents(parameters: EPCISQueryParameters): ArrayList<IEvent> {
        var events: ArrayList<IEvent> = ArrayList<IEvent>()

        this.Events.forEach lit@ { evt ->
            // filter: GE_eventTime
            if (parameters.query.GE_eventTime != null)
            {
                if (evt.EventTime == null || evt.EventTime!! < parameters.query.GE_eventTime)
                {
                    return@lit
                }
            }

            // filter: LE_eventTime
            if (parameters.query.LE_eventTime != null)
            {
                if (evt.EventTime == null || evt.EventTime!! > parameters.query.LE_eventTime)
                {
                    return@lit
                }
            }

            // filter: GE_recordTime
            if (parameters.query.GE_recordTime != null)
            {
                if (evt.RecordTime == null || evt.RecordTime!! < parameters.query.GE_recordTime)
                {
                    return@lit
                }
            }

            // filter: LE_recordTime
            if (parameters.query.LE_recordTime != null)
            {
                if (evt.RecordTime == null || evt.RecordTime!! > parameters.query.LE_recordTime)
                {
                    return@lit
                }
            }

            //TODO: Not yet implemented
            /*
            // filter: EQ_bizStep
            if (parameters.query.EQ_bizStep != null && parameters.query.EQ_bizStep.count() > 0)
            {
                if (!HasUriMatch(evt.BusinessStep, parameters.query.EQ_bizStep, "https://ref.gs1.org/cbv/BizStep-", "urn:epcglobal:cbv:bizstep:"))
                {
                    return@lit
                }
            }

            // filter: EQ_bizLocation
            if (parameters.query.EQ_bizLocation != null && parameters.query.EQ_bizLocation.count() > 0)
            {
                if (evt.Location?.GLN == null || !parameters.query.EQ_bizLocation.select(e => e.ToString().ToLower()).contains(evt.Location.GLN.toString().ToLower()))
                {
                    return@lit
                }
            }

            // filter: MATCH_anyEPC
            if (parameters.query.MATCH_anyEPC != null && parameters.query.MATCH_anyEPC.count() > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPC))
                {
                    return@lit
                }
            }

            // filter: MATCH_anyEPCClass
            if (parameters.query.MATCH_anyEPCClass != null && parameters.query.MATCH_anyEPCClass.count() > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_anyEPCClass))
                {
                    return@lit
                }
            }

            // filter: MATCH_epc
            if (parameters.query.MATCH_epc != null && parameters.query.MATCH_epc.count() > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_epc, EventProductType.Reference, EventProductType.Child))
                {
                    return@lit
                }
            }

            // filter: MATCH_epcClass
            if (parameters.query.MATCH_epcClass != null && parameters.query.MATCH_epcClass.count() > 0)
            {
                if (!HasMatch(evt, parameters.query.MATCH_epcClass, EventProductType.Reference, EventProductType.Child))
                {
                    return@lit
                }
            }
            */
            events.add(evt);
        }


        return events;
    }

    fun HasMatch(evt: IEvent, epcs: ArrayList<String>, allowedTypes: ArrayList<EventProductType>): Boolean {
        TODO("Not yet implemented")
    }

    fun HasUriMatch(uri: URL?, filter: ArrayList<String>, prefix: String, replacePrefix: String): Boolean {
        TODO("Not yet implemented")
    }
}
