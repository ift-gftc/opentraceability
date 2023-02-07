using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;
using GS1.Interfaces.Models.Events;
using DSUtil;
using System.ComponentModel;
using System.ComponentModel.DataAnnotations.Schema;

namespace OpenTraceability.Models.Events.KDEs
{
    [Description("cbvmda:vesselCatchInformationList")]
    public class VesselCatchInformationList : IEventKDE
    {
        public List<VesselCatchInformation> Vessels { get; set; } = new List<VesselCatchInformation>();

        public DSXML ToXml()
        {
            DSXML xml = new DSXML("cbvmda:vesselCatchInformationList");
            foreach (var v in Vessels)
            {
                xml.AddChild(v.ToXml());
            }
            return xml;
        }

        public void FromXml(DSXML xml)
        {
            foreach (DSXML xVessel in xml.Elements("cbvmda:vesselCatchInformation"))
            {
                VesselCatchInformation v = new VesselCatchInformation();
                v.FromXml(xVessel);
                this.Vessels.Add(v);
            }
        }

        #region IEventKDE

        public string Key { get; set; } = "cbvmda:vesselCatchInformationList";
        public Type ValueType => typeof(VesselCatchInformationList);

        [NotMapped]
        public DSXML XmlValue { get => ToXml(); set => FromXml(value); }

        [NotMapped]
        public JToken JsonValue { get => throw new NotImplementedException(); set => throw new NotImplementedException(); }

        #endregion
    }
}
