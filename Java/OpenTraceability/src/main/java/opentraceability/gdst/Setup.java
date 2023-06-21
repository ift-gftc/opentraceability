package opentraceability.gdst;

import opentraceability.Constants;
import opentraceability.OpenTraceabilityEventKDEProfile;
import opentraceability.OpenTraceabilityEventProfile;
import opentraceability.gdst.events.*;
import opentraceability.gdst.masterdata.GDSTLocation;
import opentraceability.models.events.EventAction;
import opentraceability.models.events.EventType;
import opentraceability.models.masterdata.Location;

import java.util.ArrayList;
import java.util.List;

public class Setup {
    private static final Object _locker = new Object();
    private static boolean _isInitialized = false;

    public static void Initialize() throws Exception {
        synchronized (_locker) {
            if (!_isInitialized) {
                opentraceability.Setup.Initialize();

                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFishingEvent.class, EventType.ObjectEvent, "urn:gdst:bizStep:fishingEvent", EventAction.ADD));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTTransshipmentEvent.class, EventType.ObjectEvent, "urn:gdst:bizStep:transshipment", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTLandingEvent.class, EventType.ObjectEvent, "urn:gdst:bizstep:landing", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTHatchingEvent.class, EventType.ObjectEvent, "urn:gdst:bizstep:hatching", EventAction.ADD));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFarmHarvestEvent.class, EventType.TransformationEvent, "urn:gdst:bizstep:farmharvest"));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTProcessingEvent.class, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning"));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTProcessingEvent.class, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning"));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTReceiveEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:receiving", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTReceiveEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-receiving", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTShippingEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:shipping", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTShippingEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-shipping", EventAction.OBSERVE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTAggregationEvent.class, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:packing", EventAction.ADD));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTAggregationEvent.class, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-packing", EventAction.ADD));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTDisaggregationEvent.class, EventType.AggregationEvent, "urn:epcglobal:cbv:bizstep:unpacking", EventAction.DELETE));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTDisaggregationEvent.class, EventType.AggregationEvent, "https://ref.gs1.org/cbv/BizStep-unpacking", EventAction.DELETE));

                // The feedmill event is special in the sense that it requires a KDE profile to detect.
                // We know it is a feedmill event when it has the proteinSource KDE in the ILMD.
                List<OpenTraceabilityEventKDEProfile> feedmillKDEProfile = new ArrayList<>();
                feedmillKDEProfile.add(new OpenTraceabilityEventKDEProfile("extension/ilmd/" + (Constants.GDST_XNAMESPACE + "proteinSource"), "ilmd/" + (Constants.GDST_XNAMESPACE + "proteinSource"), "ilmd.gdst:proteinSource"));

                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent.class, EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning", feedmillKDEProfile));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillTransformationEvent.class, EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", feedmillKDEProfile));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent.class, EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:commissioning", EventAction.ADD, feedmillKDEProfile));
                opentraceability.Setup.registerEventProfile(new OpenTraceabilityEventProfile(GDSTFeedmillObjectEvent.class, EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-commissioning", EventAction.ADD, feedmillKDEProfile));

                opentraceability.Setup.registerMasterDataType(GDSTLocation.class, Location.class);
                _isInitialized = true;
            }
        }
    }
}
