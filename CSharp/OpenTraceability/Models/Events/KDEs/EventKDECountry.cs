using DSUtil;
using DSUtil.StaticData;
using GS1.Interfaces.Models.Events;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Xml.Linq;

namespace OpenTraceability.Models.Events.KDEs
{
    public class EventKDECountry : EventKDEBase, IEventKDE
    {
        [NotMapped]
        public DSXML XmlValue
        {
            get
            {
                DSXML xKDE = new DSXML(this.Key);
                if (this.Value != null)
                {
                    xKDE.Value = Value.Abbreviation;
                }
                return xKDE;
            }
            set
            {
                if (value != null & !value.IsNull)
                {
                    this.Value = Countries.Parse(value.Value);
                }
            }
        }

        [NotMapped]
        public JToken JsonValue
        {
            get
            {
                throw new NotImplementedException();
            }
            set
            {
                throw new NotImplementedException();
            }
        }
        public Type ValueType => typeof(DateTime?);
        public Country Value { get; set; }

        public override string ToString()
        {
            return Value?.Name ?? String.Empty;
        }
    }
}
