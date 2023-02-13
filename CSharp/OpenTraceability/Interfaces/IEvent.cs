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
        double? EventTimeZoneOffset { get; set; }
        DateTimeOffset? RecordTime { get; set; }
        EventType EventType { get; }
        EventAction? Action { get; set; }
        Uri? BusinessStep { get; set; }
        Uri? Disposition { get; set; }
        PersistentDisposition? PersistentDisposition { get; set; }
        ErrorDeclaration? ErrorDeclaration { get; set; }
        EventLocation? Location { get; set; }
        EventReadPoint? ReadPoint { get; set; }
        List<EventBusinessTransaction> BizTransactionList { get; set; }
        List<EventSource> SourceList { get; set; }
        List<EventDestination> DestinationList { get; set; }
        List<Certificate> Certificates { get; set; }
        ReadOnlyCollection<IEventKDE> KDEs { get; }
        List<SensorElement> SensorElementList { get; set; }
        EventILMD? ILMD { get; set; }
        ReadOnlyCollection<EventProduct> Products { get; }
        T? GetKDE<T>(string ns, string name) where T : IEventKDE;
        T? GetKDE<T>() where T : IEventKDE;
        void AddKDE(IEventKDE kde);
        void AddProduct(EventProduct product);
    }
}
