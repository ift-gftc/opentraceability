using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using OpenTraceability.Models.Common;
using OpenTraceability.Utility.Attributes;
using System.ComponentModel.DataAnnotations.Schema;
using System.Xml.Linq;

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