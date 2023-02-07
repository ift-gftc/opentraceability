using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Identifiers;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace OpenTraceability.Interfaces
{
    /// <summary>
    /// Interface for all events.
    /// </summary>
    public interface IEvent
    {
        long ID { get; set; }
        string? EventID { get; set; }
        string? CertificationInfo { get; set; }
        PGLN? DataOwner { get; set; }
        PGLN? Owner { get; set; }
        DateTimeOffset? EventTime { get; set; }
        double? EventTimeOffset { get; set; }
        DateTime? Recorded { get; set; }
        EventType EventType { get; }
        EventAction Action { get; set; }
        string? BusinessStep { get; set; }
        string? Disposition { get; set; }
        PersistentDisposition? PersistentDisposition { get; set; }
        ErrorDeclaration? ErrorDeclaration { get; set; }
        EventLocation? Location { get; set; }
        EventReadPoint? ReadPoint { get; set; }
        List<EventBusinessTransaction> BusinessTransactions { get; set; }
        List<EventSource> SourceList { get; set; }
        List<EventDestination> DestinationList { get; set; }
        List<Certificate> Certificates { get; set; }
        List<IEventKDE> KDEs { get; set; }
        List<SensorElement> SensorElementList { get; set; }
        EventILMD? ILMD { get; set; }
        ReadOnlyCollection<EventProduct> Products { get; }
        T? GetKDE<T>(string key);
        T? GetKDE<T>();

        void AddProduct(EventProduct product);
    }
}
