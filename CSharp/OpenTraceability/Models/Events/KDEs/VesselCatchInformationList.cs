using Newtonsoft.Json.Linq;
using OpenTraceability.Interfaces;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations.Schema;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    [Description("cbvmda:vesselCatchInformationList")]
    public class VesselCatchInformationList : EventKDEBase, IEventKDE
    {
        public Type ValueType => typeof(VesselCatchInformationList);
        public List<VesselCatchInformation> Vessels { get; set; } = new List<VesselCatchInformation>();

        public void SetFromJson(JToken json)
        {
            throw new NotImplementedException();
        }

        public JToken? GetJson()
        {
            throw new NotImplementedException();
        }

        public void SetFromXml(XElement xml)
        {
            throw new NotImplementedException();
        }

        public XElement? GetXml()
        {
            throw new NotImplementedException();
        }
    }
}