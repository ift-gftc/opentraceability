using OpenTraceability.Models.Events;
using OpenTraceability.MSC.Events;

namespace OpenTraceability.MSC
{
    public static class Setup
    {
        private static object _locker = new object();
        private static bool _isInitialized = false;

        public static void Initialize()
        {
            lock (_locker)
            {
                if (!_isInitialized)
                {
                    OpenTraceability.GDST.Setup.Initialize();

                    // GDST 1.2 events. The GDST 2.0 package ships 2.0 only, but MSC still has to read GDST 1.2
                    // solutions. These business steps are GDST specific and do not collide with the CBV business
                    // steps the GDST 2.0 profiles are registered on, so both vocabularies resolve side by side.
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTFishingEvent), EventType.ObjectEvent, "urn:gdst:bizStep:fishingEvent", Models.Events.EventAction.ADD));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTTransshipmentEvent), EventType.ObjectEvent, "urn:gdst:bizStep:transshipment", Models.Events.EventAction.OBSERVE));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTLandingEvent), EventType.ObjectEvent, "urn:gdst:bizStep:landing", Models.Events.EventAction.OBSERVE));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTHatchingEvent), EventType.ObjectEvent, "urn:gdst:bizStep:hatching", Models.Events.EventAction.ADD));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(GDSTFarmHarvestEvent), EventType.TransformationEvent, "urn:gdst:bizStep:farmHarvest"));

                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCProcessingEvent), EventType.TransformationEvent, "urn:epcglobal:cbv:bizstep:commissioning"));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCProcessingEvent), EventType.TransformationEvent, "https://ref.gs1.org/cbv/BizStep-commissioning"));

                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCReceiveEvent), EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:receiving", Models.Events.EventAction.OBSERVE));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCReceiveEvent), EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-receiving", Models.Events.EventAction.OBSERVE));

                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCShippingEvent), EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:shipping", Models.Events.EventAction.OBSERVE));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCShippingEvent), EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-shipping", Models.Events.EventAction.OBSERVE));

                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCStorageEvent), EventType.ObjectEvent, "urn:epcglobal:cbv:bizstep:storing", Models.Events.EventAction.OBSERVE));
                    OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(MSCStorageEvent), EventType.ObjectEvent, "https://ref.gs1.org/cbv/BizStep-storing", Models.Events.EventAction.OBSERVE));

                    _isInitialized = true;
                }
            }
        }
    }
}