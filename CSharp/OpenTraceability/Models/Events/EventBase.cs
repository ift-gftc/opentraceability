using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
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
        public Dictionary<string, string> _namespaces = new Dictionary<string, string>();
        public Dictionary<string, string> _prefixes = new Dictionary<string, string>();
        private List<IEventKDE> _kdes = new List<IEventKDE>();

        public long ID { get; set; }
        public string? EventID { get; set; } = string.Empty;
        public string? CertificationInfo { get; set; } = string.Empty;
        public DateTimeOffset? EventTime { get; set; }
        public double? EventTimeOffset { get; set; }
        public DateTimeOffset? Recorded { get; set; }
        public PGLN? DataOwner { get; set; }
        public PGLN? Owner { get; set; }
        public EventAction? Action { get; set; }
        public string? BusinessStep { get; set; } = string.Empty;
        public string? Disposition { get; set; } = string.Empty;
        public PersistentDisposition? PersistentDisposition { get; set; }
        public EventLocation? Location { get; set; }
        public EventReadPoint? ReadPoint { get; set; }
        public List<EventSource> SourceList { get; set; } = new List<EventSource>();
        public List<EventDestination> DestinationList { get; set; } = new List<EventDestination>();
        public List<Certificate> Certificates { get; set; } = new List<Certificate>();
        public ReadOnlyCollection<IEventKDE> KDEs { get => new ReadOnlyCollection<IEventKDE>(_kdes); }
        public List<EventBusinessTransaction> BusinessTransactions { get; set; } = new List<EventBusinessTransaction>();
        public List<SensorElement> SensorElementList { get; set; } = new List<SensorElement>();
        public ErrorDeclaration? ErrorDeclaration { get; set; }
        public EventILMD? ILMD { get; set; }

        /// <summary>
        /// Adds a KDE to the event.
        /// </summary>
        /// <param name="kde"></param>
        public void AddKDE(IEventKDE kde)
        {
            _kdes.Add(kde);
        }

        /// <summary>
        /// Gets a KDE by the type and key value.
        /// </summary>
        /// <typeparam name="T">The C# type of the KDE.</typeparam>
        /// <param name="key">The key value of the KDE.</param>
        /// <returns>The instance of the IEventKDE that matches the parameters.</returns>
        public T? GetKDE<T>(string ns, string name) where T : IEventKDE
        {
            // if we are given the prefixes...
            if (_prefixes.ContainsKey(ns))
            {
                ns = _prefixes[ns];
            }

            IEventKDE? kde = _kdes.Find(k => k.Namespace == ns && k.Name == name);
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
            IEventKDE? kde = _kdes.Find(k => k.ValueType == typeof(T));
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
        /// Sets the namespaces on the event. This will replace the existing namespaces.
        /// </summary>
        /// <param name="namespaces"></param>
        public void SetNamespaces(Dictionary<string, string> namespaces)
        {
            _namespaces = namespaces;
            _prefixes = namespaces.Reverse();
        }
    }
}