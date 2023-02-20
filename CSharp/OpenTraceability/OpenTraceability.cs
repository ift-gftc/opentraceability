using OpenTraceability.Models.Events;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability
{
    /// <summary>
    /// Static helper class for the Open Traceability library.
    /// </summary>
    public static class OpenTraceability
    {
        internal static ConcurrentBag<OpenTraceabilityEventProfile> Profiles = new ConcurrentBag<OpenTraceabilityEventProfile>();

        static OpenTraceability()
        {
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(ObjectEvent<EventILMD>), "ObjectEvent"));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransactionEvent), nameof(TransactionEvent)));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(TransformationEvent<EventILMD>), "TransformationEvent"));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AggregationEvent<EventILMD>), "AggregationEvent"));
            RegisterEventProfile(new OpenTraceabilityEventProfile(typeof(AssociationEvent), nameof(AssociationEvent)));
        }

        /// <summary>
        /// We use event profiles to automatically load extension events.
        /// </summary>
        public static void RegisterEventProfile(OpenTraceabilityEventProfile profile)
        {
            Profiles.Add(profile);
        }

        /// <summary>
        /// You must call this prior to utilizing the open traceability libraries.
        /// </summary>
        public static void Initialize()
        {

        }
    }
}
