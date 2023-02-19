using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;
using System.Collections.ObjectModel;

namespace OpenTraceability.Models.Events
{
    public enum EventProductType
    {
        Reference = 1,
        Input = 2,
        Output = 3,
        Parent = 4,
        Child = 5
    };

    public enum EventType
    {
        Object,
        Transformation,
        Aggregation,
        Transaction,
        Association
    }

    public enum EventAction
    {
        ADD,
        OBSERVE,
        DELETE
    }

    public static class EventBusinessStep
    {
        public const string Receiving = "urn:epcglobal:cbv:bizstep:receiving";
        public const string Shipping = "urn:epcglobal:cbv:bizstep:shipping";
        public const string Storage = "urn:epcglobal:cbv:bizstep:storing";
        public const string Fishing = "urn:gdst:bizstep:fishingevent";
        public const string Commissioning = "urn:epcglobal:cbv:bizstep:commissioning";
        public const string Commingling = "urn:gdst:bizstep:commingling";
        public const string Sampling = "urn:gdst:bizstep:sampling";
        public const string Freezing = "urn:gdst:bizstep:freezing";
        public const string Landing = "urn:gdst:bizstep:landing";
        public const string Feeding = "urn:gdst:bizstep:feeding";
        public const string Hatching = "urn:gdst:bizstep:hatching";
        public const string Temperature = "urn:gdst:bizstep:temperature";
        public const string Packaging = "urn:gdst:bizstep:packaging";
        public const string Transshipment = "urn:gdst:bizstep:transshipment";
        public const string FarmHarvest = "urn:gdst:bizstep:farmharvest";
    }

    public class EventBase
    {
        [OpenTraceability("eventTime", 1)]
        public DateTimeOffset? EventTime { get; set; }

        [OpenTraceability("recordTime", 2)]
        public DateTimeOffset? RecordTime { get; set; }

        [OpenTraceability("eventTimeZoneOffset", 3)]
        public TimeSpan? EventTimeZoneOffset { get; set; }

        [OpenTraceability("eventID", 4, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/eventID", 4, EPCISVersion.V1)]
        public Uri? EventID { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability("errorDeclaration", 5, EPCISVersion.V2)]
        [OpenTraceability("baseExtension/errorDeclaration", 5, EPCISVersion.V1)]
        public ErrorDeclaration? ErrorDeclaration { get; set; }

        [OpenTraceability("certificationInfo", 6, EPCISVersion.V2)]
        public string? CertificationInfo { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "certificationList")]
        public CertificationList? CertificationList { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "informationProvider")]
        public PGLN? InformationProvider { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> KDEs { get; set; } = new List<IEventKDE>();

        /// <summary>
        /// Gets a KDE by the type and key value.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="key">The key value of the KDE.</param>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>(string ns, string name) where T : IEventKDE
        {
            IEventKDE? kde = KDEs.Find(k => k.Namespace == ns && k.Name == name);
            if (kde != null)
            {
                if (kde is T)
                {
                    return (T)kde;
                }
            }
            return default;
        }

        /// <summary>
        /// Gets the first KDE that matches the type provided.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>() where T : IEventKDE
        {
            IEventKDE? kde = KDEs.Find(k => k.ValueType == typeof(T));
            if (kde != null)
            {
                if (kde is T)
                {
                    return (T)kde;
                }
            }
            return default;
        }
    }
}