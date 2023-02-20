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
        [OpenTraceabilityJson("cbvmda:productionMethodForFishAndSeafoodCode")]
        public string? SeafoodProductionMethod { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "itemExpirationDate")]
        [OpenTraceabilityJson("cbvmda:itemExpirationDate")]
        public DateTimeOffset? ItemExpirationDate { get; set; }

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "productionDate")]
        [OpenTraceabilityJson("cbvmda:productionDate")]
        public DateTimeOffset? ProductionDate { get; set; }

        [OpenTraceabilityArray]
        [OpenTraceabilityRepeating]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "countryOfOrigin")]
        [OpenTraceabilityJson("cbvmda:countryOfOrigin")]
        public List<Country> CountryOfOrigin { get; set; } = new List<Country>();

        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "lotNumber")]
        [OpenTraceabilityJson("cbvmda:lotNumber")]
        public string? LotNumber { get; set; }

        [OpenTraceabilityObject]
        [OpenTraceability(Constants.CBVMDA_NAMESPACE, "certificationList")]
        [OpenTraceabilityJson("cbvmda:certificationList")]
        public CertificationList? CertificationList { get; set; }

        [OpenTraceabilityExtensionElements]
        public List<IEventKDE> ExtensionKDEs { get; internal set; } = new List<IEventKDE>();
    }
}