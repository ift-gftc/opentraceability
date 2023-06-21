package opentraceability.gdst

import opentraceability.*
import opentraceability.models.events.*

object Setup {
    private val locker = Any()
    private var isInitialized = false

    fun initialize() {
        /*
        synchronized(locker) {
            if (!isInitialized) {
                opentraceability.Setup.Initialize()

                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFishingEvent::class.java, EventType.ObjectEvent, "urn:gdst:bizStep:fishingEvent", EventAction.ADD))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTTransshipmentEvent::class.java, EventType.ObjectEvent, "urn:gdst:bizStep:transshipment", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTLandingEvent::class.java, EventType.ObjectEvent, "urn:gdst:bizstep:landing", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTHatchingEvent::class.java, EventType.ObjectEvent, "urn:gdst:bizstep:hatching", EventAction.ADD))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFarmHarvestEvent::class.java, EventType.TransformationEvent, "urn:gdst:bizstep:farmharvest"))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTProcessingEvent::class.java, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning"))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTProcessingEvent::class.java, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning"))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTReceiveEvent::class.java, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:receiving", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTReceiveEvent::class.java, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-receiving", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTShippingEvent::class.java, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:shipping", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTShippingEvent::class.java, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-shipping", EventAction.OBSERVE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTAggregationEvent::class.java, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:packing", EventAction.ADD))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTAggregationEvent::class.java, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-packing", EventAction.ADD))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTDisaggregationEvent::class.java, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:unpacking", EventAction.DELETE))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTDisaggregationEvent::class.java, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-unpacking", EventAction.DELETE))

                val feedmillKDEProfile
                        = listOf(
                    OpenTraceabilityEventKDEProfile("extension/ilmd/${Constants.GDST_XNAMESPACE}proteinSource", "ilmd/${Constants.GDST_XNAMESPACE}proteinSource", "ilmd.gdst:proteinSource")
                )

                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent::class.java, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning", feedmillKDEProfile))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent::class.java, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", feedmillKDEProfile))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent::class.java, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:commissioning", EventAction.ADD, feedmillKDEProfile))
                opentraceability.Setup.registerEventProfile(OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent::class.java, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", EventAction.ADD, feedmillKDEProfile))

                opentraceability.Setup.registerMasterDataType(opentraceability.models.masterdata.GDSTLocation::class.java, opentraceability.models.masterdata.Location::class.java)
                isInitialized = true
            }
        }
        */
    }
}