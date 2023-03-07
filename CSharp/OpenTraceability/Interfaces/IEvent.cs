using OpenTraceability.Models.Common;
using OpenTraceability.Models.Events;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Models.Identifiers;
using OpenTraceability.Utility.Attributes;
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
        Uri? EventID { get; set; }
        string? CertificationInfo { get; set; }
        DateTimeOffset? EventTime { get; set; }
        TimeSpan? EventTimeZoneOffset { get; set; }
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
        List<IEventKDE> KDEs { get; }
        List<SensorElement> SensorElementList { get; set; }
        ReadOnlyCollection<EventProduct> Products { get; }
        T? GetKDE<T>(string ns, string name) where T : IEventKDE;
        T? GetKDE<T>() where T : IEventKDE;
        void AddProduct(EventProduct product);
        CertificationList? CertificationList { get; set; }
        EventILMD? GetILMD();
    }
}
