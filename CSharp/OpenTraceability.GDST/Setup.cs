using OpenTraceability.GDST.Events;
using OpenTraceability.GDST.MasterData;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.MasterData;

namespace OpenTraceability.GDST
{
    public static class Setup
    {
        private static readonly object Locker = new object();
        private static bool _isInitialized;

        public static void Initialize()
        {
            lock (Locker)
            {
                if (_isInitialized)
                {
                    return;
                }

                OpenTraceability.Setup.Initialize();

                RegisterObjectProfiles(typeof(GDSTCommissionEvent), EventBusinessStep.Commissioning, EventBusinessStep.CommissioningURI, EventAction.ADD);
                RegisterObjectProfiles(typeof(GDSTShippingEvent), EventBusinessStep.Shipping, EventBusinessStep.ShippingURI, EventAction.OBSERVE);
                RegisterObjectProfiles(typeof(GDSTReceivingEvent), EventBusinessStep.Receiving, EventBusinessStep.ReceivingURI, EventAction.OBSERVE);
                RegisterObjectProfiles(typeof(GDSTDecommissionEvent), "urn:epcglobal:cbv:bizstep:destroying", "https://ref.gs1.org/cbv/BizStep-destroying", EventAction.DELETE);

                RegisterAggregationProfiles(typeof(GDSTAggregationEvent), "urn:epcglobal:cbv:bizstep:packing", "https://ref.gs1.org/cbv/BizStep-packing", EventAction.ADD);
                RegisterAggregationProfiles(typeof(GDSTDisaggregationEvent), "urn:epcglobal:cbv:bizstep:unpacking", "https://ref.gs1.org/cbv/BizStep-unpacking", EventAction.DELETE);

                RegisterTransformationProfiles(typeof(GDSTTransformationEvent), EventBusinessStep.Commissioning, EventBusinessStep.CommissioningURI);

                OpenTraceability.Setup.RegisterMasterDataType<GDSTTradeItem, Tradeitem>();
                OpenTraceability.Setup.RegisterMasterDataType<GDSTLocation, Location>();

                _isInitialized = true;
            }
        }

        private static void RegisterObjectProfiles(Type eventType, string urnBusinessStep, string uriBusinessStep, EventAction action)
        {
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.ObjectEvent, urnBusinessStep, action));
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.ObjectEvent, uriBusinessStep, action));
        }

        private static void RegisterAggregationProfiles(Type eventType, string urnBusinessStep, string uriBusinessStep, EventAction action)
        {
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.AggregationEvent, urnBusinessStep, action));
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.AggregationEvent, uriBusinessStep, action));
        }

        private static void RegisterTransformationProfiles(Type eventType, string urnBusinessStep, string uriBusinessStep)
        {
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.TransformationEvent, urnBusinessStep));
            OpenTraceability.Setup.RegisterEventProfile(new OpenTraceabilityEventProfile(eventType, EventType.TransformationEvent, uriBusinessStep));
        }
    }
}
