using OpenTraceability.Interfaces;
using OpenTraceability.Models.Events.KDEs;
using OpenTraceability.Utility;
using OpenTraceability.Utility.Attributes;

namespace OpenTraceability.Models.Events
{
    /// <summary>
    /// The ILMD section of the event.
    /// </summary>
    public class EventILMD
    {
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "productionMethodForFishAndSeafoodCode")]
        public string? SeafoodProductionMethod { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")]
        public DateTimeOffset? ItemExpirationDate { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "productionDate")]
        public DateTimeOffset? ProductionDate { get; set; }

        [OpenTraceabilityArray]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")]
        public List<Country> CountryOfOrigin { get; set; } = new List<Country>();

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "lotNumber")]
        public string? LotNumber { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "certificationList")]
        public CertificationList? CertificationList { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}