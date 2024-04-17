using OpenTraceability.Models.Common;
using OpenTraceability.Utility.Attributes;
using System.Collections.Generic;

namespace OpenTraceability.Models.Events.KDEs
{
    public class CertificationList
    {
        [OpenTraceabilityArray]
        [OpenTraceabilityObject]
        [OpenTraceability("certification")]
        public List<Certificate> Certificates { get; set; } = new List<Certificate>();
    }
}