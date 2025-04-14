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