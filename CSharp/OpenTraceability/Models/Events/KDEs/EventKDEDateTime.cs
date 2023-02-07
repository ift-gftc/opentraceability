using DSUtil;
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
    public class EventKDEDateTime : EventKDEBase, IEventKDE
    {
        [NotMapped]
        public DSXML XmlValue
        {
            get
            {
                DSXML xKDE = new DSXML(this.Key);
                if (this.Value != null)
                {
                    xKDE.Value = Value?.ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ss.fffZ");
                }
                return xKDE;
            }
            set
            {
                if (value != null & !value.IsNull)
                {
                    if (DateTime.TryParse(value.Value, out DateTime dt))
                    {
                        this.Value = dt;
                    }
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
        public DateTime? Value { get; set; }

        public override string ToString()
        {
            return Value?.ToString() ?? String.Empty;
        }
    }
}
